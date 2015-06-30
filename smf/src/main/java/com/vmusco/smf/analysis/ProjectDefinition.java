package com.vmusco.smf.analysis;

public abstract class ProjectDefinition {
	/**
	 * This method defines the changes imposed by the project class (define paths, ...)
	 */
	public abstract void projectConfiguration(ProcessStatistics ps);
	
	public void mutationConfiguration(ProcessStatistics ps, MutationStatistics ms){
		ms.classToMutate = new String[]{};
	}
}
