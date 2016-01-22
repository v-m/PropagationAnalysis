package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vmusco.pminer.utils.Tools;
import com.vmusco.smf.analysis.MutantIfos;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationStatisticsCollecter extends MutantTestAnalyzer {

	private PRFStatistics prf;
	private SOUDStatistics soud;
	private List<MutantTestProcessingListener<MutationStatisticsCollecter>> mtpl;

	// Last values retainer
	private MutantIfos lastMutantIfos;
	private String[] lastGraphDetermined;
	private List<Double> times;
	private boolean lastUnbounded;
	private boolean lastIsolated;
	
	public MutationStatisticsCollecter() {
		clear();
	}
	
	public MutationStatisticsCollecter(MutantTestProcessingListener<MutationStatisticsCollecter> mtpl) {
		this();
		addListener(mtpl);
	}
	
	public MutationStatisticsCollecter(List<MutantTestProcessingListener<MutationStatisticsCollecter>> mtpl) {
		this();
		this.mtpl.addAll(mtpl);
	}
	
	public Iterator<MutantTestProcessingListener<MutationStatisticsCollecter>> listenerIterator(){
		return mtpl.iterator();
	}
	
	public void addListener(MutantTestProcessingListener<MutationStatisticsCollecter> mtpl){
		this.mtpl.add(mtpl);
	}


	@Override
	public void executionStarting() {

	}

	public void declareNewTime(long propatime){
		times.add(propatime * 1d);
	}
	
	/**
	 * To declare an unbounded case, use {@link MutationStatisticsCollecter#unboundedFound(MutantIfos)}
	 * To declare an isolated case, use {@link MutationStatisticsCollecter#isolatedFound(MutantIfos)}
	 * 
	 * 
	 * To declare an unbounded case, pass impactedNodes = null and impactedTests = null
	 * To declare an isolated case, pass impactedNodes = null and impactedTests = []
	 */
	@Override
	public void intersectionFound(MutantIfos mi, String[] ais, String[] cis) {
		lastMutantIfos = mi;
		lastGraphDetermined = cis;
		lastUnbounded = false;
		lastIsolated  = false;
		
		soud.cumulate(mi.getId(), ais, cis);
		prf.cumulate(ais, cis);

		Iterator<MutantTestProcessingListener<MutationStatisticsCollecter>> listenerIterator = listenerIterator();
		
		while(listenerIterator.hasNext()){
			MutantTestProcessingListener<MutationStatisticsCollecter> mtpl = listenerIterator.next();
			mtpl.aMutantHasBeenProceeded(this);
		}
	}
	
	@Override
	public void isolatedFound(MutantIfos mi) {
		lastMutantIfos = mi;
		soud.addIsolated(mi.getId());
		lastUnbounded = false;
		lastIsolated = true;
	}
	
	@Override
	public void unboundedFound(MutantIfos mi) {
		lastMutantIfos = mi;
		soud.addUnbounded(mi.getId());
		lastUnbounded = true;
		lastIsolated = false;
	}
	
	

	@Override
	public void executionEnded() {

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
	
	public void clear(){
		prf = new PRFStatistics();
		soud = new SOUDStatistics();
		mtpl = new ArrayList<>();

		lastMutantIfos = null;
		lastGraphDetermined = null;
		times = new ArrayList<Double>();
		lastUnbounded = false;
		lastIsolated = false;
	}
}
