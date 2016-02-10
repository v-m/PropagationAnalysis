package com.vmusco.smf.compilation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class containing all compilation methods 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class Compilation {
	private long buildTime;
	
	/**
	 * Compiles a class passed as string to a bytecode (byte[]) built version in memory (no file produced).
	 * @return null if compilation fails. bytecodes if succeed.
	 */
	public abstract Map<String, byte[]> buildInMemory(String qualifiedName, String source, String[] classpath, int compliance);
	/**
	 * Takes files (.java) as input and produce built classes files (.class) as output in outputFolder.
	 */
	public abstract boolean buildInDirectory(File[] sourceFiles, File outputFolder, String[] classpath, int compliance);
	
	protected void setLastBuildTime(long buildTime){
		this.buildTime = buildTime;
	}
	
	public long getLastBuildTime() {
		return buildTime;
	}

	public static String[] getLibraryAccess(String[] cp) {
		Set<String> ret = getLibraryAccess();

		if(cp != null){
			for(String c : cp){
				ret.add(c);
			}
		}

		return ret.toArray(new String[0]);
	}

	public static Set<String> getLibraryAccess() {
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
