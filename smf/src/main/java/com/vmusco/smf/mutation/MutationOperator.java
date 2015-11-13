package com.vmusco.smf.mutation;

public interface MutationOperator {

	public String shortDescription();
	
	/**
	 * This method simply return a code for identifying the mutator
	 * @return
	 */
	public abstract String operatorId();
}
