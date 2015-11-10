package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.analysis.MutantIfos;
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
	
	/**
	 * To declare an unbounded case, use {@link MutationStatisticsCollecter#fireUnboundedFound(MutantIfos)}
	 * To declare an isolated case, use {@link MutationStatisticsCollecter#fireIsolatedFound(MutantIfos)}
	 * 
	 * 
	 * To declare an unbounded case, pass impactedNodes = null and impactedTests = null
	 * To declare an isolated case, pass impactedNodes = null and impactedTests = []
	 */
	@Override
	public void fireIntersectionFound(MutantIfos mi, String[] ais, String[] cis) {
		lastMutantIfos = mi;
		lastGraphDetermined = cis;
		lastUnbounded = false;
		lastIsolated  = false;
		
		soud.cumulate(mi.getId(), ais, cis);
		prf.cumulate(ais, cis);

		if(mtpl != null)
			mtpl.aMutantHasBeenProceeded(this);
	}
	
	@Override
	public void fireIsolatedFound(MutantIfos mi) {
		lastMutantIfos = mi;
		soud.addIsolated(mi.getId());
		lastUnbounded = false;
		lastIsolated = true;
	}
	
	@Override
	public void fireUnboundedFound(MutantIfos mi) {
		lastMutantIfos = mi;
		soud.addUnbounded(mi.getId());
		lastUnbounded = true;
		lastIsolated = false;
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
		return lastMutantIfos.getId();
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
