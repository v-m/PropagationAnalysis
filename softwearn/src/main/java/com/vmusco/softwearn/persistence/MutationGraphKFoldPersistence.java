package com.vmusco.softwearn.persistence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.pminer.impact.GraphPropagationExplorerForTests;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.persistence.GraphML;
import com.vmusco.softminer.graphs.persistence.GraphPersistence;
import com.vmusco.softwearn.exceptions.BadElementException;
import com.vmusco.softwearn.learn.LearningKGraph;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.LateMutationGraphKFold;

public class MutationGraphKFoldPersistence {
	private LateMutationGraphKFold thefold;
	private LearningKGraphStream g;
	
	public MutationGraphKFoldPersistence(LateMutationGraphKFold thefold) throws BadElementException {
		this.thefold = thefold;
	}
	
	public void save(OutputStream os, String graphpath, String mutpath, String algoname) throws IOException, BadElementException{
		if(!(thefold.getGraph() instanceof LearningKGraphStream)){
			throw new BadElementException("A LearningKGraphStream objectis required !");
		}
		this.g = (LearningKGraphStream) thefold.getGraph();
		
		Format format = Format.getPrettyFormat();
		format.setLineSeparator(LineSeparator.UNIX);
		XMLOutputter output = new XMLOutputter(format);
		Document document = new Document(getSaveContent(graphpath, mutpath, algoname));
		//Comment c = new Comment("\nThis file resumes the weight learning.\n");
		//document.getContent().add(0, c);
		output.output(document, os);
	}
	
	private Element getSaveContent(String graphpath, String mutpath, String algoname) {
		//<MutationGraphKFold>
		Element root = new Element("MutationGraphKFold");
		
		//- <dependencies>
		Element top = new Element("dependencies");
		root.addContent(top);
		
		//-- <graph>
		Element in = new Element("graph");
		in.setText(graphpath);
		top.addContent(in);
		
		//-- <mutations>
		in = new Element("mutations");
		in.setText(mutpath);
		top.addContent(in);
		
		/* 
		 * <config>
		 * 	<nbmut>300</nbmut>
		 *  <kfold>10</kfold>
		 *  <ksp>10</ksp>
		 *  <init-weight>0</init-weight>
		 *  <algo>dicho</algo>
		 * </config>
		 */
		
		top = new Element("config");
		root.addContent(top);
		
		in = new Element("nbmut");
		in.setText(Integer.toString(thefold.getInputDatasetSize()));
		top.addContent(in);

		in = new Element("kfold");
		in.setText(Integer.toString(thefold.getK()));
		top.addContent(in);

		in = new Element("ksp");
		in.setText(Integer.toString(thefold.getLearner().getKspNr()));
		top.addContent(in);

		in = new Element("init-weight");
		in.setText(Float.toString(thefold.getLearner().defaultInitWeight()));
		top.addContent(in);

		in = new Element("algo");
		in.setText(algoname);
		top.addContent(in);
		
		/*
		 * <execution>
		 * 	<mutation-split>
		 * 		<k id="1">
		 * 			<mutant id="mutant_12" />
		 * 		</k>
		 *  </mutation-split>
		 *  <graph-mapping>
		 *  	<edge id="" name="" />
		 *  </graph-mapping>
		 *  <weights>
		 *  	<k id="1">
		 *  		<weight nodeid="1">0.34</weight>
		 *  	</k>
		 *  </weights>
		 * </execution>
		 */

		top = new Element("execution");
		root.addContent(top);
		
		/*
		 * <mutation-split>
		 * 		<k id="1">
		 * 			<mutant id="mutant_12" />
		 * 		</k>
		 *  </mutation-split>
		 */
		
		in = new Element("mutation-split");
		top.addContent(in);
		
		int kcpt = 0;
		for(MutantIfos[] mis : thefold.getPartitionDataset()){
			Element k = new Element("k");
			k.setAttribute("id", Integer.toString(kcpt++));
			in.addContent(k);
			
			for(MutantIfos mi : mis){
				Element mutant = new Element("mutant");
				mutant.setAttribute("id", mi.getId());
				k.addContent(mutant);
			}
		}
		
		/*
		 *  <graph-mapping>
		 *  	<edge id="" name="" />
		 *  </graph-mapping>
		 */
		
		in = new Element("graph-mapping");
		top.addContent(in);
		
		int  i = 0;
		for(String s : g.getIntToStrBuffer()){
			Element node = new Element("edge");
			node.setAttribute("id", Integer.toString(i++));
			node.setAttribute("name", s);
			in.addContent(node);
		}
		
		/*
		 *  <weights learning-time="x">
		 *  	<k id="1">
		 *  		<weight nodeid="1">0.34</weight>
		 *  	</k>
		 *  </weights>
		 */

		Map<Integer, Map<Integer, Float>> allThresholds = g.getAllThresholds();
		
		in = new Element("weights");
		in.setAttribute("learning-time", Long.toString(thefold.getLearner().getLastLearningTime()));
		top.addContent(in);
		
		for(int kid : allThresholds.keySet()){
			Element k = new Element("k");
			k.setAttribute("id", Integer.toString(kid));
			in.addContent(k);
			
			Map<Integer, Float> map = allThresholds.get(kid);
			
			for(int kkid : map.keySet()){
				Element weight = new Element("weight");
				weight.setAttribute("id", Integer.toString(kkid));
				weight.setText(Float.toString(map.get(kkid)));
				k.addContent(weight);
			}
		}
		
		return root;
	}

