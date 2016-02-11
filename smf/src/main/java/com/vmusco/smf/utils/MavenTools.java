package com.vmusco.smf.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;

/**
 * This class offers utility functions to get classpath informations
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MavenTools {

	private MavenTools(){

	}
	
	public static File[] findAllPomsFiles(String entry){
		ArrayList<File> poms = new ArrayList<File>();

		File f = new File(entry);
		ArrayList<File> treat = new ArrayList<File>();
		treat.add(f);

		while(treat.size() > 0){
			File cur = treat.remove(0);

			if(cur.isDirectory()){
				for(File tt : cur.listFiles()){
					treat.add(tt);
				}
			}else{
				if(cur.getName().equals("pom.xml")){
					poms.add(cur);
				}
			}
		}

		return poms.toArray(new File[0]);
	}

	public static void exportDependenciesUsingMaven(String projectRoot, String where, String consoleout) throws IOException{
		System.out.println("[MAVEN] Dependencies extraction in "+where+"...");
		
		File f = new File(where);
		f.mkdirs();
		
		String[] cmd = new String[]{
				"mvn", "dependency:copy-dependencies", "-DoutputDirectory="+where
		};
		
		ProcessBuilder proc1 = new ProcessBuilder(cmd);
		proc1.directory(new File(projectRoot));
		proc1.redirectErrorStream(true);
		
		Process start = proc1.start();
		
		FileOutputStream fos = new FileOutputStream(new File(consoleout));
		IOUtils.copy(start.getInputStream(), fos);
		fos.close();
	}
	
	public static String extractClassPathUsingMvnV2(String projectRoot) throws IOException, InterruptedException{
		return extractClassPathUsingMvnV2(projectRoot, true);
	}
	
	public static String extractClassPathUsingMvnV2(String projectRoot, boolean installIt) throws IOException, InterruptedException{
		if(installIt){
			// Prior installation to produce full pom (in case of multi modules)
			System.out.println("[MAVEN] Installing module....");
			ProcessBuilder proc0 = new ProcessBuilder("mvn", "install");
			proc0.directory(new File(projectRoot));
			proc0.start().waitFor();
		}

		System.out.println("[MAVEN] Dependencies extraction...");
		
		// Extract dependencies
		ProcessBuilder proc1 = new ProcessBuilder("mvn", "dependency:build-classpath");
		proc1.directory(new File(projectRoot));
		Process start = proc1.start();
		
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(start.getInputStream()));

		String s = null;
		String cp = "";
		
		while ((s = stdInput.readLine()) != null) {
			if(!s.startsWith("[") && !s.contains(" ") && !s.contains("http://") && !s.contains("https://")){
				cp += (cp.length()==0?"":":")+s;
			}
		}
		
		return cp;
	}
}
