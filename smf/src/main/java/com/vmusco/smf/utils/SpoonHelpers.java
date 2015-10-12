package com.vmusco.smf.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vmusco.smf.mutation.MutationGateway;

import spoon.compiler.SpoonCompiler;
import spoon.processing.Processor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.Filter;
import spoon.reflect.visitor.Query;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

/**
 * Tools class containing helpers for interacting with Spoon
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public final class SpoonHelpers {

	private SpoonHelpers() {
	}

	/**
	 * Get the source code for a CtClass element
	 * @param anElement
	 * @return as a string the source ready for compilation
	 */
	public static String generateAssociatedClassContent(CtClass<?> anElement){
		DefaultJavaPrettyPrinter prettyPrinter = new DefaultJavaPrettyPrinter(new StandardEnvironment());
		prettyPrinter.scan(anElement);
		String sourceCode = ("package "+anElement.getPackage().getQualifiedName()+"; "+prettyPrinter.toString());
		return sourceCode;
	}

	/**
	 * Get a Spoon factory to work with
	 * @return
	 */
	public static Factory obtainFactory(){
		StandardEnvironment standardEnvironment = new StandardEnvironment();
		standardEnvironment.setAutoImports(true);

		return new FactoryImpl(new DefaultCoreFactory(), standardEnvironment);
	}

	/**
	 * Get the CtClass object linked to a Class object
	 * @param object
	 * @return
	 */
	public static CtClass getClassElement(String[] sourcefolder, String cp, final String classFullName){
		Factory factory = SpoonHelpers.obtainFactory();
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		for(String srcitem : sourcefolder){
			compiler.addInputSource(new File(srcitem));
		}

		//Updating classpath
		if(cp != null)
			compiler.setSourceClasspath(cp);

		// Build (in memory)
		compiler.build();

		List<CtClass<?>> elements = Query.getElements(factory, new Filter<CtClass<?>>() {
			@Override
			public boolean matches(CtClass arg0) {
				return arg0.getQualifiedName().equals(classFullName);
			}
		});
		
		if(elements.size() == 1)
			return elements.get(0);
		else 
			return null;
	}
}
