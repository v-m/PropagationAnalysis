package com.vmusco.smf.mutation.operators.pitest;

import com.vmusco.smf.mutation.operators.AbstractMethodsCallsOperator;

import spoon.reflect.code.CtInvocation;

public class VoidsMethodInvocationOperator extends AbstractMethodsCallsOperator{

	@Override
	public void process(CtInvocation element) {
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
