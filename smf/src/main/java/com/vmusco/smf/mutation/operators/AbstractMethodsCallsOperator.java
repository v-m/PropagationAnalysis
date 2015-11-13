package com.vmusco.smf.mutation.operators;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.SmfMutationOperator;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class AbstractMethodsCallsOperator extends SmfMutationOperator<CtInvocation<?>>{
	
	@Override
	public CtElement[] getMutatedEntries(CtInvocation<?> t, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();
		
		CtTypeReference<?> type = (CtTypeReference<?>) t.getExecutable().getType();
		CtElement e;
		
		if(type.getSimpleName().equals("void")){
			// This is a void
			e = factory.Core().createBlock();
		}else{
			// This is a non void
			if(type.getSimpleName().equals("boolean")){
				e = factory.Code().createLiteral(false);
			}else if(type.getSimpleName().equals("int") ||
					type.getSimpleName().equals("byte") ||
					type.getSimpleName().equals("short") ||
					type.getSimpleName().equals("long")){
				e = factory.Code().createLiteral(0);
			}else if(type.getSimpleName().equals("float") ||
					type.getSimpleName().equals("double") ||
					type.getSimpleName().equals("short")){
				e = factory.Code().createLiteral(0.0);
			}else if(type.getSimpleName().equals("char")){
				e = factory.Code().createLiteral('\u0000');
			}else{
				e = factory.Code().createLiteral(null);
			}
		}
		
		e.setParent(t.getParent());
		result.add(e);

		return result.toArray(new CtElement[0]);
	}
}
