package com.vmusco.softminer.graphs;

import java.util.Stack;

import com.vmusco.smf.instrumentation.MethodInInstrumentationProcessor;
import com.vmusco.smf.testing.TestingInstrumentedCodeHelper;
import com.vmusco.smf.testing.TestsExecutionListener;

public class DynamicCallGraphGenerator implements TestsExecutionListener {
	private MethodInInstrumentationProcessor instr = new MethodInInstrumentationProcessor(); 
	private Graph aGraph = new GraphStream();
	private Stack<String> whereAmI = new Stack<>();
	
	@Override
	public void testSuiteExecutionStart(int nbtest, int length, String cmd) {
	}

	@Override
	public void testCaseException(int nbtest, String readLine, String[] executedCommand) {
	}

	@Override
	public void testCaseExecutionFinished(int cpt, String[] all, String[] fail, String[] ignored, String[] hang) {
	}

	@Override
	public void testCaseNewFail(int cpt, String line) {
	}

	@Override
	public void testCaseNotPermitted(int cpt, String line) {
	}

	@Override
	public void testCaseNewIgnored(int cpt, String line) {
	}

	@Override
	public void testCaseEntered(int cpt, String line) {
	}

	@Override
	public void testCaseUndeterminedTest(int cpt, String line) {
	}

	@Override
	public void testCaseOtherCase(int cpt, String line) {
	}

	@Override
	public void testCaseNewLoop(int cpt, String line) {
	}

	@Override
	public void testCaseFailureInfos(int cpt, String line) {
	}

	@Override
	public void testSuiteUnrunnable(int cpt, String aTest, String line) {
	}

	@Override
	public void currentTimeout(int timeout) {
	}

	@Override
	public void newTimeout(int timeout) {
	}

	public Graph getGraph() {
		return aGraph;
	}

	@Override
	public void testCaseEnteringMethod(String currentTestCase, String enteredMethod) {
		if(whereAmI.size() > 0){
			aGraph.addDirectedEdgeAndNodeIfNeeded(whereAmI.lastElement(), enteredMethod);
		}
		whereAmI.push(enteredMethod);
	}

	@Override
	public void testCaseLeavingMethod(String currentTestCase, String leftMethod, String way) {
		whereAmI.pop();
		
		/*if(way.equals("exception")){
			System.out.println("< Leaving (exception throwing): "+l);
		}else if(way.equals("return")){
			System.out.println("< Leaving (return): "+l);
		}else{
			System.out.println("< Leaving: "+l);
		}*/
	}
}
