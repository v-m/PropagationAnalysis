package com.vmusco.smf.analysis;

import java.io.File;

import spoon.reflect.cu.SourcePosition;
import spoon.support.reflect.declaration.CtElementImpl;

import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.exceptions.MalformedSourcePositionException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.utils.SourceReference;

/**
 * This class bundles all informations relatives to one mutation
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutantInfoXMLPersisitence
 */
public class MutantIfos{
	private String id;
	
	/**
	 * Describes the method in which the mutation occured (mutant id => method)
	 */
	private String mutationIn;
	/**
	 * Describes the mutation point (before) (mutant id => point)
	 */
	private String mutationFrom;
	/**
	 * Describes the mutation point (after) (mutant id => point)
	 */
	private String mutationTo;
	/**
	 * Describes the mutation viability (mutant id => true (compiles), false (fail))
	 */
	private boolean viable;
	
	/**
	 * This file describes where generated files are located. This information is NOT persisted in the XML file,
	 * thus it should not be accurate (only used for generation phase).
	 */
	private File generationDirectory;
	
	/**
	 * This file describes where generated files are located. This information is NOT persisted in the XML file,
	 * thus it should not be accurate (only used for generation phase).
	 */
	public File getGenerationDirectory() {
		return generationDirectory;
	}

	/**
	 * This file describes where generated files are located. This information is NOT persisted in the XML file,
	 * thus it should not be accurate (only used for generation phase).
	 */
	public void setGenerationDirectory(File generationDirectory) {
		this.generationDirectory = generationDirectory;
	}
	
	/*
	 * Informations under identifies the mutant
	 */
	/**
	 * The hash of the source generated.
	 * Obtained via {@link Mutation#getHashForMutationSource(java.io.File)}
	 */
	private String hash = null;
	private SourceReference sourceRef = null;
	private TestsExecutionIfos execution = null;
	
	public void setSourceReference(SourcePosition sp) throws MalformedSourcePositionException {
		this.sourceRef = new SourceReference(sp);
	}
	
	public void setSourceReference(SourceReference sp) {
		this.sourceRef = sp;
	}
	
	public SourceReference getSourceReference() {
		return sourceRef;
	}
	
	public String getMutationIn() {
		return mutationIn;
	}
	
	public void setMutationIn(String mutationIn) {
		this.mutationIn = mutationIn;
	}
	
	public String getMutationFrom() {
		return mutationFrom;
	}
	
	public String getMutationTo() {
		return mutationTo;
	}
	
	public void setMutationFrom(String mutationFrom) {
		this.mutationFrom = mutationFrom;
	}
	
	public void setMutationTo(String mutationTo) {
		this.mutationTo = mutationTo;
	}
	
	public boolean isViable() {
		return viable;
	}
	
	public void setViable(boolean viable) {
		this.viable = viable;
	}
	
	public void setHash(String hash) {
		this.hash = hash;
	}
	
	public String getHash() {
		return hash;
	}
	
	public void setExecutedTestsResults(TestsExecutionIfos exec) {
		this.execution = exec;
	}
	
	public TestsExecutionIfos getExecutedTestsResults() throws MutationNotRunException {
		if(execution == null) throw new MutationNotRunException();
		return execution;
	}
	
	/**
	 * @return true if the mutation statistics has been loaded or computed
	 */
	public boolean isExecutionKnown(){
		return this.execution != null;
	}
	
	public void loadExecution(MutationStatistics<?> ms) throws MutationNotRunException, PersistenceException{
		ms.loadMutationStats(id);
	}

	public void setId(String mutationId) {
		this.id = mutationId;
	}
	
	public String getId() {
		return id;
	}
}