	public void load(InputStream is) throws JDOMException, IOException, PersistenceException{
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		document = sxb.build(is);
		Element root = document.getRootElement();
		effectiveLoading(root);
	}

	private void effectiveLoading(Element root) throws IOException, PersistenceException {
		// Restore ms
		MutationStatistics ms = MutationStatistics.loadState(root.getChild("dependencies").getChild("mutations").getText());
		thefold.setMutationStatisticsObject(ms);
		ms.listViableAndRunnedMutants(true);
		
		// Restore the mutation partition state
		List<MutantIfos[]> partition = new ArrayList<>();
		for(Element splitk : root.getChild("execution").getChild("mutation-split").getChildren("k")){
			int splitid = Integer.parseInt(splitk.getAttributeValue("id"));
			
			List<MutantIfos> onemicontent = new ArrayList<>();
			
			for(Element splitone : splitk.getChildren("mutant")){
				String mutid = splitone.getAttributeValue("id");
				MutantIfos onemi = ms.getMutationStats(mutid);
				onemicontent.add(onemi);
			}
			
			partition.add(onemicontent.toArray(new MutantIfos[onemicontent.size()]));
		}
		
		thefold.setPartitionDataset(partition);
		
		// Restore the graph
		String graphPath = root.getChild("dependencies").getChild("graph").getText();
		float initW = Float.parseFloat(root.getChild("config").getChild("init-weight").getText());
		int k = Integer.parseInt(root.getChild("config").getChild("kfold").getText());

		LearningKGraphStream g = new LearningKGraphStream(initW, k);
		GraphPersistence gp = new GraphML(g.graph());
		gp.load(new FileInputStream(graphPath));
		
		
		
		// Restore mappings
		List<String> maps = new ArrayList<>();
		for(Element e : root.getChild("execution").getChild("graph-mapping").getChildren("edge")){
			String name = e.getAttributeValue("name");
			maps.add(name);
		}
		
		g.setIntToStrBuffer(maps.toArray(new String[maps.size()]));
		
		// Restore K-thresholds
		Map<Integer, Map<Integer, Float>> kthresh = new HashMap<Integer, Map<Integer,Float>>();
		for(Element kelem : root.getChild("execution").getChild("weights").getChildren("k")){
			int kid = Integer.parseInt(kelem.getAttributeValue("id"));
			
			Map<Integer,Float> mapForK = new HashMap<>();
			
			for(Element kelemw : kelem.getChildren("weight")){
				int nodeid = Integer.parseInt(kelemw.getAttributeValue("id"));
				float weight = Float.parseFloat(kelemw.getText());
				
				mapForK.put(nodeid, weight);
			}
			
			kthresh.put(kid, mapForK);
		}
		thefold.setGraph(g);
		
		g.setAllThresholds(kthresh);
		
		thefold.setLearner(null);
		
		String[] tests = ms.getTestCases();
		ConsequencesExplorer t = new GraphPropagationExplorerForTests(g.graph(), tests);
		thefold.setTester(t);
	}
}
