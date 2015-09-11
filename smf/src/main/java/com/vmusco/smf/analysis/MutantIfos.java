package com.vmusco.smf.analysis;

import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;

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
	
	
	/*
	 * Informations under identifies the mutant
	 */
	/**
	 * The hash of the source generated.
	 * Obtained via {@link Mutation#getHashForMutationSource(java.io.File)}
	 */
	private String hash = null;
	private int startcolumn = -1;
	private int endcolumn = -1;
	private int startline = -1;
	private int endline = -1;
	private int startsource = -1;
	private int endsource = -1;
	private String file = null;

	public void setStartColumn(int startcolumn) {
		this.startcolumn = startcolumn;
	}
	
	public int getStartColumn() {
		return startcolumn;
	}
	
	public void setEndColumn(int endcolumn) {
		this.endcolumn = endcolumn;
	}
	
	public int getEndColumn() {
		return endcolumn;
	}
	
	public void setStartLine(int startline) {
		this.startline = startline;
	}
	
	public int getStartLine() {
		return startline;
	}
	
	public void setEndLine(int endline) {
		this.endline = endline;
	}
	
	public int getEndLine() {
		return endline;
	}
	
	public void setStartSource(int startsource) {
		this.startsource = startsource;
	}
	
	public int getStartSource() {
		return startsource;
	}
	
	public void setEndSource(int endsource) {
		this.endsource = endsource;
	}
	
	public int getEndSource() {
		return endsource;
	}
	
	public void setSourceFile(String file){
		this.file = file;
	}
	
	public String getSourceFile() {
		return file;
	}
	
	
	
	
	
	
	
	
	private MutantExecutionIfos execution = null;
	
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
	
	public void setExecutedTestsResults(MutantExecutionIfos exec) {
		this.execution = exec;
	}
	
	public MutantExecutionIfos getExecutedTestsResults() throws MutationNotRunException {
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