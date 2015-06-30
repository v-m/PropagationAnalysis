package com.vmusco.smf.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public abstract class MavenTools {

	public static String MAVEN_DIR = "/home/vince/.m2/repository";
	
	private MavenTools(){

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
	
	// This method uses the local mvn installation to resolve and generate the effective POM
	// As a consequence, the generated POM contains all dependencies (even nested !)
	// Return null if a dependency cannot be resolved !!!
	// TODO: This is duplicate as it is already defined on another project, but as long as there is conflict with spoon version we are blocked with this !
	@Deprecated
	public static String extractClassPathFromPom(String projectRoot, String mavenDir) throws FileNotFoundException, IOException, XmlPullParserException, InterruptedException{
		// First step: we execute package the project in order to get all dependencies in .m2 cache
		String outprintlog = "/tmp/"+System.currentTimeMillis();
		System.out.println("Packaging the project to obtain all dependencies in meaven cache...");
		System.out.println("Mvn console: "+outprintlog);
		
		ProcessBuilder proc1 = new ProcessBuilder("mvn", "package");
		proc1.directory(new File(projectRoot));
		proc1.redirectOutput(new File(outprintlog));
		proc1.redirectError(new File(outprintlog));
		proc1.start().waitFor();
		
		System.out.println("Generating a pom in order to get all dependencies...");
		
		ProcessBuilder p2 = new ProcessBuilder("mvn", "help:effective-pom");
		p2.directory(new File(projectRoot));
		p2.redirectOutput(new File(outprintlog));
		p2.redirectError(new File(outprintlog));
		Process proc2 = p2.start();

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc2.getInputStream()));

		String s = null;
		String effectivePomReaded = ""; 
		boolean recordOutputAsXml = false;

		while ((s = stdInput.readLine()) != null) {
			String st = s.trim();
			if(st.startsWith("<?xml"))
				recordOutputAsXml = true;

			if(recordOutputAsXml)
				effectivePomReaded += s;

			if(st.endsWith("</project>"))
				recordOutputAsXml = false;
		}

		StringReader sr = new StringReader(effectivePomReaded);

		try{
			MavenXpp3Reader reader = new MavenXpp3Reader();
			Model model = reader.read(sr);
	
			String classpath = "";
	
			for(Dependency dep : model.getDependencies()){
				String ff = mavenDir + File.separator + dep.getGroupId().replaceAll("\\.", File.separator) + File.separator + dep.getArtifactId() + File.separator + dep.getVersion() + File.separator + dep.getArtifactId()+"-"+dep.getVersion()+".jar";
	
				File fff = new File(ff);
				if(!fff.exists()){
					return null;
				}else{
					classpath += ff + File.pathSeparator;
				}
	
			}
	
			return classpath;
		}catch(EOFException e){
			System.out.println("EOF reached -- no classpath !");
			return "";
		}
	}
	
	@Deprecated
	public static String extractClassPathFromPom(String projectRoot) throws FileNotFoundException, IOException, XmlPullParserException, InterruptedException{
		return extractClassPathFromPom(projectRoot, MAVEN_DIR);
	}
}
