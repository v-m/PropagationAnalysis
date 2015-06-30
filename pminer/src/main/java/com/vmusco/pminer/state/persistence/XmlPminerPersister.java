package com.vmusco.pminer.state.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Attribute;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.analyze.UseGraphMutantStats;

public abstract class XmlPminerPersister {
	public static void persistAll(PropagationStatistics stats, File persistFile) throws IOException{
		if(persistFile.exists())
			persistFile.delete();

		persistFile.createNewFile();

		Attribute attr;

		Element pminer = new Element("pminer");
		Comment c = new Comment("\nUse-graph\n");
		pminer.getContent().add(0, c);
		Document d = new Document(pminer);

		for(String mutid : stats.getAllPropagationsMutantsIds()){
			UseGraphMutantStats gstats = stats.getPropagationStatsticsInMutation(mutid);

			Element aMutant = new Element("mutant");
			attr = new Attribute("id", gstats.mutationId);
			aMutant.setAttribute(attr);
			attr = new Attribute("change", gstats.mutationInsertionPoint);
			aMutant.setAttribute(attr);

			pminer.addContent(aMutant);

			Element impact = new Element("impact");
			aMutant.addContent(impact);
			UseGraph graph = stats.getPropagationInMutation(mutid);
			persistImpact(impact, graph);

			Element stnode = new Element("statistics");
			aMutant.addContent(stnode);
			persistStats(stnode, gstats);
		}

		FileOutputStream fos = new FileOutputStream(persistFile);
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(d, fos);
		fos.close();
	}

	private static void persistStats(Element root, UseGraphMutantStats gstats){
		Element e;

		e = new Element("graph-size");
		e.setText(Integer.toString(gstats.graphsize));
		root.addContent(e);

		Element sets = new Element("set-sizes");
		root.addContent(sets);

		e = new Element("total-by-graph");
		e.setText(Integer.toString(gstats.nb_graph));
		sets.addContent(e);

		e = new Element("total-by-tests");
		e.setText(Integer.toString(gstats.nb_mutat));
		sets.addContent(e);

		e = new Element("only-in-usegraph");
		e.setText(Integer.toString(gstats.nb_mores));
		sets.addContent(e);

		e = new Element("only-in-testing");
		e.setText(Integer.toString(gstats.nb_lesss));
		sets.addContent(e);

		e = new Element("in-both");
		e.setText(Integer.toString(gstats.nb_boths));
		sets.addContent(e);
	}

	private static void persistImpact(Element root, UseGraph aGraph){
		for(String node : aGraph.getBasinNodes()){
			Element e = new Element("node");
			Attribute attr = new Attribute("id", node);
			e.setAttribute(attr);
			root.addContent(e);
		}
	}

}
