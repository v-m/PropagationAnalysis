package com.vmusco.smf.basics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.junit.Assert;
import org.junit.Test;

public class CompilerTests {

	@Test
	public void testSimpleCompilation() throws URISyntaxException{
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		String base = "/home/vince/Experiments/datasets/commons-io/";
		String file = "src/main/java/org/apache/commons/io/FileUtils.java";
		String cp = "/home/vince/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/vince/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/vince/Experiments/datasets/commons-io/target/commons-io-2.5-SNAPSHOT.jar";
		
		URI fileuri = new URI("file:///"+base+file);

		// Class path handling
		List<String> options = new ArrayList<String>();
		options.add("-cp");
		
		for(String s : getLibraryAccess()){
			cp += File.pathSeparator + s;
		}
		
		System.out.println(cp);
		options.add(cp);
		
		File f = new File(base+file);
		JavaFileObject jfo = new JavaSourceFile(f);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(jfo);
		CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, compilationUnits);

		boolean success = task.call();

		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
			System.out.println(diagnostic.getCode());
			System.out.println(diagnostic.getKind());
			System.out.println(diagnostic.getPosition());
			System.out.println(diagnostic.getStartPosition());
			System.out.println(diagnostic.getEndPosition());
			System.out.println(diagnostic.getSource());
			System.out.println(diagnostic.getMessage(null));
			System.out.println("=====");
		}

		Assert.assertTrue(success);
	}
	
	private Set<String> getLibraryAccess() {
		
		// strategy 1
		String bootpath = System.getProperty("sun.boot.class.path");
		Set<String> lst = new HashSet<String>();
		for (String s : bootpath.split(File.pathSeparator)) {
			File f = new File(s);
			if (f.exists()) {
				lst.add(f.getAbsolutePath());
			}
		}
		
		
		// strategy 2
		ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();//ClassLoader.getSystemClassLoader();		
		if(currentClassLoader instanceof URLClassLoader){
			URL[] urls = ((URLClassLoader) currentClassLoader).getURLs();
			if(urls!=null && urls.length>0){
				
				for (URL url : urls) {
				//	classpath+=File.pathSeparator+url.getFile();
					lst.add(url.getFile());
				}
			
			}
		}
		
		// strategy 3
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

class JavaSourceFile extends SimpleJavaFileObject {
	JavaSourceFile(File f) {
		super(URI.create("file://" + f.getAbsolutePath()) ,Kind.SOURCE);
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		File f = new File(this.uri.getPath());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		FileInputStream fis = new FileInputStream(f);
		int t;

		while((t = fis.read()) != -1){
			baos.write(t);
		}

		return baos.toString();
	}
}