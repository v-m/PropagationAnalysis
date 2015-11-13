package com.vmusco.smf.mutation.operators.KingOffutt91;

import java.util.ArrayList;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import com.vmusco.smf.mutation.SmfMutationOperator;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ArithmeticMutatorOperator extends SmfMutationOperator<CtBinaryOperator<?>>{

	private BinaryOperatorKind[] operators = new BinaryOperatorKind[]{
			BinaryOperatorKind.DIV, 
			BinaryOperatorKind.MUL,
			BinaryOperatorKind.MINUS,
			BinaryOperatorKind.PLUS,
			BinaryOperatorKind.MOD
	};
	
	@Override
	public void process(CtBinaryOperator<?> element) {
		for(BinaryOperatorKind kind : operators){
			if(element.getKind() == kind){
				addElement(element);
				break;
			}
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtBinaryOperator<?> element, Factory factory) {
		ArrayList<CtElement> ret = new ArrayList<CtElement>();

		if(element == null)
			return emptySet();
		
		for(BinaryOperatorKind kind : operators){
			if(element.getKind() != kind){
				CtExpression<?> right_c = factory.Core().clone(element.getRightHandOperand());
				CtExpression<?> left_c = factory.Core().clone(element.getLeftHandOperand());
				CtBinaryOperator<?> binaryOp = factory.Code().createBinaryOperator(left_c, right_c, kind);
				
				binaryOp.setParent(element.getParent());
				// Set parent
				right_c.setParent(binaryOp);
				left_c.setParent(binaryOp);
				
				ret.add(binaryOp);
			}
		}
		
		CtExpression<?> exp = factory.Core().clone(element.getLeftHandOperand());
		exp.setParent(element.getParent());
		ret.add(exp);
		
		exp = factory.Core().clone(element.getRightHandOperand());
		exp.setParent(element.getParent());
		ret.add(exp);
		
		return ret.toArray(new CtElement[0]);
	}
	
	@Override
	public String operatorId() {
		return "AOR";		// From [King and Offutt, 1991]
	}

	@Override
	public String shortDescription() {
		return "Replace all +, -, *, /, %, leftop, rightop";
	}
}
