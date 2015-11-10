package com.vmusco.softwearn.learn.graph;

import java.util.Set;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;

import com.vmusco.softminer.sourceanalyzer.processors.AbstractFeaturesProcessor;

/**
 * A processor layer to plug on top of another in order to do learning. 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class LearningFeaturesProcessor extends AbstractFeaturesProcessor {
	final AbstractFeaturesProcessor afp;
	
	public LearningFeaturesProcessor(AbstractFeaturesProcessor afp) {
		this.afp = afp;
	}
	
	
	@Override
	public void newReadFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess) {
		
		afp.newReadFieldAccess(src, anAccess);
	}

	@Override
	public void newWriteFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess) {
		
		afp.newWriteFieldAccess(src, anAccess);
	}

	@Override
	public void newReflexionUsage(CtExecutable<?> src) {
		
		afp.newReflexionUsage(src);
	}

	@Override
	public void newMethodCall(CtExecutable<?> src, CtExecutable<?> aReferenceExecutable) {
		CtMethod<?> e = src.getParent(CtMethod.class);
		
		if(e != null){
			Set<ModifierKind> modifiers = e.getModifiers();
			
			for(ModifierKind mk : modifiers){
				if(mk.equals(ModifierKind.ABSTRACT)){
					
				}else if(mk.equals(ModifierKind.FINAL)){
					
				}else if(mk.equals(ModifierKind.PRIVATE)){
					
				}else if(mk.equals(ModifierKind.PROTECTED)){
					
				}else if(mk.equals(ModifierKind.PUBLIC)){
					
				//}else if(mk.equals(ModifierKind.)){
					
				}
			}
		}
		
		afp.newMethodCall(src, aReferenceExecutable);
	}

	@Override
	public void newIfceImplementation(CtExecutable<?> src, CtMethod<?> exo) {
		
		afp.newIfceImplementation(src, exo);
	}

	@Override
	public void newAbstractImplementation(CtExecutable<?> src, CtMethod<?> exo) {

		afp.newAbstractImplementation(src, exo);
	}

	@Override
	public void newDeclarationMethodCall(CtField<?> src, CtExecutable<?> declaration) {
		
		afp.newDeclarationMethodCall(src, declaration);
	}


	@Override
	public void methodVisited(CtExecutable<?> execElement) {
		// Here we can extract more informations about the method
		
		afp.methodVisited(execElement);
	}

}
