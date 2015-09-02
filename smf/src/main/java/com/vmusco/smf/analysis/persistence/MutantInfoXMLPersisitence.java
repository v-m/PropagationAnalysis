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

import com.vmusco.smf.analysis.MutantExecutionIfos;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;

/**
 * This class is responsible of persisting mutation result !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutantIfos
 */
public class MutantInfoXMLPersisitence extends ExecutionPersistence<MutantIfos>{
	private File f;
	private static final String ROOT = "mutation-execution";
	private static final String ID = "id";
	
	private String mutantId;
	private FileOutputStream fos;

	/**
	 * Used for loading
	 */
	public MutantInfoXMLPersisitence(File persistFile) {
		this.f = persistFile;
	}
	
	/**
	 * Used for saving
	 * @param locked_fos
	 * @param mutantId
	 */
	public MutantInfoXMLPersisitence(FileOutputStream locked_fos, String mutantId) {
		this.f = null;
		this.mutantId = mutantId;
		this.fos = locked_fos;
	}

	/**
	 * Do not use directly this method. Pass through the MutationStatistics object to get a coherent instance !
	 */
	@Override
	public MutantIfos loadState() throws PersistenceException {
		MutantIfos ifos = new MutantIfos();
		loadState(ifos);
		return ifos;
	}

	@Override
	public void saveState(MutantIfos mi) throws PersistenceException {
		MutantExecutionIfos mei;
		try {
			mei = mi.getExecutedTestsResults();
		} catch (MutationNotRunException e2) {
			throw new PersistenceException(e2);
		}
			
		
		Element mutations = new Element(ROOT);
		Document document = new Document(mutations);

		mutations.setAttribute(new Attribute(ID, this.mutantId));
		mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(mei.getRunTestOnMutantTime())));

		Element e = new Element(ProcessXMLPersistence.FAILING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TS_4, mei.getRawMutantErrorOnTestSuite());
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawMutantFailingTestCases());

		e = new Element(ProcessXMLPersistence.IGNORED_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawMutantIgnoredTestCases());

		e = new Element(ProcessXMLPersistence.HANGING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawMutantHangingTestCases());
		
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		try {
			output.output(document, fos);
		} catch (IOException e1) {
			throw new PersistenceException(e1);
		}
			
	}

	@Override
	public void loadState(MutantIfos updateMe) throws PersistenceException {
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			try {
				document = sxb.build(f);
			} catch (IOException e) {
				throw new PersistenceException(e);
			}
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return;
		}

		MutantExecutionIfos mei = new MutantExecutionIfos();
		
		Element root = document.getRootElement();
		mei.setRunTestOnMutantTime(Long.valueOf(root.getAttributeValue(ProcessXMLPersistence.TIME_ATTRIBUTE)));
		
		Element tmp;

		if((tmp = root.getChild(ProcessXMLPersistence.FAILING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setMutantFailingTestCases(al.toArray(new String[0]));
			
			tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TS_4);
			al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setMutantErrorOnTestSuite(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.IGNORED_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setMutantIgnoredTestCases(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.HANGING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setMutantHangingTestCases(al.toArray(new String[0]));
		}
		
		updateMe.setExecutedTestsResults(mei);
	}

}
