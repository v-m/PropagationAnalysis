package com.vmusco.smf.mutation.operators.KingOffutt91;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public class LogicalConnectorReplacementOperator extends AbstractBinaryOperator{

	@Override
	public BinaryOperatorKind[] operatorsToUse() {
		return new BinaryOperatorKind[]{
				BinaryOperatorKind.AND, BinaryOperatorKind.OR
		};
	}

	@Override
	public ManualProcessingStep[] manualProcessThose() {
		return new ManualProcessingStep[]{
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtLiteral ctl = f.Core().createLiteral();
						ctl.setValue(true);
						return ctl;
					}
				},
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtLiteral ctl = f.Core().createLiteral();
						ctl.setValue(false);
						return ctl;
					}
				},
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtBinaryOperator<?> bo = (CtBinaryOperator<?>) anElement;
						CtExpression<?> exp = f.Core().clone(bo.getLeftHandOperand());
						exp.setParent(anElement.getParent());
						return exp;
					}
				},
				new ManualProcessingStep() {
					@Override
					public CtElement mutate(CtElement anElement, Factory f) {
						CtBinaryOperator<?> bo = (CtBinaryOperator<?>) anElement;
						CtExpression<?> exp = f.Core().clone(bo.getRightHandOperand());
						exp.setParent(anElement.getParent());
						return exp;
					}
				}
		};	
	};

	@Override
	public String operatorId() {
		return "LCR";	// From [King and Offutt, 1991]
	}
	
	@Override
	public String shortDescription() {
		return "Replace all &&, ||, left, right, true and false";
	}
}
