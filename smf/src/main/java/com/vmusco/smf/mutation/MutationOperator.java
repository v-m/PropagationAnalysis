package com.vmusco.smf.mutation;

import com.vmusco.smf.analysis.MutationStatistics;

public interface MutationOperator {

	public String shortDescription();
	
	/**
	 * This method simply return a code for identifying the mutator
	 * @return
	 */
	public String operatorId();

	public void setMutationStatistic(MutationStatistics mutationStatistics);
}
