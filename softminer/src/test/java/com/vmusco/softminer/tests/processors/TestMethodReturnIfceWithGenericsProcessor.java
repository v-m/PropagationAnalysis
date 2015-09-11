package com.vmusco.softminer.tests.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.reference.CtTypeReference;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class TestMethodReturnIfceWithGenericsProcessor extends AbstractProcessor<CtExecutable<?>>{ 

	@Override
	public void process(CtExecutable<?> element) {
		CtType<?> elemDecType = ((CtTypeMember) element).getDeclaringType();
		
		if(elemDecType instanceof CtClass){
			CtClass<?> aClass = ((CtClass<?>)elemDecType);
			CtTypeReference<?> analyzeThis = aClass.getSuperclass();

			if(analyzeThis != null){
				System.out.println(aClass.getQualifiedName()+" => "+aClass.getFormalTypeParameters()+" "+aClass.getParent().getClass());
				
				if (aClass.getParent() instanceof CtNewClass) {
					//CtNewClass<?> c = (CtNewClass<?>)aClass.getParent();
					System.out.println(aClass.toString());
				}
				
				
				if(analyzeThis.getDeclaration() instanceof CtInterface){
					CtInterface<?> anIfce = ((CtInterface<?>)analyzeThis.getDeclaration());
					System.out.println(anIfce.getQualifiedName()+" => "+anIfce.getFormalTypeParameters());
				}
			}
		}
	}
}