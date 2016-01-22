package com.vmusco.smf.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.vmusco.smf.analysis.persistence.ProjectXmlPersistenceManager;
import com.vmusco.smf.analysis.persistence.XMLPersistence;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.instrumentation.AbstractInstrumentationProcessor;
import com.vmusco.smf.instrumentation.Instrumentation;
import com.vmusco.smf.testing.TestCasesProcessor;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.testing.TestsExecutionListener;
import com.vmusco.smf.utils.MavenTools;
import com.vmusco.smf.utils.SetTools;

/**
 * This class contains all informations required for a mutation project
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ProcessStatistics implements Serializable{
	private static final long serialVersionUID = 1L;

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
		 * On this state, the project has just been CREATED - user is free to declare configuration
		 */
		FRESH,
		/**
		 * On this state, the project has been created and has strongly been set (cp, mutation, ...) and BUILT
		 */
		BUILD,
		/**
		 * On this state, the test has been run (not mutated ones)
		 */
		READY
	};

	//private static STATE[] orderStates = new STATE[]{ STATE.NEW, STATE.DEFINED, STATE.BUILD, STATE.BUILD_TESTS, STATE.READY };

	/***************
	 ** Variables **
	 ***************/

	private STATE currentState;
	/**
	 * A name for this project
	 */
	private String projectName;
	/**
	 * The project working dir (containing src/test folders)
	 */
	private String projectIn;

	/**
	 * The folders/files in projectIn of files to compile
	 */
	private String[] srcToCompile;
	/**
	 * The folders/files in projectIn of test src files to compile/consider
	 */
	private String[] srcTestsToTreat;
	/**
	 * The folder where the process should take place (working dir)
	 */
	private String workingDir;
	/**
	 * The subfolder in workingDir where we can generate the program bytecode
	 */
	private String projectOut;
	/**
	 * The subfolder in workingDir where we can generate the tests bytecode
	 */
	private String testsOut;
	/**
	 * The classpath used for running (app)
	 */
	private String[] classpath;
	/**
	 * Set to true to avoid the automatic determination of the class path
	 * using the method {@link ProcessStatistics#determineClassPath()}
	 */
	private boolean skipMvnClassDetermination = false;
	/**
	 * List of *ressources* folder to consider for running the test cases.
	 * Typically it consist in files which are used to read/write to during the 
	 * testing phase. Ignoring this element when required can lead to 
	 * failing tests.
	 */
	private String[] testingRessources = new String[]{};

	/**
	 * The discovered test cases
	 * null if no discovery process has been run
	 */
	private String[] testCases = null;
	/**
	 * The discovered test classes
	 * null if no discovery process has been run
	 */
	private String[] testClasses = null;
	
	
	/**
	 * Test cases execution results on original project
	 * null if no discovery process has been run
	 */
	private TestsExecutionIfos cleanTestExecution = null;
	
	/**
	 * The folder in which all mutation takes place
	 */
	private String mutantsBasedir;
	/**
	 * Where are stored the test execution results against the mutants
	 */
	private String mutantsTestResults;
	/**
	 * The subfolder in workingDir where we generate the mutants bytecode
	 */
	private String mutantsBytecodeOut;
	/**
	 * The subfolder in workingDir where we generate the mutants
	 */
	private String mutantsOut;
	private Long buildProjectTime = null;
	private Long buildTestsTime = null;
	private Long runTestsOriginalTime = null;
	
	private String originalSrc = null;
	private String cpLocalFolder = null;

	/*********************************************
	 *********************************************/

	public ProcessStatistics(String workingDir) { 
		this.workingDir = workingDir;
		projectIn = null;

		srcToCompile = new String[]{DEFAULT_SOURCE_FOLDER};
		srcTestsToTreat = new String[]{DEFAULT_TEST_FOLDER};
		testingRessources = new String[]{};

		projectOut = DEFAULT_PROJECT_BYTECODE;
		testsOut = DEFAULT_TESTS_BYTECODE;
		mutantsBasedir = DEFAULT_MUTANT_BASEDIR;
		mutantsOut = DEFAULT_MUTANT_SOURCE;
		mutantsBytecodeOut = DEFAULT_MUTANT_BYTECODE;
		mutantsTestResults = DEFAULT_MUTANT_EXECUTION;

		currentState = STATE.FRESH;
	}
	
	public ProcessStatistics(String datasetRepository, String workingDir) {
		this(workingDir);
		this.projectIn = datasetRepository;
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
			}catch(Exception e){
				System.out.println("Exception on loading state. Creating new...");
				e.printStackTrace();
			}
		}

		return ps;
	}

	public String getPersistFile(boolean resolve) {
		if(!resolve){
			return DEFAULT_CONFIGFILE;
		}

		// Relative path !
		return this.workingDir + File.separatorChar + DEFAULT_CONFIGFILE;
	}

	public static String fromStateToString(STATE aState){
		switch(aState){
		case BUILD:
			return "BUILD";
		case READY:
			return "DRY_TESTS";
		case FRESH:
			return "FRESH";
		}

		return "UNKNOWN";
	}

	public static STATE fromStringToState(String aState){
		if(aState.equals("BUILD"))
			return STATE.BUILD;
		else if(aState.equals("DRY_TESTS"))
			return STATE.READY;
		else if(aState.equals("FRESH"))
			return STATE.FRESH;
		else 
			return null;
	}

	public static String generatePersistanceFileName(){
		return "null";
	}

	public String getConfigFilePath(){
		return buildPath(DEFAULT_CONFIGFILE);
	}
	
	/**
	 * This method saves the instance of a ProcessStatistics object
	 * @throws IOException 
	 */
	public static void saveState(ProcessStatistics ps) throws PersistenceException {
		ProjectXmlPersistenceManager mgr = new ProjectXmlPersistenceManager(ps);
		XMLPersistence.save(mgr);
		//File f = new File(ps.buildPath(DEFAULT_CONFIGFILE));
		//ExecutionPersistence<ProcessStatistics> persist = new ProcessXMLPersistence(f);
		//persist.saveState(ps);
	}
	
	/**
	 * This method loads the last saved instance of the object
	 * @throws PersistenceException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static ProcessStatistics loadState(String persistFile) throws PersistenceException{
		File f = new File(persistFile);

		if(!f.isDirectory())
			f = f.getParentFile();
		
		ProcessStatistics ps = new ProcessStatistics(f.getAbsolutePath());
		ProjectXmlPersistenceManager mgr = new ProjectXmlPersistenceManager(ps);
		XMLPersistence.load(mgr);
		
		return ps;
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

		for(File fp : MavenTools.findAllPomsFiles(this.getProjectIn(true))){
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

		f.mkdirs();
	}

	/**************************************************************************************************
	 **************************************************************************************************
	 **************************************************************************************************/


	public STATE getCurrentState() {
		return currentState;
	}

	public void setCurrentState(STATE currentState) {
		this.currentState = currentState;
	}


	public String[] getTestCases() {
		return testCases;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String[] resolveThis(String[] in){
		String[] out = new String[in.length];

		int i = 0;
		for(String it : in){
			out[i++] = getProjectIn(true) + File.separator + it;
		}

		return out;
	}

	public String[] getSrcToCompile(boolean resolve) {
		if(resolve){
			return resolveThis(srcToCompile);
		}else{
			return srcToCompile;
		}
	}

	public void setSrcToCompile(String[] srcToCompile) {
		this.srcToCompile = srcToCompile;
	}

	public String[] getSrcTestsToTreat(boolean resolve) {
		if(resolve){
			return resolveThis(srcTestsToTreat);
		}else{
			return srcTestsToTreat;
		}
	}

	public void setSrcTestsToTreat(String[] srcTestsToTreat) {
		this.srcTestsToTreat = srcTestsToTreat;
	}

	public String getWorkingDir() {
		return workingDir;
	}

	/**
	 * Resolve the folder with full path for working directory
	 * @param s
	 * @return
	 */
	public String resolveThis(String s){
		return getWorkingDir() + File.separator + s;
	}

	public String getProjectOut(boolean resolve) {
		if(resolve){
			return resolveThis(projectOut);
		}else{
			return projectOut;
		}
	}

	public void setProjectOut(String projectOut) {
		this.projectOut = projectOut;
	}

	public String getTestsOut(boolean resolve) {
		if(resolve){
			return resolveThis(testsOut);
		}else{
			return testsOut;
		}
	}

	public void setTestsOut(String testsOut) {
		this.testsOut = testsOut;
	}

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

	public boolean isSkipMvnClassDetermination() {
		return skipMvnClassDetermination;
	}

	public void setSkipMvnClassDetermination(boolean skipMvnClassDetermination) {
		this.skipMvnClassDetermination = skipMvnClassDetermination;
	}

	public String[] getTestingRessources(boolean resolve) {
		if(resolve){
			return resolveThis(testingRessources);
		}else{
			return testingRessources;
		}
	}

	public void setTestingRessources(String[] testingRessources) {
		this.testingRessources = testingRessources;
	}

	public String[] getTestClasses() {
		return testClasses;
	}

	public void setTestClasses(String[] testClasses) {
		this.testClasses = testClasses;
	}

	public void setTestCases(String[] testCases) {
		this.testCases = ProcessStatistics.fixTestSignatures(testCases);
	}


	public String[] getFailingTestCases() {
		if(cleanTestExecution != null)
			return cleanTestExecution.getRawFailingTestCases();
		else
			return null;
	}

	public String[] getIgnoredTestCases() {
		if(cleanTestExecution != null)
			return cleanTestExecution.getRawIgnoredTestCases();
		else
			return null;
		
	}

	public String[] getHangingTestCases() {
		if(cleanTestExecution != null)
			return cleanTestExecution.getRawHangingTestCases();
		else
			return null;
	}
	
	/*public String[][] getStackTraces() {
		if(cleanTestExecution != null)
			return cleanTestExecution.getStacktraces();
		else
			return null;
	}*/

	public static String[] fixTestSignatures(String[] hangingTestCases2) {
		String[] ret = new String[hangingTestCases2.length];

		for(int i = 0; i < hangingTestCases2.length; i++){
			ret[i] = ProcessStatistics.fixTestSignature(hangingTestCases2[i]);
		}

		return ret;
	}

	public String[] getErrorOnTestSuite() {
		if(cleanTestExecution != null)
			return cleanTestExecution.getRawErrorOnTestSuite();
		else
			return null;
	}

	public void setTestExecutionResult(TestsExecutionIfos cleanTestExecution){
		this.cleanTestExecution = cleanTestExecution;
	}
	
	public TestsExecutionIfos getTestExecutionResult(){
		return this.cleanTestExecution;
	}
	
	
	
	
	

	public String[] includeTestSuiteGlobalFailingCases(String[] testsuites, String[] include){
		Set<String> cases = new HashSet<String>();

		if(include != null){
			for(String s : include){
				cases.add(s);
			}
		}

		for(String ts : testsuites){
			for(String s : getTestCases()){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		return cases.toArray(new String[0]);
	}

	/**
	 * This method return the failing function after...
	 *  - removing functions already failing on execution on the unmutated version of the software;
	 *  - adding all functions included in test suite global failing methods.
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantFailingTestCases(TestsExecutionIfos tei) throws MutationNotRunException {
		String[] mutset = includeTestSuiteGlobalFailingCases(tei.getRawErrorOnTestSuite(), tei.getRawFailingTestCases());
		String[] glbset = includeTestSuiteGlobalFailingCases(getErrorOnTestSuite(), getFailingTestCases());

		return SetTools.setDifference(mutset, glbset);
	}

	/**
	 * This method return the ignored function after removing functions already ignored on execution on the unmutated version of the software;
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantIgnoredTestCases(TestsExecutionIfos tei) throws MutationNotRunException {
		String[] mutset = tei.getRawIgnoredTestCases();
		String[] glbset = getIgnoredTestCases();

		return SetTools.setDifference(mutset, glbset);
	}

	/**
	 * This method return the hanging function after removing functions already hanging on execution on the unmutated version of the software;
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return 
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantHangingTestCases(TestsExecutionIfos tei) throws MutationNotRunException {
		String[] mutset = tei.getRawHangingTestCases();
		String[] glbset = getHangingTestCases();

		return SetTools.setDifference(mutset, glbset);
	}

	/**
	 * Includes tests hanging when the whole test case fail, the failing and the hanging cases in one shot.
	 * The result do not includes the elements already failing or hanging in the execution of the un mutated version of the code. 
	 * @param ps The {@link ProcessStatistics} object which describes the execution
	 * @return
	 * @throws MutationNotRunException
	 */
	public String[] getCoherentMutantFailAndHangTestCases(TestsExecutionIfos tei) throws MutationNotRunException {
		Set<String> cases = new HashSet<String>();

		for(String s : includeTestSuiteGlobalFailingCases(tei.getRawErrorOnTestSuite(), null)){
			cases.add(s);
		}
		
		for(String s:tei.getRawHangingTestCases()){
			cases.add(s);
		}

		for(String s:tei.getRawFailingTestCases()){
			cases.add(s);
		}

		return SetTools.setDifference(cases.toArray(new String[cases.size()]), getUnmutatedFailAndHang());
	}
	
	
	/**
	 * Return the base directory for mutants. 
	 * Normally, this string should contain {id} which represent the mutation project name and
	 * {op} which represent the mutation operator consiered. Those two patterns should be replaced
	 * by the desired value
	 * @return
	 */
	public String getMutantsBasedir() {
		return mutantsBasedir;
	}

	/**
	 * Return the path to the base directory containing all projects id
	 * @return
	 */
	public String getMutantsIdsBaseDir(){
		int pos = mutantsBasedir.indexOf("{id}");
		return mutantsBasedir.substring(0, pos);
	}

	public String getMutantsOpsBaseDir(String id){
		int pos = mutantsBasedir.indexOf("{op}");
		String tmp = mutantsBasedir.substring(0, pos);
		return tmp.replace("{id}", id);
	}

	public void setMutantsBasedir(String mutantsBasedir) {
		this.mutantsBasedir = mutantsBasedir;
	}


	public String getMutantsOut() {
		return mutantsOut;
	}

	public void setMutantsOut(String mutantsOut) {
		this.mutantsOut = mutantsOut;
	}

	public String getMutantsBytecodeOut() {
		return mutantsBytecodeOut;
	}

	public void setMutantsBytecodeOut(String mutantsBytecodeOut) {
		this.mutantsBytecodeOut = mutantsBytecodeOut;
	}

	public String getMutantsTestResults() {
		return mutantsTestResults;
	}

	public void setMutantsTestResults(String mutantsTestResults) {
		this.mutantsTestResults = mutantsTestResults;
	}

	public Long getBuildProjectTime() {
		return buildProjectTime;
	}
	public Long getBuildTestsTime() {
		return buildTestsTime;
	}
	public Long getRunTestsOriginalTime() {
		return runTestsOriginalTime;
	}

	public void setBuildProjectTime(Long buildProjectTime) {
		this.buildProjectTime = buildProjectTime;
	}
	public void setBuildTestsTime(Long buildTestsTime) {
		this.buildTestsTime = buildTestsTime;
	}
	public void setRunTestsOriginalTime(Long runTestsOriginalTime) {
		this.runTestsOriginalTime = runTestsOriginalTime;
	}


	public String[] getUnmutatedFailAndHang(){
		Set<String> cases = new HashSet<String>();

		for(String ts : getErrorOnTestSuite()){
			for(String s : testCases){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		for(String s : getHangingTestCases()){
			cases.add(s);
		}

		for(String s : getFailingTestCases()){
			cases.add(s);
		}

		for(String s : getErrorOnTestSuite()){
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

			if(new File(this.srcGenerationFolder()).exists())
				l.add(this.srcGenerationFolder());
			
			if(new File(this.testsGenerationFolder()).exists())
				l.add(this.testsGenerationFolder());

			//l.add(Testing.getCurrentVMClassPath(new String[]{"smf"})[0]);
			
			return l.toArray(new String[0]);
		}else{
			return new String[]{
					this.srcGenerationFolder(),
					this.testsGenerationFolder(),
					//Testing.getCurrentVMClassPath(new String[]{"smf"})[0]
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

	public String getOriginalSrc() {
		return originalSrc;
	}
	public void setOriginalSrc(String originalSrc) {
		this.originalSrc = originalSrc;
	}

	public String getCpLocalFolder() {
		return cpLocalFolder;
	}

	public void setCpLocalFolder(String cpLocalFolder) {
		this.cpLocalFolder = cpLocalFolder;
	}

	public int getTestTimeOut() {
		if(cleanTestExecution != null) return cleanTestExecution.getTestTimeOut();
		else return Testing.MAX_TEST_TIMEOUT;
	}

	public boolean workingDirAlreadyExists() {
		File f = new File(this.workingDir);
		return f.exists();
	}

	public void exportClassPath() throws IOException {
		System.out.println("[MAVEN] Exporting classpath...");

		File dst = new File(this.buildPath(this.cpLocalFolder));
		MavenTools.exportDependenciesUsingMaven(this.getProjectIn(true), dst.getAbsolutePath(), this.buildPath("mvn_copy.log"));
	}



	public void exportClassPathOnAll() throws IOException {
		for(File fp : MavenTools.findAllPomsFiles(this.getProjectIn(true))){
			System.out.println("Handling "+fp.getAbsolutePath());
			MavenTools.exportDependenciesUsingMaven(fp.getParentFile().getAbsolutePath(), this.buildPath(this.cpLocalFolder), this.buildPath("maven.log"));
		}
	}

	public void setRessources(String[] split) {
		testingRessources = split;
	}

	public boolean isSubversionProject(){
		File f = new File(getProjectIn(true), ".svn");
		return f.exists();
	}

	public boolean isGitProject(){
		File f = new File(getProjectIn(true), ".git");
		return f.exists();
	}


	public static String fixTestSignature(String test) {
		if(test.endsWith(")")){
			return test;
		}else{
			return test+"()";
		}
	}


	public static boolean areTestsEquivalents(String bug, String test) {
		String  cbug = fixTestSignature(bug);
		String ctest = fixTestSignature(test);

		return cbug.equals(ctest);
	}

	public boolean instrumentAndBuildProjectAndTests(AbstractInstrumentationProcessor[] aips) throws BadStateException, IOException{
		if(getCurrentState() != STATE.FRESH)
			throw new BadStateException("Expecting state to be FRESH");
		
		System.out.println("Instrumenting project...");
		
		File pji = new File(this.getProjectIn(true));
		File orig = new File(pji.getParentFile(), pji.getName()+".original");

		//pji.renameTo(orig);
		FileUtils.copyDirectory(pji, orig);

		for(String srce : getSrcToCompile(false)){
			Instrumentation.instrumentSource(new String[]{orig.getAbsolutePath() + File.separator + srce}, getClasspath(), new File(pji, srce), aips);
		}
		
		if(!compileProjectWithSpoon()){
			return false;
		}
		for(String srce : getSrcTestsToTreat(false)){
			Instrumentation.instrumentSource(new String[]{orig.getAbsolutePath() + File.separator + srce}, getTestingClasspath(), new File(pji, srce), aips);
		}
		if(!compileTestWithSpoon()){
			return false;
		}
		
		this.currentState = STATE.BUILD;
		return true;
	}

	private boolean compileProjectWithSpoon() throws BadStateException, IOException {
		String pt = buildPath("spoonCompilation.log");
		long ret = Compilation.compileUsingSpoon(getSrcToCompile(true), getClasspath(), srcGenerationFolder(), pt);

		if(ret < 0){
			System.err.println("Error on compilation phase !");
			return false;
		}else{
			setBuildProjectTime(ret);
			return true;
		}
	}

	private boolean compileTestWithSpoon() throws BadStateException, IOException {
		long ret = Compilation.compileUsingSpoon(getSrcTestsToTreat(true), getTestingClasspath(), testsGenerationFolder(), buildPath("spoonTestCompilations.log"));
		
		if(ret < 0){
			System.err.println("Error on compilation phase !");
			return false;
		}else{
			setBuildTestsTime(ret);
			return true;
		}
	}
	
	public boolean compileWithSpoon() throws BadStateException, IOException{
		if(getCurrentState() != STATE.FRESH)
			throw new BadStateException("Expecting state to be FRESH");
		
		if(compileProjectWithSpoon()){
			if(compileTestWithSpoon()){
				this.currentState = STATE.BUILD;
				return true;
			}
		}
		
		return false;
	}

	public void performFreshTesting(TestsExecutionListener tel) throws IOException {
		Testing.executeTestDetection(getSrcTestsToTreat(true), getTestingClasspath());
		setTestClasses(TestCasesProcessor.getTestClassesString());
		setTestCases(TestCasesProcessor.getTestCasesString());
		
		cleanTestExecution = Testing.runTestCases(getProjectIn(true), getRunningClassPath(), getTestClasses(), 0, tel);
		
		this.currentState = STATE.READY;
	}
	
	public String[] getRunningClassPath() throws IOException{
		List<String> ret = new ArrayList<String>();

		for(String s : getTestingClasspath()){
			ret.add(s);
		}
		
		ret.add(testsGenerationFolder());
		
		for(String aRess : getTestingRessources(true)){
			ret.add(aRess);
		}

		for(String cpe : Testing.getCurrentVMClassPath(new String[]{"smf", "junit"})){
			ret.add(cpe);
		}

		this.currentState = STATE.READY;
		
		return ret.toArray(new String[0]);
	}
	
	public boolean isInstrumented() {
		return new File(getProjectIn(true)+File.separator+".original").exists();
	}
}
