package com.vmusco.smf.mutation;

import java.util.ArrayList;

import spoon.reflect.declaration.CtElement;

public abstract class MutationGateway {
	protected static ArrayList<CtElement> candidates;
	
	public static CtElement[] getMutationCandidates(){
		return candidates.toArray(new CtElement[0]);
	}
	
	public static void addElement(CtElement element){
		candidates.add(element);
	}
}
