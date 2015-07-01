package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.mutation.MutationOperator;

/**
 * 
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public class MutantInfoXMLPersisitence extends ExecutionPersistence<MutantIfos>{
	private static final String ROOT = "mutation-execution";
	private static final String ID = "id";
	
	private String mutantId;
	private FileOutputStream fos;

	/**
	 * Used for loading
	 */
	public MutantInfoXMLPersisitence(File persistFile) {
		super(persistFile);
	}
	
	/**
	 * Used for saving
	 * @param locked_fos
	 * @param mutantId
	 */
	public MutantInfoXMLPersisitence(FileOutputStream locked_fos, String mutantId) {
		super(null);
		this.mutantId = mutantId;
		this.fos = locked_fos;
	}

	@Override
	public MutantIfos loadState() throws Exception {
		MutantIfos ifos = new MutantIfos();
		
		loadState(ifos);
		
		return ifos;
	}

	@Override
	public void saveState(MutantIfos mi) throws IOException {
		Element mutations = new Element(ROOT);
		Document document = new Document(mutations);

		mutations.setAttribute(new Attribute(ID, this.mutantId));
		mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(mi.runTestOnMutantTime)));

		Element e = new Element(ProcessXMLPersistence.FAILING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TS_4, mi.mutantErrorOnTestSuite);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.mutantFailingTestCases);

		e = new Element(ProcessXMLPersistence.IGNORED_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.mutantIgnoredTestCases);

		e = new Element(ProcessXMLPersistence.HANGING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.mutantHangingTestCases);
		
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(document, fos);
	}

	@Override
	public void loadState(MutantIfos updateMe) throws Exception {
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			document = sxb.build(f);
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return;
		}

		Element root = document.getRootElement();
		updateMe.runTestOnMutantTime = Long.valueOf(root.getAttributeValue(ProcessXMLPersistence.TIME_ATTRIBUTE));
		updateMe.excutedTests = true;
		
		Element tmp;

		if((tmp = root.getChild(ProcessXMLPersistence.FAILING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.mutantFailingTestCases = al.toArray(new String[0]);
			
			tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TS_4);
			al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.mutantErrorOnTestSuite = al.toArray(new String[0]);
		}

		if((tmp = root.getChild(ProcessXMLPersistence.IGNORED_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.mutantIgnoredTestCases = al.toArray(new String[0]);
		}

		if((tmp = root.getChild(ProcessXMLPersistence.HANGING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.mutantHangingTestCases = al.toArray(new String[0]);
		}
	}

}
