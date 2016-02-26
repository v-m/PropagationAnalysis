package com.vmusco.smf.testing;

import java.util.ArrayList;

import org.junit.internal.requests.ClassRequest;
import org.junit.internal.requests.FilterRequest;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/**
 * This entry point is used to do test cases executions.
 * Used in {@link Testing#runTestCases(String, String[], String[], int, TestsExecutionListener)}
 * @see Testing#Testing#runTestCases(String, String[], String[], int, TestsExecutionListener)
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class TestExecutor {
	public static final String TEST_MARKER = "(vmdtest)";
	public static final String FAIL_MARKER = "(vmdfails)";
	public static final String FAILDETAILS_MARKER = "(+++vmdfails+++)";
	public static final String IGNORE_MARKER = "(vmdignores)";
	public static final String STATS_MARKER = "(vmdstats)";
	public static final String UNDETERMINED_MARKER = "(vmd???)";
	public static final String INSTRU_OPT = "-_instrument-option:";

	
	
	//private static PrintStream file, defile; 
	
	
	
	/**
	 * Receives ONE test case a time. Other parameters are intended for test skipping (on case of hanging !)
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String[] s = args;

		Class c = Class.forName(s[0]);
		final ArrayList skip = new ArrayList();

		int i = 1;
		while(i<s.length){
			if(s[i].startsWith(INSTRU_OPT)){
				String io_analyze = s[i].substring(INSTRU_OPT.length());
				String[] io_tok = io_analyze.split("=");
				
				boolean isYes = io_tok[1].equals("yes");
				
				if(io_tok[0].equals("enter")){
					TestingInstrumentedCodeHelper.setEnteringPrinting(isYes);
				}else if(io_tok[0].equals("exit")){
					TestingInstrumentedCodeHelper.setLeavingPrinting(isYes);
				}else{
					printDefault("Unknown instrumentation option: "+io_tok[0]);
				}
			}else{
				skip.add(s[i]);
				printDefault("SKIP:"+s[i]);
			}
			i++;
		}

		Filter f = new Filter() {
			public boolean shouldRun(Description description) {
				String name = description.getDisplayName();
				
				// For Parametric Testing (to be continued...)
				//TODO: See what happened with parametric tests... (i.e. [...])
				/*if(name.charAt(0) == '[')
					return true;*/
				
				int pos = name.lastIndexOf('(');
				if(pos <= 0){
					printDefault(UNDETERMINED_MARKER+name);
					return false;
				}
				
				name = name.substring(0, pos);

				if(skip.contains(name+"()"))
					return false;
				
				return true;
			}

			public String describe() {
				return "Hello I'm the hang skipping filter :)";
			}
		};

		JUnitCore juc = new JUnitCore();
		Request r1 = new ClassRequest(c);
		Request r2 = new FilterRequest(r1, f);

		juc.addListener(new RunListener(){
			private String testname(String description){
				int pos = description.lastIndexOf('(');
				String right = description.substring(0, pos);
				String left = description.substring(pos+1, description.length()-1);

				return left+"."+right+"()";
			}

			public void testStarted(Description description) throws Exception {
				String line = TEST_MARKER + testname(description.getDisplayName());
				printDefault(line);
				System.out.println(line);
				TestingInstrumentedCodeHelper.resetMemory();
			}

			public void testFinished(Description description) throws Exception{
				//printDefault("> Finished: "+testname(description.getDisplayName()));
			}

			public void testFailure(Failure failure) throws Exception {
				printDefault(FAIL_MARKER+testname(failure.getTestHeader()));
				String[] ll = failure.getTrace().split("\n");
				for(int i = 0; i<ll.length; i++){
					String l = ll[i];
					printDefault(FAILDETAILS_MARKER+l);
				}
			}

			public void testIgnored(Description description) throws Exception {
				printDefault(IGNORE_MARKER+testname(description.getDisplayName()));
			}
		});

		Result result = juc.run(r2);

		//Iterator<Failure> iterator = result.getFailures().iterator();
		printDefault(STATS_MARKER+result.getRunCount()+"#"+result.getFailureCount()+"#"+result.getIgnoreCount());

		System.exit(0);
	}
	
	private static void printDefault(String s){
		//System.setOut(defile);
		System.out.println(s);
		//System.setOut(file);
	}
}