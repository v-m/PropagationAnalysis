package com.vmusco.softminer.graphs;

import java.util.Stack;

import com.vmusco.smf.instrumentation.EntryMethodInstrumentationProcessor;
import com.vmusco.smf.testing.TestingInstrumentedCodeHelper;
import com.vmusco.smf.testing.TestsExecutionListener;

public class DynamicCallGraphGenerator implements TestsExecutionListener {
	private EntryMethodInstrumentationProcessor instr = new EntryMethodInstrumentationProcessor(); 
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
		String l;
		
		if((l = instr.getLineIfInstrumented(line)) != null){
			if(line.startsWith(TestingInstrumentedCodeHelper.STARTKEY)){
				if(whereAmI.size() > 0){
					aGraph.addDirectedEdgeAndNodeIfNeeded(whereAmI.lastElement(), l);
				}
				whereAmI.push(l);
				//System.out.println("> Entering: "+l);
			}else if(line.startsWith(TestingInstrumentedCodeHelper.ENDKEY)){
				whereAmI.pop();
				//System.out.println("< Leaving: "+l);
			}else if(line.startsWith(TestingInstrumentedCodeHelper.THROWKEY)){
				whereAmI.pop();
				//System.out.println("< Leaving (exception throwing): "+l);
			}else if(line.startsWith(TestingInstrumentedCodeHelper.RETURNKEY)){
				whereAmI.pop();
				//System.out.println("< Leaving (return): "+l);
			}
		}
		
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
}
