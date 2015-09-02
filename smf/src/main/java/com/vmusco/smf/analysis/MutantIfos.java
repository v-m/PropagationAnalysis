package com.vmusco.smf.analysis;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.utils.MutationsSetTools;

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
	 * The hash of the source generated.
	 * Obtained via {@link Mutation#getHashForMutationSource(java.io.File)}
	 */
	private String hash = null;
	
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