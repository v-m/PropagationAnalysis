package com.vmusco.smf.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import spoon.compiler.SpoonCompiler;
import spoon.processing.Processor;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.exceptions.MutantHangsException;
import com.vmusco.smf.exceptions.TestingException;

/**
 * Tests execution logic
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public final class Testing {
	private static final Logger logger = LogManager.getFormatterLogger(Testing.class.getSimpleName());
	public static ArrayList<CtClass<?>> temp;

	/**
	 * The time after which the test fails (in seconds)
	 */
	public static int MIN_TEST_TIMEOUT = 10;
	public static int MAX_TEST_TIMEOUT = 60;
	public static int INC_TEST_TIMEOUT = 10;

	private Testing() {
	}

	/**
	 * Run the test detections on source files and return results on TestCasesProcessor
	 * @param srcFolder
	 * @param classpath
	 */
	public static void executeTestDetection(String[] srcFolder, String[] classpath){
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		for(String src : srcFolder){
			compiler.addInputSource(new File(src));
		}


		String[] cp = Compilation.getLibraryAccess(classpath);
		
		//Updating classpath
		//if(classpath != null)
		compiler.setSourceClasspath(cp);

		// Build (in memory)
		compiler.build();

		// Obtain list of element to mutate
		List<Processor<?>> arg0 = new ArrayList<>();
		arg0.add(new TestCasesProcessor());
		compiler.process(arg0);
	}
	
	/**
	 * Search for JUnit test classes declared by inheritance (JUnit 3) and annotation (Junit 4) and store those in ps
	 * NOTE: after running the test cases, the statistics can be obtained in TestCasesProcessor (getNbFromAnnotation(), getNbFromTestCases()).
	 * Note that those are not thread safe !
	 * @param srcFolder
	 * @param classpath
	 * @return the list of test classes
	 * @see TestCasesProcessor
	 */
	public static String[] findTestClassesString(String[] srcFolder, String[] classpath){
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestClassesString();
	}




	public static String[] findTestCasesString(String[] srcFolder, String[] classpath){
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestCasesString();
	}



	public static CtClass<?>[] findTestClasses(String[] srcFolder, String[] classpath){
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestClasses();
	}





	public static CtMethod<?>[] findTestCases(String[] srcFolder, String[] classpath){
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestCases();
	}


	public static String[] getCurrentVMClassPath(String[] filter){
		ArrayList<String> cp = new ArrayList<String>();

		// Add JUnit for currently running CP
		for(String cpadd : System.getProperty("java.class.path").split(":")){
			boolean add = false;

			if(filter == null){
				add = true;
			}else{
				for(String fel : filter){
					if(cpadd.contains(fel)){
						add = true;
					}
				}
			}

			if(add){
				if(!cpadd.startsWith(File.separator)){
					cpadd = System.getProperty("user.dir") + File.separator + cpadd;
				}

				cp.add(cpadd);
			}
		}

		return cp.toArray(new String[0]);
	}

	public static String[] getCurrentVMClassPath(){
		return getCurrentVMClassPath(null);
	}

	public static TestsExecutionIfos runTestCases(String projectIn, String[] classpath, String[] testClasses, TestsExecutionListener tel, String alternativeJre) throws IOException, TestingException{
		return runTestCases(projectIn, classpath, testClasses, -1, tel, alternativeJre);
	}
	
	public static TestsExecutionIfos runTestCases(String projectIn, String[] classpath, String[] testClasses, int timeout, TestsExecutionListener tel, String alternativeJre) throws IOException, TestingException{
		try{
			return runTestCases(projectIn, classpath, testClasses, timeout, tel, alternativeJre, false);
		}catch(MutantHangsException ex){
			// Never occurs
			return null;
		}
	}
	
	public static TestsExecutionIfos runTestCases(String projectIn, String[] classpath, String[] testClasses, int timeout, TestsExecutionListener tel, String alternativeJre, boolean skipHanging) throws IOException, TestingException, MutantHangsException{
		logger.trace("Running project files in %s", projectIn);
		Set<String> tests = new HashSet<>();
		Set<String> failing = new HashSet<>();
		Set<String> ignored = new HashSet<>();
		Set<String> infloops = new HashSet<>();
		Set<String> errorts = new HashSet<>();
		Map<String, String[]> allEntering = new HashMap<>();

		long t1 = System.currentTimeMillis();
		int cpt = 0;

		boolean tweaking_timeout;

		if(timeout == 0){
			tweaking_timeout = true;
			timeout = MIN_TEST_TIMEOUT;
		}else{
			tweaking_timeout = false;
		}
		
		if(tel != null)		tel.currentTimeout(timeout);

		for(String aTest : testClasses){
			cpt++;
			boolean testcase_finished = false;
			ArrayList<String> hangingTests = new ArrayList<String>();

			while(!testcase_finished){
				ExecutorService executor = Executors.newFixedThreadPool(2);
				String currentTestCase = null;
				List<String> enteringMethods = null;

				String[] cmd = buildExecutionPathWithInstrumentationOptions(alternativeJre, TestExecutor.class, aTest, classpath, hangingTests.toArray(new String[0]));
				
				String s = "";
				for(String c : cmd)
					s += c+" ";
				s = s.trim();

				if(tel != null)		tel.testSuiteExecutionStart(cpt, testClasses.length, s);

				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(new File(projectIn));
				logger.debug("Running tests with command [%s] in working directory %s", s, projectIn);
				pb.redirectErrorStream(true);
				Process proc = pb.start();

				final InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				final BufferedReader br = new BufferedReader(isr);

				String line=null;

				// This part allows to interrupt infinite loops - if infinite loops occurs, all the testclass is considered as failing
				Callable<String> readTask = new Callable<String>() {
					@Override
					public String call() throws Exception {
						return br.readLine();
					}
				};

				Future<String> future = executor.submit(readTask);

				boolean thisisfirstline = true;

				try{
					while ((line = future.get(timeout, TimeUnit.SECONDS)) != null){
						//System.out.println(line);
						if(thisisfirstline && (line.startsWith("Exception") || line.startsWith("Error")) ){
							testcase_finished = true;
							if(line.contains("ExceptionInInitializerError")){
								errorts.add(aTest);
								if(tel != null)		tel.testSuiteUnrunnable(cpt, aTest, line);
							}else{
								if(tel != null)		tel.testCaseException(cpt, line, cmd);
								throw new TestingException(String.format("Testing reported exception message: %s", line));
							}
							break;
						}

						thisisfirstline = false;

						if(line.startsWith(TestExecutor.FAIL_MARKER)){
							line = line.substring(TestExecutor.FAIL_MARKER.length());
							if(addIfPermited(line, testClasses, failing)){
								if(tel != null)		tel.testCaseNewFail(cpt, line);
							}else{
								if(tel != null)		tel.testCaseNotPermitted(cpt, line);
							}
						}if(line.startsWith(TestExecutor.FAILDETAILS_MARKER)){
							line = line.substring(TestExecutor.FAILDETAILS_MARKER.length());
							if(tel != null)		tel.testCaseFailureInfos(cpt, line);
						}else if(line.startsWith(TestExecutor.IGNORE_MARKER)){
							line = line.substring(TestExecutor.IGNORE_MARKER.length());
							if(addIfPermited(line, testClasses, ignored)){
								if(tel != null)		tel.testCaseNewIgnored(cpt, line);
							}else{
								if(tel != null)		tel.testCaseNotPermitted(cpt, line);
							}
						}else if(line.startsWith(TestExecutor.TEST_MARKER)){
							// Save all previous entered methods
							if(currentTestCase != null && enteringMethods != null){
								allEntering.put(currentTestCase, enteringMethods.toArray(new String[enteringMethods.size()]));
							}
							
							// Here all success and failing tests
							line = line.substring(TestExecutor.TEST_MARKER.length());
							currentTestCase =  line;
							enteringMethods = new ArrayList<String>();
							if(addIfPermited(line, testClasses, tests)){
								if(tel != null)		tel.testCaseEntered(cpt, line);
							}else{
								if(tel != null)		tel.testCaseNotPermitted(cpt, line);
							}
							
						}else if(line.startsWith(TestExecutor.UNDETERMINED_MARKER)){
							// Here all success and failing tests
							line = line.substring(TestExecutor.UNDETERMINED_MARKER.length());
							if(tel != null)		tel.testCaseUndeterminedTest(cpt, line);
						}else if(line.startsWith(TestingInstrumentedCodeHelper.STARTKEY)){
							// This is method entry information
							line = line.substring(TestingInstrumentedCodeHelper.STARTKEY.length());
							if(currentTestCase != null && !line.equals(currentTestCase)){
								enteringMethods.add(line);
								if(tel != null)		tel.testCaseEnteringMethod(currentTestCase, line);
							}
						}else if(line.startsWith(TestingInstrumentedCodeHelper.ENDKEY) || 
								line.startsWith(TestingInstrumentedCodeHelper.RETURNKEY) ||
								line.startsWith(TestingInstrumentedCodeHelper.THROWKEY)){
							
							String way = "?";
							
							if(line.startsWith(TestingInstrumentedCodeHelper.ENDKEY)){
								line = line.substring(TestingInstrumentedCodeHelper.ENDKEY.length());
								way = "end";
							}else if(line.startsWith(TestingInstrumentedCodeHelper.RETURNKEY)){
								line = line.substring(TestingInstrumentedCodeHelper.RETURNKEY.length());
								way = "return";
							}else if(line.startsWith(TestingInstrumentedCodeHelper.THROWKEY)){
								line = line.substring(TestingInstrumentedCodeHelper.THROWKEY.length());
								way = "exception";
							}
							
							if(!line.equals(currentTestCase)){
								if(tel != null)		tel.testCaseLeavingMethod(currentTestCase, line, way);
							}
						}else{
							if(tel != null)		tel.testCaseOtherCase(cpt, line);
						}

						future = executor.submit(readTask);
					}
					
					// Save last entered methods
					if(currentTestCase != null && enteringMethods != null){
						allEntering.put(currentTestCase, enteringMethods.toArray(new String[enteringMethods.size()]));
					}

					testcase_finished = true;
				}catch(TimeoutException e){
					if(skipHanging)
						throw new MutantHangsException();
					if(tweaking_timeout && timeout < MAX_TEST_TIMEOUT){
						timeout += INC_TEST_TIMEOUT;
						if(tel != null)		tel.newTimeout(timeout);
					}else{
						try{
							String shortcurrent = currentTestCase.substring(currentTestCase.lastIndexOf('.') + 1);
							hangingTests.add(shortcurrent);

							if(addIfPermited(currentTestCase, testClasses, infloops)){
								if(tel != null)		tel.testCaseNewLoop(cpt, line);
							}else{
								if(tel != null)		tel.testCaseNotPermitted(cpt, line);
							}

							future.cancel(true);
							executor.shutdownNow();
							proc.destroy();
						}catch(Exception ex){
							if(tel != null)		tel.testCaseException(cpt, line, cmd);
							ex.printStackTrace();
							testcase_finished = true;
							throw new TestingException(e);
						}
					}
				}catch(Exception e){
					if(tel != null)		tel.testCaseException(cpt, line, cmd);
					testcase_finished = true;
					throw new TestingException(e);
				}finally{
					is.close();
					future.cancel(true);
					executor.shutdownNow();
					proc.destroy();
				}

			}
		}

		long t2 = System.currentTimeMillis();

		String[] failing_arr = failing.toArray(new String[0]);
		String[] ignored_arr = ignored.toArray(new String[0]);
		String[] hanging_arr = infloops.toArray(new String[0]);
		String[] tests_arr = tests.toArray(new String[0]);
		String[] errts_arr = errorts.toArray(new String[0]);

		TestsExecutionIfos tei = new TestsExecutionIfos();
		tei.setFailingTestCases(failing_arr);
		tei.setIgnoredTestCases(ignored_arr);
		tei.setHangingTestCases(hanging_arr);
		tei.setErrorOnTestSuite(errts_arr);
		tei.setAllRunnedTests(tests_arr);
		tei.setCalledNodeInformation(allEntering);
		tei.setRunTestsTime(t2 - t1);

		if(tweaking_timeout){
			tei.setTestTimeOut(timeout);
		}
		
		if(tel != null){
			tel.testCaseExecutionFinished(cpt, 
					tests_arr,
					failing_arr,
					ignored_arr,
					hanging_arr);
		}
		
		return tei;
	}
	
	public static String[] whichMethodsDependsOnAnother(String projectIn, String[] classpath, String[] testClasses, int timeout, String alternativeJre, boolean skipHanging, final String lookingFor) throws IOException, TestingException, MutantHangsException{
		
		//= "org.apache.commons.math.ode.events.EventState.evaluateStep(org.apache.commons.math.ode.sampling.StepInterpolator)";
		final Set<String> candidates = new HashSet<>();
		
		TestsExecutionListener reader = new TestsExecutionListener() {
			LinkedList<String> elements = new LinkedList<String>();
			
			@Override
			public void testSuiteUnrunnable(int cpt, String aTest, String line) {
			}
			
			@Override
			public void testSuiteExecutionStart(int nbtest, int length, String cmd) {
			}
			
			@Override
			public void testCaseUndeterminedTest(int cpt, String line) {
			}
			
			@Override
			public void testCaseOtherCase(int cpt, String line) {
			}
			
			@Override
			public void testCaseNotPermitted(int cpt, String line) {
			}
			
			@Override
			public void testCaseNewLoop(int cpt, String line) {
			}
			
			@Override
			public void testCaseNewIgnored(int cpt, String line) {
			}
			
			@Override
			public void testCaseNewFail(int cpt, String line) {
			}
			
			@Override
			public void testCaseLeavingMethod(String currentTestCase, String leftMethod, String way) {
				elements.pop();
			}
			
			@Override
			public void testCaseFailureInfos(int cpt, String line) {
			}
			
			@Override
			public void testCaseExecutionFinished(int cpt, String[] all, String[] fail, String[] ignored, String[] hang) {
			}
			
			@Override
			public void testCaseException(int nbtest, String readLine, String[] executedCommand) {
			}
			
			@Override
			public void testCaseEnteringMethod(String currentTestCase, String enteredMethod) {
				elements.push(enteredMethod);
				
				if(enteredMethod.equals(lookingFor)){
					for(String e : elements){
						candidates.add(e);
					}
				}
			}
			
			@Override
			public void testCaseEntered(int cpt, String line) {
				elements = new LinkedList<String>();
			}
			
			@Override
			public void newTimeout(int timeout) {
			}
			
			@Override
			public void currentTimeout(int timeout) {
			}
		};
		
		Testing.runTestCases(projectIn, classpath, testClasses, timeout, reader, alternativeJre, skipHanging);
		
		return candidates.toArray(new String[candidates.size()]);
	}
	
	public static String[] whichClassDependsOnMethod(String projectIn, String[] classpath, String[] testClasses, int timeout, String alternativeJre, boolean skipHanging, final String lookingFor) throws IOException, TestingException, MutantHangsException{
		Set<String> classes = new HashSet();
		
		for(String s : whichMethodsDependsOnAnother(projectIn, classpath, testClasses, timeout, alternativeJre, skipHanging, lookingFor)){
			System.out.println(s);
			if(s.contains("$")){
				classes.add(s.substring(0, s.lastIndexOf('$')));
			}else{
				String ss = s.substring(0, s.lastIndexOf('('));
				ss = ss.substring(0, ss.lastIndexOf("."));
				classes.add(ss);
			}
		}
		
		return classes.toArray(new String[classes.size()]);
	}
	
	private static String[] buildExecutionPath(String jrebin, Class<?> classToRun, String testClassToRun, String[] classpath, String... testcasesToIgnores){
		List<String> cmd = new ArrayList<String>();
		if(jrebin != null){
			cmd.add(String.format("%s%cjava", jrebin, File.separatorChar));
		}else{
			cmd.add("java");
		}
				
		cmd.add("-cp");
		
		String cpp = "";
		for(String s : classpath){
			cpp += ((cpp.length()>0)?File.pathSeparator:"")+s;
		}
		
		cmd.add(cpp);

		cmd.add(classToRun.getCanonicalName());

		cmd.add(testClassToRun);

		for(String ignore : testcasesToIgnores){
			cmd.add(ignore);
		}

		return cmd.toArray(new String[0]);
	}
	
	private static String[] buildExecutionPathWithInstrumentationOptions(String jrebin, Class<?> classToRun, String testClassToRun, String[] classpath, String... testcasesToIgnores){
		List<String> ret = new ArrayList<>();
		
		for(String a : testcasesToIgnores){
			ret.add(a);
		}
		
		ret.add(TestExecutor.INSTRU_OPT+"enter="+(TestingInstrumentedCodeHelper.isEnteringPrinting()?"yes":"no"));
		ret.add(TestExecutor.INSTRU_OPT+"exit="+(TestingInstrumentedCodeHelper.isLeavingPrinting()?"yes":"no"));
		
		return buildExecutionPath(jrebin, classToRun, testClassToRun, classpath, ret.toArray(new String[0]));
	}
	
	private static String[] buildExecutionPathWithInstrumentationOptions(Class<?> classToRun, String testClassToRun, String[] classpath, String... testcasesToIgnores){
		return buildExecutionPathWithInstrumentationOptions(null, classToRun, testClassToRun, classpath, testcasesToIgnores);
	}

	private static boolean addIfPermited(String line, String[] testclasses, Set<String> list) {
		String right = line.substring(0, line.lastIndexOf('.'));

		for(String tc : testclasses){
			if(tc.equals(right)){
				list.add(line);
				return true;
			}
		}

		return false;
	}

}
