package com.vmusco.softwearn.learn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graphstream.graph.Edge;

import com.vmusco.softminer.exceptions.IncompatibleTypesException;
import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.GraphNodeVisitorAdapter;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softwearn.learn.folding.MutationGraphKFold;

/**
 * This graph class adds a layer on top of a Graph to take into consideration weights on edges
 * Moreover, some access to graph edges are overloaded to allows to get only edges on a certain threshold
 * The threshold is defined using {@link LearningGraphStream#getThreshold()} and {@link LearningGraphStream#setRunningThreshold()}.
 * This threshold is also set using {@link LearningGraphStream#LearningGraphStream(float, float)}.  
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class LearningGraphStream extends GraphStream implements LearningGraph {
	private static final Logger logger = LogManager.getFormatterLogger(LearningGraphStream.class.getSimpleName());
	
	private List<String> intToStrBuffer = new ArrayList<String>();
	private Map<String, Integer> strToIntBuffer = new HashMap<>();
	
	protected Map<Integer, Float> thresholds = new HashMap<Integer, Float>();
	private float defaultTreshold;
	
	/**
	 * The threshold to use when using getters methods
	 */
	private float currentTreshold = -1;
	
	/**
	 * Create a learning graph with a default threshold (for new edges).
	 * The running threshold is set to 0 as long as we are in learning phase
	 * Once the graph has learn data, switch to running phase by invoking {@see LearningGraphStream#setThreshold(float)} 
	 * @param init the initial threshold for new edges
	 */
	public LearningGraphStream(float init) {
		setDefaultTreshold(init);
		switchToLearningPhase();
	}

	public void setEdgeThreshold(String from, String to, float threshold){
		String key = GraphStream.buildDirectedEdgeName(from, to);
		
		if(!strToIntBuffer.containsKey(key)){
			int pos = intToStrBuffer.size();
			intToStrBuffer.add(key);
			strToIntBuffer.put(key, pos);
		}
		
		int pos = strToIntBuffer.get(key);
		
		thresholds.put(pos, threshold);
	}

	public float getEdgeThreshold(String from, String to){
		String key = buildDirectedEdgeName(from, to);
		return thresholds.get(strToIntBuffer.get(key));
	}

	public void displayWeights() {
		for(String s : strToIntBuffer.keySet()){
			float th = thresholds.get(strToIntBuffer.get(s));
			if(th > 0f){
				System.out.println(s+" => "+th);
			}
		}
	}
	
	public int nbEdgesWithWeight(float th) {
		int i = 0;
		float epsilon = 0.00000001f;
		
		for(String s : strToIntBuffer.keySet()){
			float thr = thresholds.get(strToIntBuffer.get(s));
			if(Math.abs(thr - th) < epsilon){
				i++;
			}
		}
		
		return i;
	}

	public LearningGraphStream getPrunedGraph(float treshold){
		LearningGraphStream ng = (LearningGraphStream)createNewLikeThis();
		
		for(EdgeIdentity edge : getEdges(treshold)){
			ng.addDirectedEdgeAndNodeIfNeeded(edge.getFrom(), edge.getTo());
		}
		
		try {
			ng.conformizeWith(this);
		} catch (IncompatibleTypesException e) {
			e.printStackTrace();
		}
		
		return ng;
	}

	@Override
	public float getDefaultTreshold() {
		return defaultTreshold;
	}

	@Override
	public void setDefaultTreshold(float init) {
		defaultTreshold = init;
	}

	@Override
	public Graph graph() {
		return this;
	}
	
	@Override
	public void addDirectedEdge(String from, String to) {
		addDirectedEdge(from, to, getDefaultTreshold());
	}

	@Override
	public void addDirectedEdge(String from, String to, float treshold) {
		super.addDirectedEdge(from, to);
		setEdgeThreshold(from, to, treshold);
	}
	
	@Override
	public void addDirectedEdgeAndNodeIfNeeded(String from, String to){
		addNode(from);
		addNode(to);
		addDirectedEdge(from, to);
	}
	
	@Override
	public Graph createNewLikeThis() {
		return new LearningGraphStream(getDefaultTreshold());
	}
	
	protected void visit(final GraphNodeVisitor aVisitor, final String node, final boolean from, final float threshold){
		super.visit(new GraphNodeVisitorAdapter() {
			@Override
			public void visitNode(String node) {
				aVisitor.visitNode(node);
			}
			
			@Override
			public void visitEdge(String from, String to) {
				if(isNodeValidAccordingToThreshold(from, to, threshold)){
					aVisitor.visitEdge(from, to);
				}
			}

			@Override
			public String[] nextNodesToVisitFrom(String node) {
				return aVisitor.nextNodesToVisitFrom(node);
			}
			
			@Override
			public boolean interruptVisit() {
				return aVisitor.interruptVisit();
			}
		}, node, from);
	}

	@Override
	public void visitFrom(GraphNodeVisitor aVisitor, String node, float threshold){
		visit(aVisitor, node, true, threshold);
	}

	@Override
	public void visitTo(GraphNodeVisitor aVisitor, String node, float threshold){
		visit(aVisitor, node, false, threshold);
	}

	@Override
	public EdgeIdentity[] getEdges(float threshold) {
		List<EdgeIdentity> ret = new ArrayList<EdgeIdentity>();
		
		for(EdgeIdentity edge : super.getEdges()){
			if(isNodeValidAccordingToThreshold(edge.getFrom(), edge.getTo(), threshold)){
				ret.add(edge);
			}
		}
		
		return ret.toArray(new EdgeIdentity[ret.size()]);
	}
	
	public void setThreshold(float threshold) {
		currentTreshold = threshold;
	}

	public float getThreshold() {
		return (currentTreshold<=0)?0:currentTreshold;
	}
	
	/*
	 * Overriding default edges access using the "current threshold"
	 */
	@Override
	public EdgeIdentity[] getEdges() {
		return getEdges(getThreshold());
	}

	@Override
	public void visitFrom(GraphNodeVisitor aVisitor, String node) {
		visitFrom(aVisitor, node, getThreshold());
	}

	@Override
	public void visitTo(GraphNodeVisitor aVisitor, String node) {
		visitTo(aVisitor, node, getThreshold());
	}

	@Override
	public boolean hasDirectedEdge(String from, String to, float th) {
		return super.hasDirectedEdge(from, to) && isNodeValidAccordingToThreshold(from, to, th);
	}
	
	@Override
	public boolean hasDirectedEdge(String from, String to) {
		return hasDirectedEdge(from, to, getThreshold());
	}

	@Override
	public String[] getNodesConnectedFrom(String node, float th) {
		ArrayList<String> edges = new ArrayList<String>();

		for(String edge : super.getNodesConnectedFrom(node)){
			if(hasDirectedEdge(node, edge)){
				edges.add(edge);
			}
		}

		return edges.toArray(new String[edges.size()]);
	}
	
	@Override
	public String[] getNodesConnectedFrom(String node) {
		return getNodesConnectedFrom(node, getThreshold());
	}

	@Override
	public String[] getNodesConnectedTo(String node, float th) {
		ArrayList<String> edges = new ArrayList<String>();

		for(String edge : super.getNodesConnectedTo(node)){
			if(hasDirectedEdge(edge, node)){
				edges.add(edge);
			}
		}

		return edges.toArray(new String[edges.size()]);
	}
	
	@Override
	public String[] getNodesConnectedTo(String node) {
		return getNodesConnectedTo(node, getThreshold());
	}

	@Override
	public void switchToLearningPhase() {
		setThreshold(-1);	// For learning phase
	}
	
	@Override
	public boolean isLearningPhrase(){
		return currentTreshold < 0;
	}

	private boolean isNodeValidAccordingToThreshold(String from, String to, float threshold) {
		return getEdgeThreshold(from, to) >= threshold;
	}

	@Override
	public void resetLearnedInformations() {
		logger.info("Graph edges threshold information reset (value is now %f for all edges)", getDefaultTreshold());
		
		for(Integer k : thresholds.keySet()){
			thresholds.put(k, getDefaultTreshold());
		}
		switchToLearningPhase();
	}
}
