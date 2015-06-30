package com.vmusco.pminer.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.analyze.UseGraphMutantStats;

public abstract class UseGraphStatsXml {
	/**
	 * This constats enable to persist the whole informations
	 * However it can takes more than Gigs to persist all so we disable it
	 */
	private static boolean ENABLE_RAWDATA = false;
	
	
	
	// XML ELEMENTS/ATTRIBUTES DEFINITION
	private static final String ROOT = "statistics-for-mutop";
	private static final String _MUTATION = "mutation";
	private static final String __MUTATION_ID = "id";
	private static final String __MUTATION_PREDICTTIME = "prediction-time";
	private static final String __MUTATION_INSERTPOINT = "insert-point";
	
	private static final String __MUTATION_SETSIZES = "stats";
	private static final String ___MUTSETS_FOUNDTESTINGONLY = "found-with-testing-only";
	private static final String ___MUTSETS_FOUNDGRAPHONLY = "found-with-graph-only";
	private static final String ___MUTSETS_FOUNDBOTH = "found-with-both";
	private static final String ___MUTSETS_FOUNDTESTING = "found-with-testing";
	private static final String ___MUTSETS_FOUNDUSEGRAPH = "found-with-graph";
	private static final String ___MUTSETS_SETSIZES = "set-sizes";
	private static final String ___MUTSETS_TESTGRAPHSIZE = "tests-graph-size";
	private static final String ___MUTSETS_GRAPHSIZE = "graph-size";
	
	private static final String __MUTATION_STATS = "statistics";
	private static final String ___MUTSTAT_WITHNULLS = "nulls";
	private static final String ___MUTSTAT_NONULLS = "without-nulls";
	private static final String ____MUTSTAT_FSCORE = "f-score";
	private static final String ____MUTSTAT_RECALL = "recall";
	private static final String ____MUTSTAT_PRECISION = "precision";
	
	private static final String __MUTATION_RAWDATA = "raw-data";
	private static final String ___RAWDATA_EXECONLY = "intersection-execution-only";
	private static final String ___RAWDATA_GRAPHONLY = "intersection-graph-only";
	private static final String ___RAWDATA_INTERSECT = "intersection-exection-graph";
	private static final String ___RAWDATA_GRAPHDETERM = "graph-determined";
	private static final String ___RAWDATA_EXECDETERM = "execution-determined";
	private static final String ___RAWDATA_TESTNODES = "graph-test-nodes";
	private static final String ___RAWDATA_GRAPHNODES = "graph-nodes";
	private static final String ____RAWDATA_TEST = "test";
	
	
	public static void persistResults(StatisticsMutantAnalyzer obj, OutputStream os) throws IOException{
		Element root = new Element(ROOT);
		Document document = new Document(root);
		Comment c = new Comment("\nStatistics...\n");
		document.getContent().add(0, c);

		for(UseGraphMutantStats ugms : obj.getAllMutatObjs()){
			root.addContent(generateMutantElement(ugms));
		}
		
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(document, os);
	}

	public static StatisticsMutantAnalyzer restoreResults(InputStream is) throws IOException{
		StatisticsMutantAnalyzer sma = new StatisticsMutantAnalyzer(-1, null);
		
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			document = sxb.build(is);
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return null;
		}

		Element root = document.getRootElement();
		
		for(Element mut : root.getChildren(_MUTATION)){
			UseGraphMutantStats ugms = recoverMutant(mut);
			sma.updateStructure(ugms);
		}
		
