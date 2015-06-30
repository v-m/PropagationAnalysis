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
	public static void persistResults(StatisticsMutantAnalyzer obj, OutputStream os) throws IOException{
		Element root = new Element("statistics-for-mutop");
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
		
		for(Element mut : root.getChildren()){
			UseGraphMutantStats ugms = recoverMutant(mut);
			sma.updateStructure(ugms);
		}
		
		return sma;
	}

	public static Element generateMutantElement(UseGraphMutantStats ugms){
		Element e = new Element("mutation");

		Attribute at = new Attribute("id", ugms.mutationId);
		e.setAttribute(at);
		at = new Attribute("insert-point", ugms.mutationInsertionPoint);
		e.setAttribute(at);
		if(ugms.prediction_time >= 0){
			at = new Attribute("prediction-time", Long.toString(ugms.prediction_time));
			e.setAttribute(at);
		}

		Element stats = new Element("stats");
		e.addContent(stats);

		Element tmp = new Element("graph-size");
		tmp.setText(Integer.toString(ugms.graphsize));
		stats.addContent(tmp);

		tmp = new Element("tests-graph-size");
		tmp.setText(Integer.toString(ugms.graphsizeonlytests));
		stats.addContent(tmp);

		Element setsizes = new Element("set-sizes");
		stats.addContent(setsizes);
		tmp = new Element("found-with-graph");
		tmp.setText(Integer.toString(ugms.nb_graph));
		setsizes.addContent(tmp);

		tmp = new Element("found-with-testing");
		tmp.setText(Integer.toString(ugms.nb_mutat));
		setsizes.addContent(tmp);

		tmp = new Element("found-with-both");
		tmp.setText(Integer.toString(ugms.nb_boths));
		setsizes.addContent(tmp);

		tmp = new Element("found-with-graph-only");
		tmp.setText(Integer.toString(ugms.nb_mores));
		setsizes.addContent(tmp);

		tmp = new Element("found-with-testing-only");
		tmp.setText(Integer.toString(ugms.nb_lesss));
		setsizes.addContent(tmp);

		Element sta = new Element("statistics");
		stats.addContent(sta);

		Element nulls = new Element("nulls");
		sta.addContent(nulls);

		tmp = new Element("precision");
		tmp.setText(Double.toString(ugms.precision));
		nulls.addContent(tmp);

		tmp = new Element("recall");
		tmp.setText(Double.toString(ugms.recall));
		nulls.addContent(tmp);

		tmp = new Element("f-score");
		tmp.setText(Double.toString(ugms.fscore));
		nulls.addContent(tmp);

		Element notnulls = new Element("without-nulls");
		sta.addContent(notnulls);

		tmp = new Element("precision");
		tmp.setText(Double.toString(ugms.precision_notnull));
		notnulls.addContent(tmp);

		tmp = new Element("recall");
		tmp.setText(Double.toString(ugms.recall_notnull));
		notnulls.addContent(tmp);

		tmp = new Element("f-score");
		tmp.setText(Double.toString(ugms.fscore_notnull));
		notnulls.addContent(tmp);

		// RAW DATA

		/*Element datas = new Element("raw-data");
		e.addContent(datas);

		addData("graph-nodes", "test", ugms.data_basin, datas);
		addData("graph-test-nodes", "test", ugms.data_basin_testnodes, datas);

		addData("execution-determined", "test", ugms.data_relevant, datas);
		addData("graph-determined", "test", ugms.data_retrieved, datas);

		addData("intersection-exection-graph", "test", ugms.data_inter, datas);
		addData("intersection-graph-only", "test", ugms.data_graphOnly, datas);
		addData("intersection-execution-only", "test", ugms.data_mutationOnly, datas);*/
		
		return e;
	}




	public static UseGraphMutantStats recoverMutant(Element e){
		UseGraphMutantStats ugms = new UseGraphMutantStats();

		ugms.mutationId = e.getAttributeValue("id");
		ugms.mutationInsertionPoint = e.getAttributeValue("insert-point");
		
		if(e.getAttributeValue("prediction-time") != null){
			ugms.prediction_time = Long.valueOf(e.getAttributeValue("prediction-time"));
		}

		Element ee = e.getChild("stats");
		ugms.graphsize = Integer.parseInt(ee.getChild("graph-size").getText());
		ugms.graphsizeonlytests = Integer.parseInt(ee.getChild("tests-graph-size").getText());

		Element eee = ee.getChild("set-sizes");
		ugms.nb_graph = Integer.parseInt(eee.getChild("found-with-graph").getText());
		ugms.nb_mutat = Integer.parseInt(eee.getChild("found-with-testing").getText());
		ugms.nb_boths = Integer.parseInt(eee.getChild("found-with-both").getText());
		ugms.nb_mores = Integer.parseInt(eee.getChild("found-with-graph-only").getText());
		ugms.nb_lesss = Integer.parseInt(eee.getChild("found-with-testing-only").getText());

		eee = ee.getChild("statistics");
		Element eeee = eee.getChild("nulls");
		ugms.precision = Double.parseDouble(eeee.getChild("precision").getText());
		ugms.recall = Double.parseDouble(eeee.getChild("recall").getText());
		ugms.fscore = Double.parseDouble(eeee.getChild("f-score").getText());

		eeee = eee.getChild("without-nulls");
		ugms.precision_notnull = Double.parseDouble(eeee.getChild("precision").getText());
		ugms.recall_notnull = Double.parseDouble(eeee.getChild("recall").getText());
		ugms.fscore_notnull = Double.parseDouble(eeee.getChild("f-score").getText());


		// RAW DATA

		/*ee = e.getChild("raw-data");

		Set<String> tmp;

		tmp = extractData(ee.getChild("graph-nodes"));
		ugms.data_basin = tmp;

		tmp = extractData(ee.getChild("graph-test-nodes"));
		ugms.data_basin_testnodes = tmp;

		tmp = extractData(ee.getChild("execution-determined"));
		ugms.data_relevant = tmp;

		tmp = extractData(ee.getChild("graph-determined"));
		ugms.data_retrieved = tmp;

		tmp = extractData(ee.getChild("intersection-exection-graph"));
		ugms.data_inter = tmp;

		tmp = extractData(ee.getChild("intersection-graph-only"));
		ugms.data_graphOnly = tmp;

		tmp = extractData(ee.getChild("intersection-execution-only"));
		ugms.data_mutationOnly = tmp;*/

		return ugms;
	}

	private static Set<String> extractData(Element e){
		Set<String> ret = new HashSet<String>();

		for(Element ee : e.getChildren()){
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
