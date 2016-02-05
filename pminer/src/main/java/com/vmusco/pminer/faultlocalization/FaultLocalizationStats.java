package com.vmusco.pminer.faultlocalization;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.SetTools;
import com.vmusco.softminer.graphs.Graph;

/**
 * Class used for containing statistics about fault localization while processing. 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class FaultLocalizationStats {
	static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getFormatterLogger(FaultLocalizationStatsWithMutantIfos.class.getSimpleName());
	private String[] UUTs;
	
	// FOR EXECUTION
	private String currentTestingNode;	
	private String[] totalfailed;		//T_f
	private String[] totalpassed;		//T_p
	private String[] concernedTests; 	//E
	private String[] failed;		 	//E_f
	private String[] passed;			//E_f
	
	private Map<String, String[][]> buffer;
	
	// FOR GRAPH
	private Graph base;
	//private SoftMinerFaultPropagationUnion union;
	private GraphFaultLocalizationByIntersection inter;
	private String[] testcases;
	private Map<String, Set<String>> UUTusedByTests;
	
	//////////////////////
	
	public FaultLocalizationStats(String[] UUTs, String[] testcases) throws MutationNotRunException, BadStateException {
		this(UUTs, testcases, null);
	}
	
	public FaultLocalizationStats(String[] UUTs, String[] testcases, Graph base) throws MutationNotRunException, BadStateException {
		this.UUTs = UUTs;
		this.base = base;
		this.testcases = testcases;
	}
	
	/**
	 * Switch the mutation under observation
	 * @param UUTusedByTests the list of all UUTs and the tests which depends on it
	 * @param fails the list of failing tests
	 * @throws MutationNotRunException
	 */
	public void changeMutantIdentity(Map<String, Set<String>> UUTusedByTests, String[] fails) throws MutationNotRunException{
		// EXECUTION STATS UPDATING
		this.UUTusedByTests = UUTusedByTests;
		this.totalfailed = fails;
		this.totalpassed = SetTools.setDifference(testcases, this.totalfailed);

		if(base != null){
			inter = new GraphFaultLocalizationByIntersection(base);
			inter.visit(fails);
		}
		
		buffer = new HashMap<>();
	}

	public void changeTestingNode(String testingNode) throws BadStateException{
		this.currentTestingNode = testingNode;

		if(!buffer.containsKey(testingNode)){
			this.concernedTests = concernedTests();
			this.failed = failingTests();
			this.passed = passedTests();
			String[][] ret = new String[][]{
					this.concernedTests, this.failed, this.passed
			};
			buffer.put(testingNode, ret);
		}else{
			String[][] ret = buffer.get(testingNode);
			
			this.concernedTests = ret[0];
			this.failed = ret[1];
			this.passed = ret[2];
		}
	}

	public Graph getLastIntersectedGraph(){
		return inter.getLastConcequenceGraph();
	}
	
	public String[] getLastIntersectedNodes(){
		return getLastIntersectedGraph().getNodesNames();
	}

	public int getNbTotalFailed(){
		return totalfailed.length;
	}
	
	
	public int getNbExecutingFailed(){
		return failed.length;
	}
	
	public int getNbNonExecutingFailed(){
		return getNbTotalFailed() - getNbExecutingFailed();
	}
	
	public String[] getAllFailedTests(){
		return totalfailed;
	}
	
	public String[] getExecutingFailedTests(){
		return failed;
	}
	
	public String[] getNonExecutingFailedTests(){
		return SetTools.setDifference(totalfailed, failed);
	}
	
	
	public int getNbTotalPassed(){
		return totalpassed.length;
	}
	
	public int getNbExecutingPassed(){
		return passed.length;
	}
	
	public int getNbNonExecutingPassed(){
		return getNbTotalPassed() - getNbExecutingPassed();
	}
	
	public String[] getAllPassedTests(){
		return totalpassed;
	}
	
	public String[] getExecutingPassedTests(){
		return passed;
	}
	
	public String[] getNonExecutingPassedTests(){
		return SetTools.setDifference(totalpassed, passed);
	}
	
	private String[] failingTests(){
		return SetTools.setIntersection(
				totalfailed,
				this.concernedTests);
	}

	private String[] passedTests(){
		return SetTools.setDifference(concernedTests, this.failed);
	}
	
	private String[] concernedTests() throws BadStateException {
		if(!UUTusedByTests.containsKey(this.currentTestingNode))
			return new String[0];
		
		String[] rarr = new String[UUTusedByTests.get(this.currentTestingNode).size()];
		return UUTusedByTests.get(this.currentTestingNode).toArray(rarr);
	}
	
	public String getCurrentTestingNode(){
		return this.currentTestingNode;
	}
	
	public String[] getUUTs() {
		return UUTs;
	}
	
	public Graph getBaseGraph() {
		return base;
	}
	
	@Override
	public String toString() {
		return String.format("Tp = %d, Tf = %d. Ep = %d, Ef = %d. Np = %d, Nf = %d", 
				getAllPassedTests().length, getAllFailedTests().length,
				getExecutingPassedTests().length, getExecutingFailedTests().length,
				getNonExecutingPassedTests().length, getNonExecutingFailedTests().length);
	}
}
