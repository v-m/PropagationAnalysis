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
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.TestsExecutionIfos;
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

	private static final String STS = "stacktraces";
	private static final String ST = "stacktrace";
	private static final String T = "trace";


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
		TestsExecutionIfos mei;
		try {
			mei = mi.getExecutedTestsResults();
		} catch (MutationNotRunException e2) {
			throw new PersistenceException(e2);
		}


		Element mutations = new Element(ROOT);
		Document document = new Document(mutations);

		mutations.setAttribute(new Attribute(ID, this.mutantId));
		mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(mei.getRunTestsTime())));

		Element e = new Element(ProcessXMLPersistence.FAILING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TS_4, mei.getRawErrorOnTestSuite());
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawFailingTestCases());

		e = new Element(ProcessXMLPersistence.IGNORED_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawIgnoredTestCases());

		e = new Element(ProcessXMLPersistence.HANGING_TC_3);
		mutations.addContent(e);
		ProcessXMLPersistence.populateXml(e, ProcessXMLPersistence.ONE_TC_4, mei.getRawHangingTestCases());

		if(mei.getStacktraces() != null){
			e = new Element(STS);

			for(String[] st : mei.getStacktraces()){
				Element ee = new Element(ST);

				for(String t : st){
					Element eee = new Element(T);
					eee.setText(t);
					ee.addContent(eee);
				}
				
				e.addContent(ee);
			}

			mutations.addContent(e);
		}

		Format format = Format.getPrettyFormat();
		format.setLineSeparator(LineSeparator.UNIX);
		XMLOutputter output = new XMLOutputter(format);

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

		TestsExecutionIfos mei = new TestsExecutionIfos();

		Element root = document.getRootElement();
		mei.setRunTestsTime(Long.valueOf(root.getAttributeValue(ProcessXMLPersistence.TIME_ATTRIBUTE)));

		Element tmp;

		if((tmp = root.getChild(ProcessXMLPersistence.FAILING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setFailingTestCases(al.toArray(new String[0]));

			tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TS_4);
			al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setErrorOnTestSuite(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.IGNORED_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setIgnoredTestCases(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(ProcessXMLPersistence.HANGING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ProcessXMLPersistence.ONE_TC_4);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			mei.setHangingTestCases(al.toArray(new String[0]));
		}
		
		if((tmp = root.getChild(STS)) != null){
			List<String[]> st = new ArrayList<String[]>();
			
			List<Element> tmplist = tmp.getChildren(ST);
			for(Element e: tmplist){
				List<String> stt = new ArrayList<String>();
				List<Element> tmplist2 = e.getChildren(T);
				
				for(Element ee: tmplist2){
					stt.add(ee.getText());
				}
				
				st.add(stt.toArray(new String[0]));
			}
			
			mei.setStacktraces(st.toArray(new String[0][0]));
		}

		updateMe.setExecutedTestsResults(mei);
	}

}
