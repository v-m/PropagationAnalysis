package com.vmusco.pminer.persistence;

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
	private static final String PMINER = "pminer";
	
	private static final String _MUTANT = "mutant";
	private static final String __MUTANT_ID = "id";
	private static final String __MUTANT_CHANGE = "change";
	
	private static final String _IMPACT = "impact";
	private static final String __IMPACT_NODE = "node";
	private static final String __IMPACT_NODEID = "id";
	
	private static final String _STATS = "statistics";
	private static final String __STATS_BOTH = "in-both";
	private static final String __STATS_ONLYTESTING = "only-in-testing";
	private static final String __STATS_ONLYUSEGRAPH = "only-in-usegraph";
	private static final String __STATS_TOTALTESTS = "total-by-tests";
	private static final String __STATS_TOTALGRAPH = "total-by-graph";
	private static final String __STATS_SETSIZES = "set-sizes";
	private static final String __STATS_GRAPHSIZE = "graph-size";

	public static void persistAll(PropagationStatistics stats, File persistFile) throws IOException{
		if(persistFile.exists())
			persistFile.delete();

		persistFile.createNewFile();

		Attribute attr;

		Element pminer = new Element(PMINER);
		Comment c = new Comment("\nUse-graph\n");
		pminer.getContent().add(0, c);
		Document d = new Document(pminer);

		for(String mutid : stats.getAllPropagationsMutantsIds()){
			UseGraphMutantStats gstats = stats.getPropagationStatsticsInMutation(mutid);

			Element aMutant = new Element(_MUTANT);
			attr = new Attribute(__MUTANT_ID, gstats.mutationId);
			aMutant.setAttribute(attr);
			attr = new Attribute(__MUTANT_CHANGE, gstats.mutationInsertionPoint);
			aMutant.setAttribute(attr);

			pminer.addContent(aMutant);

			Element impact = new Element(_IMPACT);
			aMutant.addContent(impact);
			UseGraph graph = stats.getPropagationInMutation(mutid);
			persistImpact(impact, graph);

			Element stnode = new Element(_STATS);
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

		e = new Element(__STATS_GRAPHSIZE);
		e.setText(Integer.toString(gstats.graphsize));
		root.addContent(e);

		Element sets = new Element(__STATS_SETSIZES);
		root.addContent(sets);

		e = new Element(__STATS_TOTALGRAPH);
		e.setText(Integer.toString(gstats.nb_graph));
		sets.addContent(e);

		e = new Element(__STATS_TOTALTESTS);
		e.setText(Integer.toString(gstats.nb_mutat));
		sets.addContent(e);

		e = new Element(__STATS_ONLYUSEGRAPH);
		e.setText(Integer.toString(gstats.nb_mores));
		sets.addContent(e);

		e = new Element(__STATS_ONLYTESTING);
		e.setText(Integer.toString(gstats.nb_lesss));
		sets.addContent(e);

		e = new Element(__STATS_BOTH);
		e.setText(Integer.toString(gstats.nb_boths));
		sets.addContent(e);
	}

	private static void persistImpact(Element root, UseGraph aGraph){
		for(String node : aGraph.getBasinNodes()){
			Element e = new Element(__IMPACT_NODE);
			Attribute attr = new Attribute(__IMPACT_NODEID, node);
			e.setAttribute(attr);
			root.addContent(e);
		}
	}

}
