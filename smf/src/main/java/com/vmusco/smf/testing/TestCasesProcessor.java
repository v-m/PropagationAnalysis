package com.vmusco.smf.testing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;

// Based on logic of TestFilter (sacha-infrastructure project)
public class TestCasesProcessor extends AbstractProcessor<CtClass<?>> {
	private static ArrayList<CtClass<?>> annot_classes;
	private static ArrayList<CtClass<?>> tc_classes;
	//private static ArrayList<CtClass> rw_classes;

	//private static ArrayList<String> output;

	public TestCasesProcessor() {
		TestCasesProcessor.annot_classes = new ArrayList<CtClass<?>>();
		TestCasesProcessor.tc_classes = new ArrayList<CtClass<?>>();
		//TestCasesFinderProcessor.rw_classes = new ArrayList<CtClass>();

		//TestCasesProcessor.output = new ArrayList<String>();
	}

	@Override
	public void process(CtClass<?> element) {
		if(element.getModifiers().contains(ModifierKind.ABSTRACT))
			return;

		// TEST_CLASSES
		Iterator<CtMethod<?>> iterator = element.getMethods().iterator();

		while(iterator.hasNext()){
			Object o = iterator.next();

			if(o instanceof CtMethod){
				CtMethod<?> method = (CtMethod<?>) o;

				if(method.getAnnotation(Test.class) != null){
					// Ok there is tests :)
					annot_classes.add(element);
					return;
				}
			}
		}

		// JUNIT38 -- lookup reccurssively
		CtClass<?> explore = element;
		while(explore.getSuperclass() != null 
				&& !explore.getSuperclass().getQualifiedName().equals(TestCase.class.getCanonicalName())
				&& explore.getSuperclass().getDeclaration() instanceof CtClass){
			explore = (CtClass<?>)explore.getSuperclass().getDeclaration();
		}
		
		
		if(explore.getSuperclass() != null && 
				explore.getSuperclass().getQualifiedName().equals(TestCase.class.getCanonicalName())){
			// Ok this is a testing class :)
			tc_classes.add(element);
			return;
		}

		// RUN_WITH_CLASSES
		/*if(element.getAnnotation(RunWith.class) != null){
			rw_classes.add(element);
			return;
		}*/
	}

	public static String[] getTestClassesString(){
		int i=0;
		String[] ret = new String[TestCasesProcessor.annot_classes.size() +
		                          //TestCasesFinderProcessor.rw_classes.size() +
		                          TestCasesProcessor.tc_classes.size()];

		for(CtClass<?> aClass : TestCasesProcessor.annot_classes){
			ret[i++] = aClass.getQualifiedName();
		}

		for(CtClass<?> aClass : TestCasesProcessor.tc_classes){
			ret[i++] = aClass.getQualifiedName();
		}

		/*for(CtClass aClass : TestCasesFinderProcessor.rw_classes){
			ret[i++] = aClass.getQualifiedName();
		}*/

		return ret;
	}
	
	public static CtClass<?>[] getTestClasses(){
		int i=0;
		CtClass<?>[] ret = new CtClass[TestCasesProcessor.annot_classes.size() +
		                          //TestCasesFinderProcessor.rw_classes.size() +
		                          TestCasesProcessor.tc_classes.size()];

		for(CtClass<?> aClass : TestCasesProcessor.annot_classes){
			ret[i++] = aClass;
		}

		for(CtClass<?> aClass : TestCasesProcessor.tc_classes){
			ret[i++] = aClass;
		}

		/*for(CtClass aClass : TestCasesFinderProcessor.rw_classes){
			ret[i++] = aClass.getQualifiedName();
		}*/

		return ret;
	}

	public static String[] getTestCasesString(){
		ArrayList<String> ret = new ArrayList<String>();

		// Annotation usage
		for(CtClass<?> aClass : TestCasesProcessor.annot_classes){
			Iterator<CtMethod<?>> iterator = aClass.getMethods().iterator();

			while(iterator.hasNext()){
				Object o = iterator.next();

				if(o instanceof CtMethod){
					CtMethod<?> method = (CtMethod<?>) o;

					if(method.getAnnotation(Test.class) != null){
						ret.add("<A>"+getFullMethodSignature(method));
					}
				}
			}
		}

		// TestCase abstract class usage
		for(CtClass<?> aClass : TestCasesProcessor.tc_classes){
			Iterator<?> iterator = aClass.getMethods().iterator();

			while(iterator.hasNext()){
				Object o = iterator.next();

				if(o instanceof CtMethod){
					CtMethod<?> method = (CtMethod<?>) o;

					if(method.getSimpleName().startsWith("test")){
						ret.add("<I>"+getFullMethodSignature(method));
					}
				}
			}
		}

		/*for(CtClass aClass : TestCasesFinderProcessor.rw_classes){

		}*/

		return ret.toArray(new String[0]);
	}
	
	public static CtMethod<?>[] getTestCases(){
		List<CtMethod<?>> ret = new ArrayList<CtMethod<?>>();
		
		// Annotation usage
		for(CtClass<?> aClass : TestCasesProcessor.annot_classes){
			Iterator<?> iterator = aClass.getMethods().iterator();

			while(iterator.hasNext()){
				Object o = iterator.next();

				if(o instanceof CtMethod){
					CtMethod<?> method = (CtMethod<?>) o;

					if(method.getAnnotation(Test.class) != null){
						ret.add(method);
					}
				}
			}
		}

		// TestCase abstract class usage
		for(CtClass<?> aClass : TestCasesProcessor.tc_classes){
			Iterator<CtMethod<?>> iterator = aClass.getMethods().iterator();

			while(iterator.hasNext()){
				Object o = iterator.next();

				if(o instanceof CtMethod){
					CtMethod<?> method = (CtMethod<?>) o;

					if(method.getSimpleName().startsWith("test")){
						ret.add(method);
					}
				}
			}
		}

		/*for(CtClass aClass : TestCasesFinderProcessor.rw_classes){

		}*/

		return ret.toArray(new CtMethod[0]);
	}

	public static int getNbFromAnnotations(){
		return annot_classes.size();
	}

	public static int getNbFromTestCases(){
		return tc_classes.size();
	}
	
	private static String getFullMethodSignature(CtMethod<?> method){
		int pos = method.getSignature().indexOf("(");
		String st = method.getSignature().substring(0, pos);
		pos = st.lastIndexOf(' ');
		return method.getDeclaringType().getQualifiedName()+"."+method.getSignature().substring(pos+1);
	}
}
