package com.vmusco.smf.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

import com.vmusco.smf.analysis.TestsExecutionIfos;

/**
 * Tests execution logic
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public final class Testing {
	public static ArrayList<CtClass<?>> temp;

	/**
	 * The time after which the test fails (in seconds)
	 */
	public static final int MIN_TEST_TIMEOUT = 10;
	public static final int MAX_TEST_TIMEOUT = 60;
	public static final int INC_TEST_TIMEOUT = 10;

	private Testing() {
	}

	private static void executeTestDetection(String[] srcFolder, String[] classpath){
		Factory factory = new FactoryImpl(new DefaultCoreFactory(), new StandardEnvironment());
		SpoonCompiler compiler = new JDTBasedSpoonCompiler(factory);

		for(String src : srcFolder){
			compiler.addInputSource(new File(src));
		}

		//Updating classpath
		if(classpath != null)
			compiler.setSourceClasspath(classpath);

		// Build (in memory)
		compiler.build();

		// Obtain list of element to mutate
		List<String> arg0 = new ArrayList<String>();
		arg0.add(TestCasesProcessor.class.getName());
		compiler.process(arg0);

		/*System.out.println(TestCasesProcessor.getNbFromAnnotations()+" test classes based on Annotation (Junit 4)");
		System.out.println(TestCasesProcessor.getNbFromTestCases()+" test classes based on inheritance (Junit 3)");
		System.out.println();*/
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

	public static TestsExecutionIfos runTestCases(String projectIn, String[] classpath, String[] testClasses, TestsExecutionListener tel) throws IOException{
		return runTestCases(projectIn, classpath, testClasses, -1, tel);
	}
	
	public static TestsExecutionIfos runTestCases(String projectIn, String[] classpath, String[] testClasses, int timeout, TestsExecutionListener tel) throws IOException{
		Set<String> tests = new HashSet<>();
		Set<String> failing = new HashSet<>();
		Set<String> ignored = new HashSet<>();
		Set<String> infloops = new HashSet<>();
		Set<String> errorts = new HashSet<>();
		boolean run_exception_skipped = false;

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

				String[] cmd = buildExecutionPath(TestExecutor.class, aTest, classpath, hangingTests.toArray(new String[0]));

				String s = "";
				for(String c : cmd)
					s += c+" ";
				s = s.trim();

				if(tel != null)		tel.testSuiteExecutionStart(cpt, testClasses.length, s);

				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(new File(projectIn));
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
					while (!run_exception_skipped && (line = future.get(timeout, TimeUnit.SECONDS)) != null){

						if(thisisfirstline && (line.startsWith("Exception") || line.startsWith("Error")) ){
							testcase_finished = true;
							if(line.contains("ExceptionInInitializerError")){
								errorts.add(aTest);
								if(tel != null)		tel.testSuiteUnrunnable(cpt, aTest, line);
							}else{
								if(tel != null)		tel.testCaseException(cpt, line, cmd);
								run_exception_skipped = true;
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
							// Here all success and failing tests
							line = line.substring(TestExecutor.TEST_MARKER.length());
							currentTestCase =  line;
							if(addIfPermited(line, testClasses, tests)){
								if(tel != null)		tel.testCaseEntered(cpt, line);
							}else{
								if(tel != null)		tel.testCaseNotPermitted(cpt, line);
							}
						}else if(line.startsWith(TestExecutor.UNDETERMINED_MARKER)){
							// Here all success and failing tests
							line = line.substring(TestExecutor.UNDETERMINED_MARKER.length());
							//System.out.println("\tDROPPED (undt): "+line);
							if(tel != null)		tel.testCaseUndeterminedTest(cpt, line);
						}else{
							//System.out.println("((((( "+line+" )))))");
							if(tel != null)		tel.testCaseOtherCase(cpt, line);
						}

						future = executor.submit(readTask);
					}

					testcase_finished = true;
				}catch(TimeoutException e){
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
							run_exception_skipped = true;
							testcase_finished = true;
						}
					}
				}catch(Exception e){
					if(tel != null)		tel.testCaseException(cpt, line, cmd);
					run_exception_skipped = true;
					testcase_finished = true;
				}finally{
					is.close();
					future.cancel(true);
					executor.shutdownNow();
					proc.destroy();
				}

			}
		}

		if(run_exception_skipped)
			return null;


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
	
	
	private static String[] buildExecutionPath(Class<?> classToRun, String testClassToRun, String[] classpath, String... testcasesToIgnores){
		List<String> cmd = new ArrayList<String>();
		cmd.add("java");
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
