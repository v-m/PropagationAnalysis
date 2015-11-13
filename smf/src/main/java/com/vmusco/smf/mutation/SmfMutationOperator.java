package com.vmusco.smf.mutation;

import java.util.ArrayList;
import java.util.HashMap;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * Defines a mutation operator
 * IMPORTANT NOTE: For a mutation operator being compatible with spoon, 
 * it must define a default constructor with no parameters and rely only on it during mutation process!
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class SmfMutationOperator<T extends CtElement> extends AbstractProcessor<T> implements MutationOperator{
	
	public SmfMutationOperator() {
		MutationGateway.candidates = new ArrayList<CtElement>();
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
			ret.put(mutatedEntry, SmfMutationOperator.createSelfTarget());
		}
		
		return ret;
	}
	
	public static TargetObtainer createSelfTarget() {
		return new TargetObtainer() {
			
			@Override
			public CtElement determineTarget(CtElement e) {
				return e;
			}
		};
	}
	
	public static TargetObtainer createParentTarget(final int nb) {
		return new TargetObtainer() {
			
			@Override
			public CtElement determineTarget(CtElement e) {
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
	
}
