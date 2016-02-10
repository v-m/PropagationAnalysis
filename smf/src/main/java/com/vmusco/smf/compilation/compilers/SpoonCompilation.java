package com.vmusco.smf.compilation.compilers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import spoon.compiler.Environment;
import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.compilation.Compilation;

/**
 * Do not use anymore the spoon builder as it do not support in memory building AND compliance level for built version
 * Instead see {@link JavaxCompilation}.
 * @author Vincenzo Musco - http://www.vmusco.com
 */
@Deprecated
public class SpoonCompilation extends Compilation{
	private String buildlogfile;

	public SpoonCompilation(String buildlogfile) {
		this.buildlogfile = buildlogfile;
	}
	
	// NOT SUPPORTED IN SPOON
	@Override
	public Map<String, byte[]> buildInMemory(String qualifiedName, String source, String[] classpath, int compliance) {
		return null;
	}

	@Override
	public boolean buildInDirectory(File[] sourceFiles, File outputFolder, String[] classpath, int compliance) {
		try{
			long t1 = System.currentTimeMillis();
			
			Environment environment = new StandardEnvironment();
			environment.setComplianceLevel(compliance);
	
			Factory factory = new FactoryImpl(new DefaultCoreFactory(), environment);
			SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);
	
			// Add all sources here
			for(File aSrcFile : sourceFiles){
				//compiler.addInputSource(new File(ps.getProjectIn(true) + File.separator + aSrcFile));
				compiler.addInputSource(aSrcFile);
			}
	
			String[] fcp = getLibraryAccess(classpath);
	
			compiler.setSourceClasspath(fcp);
	
			File fdest = outputFolder;
			if(!fdest.isDirectory()){
				System.err.println("Need a directory as input !");
				return false;
			}
	
			System.out.println("Compiling the project using spoon in "+fdest.getAbsolutePath()+".");
			System.out.println("Log at "+buildlogfile);
	
			// This part is used to log WARNINGS or stderr !!!
			File f = new File(buildlogfile);
			if(f.exists())
				f.delete();
			f.createNewFile();
	
			LogToFile ltf = new LogToFile();
			ltf.redirectTo(f);
	
			compiler.setBinaryOutputDirectory(fdest);
	
			compiler.compile();
			ltf.restablish();
			long t2 = System.currentTimeMillis();
			setLastBuildTime(t2-t1);
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
}

/**
 * This class is used to redirect and restore the stdout and stderr
 * @author Vincenzo Musco - http://www.vmusco.com
 */
class LogToFile {
	private PrintStream retainer_out = null;
	private PrintStream retainer_err = null;
	
	public void redirectTo(File f) throws IOException{
		retainer_out = System.out;
		retainer_err = System.err;
		
		f.createNewFile();
		PrintStream psfile = new PrintStream(new FileOutputStream(f));
		System.setErr(psfile);
		System.setOut(psfile);
	}
	
	public void restablish(){
		System.setOut(retainer_out);
		System.setErr(retainer_err);
		retainer_out = null;
		retainer_err = null;
	}
}
