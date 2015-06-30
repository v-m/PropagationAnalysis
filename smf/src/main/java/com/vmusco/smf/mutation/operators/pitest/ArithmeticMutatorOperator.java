package com.vmusco.smf.mutation.operators.pitest;

import java.util.ArrayList;

import com.vmusco.smf.mutation.MutationGateway;
import com.vmusco.smf.mutation.MutationOperator;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public class ArithmeticMutatorOperator extends MutationOperator<CtBinaryOperator>{

	private BinaryOperatorKind[] operators = new BinaryOperatorKind[]{
			BinaryOperatorKind.DIV, 
			BinaryOperatorKind.MUL,
			BinaryOperatorKind.MINUS,
			BinaryOperatorKind.PLUS,
			BinaryOperatorKind.MOD
	};
	
	@Override
	public void process(CtBinaryOperator element) {
		for(BinaryOperatorKind kind : operators){
			if(element.getKind() == kind){
				addElement(element);
				break;
			}
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtBinaryOperator element, Factory factory) {
		ArrayList<CtElement> ret = new ArrayList<CtElement>();

		if(element == null)
			return emptySet();
		
		for(BinaryOperatorKind kind : operators){
			if(element.getKind() != kind){
				CtExpression right_c = factory.Core().clone(element.getRightHandOperand());
				CtExpression left_c = factory.Core().clone(element.getLeftHandOperand());
				CtBinaryOperator binaryOp = factory.Code().createBinaryOperator(left_c, right_c, kind);
				// Set parent
				right_c.setParent(binaryOp);
				left_c.setParent(binaryOp);
				ret.add(binaryOp);
			}
		}
		
		return ret.toArray(new CtElement[1]);
	}
	
	@Override
	public String operatorId() {
		return "AOR";		// From [King and Offutt, 1991]
	}

}
