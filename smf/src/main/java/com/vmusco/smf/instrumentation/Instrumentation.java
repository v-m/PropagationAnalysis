package com.vmusco.smf.instrumentation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import spoon.processing.Processor;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.support.DefaultCoreFactory;
import spoon.support.JavaOutputProcessor;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.utils.NewReportedStandardEnvironment;

/**
 * Instrument source code 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class Instrumentation {
	public static List<String> areSource = new ArrayList<>();
	
	public static void instrumentSource(String[] srcs, String[] cp, File outdir, AbstractInstrumentationProcessor[] instru){
		if(!outdir.exists())
			outdir.mkdirs();
		
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		JDTBasedSpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		
		for(String source : srcs){
			compiler.addInputSource(new File(source));
		}
		
		compiler.setSourceClasspath(cp);
		compiler.setSourceOutputDirectory(outdir);
		
		StandardEnvironment env = new NewReportedStandardEnvironment();
		JavaOutputProcessor fileOutput = new JavaOutputProcessor(outdir, new DefaultJavaPrettyPrinter(env));

		ArrayList<Processor<?>> l = new ArrayList<Processor<?>>();
		for(AbstractInstrumentationProcessor i : instru){
			l.add(i);
		}
		l.add(fileOutput);
		
		compiler.build();
		compiler.process(l);
	}
}
