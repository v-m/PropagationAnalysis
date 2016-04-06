package com.vmusco.softwearn.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softwearn.exceptions.BadElementException;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.LateMutationGraphKFold;

public class MutationGraphKFoldPersistence {
	private LateMutationGraphKFold thefold;
	private LearningKGraphStream g;
	public MutationGraphKFoldPersistence(LateMutationGraphKFold thefold) throws BadElementException {
		if(!(thefold.getGraph() instanceof LearningKGraphStream)){
			throw new BadElementException("A LearningKGraphStream objectis required !");
		}
		this.thefold = thefold;
		this.g = (LearningKGraphStream) thefold.getGraph();
	}
	
	public void save(OutputStream os, String graphpath, String mutpath, String algoname) throws IOException{
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
		 *  	<node id="" name="" />
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

	public void load(InputStream is){
		
	}
}
