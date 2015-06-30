package com.vmusco.smf.mutation;

import java.util.ArrayList;
import java.util.HashMap;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

public abstract class MutationOperator<T extends CtElement> extends AbstractProcessor<T>{
	
	public enum TYPES {
		ARITHMETIC, 					// ArithmeticMutatorOperator
		CONSTANTS, 						// ConstantMutationOperator
		INCDEC, PREPOST, PREPOSTINCDEC,	// InDecrementMutationOperator
		LOGIC, 							// LogicalBinaryOperator
		NEGATION, 						// NegationUnaryOperatorConditionOperator.java
		POSNEG,							// NegateConstantsOperator.java
		NONVOIDMETHODCALLS, 			// NonVoidsMethodInvocationOperator
		RELATIONAL,						// RelationalBinaryOperator
		RETURN, 						// ReturnValueOperator.java
		VOIDMETHODCALLS 				// VoidsMethodInvocationOperator
	};
	
	public MutationOperator() {
		MutationGateway.candidates = new ArrayList();
	}
	
	public void addElement(CtElement element){
		MutationGateway.addElement(element);
	}
	
	public CtElement[] emptySet(){
		return new CtElement[]{};
	}
	
	public HashMap<CtElement, CtElement> emptyHashMap(){
		return new HashMap<>();
	}

	public abstract CtElement[] getMutatedEntries(T element, Factory factory);
	
	public HashMap<CtElement, TargetObtainer> getMutatedEntriesWithTarget(T element, Factory factory){
		HashMap<CtElement, TargetObtainer> ret = new HashMap<CtElement, TargetObtainer>();
		
		for(CtElement mutatedEntry : getMutatedEntries(element, factory)){
			ret.put(mutatedEntry, MutationOperator.createSelfTarget());
		}
		
		return ret;
	}
	
	public static TargetObtainer createSelfTarget() {
		return new TargetObtainer() {
			
			@Override
			public CtElement DetermineTarget(CtElement e) {
				return e;
			}
		};
	}
	
	public static TargetObtainer createParentTarget(int nb) {
		return new TargetObtainer() {
			
			@Override
			public CtElement DetermineTarget(CtElement e) {
				CtElement cur = e;
				
				for(int i=0; i<nb; i++){
					cur = cur.getParent();
				}
				
				return e;
			}
		};
	}

	public String shortDescription(){
		return "no description set";
	}
	
	/**
	 * This method simply return a code for identifying the mutator
	 * @return
	 */
	public abstract String operatorId();
}
