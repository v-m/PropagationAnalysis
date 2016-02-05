package com.vmusco.smf.mutation.operators;

import com.vmusco.smf.mutation.MutationOperator;

/**
 * This generic mutation operator is used to import mutations from external projects.
 * Do *NOT* use it to mutate with smf or spoon !
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ExternalMutationOperator implements MutationOperator {
	String name = "???";
	
	public ExternalMutationOperator(String name) {
		this.name = name;
	}
	
	@Override
	public String operatorId() {
		return name;
	}

	@Override
	public String shortDescription() {
		return String.format("External mutation operator \"%s\"", name);
	}

}
