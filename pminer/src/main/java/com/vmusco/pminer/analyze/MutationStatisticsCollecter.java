package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.utils.Tools;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationStatisticsCollecter extends MutantTestAnalyzer {

	private PRFStatistics prf = new PRFStatistics();
	private SOUDStatistics soud = new SOUDStatistics();
	private MutantTestProcessingListener<MutationStatisticsCollecter> mtpl = null;

	// Last values retainer
	private String lastMutantId = null;
	private ProcessStatistics lastProcessStatistics = null;
	private MutantIfos lastMutantIfos = null;
	private String[] lastGraphDetermined = null;
	private List<Double> times = new ArrayList<Double>();
	private boolean lastUnbounded = false;
	private boolean lastIsolated = false;
	
	public MutationStatisticsCollecter(MutantTestProcessingListener<MutationStatisticsCollecter> mtpl) {
		this.mtpl  = mtpl;
	}


	@Override
	public void fireExecutionStarting() {

	}

	public void declareNewTime(long propatime){
		times.add(propatime * 1d);
	}
	
	@Override
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests) throws MutationNotRunException {
		// To declare an unbounded case, pass impactedNodes = null and impactedTests = null
		// To declare an isolated case, pass impactedNodes = null and impactedTests = []
		String[] cis = impactedTests;
		
		lastMutantId = mi.getId();
		lastProcessStatistics = ps;
		lastMutantIfos = mi;
		lastGraphDetermined = cis;
		lastUnbounded = impactedNodes == null && impactedTests == null;
		lastIsolated  = impactedNodes == null && impactedTests != null && impactedTests.length == 0;
		
		String[] ais = ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());

		if(lastUnbounded){
			soud.addUnbounded(mi.getId());
		}else if(lastIsolated){
			soud.addIsolated(mi.getId());
		}
		
		soud.cumulate(mi.getId(), ais, cis);
		prf.cumulate(ais, cis);

		if(mtpl != null)
			mtpl.aMutantHasBeenProceeded(this);
	}

	@Override
	public void fireExecutionEnded() {

	}

	public PRFStatistics getPrecisionRecallFscore() {
		return prf;
	}

	public SOUDStatistics getSoud() {
		return soud;
	}
	

	public String[] getLastGraphDetermined() {
		return lastGraphDetermined;
	}

	public String getLastMutantId() {
		return lastMutantId;
	}

	public MutantIfos getLastMutantIfos() {
		return lastMutantIfos;
	}

	public boolean isLastUnbounded(){
		return lastUnbounded;
	}
	
	public boolean isLastIsolated() {
		return lastIsolated;
	}
	
	public ProcessStatistics getLastProcessStatistics() {
		return lastProcessStatistics;
	}

	public double getMedianTimes(){
		return Tools.median(Tools.toDoubleArray(times));
	}

	public double getMeanTimes(){
		return Tools.average(Tools.toDoubleArray(times));
	}
	
	public double getLastTime(){
		return times.get(times.size() - 1);
	}
}
