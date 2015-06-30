package com.vmusco.smf.analysis;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.vmusco.smf.analysis.persistence.ExecutionPersistence;
import com.vmusco.smf.analysis.persistence.ProcessXMLPersistence;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.utils.MavenTools;

/**
 * This class is intended to be serialized - it contains all informations...
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public class ProcessStatistics implements Serializable{
	/*****************
	 * CONFIGURATION *
	 *****************/

	public static final String DEFAULT_SOURCE_FOLDER = "src/main/java";
	public static final String DEFAULT_TEST_FOLDER = "src/test/java";
	public static final String DEFAULT_CONFIGFILE = "smf.run.xml";
	public static final String CLASSPATH_PACK = "libs";
	public static final String SOURCES_COPY = "src";

	public static final String DEFAULT_PROJECT_BYTECODE = "bytecode/source";
	public static final String DEFAULT_TESTS_BYTECODE = "bytecode/tests";

	public static final String DEFAULT_MUTANTION_DIR = "mutation";
	
	public static final String DEFAULT_MUTANT_BASEDIR = "mutations/{id}/{op}";
	public static final String DEFAULT_MUTANT_SOURCE = "source";
	public static final String DEFAULT_MUTANT_BYTECODE = "bytecode";
	public static final String DEFAULT_MUTANT_EXECUTION = "exec";

	public enum STATE{
		/**
		 * On this state, the project has just been CREATED
		 */
		NEW,
		/**
		 * On this state, the project has been created and has STRONGLY BEEN SET (cp, mutation, ...)
		 * STATE: preparation
		 */
		READY,
		/**
		 * On this state, the project has been created and has strongly been set (cp, mutation, ...) and BUILT
		 * STATE: preparation
		 */
		BUILD,
		/**
		 * On this state, the project has been created and has strongly been set (cp, mutation, ...), built and TEST ARE BUILT ALSO
		 * STATE: preparation
		 */
		BUILD_TESTS,
		/**
		 * On this state, the test has been run (not mutated ones)
		 * STATE: preparation
		 */
		DRY_TESTS
	};

	private static STATE[] orderStates = new STATE[]{ STATE.NEW, STATE.READY, STATE.BUILD, STATE.BUILD_TESTS, STATE.DRY_TESTS };
	

	private ProcessStatistics() { }


	public static ProcessStatistics rawCreateProject(String datasetRepository, String workingDir){
		ProcessStatistics ps;

		ps = new ProcessStatistics();
		ps.projectIn = datasetRepository;
		ps.workingDir = workingDir;

		ps.srcToCompile = new String[]{DEFAULT_SOURCE_FOLDER};
		ps.srcTestsToTreat = new String[]{DEFAULT_TEST_FOLDER};
		ps.testingRessources = new String[]{};

		ps.projectOut = DEFAULT_PROJECT_BYTECODE;
		ps.testsOut = DEFAULT_TESTS_BYTECODE;
		ps.mutantsBasedir = DEFAULT_MUTANT_BASEDIR;
		ps.mutantsOut = DEFAULT_MUTANT_SOURCE;
		ps.mutantsBytecodeOut = DEFAULT_MUTANT_BYTECODE;
		ps.mutantsTestResults = DEFAULT_MUTANT_EXECUTION;
		ps.testTimeOut = Testing.MAX_TEST_TIMEOUT;
		ps.testTimeOut_auto = true;

		ps.persistFile = DEFAULT_CONFIGFILE;
		ps.currentState = STATE.NEW;

		return ps;
	}

	public static ProcessStatistics rawLoad(String persistenceFile) throws IOException{
		ProcessStatistics ps=null;

		File f = new File(persistenceFile);
		if(f.exists() && f.isDirectory()){
			f = new File(f, ProcessStatistics.DEFAULT_CONFIGFILE);
		}
		if(f.exists()){
			
			// Load the object...
			try{
				ps =  ProcessStatistics.loadState(f.getAbsolutePath());
				ps.persistFile = f.getName();
			}catch(InvalidClassException ex){
				System.out.println("Project persistance file has changed... Reset is required! ");
			}catch(Exception e){
				System.out.println("Exception on loading state. Creating new...");
				e.printStackTrace();
			}
		}

		return ps;
	}

	public String getPersistanceFile() {
		if(this.persistFile.charAt(0) == File.separatorChar){
			// Absolute path !
			return this.persistFile;
		}else{
			// Relative path !
			return this.workingDir + File.separatorChar + this.persistFile;
		}
	}

	public static String fromStateToString(STATE aState){
		switch(aState){
		case BUILD:
			return "BUILD";
		case BUILD_TESTS:
			return "BUILD_TESTS";
		case DRY_TESTS:
			return "DRY_TESTS";
		case NEW:
			return "NEW";
		case READY:
			return "READY";
		}

		return "UNKNOWN";
	}

	public static STATE fromStringToState(String aState){
		if(aState.equals("BUILD"))
			return STATE.BUILD;
		else if(aState.equals("BUILD_TESTS"))
			return STATE.BUILD_TESTS;
		else if(aState.equals("DRY_TESTS"))
			return STATE.DRY_TESTS;
		else if(aState.equals("NEW"))
			return STATE.NEW;
		else if(aState.equals("READY"))
			return STATE.READY;
		else 
			return null;
	}

	/**
	 * Change the state and persist the object only if it follows the state flow
	 * @param newState
	 * @return false if the new state is not the expected one.
	 */
	public boolean changeState(STATE newState) throws Exception{
		if(!newState.equals(ProcessStatistics.getNextState(this.currentState)) && !newState.equals(this.currentState))
			return false;

		this.currentState = newState;
		ProcessStatistics.saveState(this);
		return true;
	}

	public static String generatePersistanceFileName(){
		return "null";
	}

	/**
	 * This method saves the instance of a ProcessStatistics object
	 * @throws IOException 
	 */
	public static void saveState(ProcessStatistics ps) throws Exception {
		File f = new File(ps.buildPath(ps.persistFile));
		ExecutionPersistence<ProcessStatistics> persist = new ProcessXMLPersistence(f);
		persist.saveState(ps);
	}

	/**
	 * This method loads the last saved instance of the object
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static ProcessStatistics loadState(String persistFile) throws Exception {
		ExecutionPersistence<ProcessStatistics> persist = new ProcessXMLPersistence(new File(persistFile));
		return persist.loadState();
	}

	public boolean isPersistanceEnabled(){
		return this.persistFile != null;
	}

	public boolean currentStateIsBefore(STATE aState){
		STATE tmpState = aState;

		while(ProcessStatistics.getPreviousState(tmpState) != null){
			tmpState = ProcessStatistics.getPreviousState(tmpState);

			if(tmpState.equals(this.currentState)){
				return true;
			}
		}

		return false;
	}

	public static STATE getPreviousState(STATE aState){
		int i=0;

		for(i=0; i<orderStates.length; i++){
			if(aState.equals(orderStates[i]))
				break;
		}

		if(!aState.equals(orderStates[i]) || i <= 0)
			return null;

		return orderStates[i-1];
	}

	public static STATE getNextState(STATE aState){
		int i=0;

		for(i=0; i<orderStates.length; i++){
			if(aState.equals(orderStates[i]))
				break;
		}

		if(!aState.equals(orderStates[i]) || i >= orderStates.length-1)
			return null;

		return orderStates[i+1];
	}

	public void determineClassPath() throws IOException, InterruptedException {
		if(skipMvnClassDetermination)	
			return;

		String cpstring = MavenTools.extractClassPathUsingMvnV2(this.projectIn, false);

		Set<String> tmp = new HashSet<String>();

		for(String item : cpstring.split(":")){
			tmp.add(item);
		}
		this.classpath = tmp.toArray(new String[0]);
	}

	public void determineClassPathOnAll() throws IOException, InterruptedException {
		if(skipMvnClassDetermination)	
			return;

		Set<String> finalcp = new HashSet<String>();

		for(File fp : findAllPomsFiles()){
			String fromthisfile = MavenTools.extractClassPathUsingMvnV2(fp.getParentFile().getAbsolutePath(), false);
			for(String item : fromthisfile.split(":")){
				if(item != null && item.length() > 0)
					finalcp.add(item);
			}
		}

		this.classpath = finalcp.toArray(new String[0]);
	}
	
	/**
	 * This utility function complete the folder with the workspace prefix
	 * @param subpath
	 * @return
	 */
	public String buildPath(String subpath) {
		return workingDir + File.separator + subpath;
	}

	/**
	 * This utility function completes all folders supplied with the workspace prefix
	 * @param subpath
	 * @return
	 */
	public String[] buildPath(String[] subpath) {
		String[] ret = new String[subpath.length];
		int i = 0;

		for(String path:subpath){
			ret[i++] = buildPath(path);
		}

		return ret;
	}

	public String srcGenerationFolder() {
		return buildPath(projectOut);
	}

	public String testsGenerationFolder() {
		return buildPath(testsOut);
	}

	public void createWorkingDir() throws IOException{
		File f = new File(workingDir);
		if(f.exists())
			FileUtils.deleteDirectory(f);

		(new File(workingDir)).mkdirs();
	}

	/**************************************************************************************************
	 **************************************************************************************************
	 **************************************************************************************************/

	public STATE currentState;


	/**
	 * NEW STATE
	 * =========
	 */
	/**
	 * A name for this project
	 */
	public String projectName;

	/**
	 * The project working dir (containing src/test folders)
	 */
	private String projectIn;

	/**
	 * The file on which this object has to be persisted
	 */
	public String persistFile = null;

	/**
	 * The folders/files in projectIn of files to compile
	 */
	public String[] srcToCompile;

	/**
	 * The folders/files in projectIn of test src files to compile/consider
	 */
	public String[] srcTestsToTreat;

	/**
	 * The folder where the process should take place (working dir)
	 */
	private String workingDir;
	
	public String getWorkingDir() {
		return workingDir;
	}

	/**
	 * The subfolder in workingDir where we can generate the program bytecode
	 */
	public String projectOut;

	/**
	 * The subfolder in workingDir where we can generate the tests bytecode
	 */
	public String testsOut;

	/**
	 * The classpath used for running (app)
	 */
	private String[] classpath;

	public String[] getClasspath(){
		if(this.cpLocalFolder != null){
			File f = new File(this.buildPath(this.cpLocalFolder));
			
			List<String> cpr = new ArrayList<String>();
			for(File ff : f.listFiles()){
				cpr.add(ff.getAbsolutePath());
			}
			
			return cpr.toArray(new String[0]);
		}else{
			return this.classpath;
		}
	}
	
	public String[] getOriginalClasspath(){
		return this.classpath;
	}
	
	public String getProjectIn(boolean resolved){
		if(this.projectIn == null)
			return null;
		
		if(!resolved || (resolved && this.projectIn.startsWith("/"))){
			return this.projectIn;
		}else{
			File f = new File(this.buildPath(this.projectIn));
			return f.getAbsolutePath();
		}
	}
	
	public void setProjectIn(String projectIn){
		this.projectIn = projectIn;
	}
	
	public void setOriginalClasspath(String[] classpath){
		this.classpath = classpath;
	}

	/**
	 * Set to true to avoid the automatic determination of the class path
	 * using the method {@link ProcessStatistics#determineClassPath()}
	 */
	public boolean skipMvnClassDetermination = false;

	/**
	 * List of *ressources* folder to consider for running the test cases.
	 * Typically it consist in files which are used to read/write to during the 
	 * testing phase. Ignoring this element when required can lead to 
	 * failing tests.
	 */
	public String[] testingRessources = new String[]{};


	/**
	 * BUILD STATE
	 * ===========
	 */





	/**
	 * DRY_TESTS STATE
	 * ===============
	 */
	
	/**
	 * The discovered test classes
	 * null if no discovery process has been run
	 */
	public String[] testClasses = null;

	/**
	 * The discovered test cases
	 * null if no discovery process has been run
	 */
	public String[] testCases = null;
	/**
	 * Failing test cases on original project
	 * null if no discovery process has been run
	 */
	public String[] failingTestCases = null;
	/**
	 * Ignored test cases on original project
	 * null if no discovery process has been run
	 */
	public String[] ignoredTestCases = null;
	/**
	 * Hanging test cases on original project (infinite loops)
	 * null if no discovery process has been run
	 */
	public String[] hangingTestCases = null;

	/**
	 * Test cases which fails because they cannot be initialized.
	 * This should never occurs in normal execution of tests
	 * only for mutation cases...
	 */
	public String[] errorOnTestSuite;


	/**
	 * The folder in which all mutation takes place
	 */
	public String mutantsBasedir;

	/**
	 * The subfolder in workingDir where we generate the mutants
	 */
	public String mutantsOut;


	/**
	 * The subfolder in workingDir where we generate the mutants bytecode
	 */
	public String mutantsBytecodeOut;
	
	/**
	 * Where are stored the test execution results against the mutants
	 */
	public String mutantsTestResults;

	/**
	 * TIMES
	 * =====
	 */

	public Long buildProjectTime = null;
	public Long buildTestsTime = null;
	public Long runTestsOriginalTime = null;

	
	public String[] getUnmutatedFailAndHang(){
		Set<String> cases = new HashSet<String>();

		for(String ts : errorOnTestSuite){
			for(String s : testCases){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}
		
		for(String s:this.hangingTestCases){
			cases.add(s);
		}

		for(String s:this.failingTestCases){
			cases.add(s);
		}

		for(String s : this.errorOnTestSuite){
			for(String ss : this.testCases){
				if(ss.startsWith(s)){
					cases.add(ss);
				}
			}
		}
		
		return cases.toArray(new String[0]);
	}

	public String[] getTestingClasspath(){
		List<String> l = new ArrayList<String>();
		
		if(this.getClasspath() != null){
			for(String c : this.getClasspath()){
				l.add(c);
			}

			l.add(this.srcGenerationFolder());
			l.add(this.testsGenerationFolder());
			
			return l.toArray(new String[0]);
		}else{
			return new String[]{
				this.srcGenerationFolder(),
				this.testsGenerationFolder(),
			};
		}
	}
	
	public void createLocalCopies(String source, String classpath_folder) throws IOException {
		// SOURCES
		if(this.projectIn.startsWith("/")){
			File packTo = new File(this.buildPath(source));
			File src = new File(this.projectIn);
			
			FileUtils.copyDirectory(src, packTo);
			originalSrc = this.projectIn;
			this.projectIn = source;
		}
		
		// CLASSPATH
		this.cpLocalFolder = classpath_folder;
		File packTo = new File(this.buildPath(this.cpLocalFolder));
		
		if(packTo.exists()){
			FileUtils.deleteDirectory(packTo);
		}
		
		packTo.mkdirs();
		
		if(this.classpath != null){
			for(String cp : this.classpath){
				File src = new File(cp);
				File dst = new File(packTo, src.getName());
				
				if(src.isDirectory()){
					FileUtils.copyDirectory(src, dst);
				}else{
					FileUtils.copyFile(src, dst);
				}
			}
		}
	}
	
	public String originalSrc = null;
	public String cpLocalFolder = null;
	
	/**
	 * Number of second after which the test is considered as hanging
	 * Can be dynamically determined
	 */
	public int testTimeOut;
	public boolean testTimeOut_auto;
	

	public boolean workingDirAlreadyExists() {
		File f = new File(this.workingDir);
		return f.exists();
	}

	public void exportClassPath() throws IOException, InterruptedException {
		System.out.println("[MAVEN] Exporting classpath...");
		//MavenTools.exportDependenciesUsingMaven(this.getProjectIn(true), this.buildPath(this.cpLocalFolder), this.buildPath("maven.log"));

		File dst = new File(this.buildPath(this.cpLocalFolder));
		/*String cp = MavenTools.extractClassPathUsingMvnV2(this.getProjectIn(true), false);
		
		for(String c : cp.split(":")){
			File src = new File(c);
			FileUtils.copyFile(src, new File(dst, src.getName()));
		}*/
		MavenTools.exportDependenciesUsingMaven(this.getProjectIn(true), dst.getAbsolutePath(), this.buildPath("mvn_copy.log"));
	}
	
	private File[] findAllPomsFiles(){
		ArrayList<File> poms = new ArrayList<File>();

		File f = new File(this.getProjectIn(true));
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
	
	public void exportClassPathOnAll() throws IOException {
		for(File fp : findAllPomsFiles()){
			System.out.println("Handling "+fp.getAbsolutePath());
			MavenTools.exportDependenciesUsingMaven(fp.getParentFile().getAbsolutePath(), this.buildPath(this.cpLocalFolder), this.buildPath("maven.log"));
		}
	}

	public void addRessources(String[] split) {
		Set<String> res = new HashSet<String>();
		
		if(this.testingRessources != null){
			for(String s : testingRessources){
				res.add(s);
			}
		}
		
		for(String s : split){
			res.add(s);
		}
			
		testingRessources = res.toArray(new String[0]);
	}
	
	public boolean isSubversionProject(){
		File f = new File(getProjectIn(true), ".svn");
		return f.exists();
	}
	
	public boolean isGitProject(){
		File f = new File(getProjectIn(true), ".git");
		return f.exists();
	}
}
