package com.vmusco.smf.mutation.operators.KingOffutt91;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

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
		return new ManualProcessingStep[]{
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtLiteral<Boolean> ctl = f.Core().createLiteral();
						ctl.setValue(true);
						return ctl;
					}
				},
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtLiteral<Boolean> ctl = f.Core().createLiteral();
						ctl.setValue(false);
						return ctl;
					}
				}
		};	
	};

	@Override
	public String operatorId() {
		return "ROR";	// From [King and Offutt, 1991]
	}
	
	@Override
	public String shortDescription() {
		return "Replace all <, <=, >, >=, ==, !=, false, true";
	}
}
