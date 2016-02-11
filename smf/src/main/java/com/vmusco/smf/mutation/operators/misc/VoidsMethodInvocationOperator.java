package com.vmusco.smf.mutation.operators.misc;

import com.vmusco.smf.mutation.operators.AbstractMethodsCallsOperator;

import spoon.reflect.code.CtInvocation;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class VoidsMethodInvocationOperator extends AbstractMethodsCallsOperator{

	@Override
	public void process(CtInvocation<?> element) {
		if(!isCtElementCandidateAcceptable(element))
			return;

		if(element.getExecutable().isConstructor())
			return;

		if(!element.getExecutable().getType().getSimpleName().equals("void"))
			return;
		
		addElement(element);
	}

	@Override
	public String operatorId() {
		return "VoidsMethodInvocationOperator";
	}
}
