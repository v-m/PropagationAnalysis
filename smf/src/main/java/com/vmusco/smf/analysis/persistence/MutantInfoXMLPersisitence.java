package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;

/**
 * This class is responsible of persisting mutation result !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutantIfos
 */
public class MutantInfoXMLPersisitence extends XMLPersistenceManager<MutantIfos>{

	private static final String ROOT = "mutation-execution";
	private static final String ID = "id";

	/*private static final String STS = "stacktraces";
	private static final String ST = "stacktrace";
	private static final String T = "trace";*/
	
	private File f;
	private FileOutputStream fos;
	private boolean deepLoading;
	
	//private String mutantId;

	public MutantInfoXMLPersisitence(MutantIfos obj, File persist_file) {
		this(obj, persist_file, false);
	}
	
	public MutantInfoXMLPersisitence(MutantIfos obj, File persist_file, boolean deepLoading) {
		super(obj);
		this.f = persist_file;
		this.deepLoading = deepLoading;
	}

	public void setFileLock(FileOutputStream locked_fos){
		this.fos = locked_fos;
	}
	
	@Override
	public OutputStream getPersistenceOutputStream() throws PersistenceException {
		if(this.fos == null)
			throw new PersistenceException("File channel not locked !");
		
		return this.fos;
	}
	
	@Override
	public File getPersistenceFile() {
		return f;
	}

	@Override
	public void load(Element root) {
		getLinkedObject().setExecutedTestsResults(TestInformationPersistence.readFrom(root, deepLoading));
	}

	@Override
	public Element getSaveContent() throws PersistenceException {
		MutantIfos mi = getLinkedObject();
		
		TestsExecutionIfos mei;
		try {
			mei = mi.getExecutedTestsResults();
		} catch (MutationNotRunException e2) {
			throw new PersistenceException(e2);
		}
		
		Element mutations = new Element(ROOT);
		mutations.setAttribute(new Attribute(ID, mi.getId()));
		
		HashMap<String, Integer> compressor = new HashMap<>();
		
		TestInformationPersistence.insertInto(mutations, mei, compressor);

		Element compressionDict = XMLPersistence.generateCompressionEntry(compressor);
		mutations.addContent(0, compressionDict);
		
		return mutations;
	}
}