		return sma;
	}

	public static Element generateMutantElement(UseGraphMutantStats ugms){
		Element e = new Element(_MUTATION);

		Attribute at = new Attribute(__MUTATION_ID, ugms.mutationId);
		e.setAttribute(at);
		at = new Attribute(__MUTATION_INSERTPOINT, ugms.mutationInsertionPoint);
		e.setAttribute(at);
		if(ugms.prediction_time >= 0){
			at = new Attribute(__MUTATION_PREDICTTIME, Long.toString(ugms.prediction_time));
			e.setAttribute(at);
		}

		Element stats = new Element(__MUTATION_SETSIZES);
		e.addContent(stats);

		Element tmp = new Element(___MUTSETS_GRAPHSIZE);
		tmp.setText(Integer.toString(ugms.graphsize));
		stats.addContent(tmp);

		tmp = new Element(___MUTSETS_TESTGRAPHSIZE);
		tmp.setText(Integer.toString(ugms.graphsizeonlytests));
		stats.addContent(tmp);

		Element setsizes = new Element(___MUTSETS_SETSIZES);
		stats.addContent(setsizes);
		tmp = new Element(___MUTSETS_FOUNDUSEGRAPH);
		tmp.setText(Integer.toString(ugms.nb_graph));
		setsizes.addContent(tmp);

		tmp = new Element(___MUTSETS_FOUNDTESTING);
		tmp.setText(Integer.toString(ugms.nb_mutat));
		setsizes.addContent(tmp);

		tmp = new Element(___MUTSETS_FOUNDBOTH);
		tmp.setText(Integer.toString(ugms.nb_boths));
		setsizes.addContent(tmp);

		tmp = new Element(___MUTSETS_FOUNDGRAPHONLY);
		tmp.setText(Integer.toString(ugms.nb_mores));
		setsizes.addContent(tmp);

		tmp = new Element(___MUTSETS_FOUNDTESTINGONLY);
		tmp.setText(Integer.toString(ugms.nb_lesss));
		setsizes.addContent(tmp);

		Element sta = new Element(__MUTATION_STATS);
		stats.addContent(sta);

		Element nulls = new Element(___MUTSTAT_WITHNULLS);
		sta.addContent(nulls);

		tmp = new Element(____MUTSTAT_PRECISION);
		tmp.setText(Double.toString(ugms.precision));
		nulls.addContent(tmp);

		tmp = new Element(____MUTSTAT_RECALL);
		tmp.setText(Double.toString(ugms.recall));
		nulls.addContent(tmp);

		tmp = new Element(____MUTSTAT_FSCORE);
		tmp.setText(Double.toString(ugms.fscore));
		nulls.addContent(tmp);

		Element notnulls = new Element(___MUTSTAT_NONULLS);
		sta.addContent(notnulls);

		tmp = new Element(____MUTSTAT_PRECISION);
		tmp.setText(Double.toString(ugms.precision_notnull));
		notnulls.addContent(tmp);

		tmp = new Element(____MUTSTAT_RECALL);
		tmp.setText(Double.toString(ugms.recall_notnull));
		notnulls.addContent(tmp);

		tmp = new Element(____MUTSTAT_FSCORE);
		tmp.setText(Double.toString(ugms.fscore_notnull));
		notnulls.addContent(tmp);

		// RAW DATA

		if(ENABLE_RAWDATA){
			Element datas = new Element(__MUTATION_RAWDATA);
			e.addContent(datas);
	
			addData(___RAWDATA_GRAPHNODES, ____RAWDATA_TEST, ugms.data_basin, datas);
			addData(___RAWDATA_TESTNODES, ____RAWDATA_TEST, ugms.data_basin_testnodes, datas);
	
			addData(___RAWDATA_EXECDETERM, ____RAWDATA_TEST, ugms.data_relevant, datas);
			addData(___RAWDATA_GRAPHDETERM, ____RAWDATA_TEST, ugms.data_retrieved, datas);
	
			addData(___RAWDATA_INTERSECT, ____RAWDATA_TEST, ugms.data_inter, datas);
			addData(___RAWDATA_GRAPHONLY, ____RAWDATA_TEST, ugms.data_graphOnly, datas);
			addData(___RAWDATA_EXECONLY, ____RAWDATA_TEST, ugms.data_mutationOnly, datas);
		}
		return e;
	}

	public static UseGraphMutantStats recoverMutant(Element e){
		UseGraphMutantStats ugms = new UseGraphMutantStats();

		ugms.mutationId = e.getAttributeValue(__MUTATION_ID);
		ugms.mutationInsertionPoint = e.getAttributeValue(__MUTATION_INSERTPOINT);
		
		if(e.getAttributeValue(__MUTATION_PREDICTTIME) != null){
			ugms.prediction_time = Long.valueOf(e.getAttributeValue(__MUTATION_PREDICTTIME));
		}

		Element ee = e.getChild(__MUTATION_SETSIZES);
		ugms.graphsize = Integer.parseInt(ee.getChild(___MUTSETS_GRAPHSIZE).getText());
		ugms.graphsizeonlytests = Integer.parseInt(ee.getChild(___MUTSETS_TESTGRAPHSIZE).getText());

		Element eee = ee.getChild(___MUTSETS_SETSIZES);
		ugms.nb_graph = Integer.parseInt(eee.getChild(___MUTSETS_FOUNDUSEGRAPH).getText());
		ugms.nb_mutat = Integer.parseInt(eee.getChild(___MUTSETS_FOUNDTESTING).getText());
		ugms.nb_boths = Integer.parseInt(eee.getChild(___MUTSETS_FOUNDBOTH).getText());
		ugms.nb_mores = Integer.parseInt(eee.getChild(___MUTSETS_FOUNDGRAPHONLY).getText());
		ugms.nb_lesss = Integer.parseInt(eee.getChild(___MUTSETS_FOUNDTESTINGONLY).getText());

		eee = ee.getChild(__MUTATION_STATS);
		Element eeee = eee.getChild(___MUTSTAT_WITHNULLS);
		ugms.precision = Double.parseDouble(eeee.getChild(____MUTSTAT_PRECISION).getText());
		ugms.recall = Double.parseDouble(eeee.getChild(____MUTSTAT_RECALL).getText());
		ugms.fscore = Double.parseDouble(eeee.getChild(____MUTSTAT_FSCORE).getText());

		eeee = eee.getChild(___MUTSTAT_NONULLS);
		ugms.precision_notnull = Double.parseDouble(eeee.getChild(____MUTSTAT_PRECISION).getText());
		ugms.recall_notnull = Double.parseDouble(eeee.getChild(____MUTSTAT_RECALL).getText());
		ugms.fscore_notnull = Double.parseDouble(eeee.getChild(____MUTSTAT_FSCORE).getText());


		// RAW DATA

		if(ENABLE_RAWDATA){
			ee = e.getChild(__MUTATION_RAWDATA);
	
			Set<String> tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_GRAPHNODES));
			ugms.data_basin = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_TESTNODES));
			ugms.data_basin_testnodes = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_EXECDETERM));
			ugms.data_relevant = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_GRAPHDETERM));
			ugms.data_retrieved = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_INTERSECT));
			ugms.data_inter = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_GRAPHONLY));
			ugms.data_graphOnly = tmp;
	
			tmp = extractData(ee.getChild(___RAWDATA_EXECONLY));
			ugms.data_mutationOnly = tmp;
		}
		
		return ugms;
	}

	private static Set<String> extractData(Element e){
		Set<String> ret = new HashSet<String>();

		for(Element ee : e.getChildren(____RAWDATA_TEST)){
			ret.add(ee.getText());
		}

		return ret;
	}

	private static void addData(String top, String in, Set<String> data, Element addTo){
		Element e = new Element(top);

		for(String s : data){
			Element ee = new Element(in);
			ee.setText(s);
			e.addContent(ee);
		}

		addTo.addContent(e);
	}
}
