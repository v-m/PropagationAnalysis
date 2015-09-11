package com.vmusco.smf.mutation.operators.misc;

import spoon.reflect.code.BinaryOperatorKind;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
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
