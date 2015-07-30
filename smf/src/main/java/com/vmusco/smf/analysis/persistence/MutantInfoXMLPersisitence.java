package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.MutantIfos;

/**
 * This class is responsible of persisting mutation result !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutantIfos
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
		mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(mi.getRunTestOnMutantTime())));

		Element e = new Element(ProcessXMLPersistence.FAILING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TS_4, mi.getMutantErrorOnTestSuite());
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.getMutantFailingTestCases());

		e = new Element(ProcessXMLPersistence.IGNORED_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.getMutantIgnoredTestCases());

		e = new Element(ProcessXMLPersistence.HANGING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mi.getMutantHangingTestCases());
		
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
		updateMe.setRunTestOnMutantTime(Long.valueOf(root.getAttributeValue(ProcessXMLPersistence.TIME_ATTRIBUTE)));
		updateMe.setExecutedTests(true);
		
		Element tmp;

		if((tmp = root.getChild(ProcessXMLPersistence.FAILING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.setMutantFailingTestCases(al.toArray(new String[0]));
			
			tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TS_4);
			al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.setMutantErrorOnTestSuite(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.IGNORED_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.setMutantIgnoredTestCases(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.HANGING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			updateMe.setMutantHangingTestCases(al.toArray(new String[0]));
		}
		
		updateMe.setExecutedTests(true);
	}

}
