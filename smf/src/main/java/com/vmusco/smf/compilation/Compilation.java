package com.vmusco.smf.compilation;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.codehaus.plexus.util.FileUtils;

import spoon.compiler.Environment;
import spoon.compiler.ModelBuildingException;
import spoon.compiler.SpoonCompiler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.LogToFile;

/**
 * Class containing all compilation methods 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class Compilation {

	public static boolean compileProjectUsingSpoon(ProcessStatistics ps) throws IOException{
		long t1 = System.currentTimeMillis();

		Environment environment = new StandardEnvironment();

		Factory factory = new FactoryImpl(new DefaultCoreFactory(), environment);
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		// Add all sources here
		for(String aSrcFile : ps.getSrcToCompile(true)){
			//compiler.addInputSource(new File(ps.getProjectIn(true) + File.separator + aSrcFile));
			compiler.addInputSource(new File(aSrcFile));
		}

		for(String cp : ps.getClasspath())
			System.out.println(cp);

		compiler.setSourceClasspath(ps.getClasspath());

		File fdest = new File(ps.srcGenerationFolder());
		if(fdest.exists())
			FileUtils.deleteDirectory(fdest);

		fdest.mkdirs();

		System.out.println("Compiling the project using spoon in "+fdest.getAbsolutePath()+".");

		// This part is used to log WARNINGS or stderr !!!
		File f = new File(ps.buildPath("spoonCompilation.log"));
		if(f.exists())
			f.delete();
		f.createNewFile();

		LogToFile ltf = new LogToFile();
		ltf.redirectTo(f);

		compiler.setDestinationDirectory(fdest);

		try{
			compiler.compile();
			return true;
		}catch(ModelBuildingException ex){
			System.err.println("Error on compilation phase !");
			return false;
		}finally{
			ltf.restablish();
			long t2 = System.currentTimeMillis();
			ps.setBuildProjectTime(t2-t1);
		}
	}

	public static boolean compileTestsDissociatedUsingSpoon(ProcessStatistics ps) throws IOException{
		long t1 = System.currentTimeMillis();

		File fdest = new File(ps.testsGenerationFolder());
		if(fdest.exists())
			FileUtils.deleteDirectory(fdest);

		fdest.mkdirs();
		File f = new File(ps.getWorkingDir() + File.separator + "spoonCompilationTests.log");
		if(f.exists())
			f.delete();
		f.createNewFile();

		// This part is used to log WARNINGS or stderr !!!
		LogToFile ltf = new LogToFile();
		ltf.redirectTo(f);

		// Add all sources here
		for(String aSrcFile : ps.getSrcTestsToTreat(false)){
			Environment environment = new StandardEnvironment();

			Factory factory = new FactoryImpl(new DefaultCoreFactory(), environment);
			SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

			compiler.addInputSource(new File(ps.getProjectIn(true) + File.separator + aSrcFile));
			compiler.setSourceClasspath(ps.getTestingClasspath());

			System.out.println("Compiling test (aSrcFile) using spoon in "+fdest.getAbsolutePath()+".");
			compiler.setDestinationDirectory(fdest);

			try{
				compiler.compile();
			}catch(ModelBuildingException ex){
				System.err.println("Error on compilation phase:");
				ex.printStackTrace();

				ltf.restablish();
				long t2 = System.currentTimeMillis();
				ps.setBuildTestsTime(t2-t1);

				return false;
			}
		}

		ltf.restablish();
		long t2 = System.currentTimeMillis();
		ps.setBuildTestsTime(t2-t1);

		return true;
	}

	public static boolean compileTestsUsingSpoon(ProcessStatistics ps) throws IOException{
		long t1 = System.currentTimeMillis();
		Environment environment = new StandardEnvironment();

		Factory factory = new FactoryImpl(new DefaultCoreFactory(), environment);
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		// Add all sources here
		for(String aSrcFile : ps.getSrcTestsToTreat(true)){
			compiler.addInputSource(new File(aSrcFile));
		}

		File fdest = new File(ps.testsGenerationFolder());
		if(fdest.exists())
			FileUtils.deleteDirectory(fdest);

		fdest.mkdirs();

		System.out.println("Compiling the project tests using spoon in "+fdest.getAbsolutePath()+".");

		// This part is used to log WARNINGS or stderr !!!
		File f = new File(ps.getWorkingDir() + File.separator + "spoonCompilationTests.log");
		if(f.exists())
			f.delete();
		f.createNewFile();

		LogToFile ltf = new LogToFile();
		ltf.redirectTo(f);

		compiler.setDestinationDirectory(fdest);

		try{
			compiler.compile();
			return true;
		}catch(ModelBuildingException ex){
			System.err.println("Error on compilation phase:");
			ex.printStackTrace();
			return false;
		}finally{
			ltf.restablish();
			long t2 = System.currentTimeMillis();
			ps.setBuildTestsTime(t2-t1);
		}
	}

	/***
	 * This method is responsible of building a Java project
	 * @param project_path
	 * @param classpath
	 * @param outputPath
	 * @throws IOException
	 */
	public static void compileProjectUsingSpoon(String project_path, String[] classpath, String outputPath) throws IOException{
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
		compiler.addInputSource(new File(project_path));
		compiler.setSourceClasspath(classpath);
		compiler.setDestinationDirectory(new File(outputPath));
		compiler.compile();
	}

	/**
	 * Compiles a class using Javax.
	 * @param aClass
	 * @param source
	 * @param classpath
	 * @return null if compilation fails. bytecodes if succeed.
	 * @throws URISyntaxException
	 */
	public static Map<String, byte[]> compilesUsingJavax(CtClass<?> aClass, String source, String[] classpath, DiagnosticCollector<JavaFileObject> diagnostics) throws URISyntaxException{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);
		VirtualFileObjectManager fileManager = new VirtualFileObjectManager(standardFileManager);

		// Class path handling
		List<String> options = new ArrayList<String>();
		options.add("-cp");
		String cp = "";

		for(String s : getLibraryAccess()){
			cp += ((cp.length()>0)?File.pathSeparator:"") + s;
		}

		if(classpath != null){
			for(String s : classpath){
				cp += ((cp.length()>0)?File.pathSeparator:"") + s;
			}
		}

		options.add(cp);

		JavaFileObject jfo = new JavaSourceFromString(aClass.getQualifiedName(), source);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(jfo);

		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
		boolean re = task.call();

		if(!re){
			return null;
		}

		Map<String, byte[]> bytecodes = new HashMap<>();

		Map<String, CompiledObjectFileObject> classFiles = fileManager.classFiles();

		for (String qualifiedName : classFiles.keySet()) {
			//String finalName = qualifiedName.split("[$]")[0];
			byte[] bytecode = classFiles.get(qualifiedName).byteCodes();
			bytecodes.put(qualifiedName, bytecode);
		}

		/*ClassFileUtil.writeToDisk(true, outputDir.getAbsolutePath(),
						fileName, compiledClass);*/

		return bytecodes;
	}

	/**
	 * Compiles a class using Javax.
	 * @param aClass
	 * @param source
	 * @param classpath
	 * @return null if compilation fails. bytecodes if succeed.
	 * @throws URISyntaxException
	 */
	public static Map<String, byte[]> compilesUsingJavax(CtClass<?> aClass, String source, String[] classpath) throws URISyntaxException{
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		return compilesUsingJavax(aClass, source, classpath, diagnostics);
	}

	private static Set<String> getLibraryAccess() {
		String bootpath = System.getProperty("sun.boot.class.path");
		Set<String> lst = new HashSet<String>();
		for (String s : bootpath.split(File.pathSeparator)) {
			File f = new File(s);
			if (f.exists()) {
				lst.add(f.getAbsolutePath());
			}
		}

		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();//ClassLoader.getSystemClassLoader();		
		if(currentClassLoader instanceof URLClassLoader){
			URL[] urls = ((URLClassLoader) currentClassLoader).getURLs();
			if(urls!=null && urls.length>0){

				for (URL url : urls) {
					lst.add(url.getFile());
				}

			}
		}

		String classpath = System.getProperty("java.class.path");
		for (String s : classpath.split(File.pathSeparator)) {
			File f = new File(s);
			if (f.exists()) {
				lst.add(f.getAbsolutePath());
			}
		}

		return lst;
	}
}

class JavaSourceFromString extends SimpleJavaFileObject {
	final String code;

	JavaSourceFromString(String name, String code) {
		super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension),Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}
