package com.vmusco.softminer.graphs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.FileSinkImages.Quality;
import org.graphstream.stream.file.FileSinkImages.RendererType;
import org.graphstream.stream.file.FileSinkImages.Resolutions;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerPipe;

import com.vmusco.smf.utils.SourceReference;
import com.vmusco.softminer.exceptions.IncompatibleTypesException;

/**
 *
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class GraphStream extends Graph {
	private Viewer lastViewer;

	public GraphStream() {
		this.graph = new SingleGraph("Untitled Graph");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
	}

	@Override
	public Graph createNewLikeThis() {
		return new GraphStream();
	}

	@Override
	public void addNode(String name){
		addNode(name, false);
	}
	
	public void addNode(String name, boolean displayLabel){
		if(!this.hasNode(name)){
			this.getGraph().addNode(name);

			if(displayLabel)
				setNodeLabel(name, name);
		}
	}

	@Override
	public void addDirectedEdge(String from, String to){
		this.addDirectedEdge(from, to, false);
	}
	
	public void addDirectedEdge(String from, String to, boolean displayLabel){
		if(!this.hasDirectedEdge(from, to)){
			String name = buildEdgeName(from, to, true);
			this.getGraph().addEdge(name, from, to, true);

			if(displayLabel)
				setEdgeLabel(name, name);
		}
	}

	protected void setNodeLabel(String id, String label){
		if(label == null){
			getGraph().getNode(id).removeAttribute("ui.label");
			HashMap<String, String> na = getNodeAttribute(id);
			na.remove("text-alignment");
			setNodeAttribute(id, na);
		}else{
			getGraph().getNode(id).setAttribute("ui.label", label);
			HashMap<String, String> na = getNodeAttribute(id);
			na.put("text-alignment", "under");
			setNodeAttribute(id, na);
		}
	}

	protected String getNodeLabel(String id){
		return getGraph().getNode(id).getAttribute("ui.label");
	}

	protected void setEdgeLabel(String id, String label){
		if(label == null)
			getGraph().getEdge(id).removeAttribute("ui.label");
		else
			getGraph().getEdge(id).setAttribute("ui.label", label);
	}

	protected String getEdgeLabel(String id){
		return getGraph().getEdge(id).getAttribute("ui.label");
	}

	/*public void bestDisplay(){
		this.getGraph().display();
	}*/

	public void bestDisplay(){
		bestDisplay(true);
	}

	public void bestDisplay(boolean closePrevious){
		if(closePrevious && lastViewer != null){
			lastViewer.close();
		}

		lastViewer = getGraph().display();
		lastViewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

		ViewerPipe fromViewer = lastViewer.newViewerPipe();
		MyViewerListener mvl = new MyViewerListener(this, lastViewer);
		fromViewer.addViewerListener(mvl);
		fromViewer.addSink(getGraph());

		while(mvl.isAlive()) {
			fromViewer.pump();
		}
	}

	@Override
	public SingleGraph getGraph(){
		return ((SingleGraph)this.graph);
	}

	private static String buildEdgeName(String from, String to, boolean directed){
		return from + (directed?">":"-") + to;
	}

	public static String buildDirectedEdgeName(String from, String to){
		return buildEdgeName(from, to, true);
	}

	@Override
	public boolean hasNode(String name) {
		return !(this.getGraph().getNode(name) == null);
	}

	@Override
	public boolean hasDirectedEdge(String from, String to) {
		String name = buildEdgeName(from, to, true);
		return !(this.getGraph().getEdge(name) == null);
	}

	@Override
	public void addDirectedEdgeAndNodeIfNeeded(String from, String to){
		addDirectedEdgeAndNodeIfNeeded(from, to, false, false);
	}
	
	public void addDirectedEdgeAndNodeIfNeeded(String from, String to, boolean nodesLabel, boolean edgesLabel) {
		this.addNode(from, nodesLabel);
		this.addNode(to, nodesLabel);
		this.addDirectedEdge(from, to, edgesLabel);
	}

	@Override
	public int getNbNodes() {
		return this.getGraph().getNodeCount();
	}

	@Override
	public int getNbNodes(NodeTypes t) {
		int nb = 0;

		for(String n : getNodesNames()){
			if(getNodeType(n) == t)
				nb++;
		}

		return nb;
	}

	@Override
	public boolean colorNode(String name, String color) {
		if(!this.hasNode(name))
			return false;

		HashMap<String, String> nodeAttribute = this.getNodeAttribute(name);
		nodeAttribute.put("fill-color", color);
		this.setNodeAttribute(name, nodeAttribute);
		return true;
	}

	@Override
	public boolean shadowNode(String name, int shadowSize, String shadowColor) {
		if(!this.hasNode(name))
			return false;

		HashMap<String, String> nodeAttribute = this.getNodeAttribute(name);
		nodeAttribute.put("shadow-color", shadowColor);
		nodeAttribute.put("shadow-width", shadowSize+"px");
		nodeAttribute.put("shadow-offset", "0px");
		nodeAttribute.put("shadow-mode", "plain");
		this.setNodeAttribute(name, nodeAttribute);

		return true;
	}
	@Override
	public String[] getNodesConnectedTo(String node) {
		Node node2 = this.getGraph().getNode(node);
		if(node2 == null)
			return null;

		Iterator<Edge> enteringEdgeIterator = node2.getEnteringEdgeIterator();

		ArrayList<String> edges = new ArrayList<String>();

		while(enteringEdgeIterator.hasNext()){
			Edge next = enteringEdgeIterator.next();
			if(!next.isDirected())
				continue;

			edges.add(next.getSourceNode().getId());
		}

		return edges.toArray(new String[edges.size()]);
	}

	@Override
	public String[] getNodesConnectedFrom(String node) {
		Node node2 = this.getGraph().getNode(node);
		if(node2 == null)
			return null;

		Iterator<Edge> enteringEdgeIterator = node2.getLeavingEdgeIterator();

		ArrayList<String> edges = new ArrayList<String>();

		while(enteringEdgeIterator.hasNext()){
			Edge next = enteringEdgeIterator.next();
			if(!next.isDirected())
				continue;

			edges.add(next.getTargetNode().getId());
		}

		return edges.toArray(new String[edges.size()]);
	}

	@Override
	public boolean sizeNode(String name, NodeSize size) {
		if(this.getGraph().getNode(name) == null)
			return false;

		HashMap<String, String> nodeAttribute = this.getNodeAttribute(name);

		if(size == NodeSize.SMALL)
			nodeAttribute.put("size", "5px");
		else if(size == NodeSize.LARGE)
			nodeAttribute.put("size", "20px");
		else
			nodeAttribute.put("size", "10px");

		this.setNodeAttribute(name, nodeAttribute);

		return true;
	}

	@Override
	public boolean shapeNode(String name, NodeShape shape) {
		if(this.getGraph().getNode(name) == null)
			return false;

		HashMap<String, String> nodeAttribute = this.getNodeAttribute(name);

		switch(shape){
		case BOX:
			nodeAttribute.put("shape", "box");
			break;
		case CROSS:
			nodeAttribute.put("shape", "cross");
			break;
		case DIAMOND:
			nodeAttribute.put("shape", "diamond");
			break;
		case PIES:
			nodeAttribute.put("shape", "pie-chart");
			break;
		case ROUNDED_BOX:
			nodeAttribute.put("shape", "rounded-box");
			break;
		default:
			nodeAttribute.put("shape", "circle");
			break;
		}

		this.setNodeAttribute(name, nodeAttribute);

		return true;
	}

	protected HashMap<String, String> getNodeAttribute(String node){
		HashMap<String, String> ret = new HashMap<String, String>();

		String attr = this.getGraph().getNode(node).getAttribute("ui.style");

		if(attr != null){
			for(String itm : attr.split(";")){
				String[] parts = itm.split(":");
				if(parts.length < 2)
					continue;
				ret.put(parts[0], parts[1]);
			}
		}

		return ret;
	}

	protected void setNodeAttribute(String node, HashMap<String, String> attrs){
		if(!this.hasNode(node))
			return;

		Iterator<String> iterator = attrs.keySet().iterator();
		String ret = "";

		while(iterator.hasNext()){
			String next = iterator.next();

			ret += next+":"+attrs.get(next)+";";
		}

		this.getGraph().getNode(node).setAttribute("ui.style", ret);
	}

	protected HashMap<String, String> getEdgeAttribute(String from, String to){
		HashMap<String, String> ret = new HashMap<String, String>();

		String attr = this.getGraph().getEdge(buildEdgeName(from, to, true)).getAttribute("ui.style");

		if(attr != null){
			for(String itm : attr.split(";")){
				String[] parts = itm.split(":");
				if(parts.length < 2)
					continue;
				ret.put(parts[0], parts[1]);
			}
		}

		return ret;
	}

	protected void setEdgeAttribute(String from, String to, HashMap<String, String> attrs){
		if(!this.hasDirectedEdge(from, to))
			return;

		Iterator<String> iterator = attrs.keySet().iterator();
		String ret = "";

		while(iterator.hasNext()){
			String next = iterator.next();

			ret += next+":"+attrs.get(next)+";";
		}

		this.getGraph().getEdge(buildEdgeName(from, to, true)).setAttribute("ui.style", ret);
	}

	@Override
	public String[] getNodesNames() {
		ArrayList<String> nodes = new ArrayList<String>();

		Iterator<Node> nodeIterator = this.getGraph().getNodeIterator();
		while(nodeIterator.hasNext()){
			Node n = nodeIterator.next();
			nodes.add(n.getId());
		}

		return nodes.toArray(new String[nodes.size()]);
	}

	@Override
	public void markEdge(String from, String to, EdgeMarkers aMarker){
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));
		edge.setAttribute(aMarker.name(), true);
	}

	@Override
	public void setEdgeType(String from, String to, EdgeTypes aType){
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));
		edge.setAttribute("type", aType);
	}

	@Override
	public boolean isEdgeMarked(String from, String to, EdgeMarkers aMarker){
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));
		return edge.hasAttribute(aMarker.name());
	}

	@Override
	public EdgeTypes getEdgeType(String from, String to){
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));
		if(edge == null)
			return null;

		return (EdgeTypes) edge.getAttribute("type");
	}


	@Override
	public void markNode(String node, NodeMarkers aMarker){
		Node node2 = this.getGraph().getNode(node);
		node2.setAttribute(aMarker.name(), true);
	}

	@Override
	public void setNodeType(String node, NodeTypes aType){
		Node node2 = this.getGraph().getNode(node);
		node2.setAttribute("type", aType);
	}

	@Override
	public boolean isNodeMarked(String node, NodeMarkers aMarker){
		Node node2 = this.getGraph().getNode(node);
		return node2.hasAttribute(aMarker.name());
	}

	@Override
	public NodeTypes getNodeType(String node){
		Node node2 = this.getGraph().getNode(node);
		return (NodeTypes) node2.getAttribute("type");
	}

	public NodeMarkers[] getNodeMarkers(String node){
		HashSet<NodeMarkers> ret = new HashSet<>();

		for(NodeMarkers aMarker : NodeMarkers.values()){
			if(isNodeMarked(node, aMarker))
				ret.add(aMarker);
		}

		return ret.toArray(new NodeMarkers[0]);
	}

	public EdgeMarkers[] getEdgeMarkers(String from, String to){
		HashSet<EdgeMarkers> ret = new HashSet<>();

		for(EdgeMarkers aMarker : EdgeMarkers.values()){
			if(isEdgeMarked(from, to, aMarker))
				ret.add(aMarker);
		}

		return ret.toArray(new EdgeMarkers[0]);
	}

	@Override
	public EdgeIdentity[] getEdges() {
		EdgeIdentity[] ret = new EdgeIdentity[this.getGraph().getEdgeCount()];

		int i = 0;
		for(Edge ed : this.getGraph().getEdgeSet()){
			ret[i] = new EdgeIdentity(ed.getSourceNode().getId(), ed.getTargetNode().getId());
			i += 1;
		}

		return ret;
	}

	@Override
	public GraphApi getGraphFamily() {
		return GraphApi.GRAPH_STREAM;
	}

	@Override
	public void persistAsImage(String persistTo, boolean shake) {
		// see: http://graphstream-project.org/doc/Tutorials/Creating-a-movie-with-FileSinkImages_1.0/

		//getGraph().addAttribute("ui.screenshot", persistTo.getAbsolutePath());
		//System.out.println("Persist image to "+getGraph().getAttribute("ui.screenshot")+" :)");

		/*FileSinkSVG2 pic = new FileSinkSVG2();
		SpringBox sb = new SpringBox();
		getGraph().addSink(sb);
		sb.addAttributeSink(getGraph());

		Toolkit.computeLayout(getGraph());

		sb.shake();
		sb.compute();

		while (sb.getStabilization() < 1) {
			sb.compute();
		}*/

		FileSinkImages pic = new FileSinkImages(OutputType.PNG, Resolutions.HD720);		
		if(shake)
			pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);

		pic.setQuality(Quality.HIGH);
		pic.setRenderer(RendererType.SCALA);			// TO RENDER OTHER TYPOS ITEMS SUCH AS SHAPE AND SHADOWS (https://github.com/graphstream/gs-core/issues/49)

		// Store and disable out stream during persistance
		// this is needed because graphstream output stuffs during the exportation process :(
		PrintStream tmp = System.out;
		System.setOut(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
			}
		}));

		try {

			pic.writeAll(getGraph(), persistTo+".png");
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setOut(tmp);
	}

	@Override
	public Graph keepOnly(NodeTypes[] nt, NodeMarkers[] nm, EdgeTypes[] et, EdgeMarkers[] em) {
		Graph g = new GraphStream();

		for(String n : getNodesNames()){
			for(NodeTypes nt2 : nt){
				if(getNodeType(n) == nt2){
					g.addNode(n);
					g.setNodeType(n, getNodeType(n));
				}
			}

			for(NodeMarkers nm2 : nm){
				if(isNodeMarked(n, nm2)){
					g.addNode(n);
					g.markNode(n, nm2);
				}
			}
		}

		for(EdgeIdentity e : getEdges()){
			for(EdgeTypes et2 : et){
				if(getEdgeType(e.getFrom(), e.getTo()) == et2){
					g.addDirectedEdgeAndNodeIfNeeded(e.getFrom(), e.getTo());
					g.setEdgeType(e.getFrom(), e.getTo(), getEdgeType(e.getFrom(), e.getTo()));
				}
			}

			for(EdgeMarkers em2 : em){
				if(isEdgeMarked(e.getFrom(), e.getTo() , em2)){
					g.addDirectedEdgeAndNodeIfNeeded(e.getFrom(), e.getTo());
					g.markEdge(e.getFrom(), e.getTo(), em2);
				}
			}
		}

		return g;
	}

	@Override
	public boolean colorEdge(String from, String to, String color) {
		if(!this.hasDirectedEdge(from, to))
			return false;

		HashMap<String, String> edgeAttributes = this.getEdgeAttribute(from, to);
		edgeAttributes.put("fill-color", color);
		this.setEdgeAttribute(from, to, edgeAttributes);
		return true;
	}

	@Override
	public boolean sizeEdge(String from, String to, int size) {
		if(!this.hasDirectedEdge(from, to))
			return false;

		HashMap<String, String> edgeAttributes = this.getEdgeAttribute(from, to);

		edgeAttributes.put("size", size+"px");
		edgeAttributes.put("arrow-size", size*2+"px");
		this.setEdgeAttribute(from, to, edgeAttributes);

		return true;
	}

	@Override
	public boolean labelEdge(String from, String to, String label){
		setEdgeLabel(buildEdgeName(from, to, true), label);
		return true;
	}

	@Override
	public boolean labelNode(String name, String label){
		setNodeLabel(name, label);
		return true;
	}

	@Override
	public boolean appendLabelEdge(String from, String to, String label) {
		String name = buildEdgeName(from, to, true);
		labelEdge(from, to, getEdgeLabel(name)+label);
		return true;
	}

	@Override
	public boolean appendLabelNode(String name, String label) {
		labelNode(name, getNodeLabel(name)+label);
		return true;
	}

	/**
	 * For debugging purposes
	 * @param a
	 * @return
	 */
	public static String printPath(String[] a){
		String r = "";

		for(String s : a){
			r += (r.length()>0?"->":"")+s;
		}

		return r;
	}

	@Override
	public void bindEdgeToSourcePosition(String from, String to, SourceReference sp) {
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));

		List<SourceReference> attribute = edge.getAttribute("sps");

		if(attribute == null){
			attribute = new ArrayList<SourceReference>();
			edge.setAttribute("sps", attribute);
		}

		attribute.add(sp);
	}

	@Override
	public SourceReference[] getSourcePositionForEdge(String from, String to) {
		Edge edge = this.getGraph().getEdge(buildEdgeName(from, to, true));

		List<SourceReference> attribute = edge.getAttribute("sps");

		if(attribute == null){
			return new SourceReference[0];
		}

		return attribute.toArray(new SourceReference[0]);
	}

	@Override
	public void bindNodeToSourcePosition(String n, SourceReference sp) {
		Node node = this.getGraph().getNode(n);

		List<SourceReference> attribute = node.getAttribute("sps");

		if(attribute == null){
			attribute = new ArrayList<SourceReference>();
			node.setAttribute("sps", attribute);
		}

		attribute.add(sp);
	}

	@Override
	public SourceReference[] getSourcePositionForNode(String n) {
		Node node = this.getGraph().getNode(n);
		
		List<SourceReference> attribute = node.getAttribute("sps");

		if(attribute == null){
			return new SourceReference[0];
		}

		return attribute.toArray(new SourceReference[0]);
	}
	
	@Override
	public void removeDirectedEdge(String from, String to) {
		getGraph().removeEdge(from, to);
	}

	@Override
	public void removeNode(String id) {
		getGraph().removeNode(id);
	}

	@Override
	public void setNodeFormalTypes(String node, List<String> types) {
		Node node2 = this.getGraph().getNode(node);
		node2.setAttribute("formaltypes", types);
	}

	@Override
	public List<String> getNodeFormalTypes(String node) {
		Node node2 = this.getGraph().getNode(node);
		
		List<String> re  = node2.getAttribute("formaltypes");
		if(re == null)
			return new ArrayList<String>();
		
		return re;
	}

	@Override
	public void renameNode(String oldname, String newname) {
		List<String> enteredge = new ArrayList<>();
		List<String> exitedge = new ArrayList<>();
		
		for(Edge e : getGraph().getNode(oldname).getEachEnteringEdge()){
			enteredge.add(e.getSourceNode().getId());
		}
		
		for(Edge e : getGraph().getNode(oldname).getEachLeavingEdge()){
			exitedge.add(e.getTargetNode().getId());
		}
		
		removeNode(oldname);
		addNode(newname);
		
		for(String s : enteredge){
			addDirectedEdgeAndNodeIfNeeded(s, newname);
		}
		
		for(String s : exitedge){
			addDirectedEdgeAndNodeIfNeeded(newname, s);
		}
	}

	@Override
	public void conformizeWith(Graph g) throws IncompatibleTypesException {
		if(g instanceof GraphStream){
			GraphStream gs = (GraphStream)g;
			
			for(Node n : getGraph().getNodeSet()){
				conformizeNodeWith(g, n.getId());
			}
			
			for(EdgeIdentity ei : getEdges()){
				conformizeEdgeWith(g, ei.getFrom(), ei.getTo());
			}
		}else{
			throw new IncompatibleTypesException();
		}
	}

	@Override
	public void conformizeNodeWith(Graph g, String node) throws IncompatibleTypesException {
		if(g instanceof GraphStream){
			GraphStream gs = (GraphStream)g;
			
			Node n = getGraph().getNode(node);
			Node mirror = gs.getGraph().getNode(node);
			
			for(String attrib : mirror.getAttributeKeySet()){
				n.setAttribute(attrib, mirror.getAttribute(attrib));
			}
		}else{
			throw new IncompatibleTypesException();
		}
	}

	@Override
	public void conformizeEdgeWith(Graph g, String from, String to) throws IncompatibleTypesException {
		if(g instanceof GraphStream){
			GraphStream gs = (GraphStream)g;
			
			String edgename = GraphStream.buildDirectedEdgeName(from, to);
			Edge e = getGraph().getEdge(edgename);
			Edge mirror = gs.getGraph().getEdge(edgename);
			
			for(String attrib : mirror.getAttributeKeySet()){
				e.setAttribute(attrib, mirror.getAttribute(attrib));
			}
		}else{
			throw new IncompatibleTypesException();
		}
		
	}

}
