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
 * Used in {@link Testing#runTestCases(com.vmusco.smf.analysis.ProcessStatistics, com.vmusco.smf.analysis.MutationStatistics, String, TestsExecutionListener)}
 * @see Testing#runTestCases(com.vmusco.smf.analysis.ProcessStatistics, com.vmusco.smf.analysis.MutationStatistics, String, TestsExecutionListener)
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class TestExecutor {
	public static final String TEST_MARKER = "(vmdtest)";
	public static final String FAIL_MARKER = "(vmdfails)";
	public static final String FAILDETAILS_MARKER = "(+++vmdfails+++)";
	public static final String IGNORE_MARKER = "(vmdignores)";
	public static final String STATS_MARKER = "(vmdstats)";
	public static final String UNDETERMINED_MARKER = "(vmd???)";

	/**
	 * Receives ONE test case a time. Other parameters are intended for test skipping (on case of hanging !)
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String[] s = args;

		Class<?> c = Class.forName(s[0]);
		final ArrayList<String> skip = new ArrayList<>();

		int i = 1;
		while(i<s.length){
			skip.add(s[i]);
			System.out.println("SKIP:"+s[i]);
			i++;
		}

		Filter f = new Filter() {
			@Override
			public boolean shouldRun(Description description) {
				String name = description.getDisplayName();
				
				// For Parametric Testing (to be continued...)
				//TODO: See what happened with parametric tests... (i.e. [...])
				/*if(name.charAt(0) == '[')
					return true;*/
				
				int pos = name.lastIndexOf('(');
				if(pos <= 0){
					System.out.println(UNDETERMINED_MARKER+name);
					return false;
				}
				
				name = name.substring(0, pos);

				if(skip.contains(name))
					return false;
				
				return true;
			}

			@Override
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

			@Override
			public void testStarted(Description description) throws Exception {
				System.out.println(TEST_MARKER + testname(description.getDisplayName()));
			}

			public void testFinished(Description description) throws Exception{
				//System.out.println("> Finished: "+testname(description.getDisplayName()));
			}

			@Override
			public void testFailure(Failure failure) throws Exception {
				System.out.println(FAIL_MARKER+testname(failure.getTestHeader()));
				for(String l : failure.getTrace().split("\n")){
					System.out.println(FAILDETAILS_MARKER+l);
				}
			}

			@Override
			public void testIgnored(Description description) throws Exception {
				System.out.println(IGNORE_MARKER+testname(description.getDisplayName()));
			}
		});

		Result result = juc.run(r2);

		//Iterator<Failure> iterator = result.getFailures().iterator();
		System.out.println(STATS_MARKER+result.getRunCount()+"#"+result.getFailureCount()+"#"+result.getIgnoreCount());

		System.exit(0);
	}
}
