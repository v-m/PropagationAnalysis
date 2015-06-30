package com.vmusco.smf.mutation.operators.pitest;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public class RelationalBinaryOperator extends AbstractBinaryOperator{

	@Override
	public BinaryOperatorKind[] operatorsToUse() {
		return new BinaryOperatorKind[]{
				BinaryOperatorKind.EQ, BinaryOperatorKind.NE, BinaryOperatorKind.GE, 
				BinaryOperatorKind.GT, BinaryOperatorKind.LE, BinaryOperatorKind.LT
		};
	}

	@Override
	public ManualProcessingStep[] manualProcessThose() {
		return new ManualProcessingStep[]{};	
	};

	@Override
	public String operatorId() {
		return "ROR";	// From [King and Offutt, 1991]
	}
}
