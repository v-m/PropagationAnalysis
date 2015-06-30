package com.vmusco.smf.mutation.operators.pitest;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.code.BinaryOperatorKind;

public class LogicalBinaryOperator extends AbstractBinaryOperator{

	@Override
	public BinaryOperatorKind[] operatorsToUse() {
		return new BinaryOperatorKind[]{
				BinaryOperatorKind.AND, BinaryOperatorKind.OR
		};
	}
	
	@Override
	public ManualProcessingStep[] manualProcessThose() {
		return new ManualProcessingStep[]{};
	}
	
	@Override
	public String operatorId() {
		return "LogicalBinaryOperator";
	}
}
