package com.vmusco.smf.compilation.compilers;

import java.io.File;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.compilation.CompiledObjectFileObject;
import com.vmusco.smf.compilation.VirtualFileObjectManager;

public class JavaxCompilation extends Compilation{
	private DiagnosticCollector<JavaFileObject> diagn;

	public JavaxCompilation(){
		this(new DiagnosticCollector<JavaFileObject>());
	}

	public JavaxCompilation(DiagnosticCollector<JavaFileObject> diagnostics) {
		this.diagn = diagnostics;
	}

	public DiagnosticCollector<JavaFileObject> getDiagnosticCollector(){
		return diagn;
	}

	@Override
	public Map<String, byte[]> buildInMemory(String qualifiedName, String source, String[] classpath, int compliance){
		long time = System.currentTimeMillis();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagn, null, null);
		VirtualFileObjectManager fileManager = new VirtualFileObjectManager(standardFileManager);

		// Class path handling
		JavaFileObject jfo = new JavaSourceFromString(qualifiedName, source);
		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(jfo);

		CompilationTask task = compiler.getTask(null, fileManager, diagn, getJavaxCompilerOptions(classpath, null, compliance), null, compilationUnits);
		boolean re = task.call();

		if(!re){
			setLastBuildSucceess(false);
			return null;
		}

		Map<String, byte[]> bytecodes = new HashMap<>();

		Map<String, CompiledObjectFileObject> classFiles = fileManager.classFiles();

		for (String qn : classFiles.keySet()) {
			byte[] bytecode = classFiles.get(qn).byteCodes();
			bytecodes.put(qn, bytecode);
		}

		setLastBuildTime(System.currentTimeMillis() - time);
		setLastBuildSucceess(true);
		return bytecodes;
	}

	@Override
	public boolean buildInDirectory(File[] sourceFiles, File outputFolder, String[] classpath, int compliance) {
		long time = System.currentTimeMillis();
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		// Retrieve files from the standard location
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagn, null, null);

		List<File> content = new ArrayList<>();
		List<File> scan = new ArrayList<>();
		scan.addAll(Arrays.asList(sourceFiles));

		while(!scan.isEmpty()){
			File next = scan.remove(0);

			if(next.isFile()){
				if(next.getName().endsWith(".java")){
					content.add(next);
				}
			}else{
				for(File f : next.listFiles())
					scan.add(f);
			}
		}

		if(content.size()==0){
			// there is no .java file in source folders
			return true;
		}

		Iterable<? extends JavaFileObject> sources = fileManager.getJavaFileObjectsFromFiles(content);

		CompilationTask task = compiler.getTask(new StringWriter(), fileManager, diagn, getJavaxCompilerOptions(classpath, outputFolder, compliance), null, sources);

		if (!task.call()) {
			return false;
		}

		setLastBuildTime(System.currentTimeMillis() - time);
		return true;
	}

	public static List<String> getJavaxCompilerOptions(String[] classpath, File outputFolder, int compliance){
		List<String> options = new ArrayList<String>();
		if(outputFolder != null){
			options.add("-d");
			options.add(outputFolder.getAbsolutePath());
		}
		options.add("-source");
		options.add(Integer.toString(compliance));
		options.add("-target");
		options.add(Integer.toString(compliance));
		options.add("-cp");
		String cp = "";

		if(classpath != null){
			for(String s : classpath){
				cp += ((cp.length()>0)?File.pathSeparator:"") + s;
			}
		}

		for(String s : getLibraryAccess()){
			cp += ((cp.length()>0)?File.pathSeparator:"") + s;
		}

		options.add(cp);

		return options;
	}

	@Override
	public int getNumberErrorsWhileLastBuild() {
		return diagn.getDiagnostics().size();
	}

	@Override
	public String getErrorsWhileLastBuild(int errorNr) {
		Diagnostic<?> diagnostic = diagn.getDiagnostics().get(errorNr);
		
		String ret = "-- ";
		
		if(diagnostic.getKind() != null){
			ret += diagnostic.getKind().toString();
		}
		
		if(diagnostic.getCode() != null){
			ret += " (";
			ret += diagnostic.getCode();
			ret += ")";
		}
		
		if(diagnostic.getSource() != null){
			ret += " in ";
			ret += diagnostic.getSource().toString();
		}
		
		ret += " @ position ";
		ret += Long.toString(diagnostic.getPosition());
		ret += " (";
		ret += Long.toString(diagnostic.getStartPosition());
		ret += ":";
		ret += Long.toString(diagnostic.getEndPosition());
		ret += ")";
		
		if(diagnostic.getMessage(null) != null){
			ret += ":\n";
			ret += diagnostic.getMessage(null);
			ret += "\n";
		}
		ret += "=====";
		
		return ret;
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
