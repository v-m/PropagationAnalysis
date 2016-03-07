package com.vmusco.smf.mutation;

import java.util.ArrayList;

import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.utils.SpoonHelpers;

/**
 * Class used to communicate with the spoon processor
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MutationGateway {
	protected static ArrayList<CtElement> candidates;
	
	public static CtElement[] getMutationCandidates(){
		return candidates.toArray(new CtElement[0]);
	}
	
	public static void addElement(CtElement element){
		candidates.add(element);
	}
	
	public static void addElement(MutationStatistics ms, CtElement element){
		if(ms.getMethodSignaturesToMutate() == null){
			addElement(element);
		}else{
			CtTypeMember e = element.getParent(CtMethod.class);
			
			if(e==null){
				e = element.getParent(CtConstructor.class);
			}
			
			if(e != null){
				String signature = SpoonHelpers.resolveName(e);
				if(ms.isMethodCandidateToMutation(signature)){
					addElement(element);
				}
			}
		}
	}
}
