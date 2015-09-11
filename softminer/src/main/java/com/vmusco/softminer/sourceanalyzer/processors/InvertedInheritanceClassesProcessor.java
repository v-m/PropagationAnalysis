package com.vmusco.softminer.sourceanalyzer.processors;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class InvertedInheritanceClassesProcessor extends ClassesProcessor {
	@Override
	public String[] processEntry(CtType<?> element, CtTypeReference<?> aReference) {
		String[] ret = super.processEntry(element, aReference);
		if(ret == null){
			return null;
		}

		if(element instanceof CtClass){
			CtTypeReference<?> superclass = ((CtClass<?>)element).getSuperclass();
			
			if(superclass != null){
				if(superclass.getQualifiedName().equals(aReference.getQualifiedName())){
					return new String[]{ret[1], ret[0]};
				}
			}
		}
		
		return ret; 
	}
}
