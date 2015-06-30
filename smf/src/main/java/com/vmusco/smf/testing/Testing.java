package com.vmusco.smf.testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.ConsoleTools;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

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

	private static void executeTestDetection(String[] srcFolder, String[] classpath) throws Exception{
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

		System.out.println(TestCasesProcessor.getNbFromAnnotations()+" test classes based on Annotation (Junit 4)");
		System.out.println(TestCasesProcessor.getNbFromTestCases()+" test classes based on inheritance (Junit 3)");
		System.out.println();
	}



	public static void findTestClassesString(ProcessStatistics ps) throws Exception{
		String[] re = new String[ps.srcTestsToTreat.length];
		int i = 0;

		for(String s : ps.srcTestsToTreat){
			re[i++] = ps.getProjectIn(true) + File.separator + s; 
		}

		ps.testClasses = findTestClassesString(re, ps.getTestingClasspath());
	}

	public static String[] findTestClassesString(String[] srcFolder, String[] classpath) throws Exception{
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestClassesString();
	}




	public static String[] findTestCasesString(String[] srcFolder, String[] classpath) throws Exception{
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestCasesString();
	}



	public static CtClass<?>[] findTestClasses(String[] srcFolder, String[] classpath) throws Exception{
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestClasses();
	}





	public static CtMethod<?>[] findTestCases(String[] srcFolder, String[] classpath) throws Exception{
		executeTestDetection(srcFolder, classpath);
		return TestCasesProcessor.getTestCases();
	}





	private static String[] buildExecutionPath(ProcessStatistics ps, Class<?> classToRun, String frontClassPathEntry, String endClassPathEntry, String testClassToRun, String... testcasesToIgnores) throws IOException{
		List<String> cmd = new ArrayList<String>();
		cmd.add("java");
		cmd.add("-cp");

		String cpp = "";

		if(frontClassPathEntry != null){
			cpp += frontClassPathEntry;
		}
		
		/*Enumeration<URL> e = Test.class.getClassLoader().getResources("");
		while (e.hasMoreElements())
		{
			URL nxt = e.nextElement();
			cpp += (cpp.length()==0?"":":")+nxt.getPath();
			System.out.println("+++ "+nxt.getPath());
		}*/

		for(String s : ps.getTestingClasspath()){
			cpp += (cpp.length()==0?"":":")+s;
		}
		cpp += (cpp.length()==0?"":":")+ps.testsGenerationFolder();
		for(String aRess : ps.testingRessources){
			cpp += (cpp.length()==0?"":":")+ps.getProjectIn(true) + File.separator + aRess;
		}

		/*for(String aSrc : ps.srcToCompile){
			cpp += (cpp.length()==0?"":":")+ps.getProjectIn(true) + File.separator + aSrc;
			System.out.println("++++++ "+ps.getProjectIn(true) + File.separator + aSrc);
		}

		for(String aTestSrc : ps.srcTestsToTreat){
			cpp += (cpp.length()==0?"":":")+ps.getProjectIn(true) + File.separator + aTestSrc;
			System.out.println("+++++++ "+ps.getProjectIn(true) + File.separator + aTestSrc);
		}

		cpp += (cpp.length()==0?"":":")+ps.getProjectIn(true);
		System.out.println("++++++++ "+ps.getProjectIn(true));*/

		if(endClassPathEntry != null){
			cpp += (cpp.length()==0?"":":")+endClassPathEntry;
		}
		
		cmd.add(cpp);

		cmd.add(classToRun.getCanonicalName());

		cmd.add(testClassToRun);

		for(String ignore : testcasesToIgnores){
			cmd.add(ignore);
		}

		return cmd.toArray(new String[0]);
	}

	public static void runTestCases(ProcessStatistics ps, TestsExecutionListener tes) throws IOException, ClassNotFoundException, InterruptedException{
		runTestCases(ps, null, null, tes);
	}

	public static void runTestCases(ProcessStatistics ps) throws IOException, ClassNotFoundException, InterruptedException{
		runTestCases(ps, null);
	}

	public static void runTestCases(MutationStatistics<?> ms, String forMutant) throws IOException, ClassNotFoundException, InterruptedException{
		runTestCases(ms, forMutant, null);
	}

	public static void runTestCases(MutationStatistics<?> ms, String forMutant, TestsExecutionListener tes) throws IOException, ClassNotFoundException, InterruptedException{
		runTestCases(ms.ps, ms, forMutant, tes);
	}

	public static void runTestCases(ProcessStatistics ps, MutationStatistics<?> ms, String forMutant, TestsExecutionListener tes) throws IOException, ClassNotFoundException, InterruptedException{
		//MutationStatistics ms = ps.mutations.get(mutationoperatorid);
		Set<String> tests = new HashSet<>();
		Set<String> failing = new HashSet<>();
		Set<String> ignored = new HashSet<>();
		Set<String> infloops = new HashSet<>();
		Set<String> errorts = new HashSet<>();
		boolean run_exception_skipped = false;

		long t1 = System.currentTimeMillis();
		int cpt = 0;
		
		boolean tweaking_timeout = ms==null && ps.testTimeOut == 0;
		
		int timeout = MAX_TEST_TIMEOUT;
		if(tweaking_timeout){
			timeout = MIN_TEST_TIMEOUT;
		}else if(ps.testTimeOut > 0){
			timeout = ps.testTimeOut;
		}

		ConsoleTools.write("Current timeout is: "+timeout);
		ConsoleTools.endLine();
		ConsoleTools.endLine();
		
		for(String aTest : ps.testClasses){
			cpt++;
			boolean testcase_finished = false;
			ArrayList<String> hangingTests = new ArrayList<String>();

			while(!testcase_finished){
				ExecutorService executor = Executors.newFixedThreadPool(2);
				String currentTestCase = null;


				String addToCpForMutant = "";
				String addToCpAtEnd = "";

				if(ms != null && forMutant != null){
					addToCpForMutant = ms.getBytecodeMutationResolved() + File.separator + forMutant;
				}

				// Add JUnit for currently running CP
				for(String cpadd : System.getProperty("java.class.path").split(":")){
					if(cpadd.contains("smf") || cpadd.contains("junit")){
						if(!cpadd.startsWith(File.separator)){
							cpadd = System.getProperty("user.dir") + File.separator + cpadd;
						}
						addToCpAtEnd += ((addToCpAtEnd==null || addToCpAtEnd.length()==0)?"":File.pathSeparator)+cpadd;
					}
				}

				String[] cmd = buildExecutionPath(ps, TestExecutor.class, addToCpForMutant, addToCpAtEnd, aTest, hangingTests.toArray(new String[0]));

				String s = "";
				for(String c : cmd)
					s += c+" ";
				s = s.trim();
				
				if(tes != null)		tes.testSuiteExecutionStart(cpt, ps.testClasses.length, s);
				
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.directory(new File(ps.getProjectIn(true)));
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
								if(tes != null)		tes.testSuiteUnrunnable(cpt, aTest, line);
							}else{
								if(tes != null)		tes.testCaseException(cpt, line, cmd);
								run_exception_skipped = true;
							}
							break;
						}

						thisisfirstline = false;

						if(line.startsWith(TestExecutor.FAIL_MARKER)){
							line = line.substring(TestExecutor.FAIL_MARKER.length());
							if(addIfPermited(line, ps, failing)){
								if(tes != null)		tes.testCaseNewFail(cpt, line);
							}else{
								if(tes != null)		tes.testCaseNotPermitted(cpt, line);
							}
						}if(line.startsWith(TestExecutor.FAILDETAILS_MARKER)){
							line = line.substring(TestExecutor.FAILDETAILS_MARKER.length());
							if(tes != null)		tes.testCaseFailureInfos(cpt, line);
						}else if(line.startsWith(TestExecutor.IGNORE_MARKER)){
							line = line.substring(TestExecutor.IGNORE_MARKER.length());
							if(addIfPermited(line, ps, ignored)){
								if(tes != null)		tes.testCaseNewIgnored(cpt, line);
							}else{
								if(tes != null)		tes.testCaseNotPermitted(cpt, line);
							}
						}else if(line.startsWith(TestExecutor.TEST_MARKER)){
							// Here all success and failing tests
							line = line.substring(TestExecutor.TEST_MARKER.length());
							currentTestCase =  line;
							if(addIfPermited(line, ps, tests)){
								if(tes != null)		tes.testCaseEntered(cpt, line);
							}else{
								if(tes != null)		tes.testCaseNotPermitted(cpt, line);
							}
						}else if(line.startsWith(TestExecutor.UNDETERMINED_MARKER)){
							// Here all success and failing tests
							line = line.substring(TestExecutor.UNDETERMINED_MARKER.length());
							//System.out.println("\tDROPPED (undt): "+line);
							if(tes != null)		tes.testCaseUndeterminedTest(cpt, line);
						}else{
							//System.out.println("((((( "+line+" )))))");
							if(tes != null)		tes.testCaseOtherCase(cpt, line);
						}

						future = executor.submit(readTask);
					}

					testcase_finished = true;
				}catch(TimeoutException e){
					if(tweaking_timeout && timeout < MAX_TEST_TIMEOUT){
						timeout += INC_TEST_TIMEOUT;
						System.out.println("Dynamic timeout tweaking... New timeout is "+timeout+" secs. \n\n\n");
					}else{
						try{
							String shortcurrent = currentTestCase.substring(currentTestCase.lastIndexOf('.') + 1);
							hangingTests.add(shortcurrent);
		
							if(addIfPermited(currentTestCase, ps, infloops)){
								if(tes != null)		tes.testCaseNewLoop(cpt, line);
							}else{
								if(tes != null)		tes.testCaseNotPermitted(cpt, line);
							}
		
							future.cancel(true);
							executor.shutdownNow();
							proc.destroy();
						}catch(Exception ex){
							if(tes != null)		tes.testCaseException(cpt, line, cmd);
							run_exception_skipped = true;
							testcase_finished = true;
						}
					}
				}catch(/*ExecutionException | */Exception e){
					//e.printStackTrace();
					//System.exit(1);
					if(tes != null)		tes.testCaseException(cpt, line, cmd);
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
			return;
		
		if(tweaking_timeout){
			ps.testTimeOut = timeout;
		}

		long t2 = System.currentTimeMillis();

		String[] failing_arr = failing.toArray(new String[0]);
		String[] ignored_arr = ignored.toArray(new String[0]);
		String[] hanging_arr = infloops.toArray(new String[0]);
		String[] tests_arr = tests.toArray(new String[0]);
		String[] errts_arr = errorts.toArray(new String[0]);

		if(forMutant == null){
			ps.failingTestCases = failing_arr;
			ps.ignoredTestCases = ignored_arr;
			ps.hangingTestCases = hanging_arr;
			ps.testCases = tests_arr;
			ps.errorOnTestSuite = errts_arr;
			ps.runTestsOriginalTime = t2 - t1;
		}else{
			MutantIfos ifos = (MutantIfos) ms.mutations.get(forMutant);
			ifos.mutantFailingTestCases = failing_arr;
			ifos.mutantIgnoredTestCases = ignored_arr;
			ifos.mutantHangingTestCases = hanging_arr;
			ifos.mutantErrorOnTestSuite = errts_arr;
			ifos.excutedTests = true;
			ifos.runTestOnMutantTime = t2 - t1;
		}

		if(tes != null){
			tes.testCaseExecutionFinished(cpt, 
					tests_arr,
					failing_arr,
					ignored_arr,
					hanging_arr);
		}
	}

	private static boolean addIfPermited(String line, ProcessStatistics ps, Set<String> list) {
		String right = line.substring(0, line.lastIndexOf('.'));

		for(String tc : ps.testClasses){
			if(tc.equals(right)){
				list.add(line);
				return true;
			}
		}

		return false;
	}

}
