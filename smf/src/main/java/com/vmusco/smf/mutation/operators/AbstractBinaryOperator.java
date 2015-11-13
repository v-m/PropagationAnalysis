package com.vmusco.smf.mutation.operators;

import java.util.ArrayList;

import com.vmusco.smf.mutation.ManualProcessingStep;
import com.vmusco.smf.mutation.SmfMutationOperator;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class AbstractBinaryOperator extends SmfMutationOperator<CtBinaryOperator<?>> {
	
	public abstract BinaryOperatorKind[] operatorsToUse();
	public abstract ManualProcessingStep[] manualProcessThose();
	
	@Override
	public void process(CtBinaryOperator<?> element) {
		boolean found = false;
		for(BinaryOperatorKind kind : operatorsToUse()){
			if(element.getKind().equals(kind)){
				found = true;
				break;
			}
		}
		
		if(found){
			addElement(element);
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtBinaryOperator<?> element, Factory factory) {
		ArrayList<CtElement> ret = new ArrayList<CtElement>();
		
		if(element == null)
			return emptySet();
		
		for (BinaryOperatorKind kind : operatorsToUse()) {
			if(!element.getKind().equals(kind)){
				CtExpression<?> right_c = factory.Core().clone(element.getRightHandOperand());
				CtExpression<?> left_c = factory.Core().clone(element.getLeftHandOperand());

				CtBinaryOperator<?> binaryOp = factory.Code().createBinaryOperator(left_c, right_c, kind);
				
				binaryOp.setParent(element.getParent());
				right_c.setParent(binaryOp);
				left_c.setParent(binaryOp);

				ret.add(binaryOp);
			}
		}
		
		for(ManualProcessingStep mps : manualProcessThose()){
			CtElement mutated = mps.mutate(element, factory);
			mutated.setParent(element.getParent());
			
			if(mutated != null){
				ret.add(mutated);
			}
		}
		
		return ret.toArray(new CtElement[0]);
	}
}
