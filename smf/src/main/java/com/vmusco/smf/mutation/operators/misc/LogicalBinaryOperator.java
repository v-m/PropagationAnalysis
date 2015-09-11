package com.vmusco.smf.mutation.operators.misc;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.code.BinaryOperatorKind;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
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
