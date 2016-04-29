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
	private List<MutantTestProcessingListener<MutationStatisticsCollecter>> mtpl = new ArrayList<>();

	// Last values retainer
	private String lastIn;
	private String lastId;
	private String[] lastGraphDetermined;
	private List<Double> times;
	private boolean lastUnbounded;
	private boolean lastIsolated;

	private boolean includeBadCases;
	private boolean foundAtLeastOne = false;

	/**
	 * Exclude by default bad cases
	 * {@link MutationStatisticsCollecter#MutationStatisticsCollecter(boolean)}
	 */
	public MutationStatisticsCollecter(){
		this(false);
	}
	
	/**
	 * @param includeBadCases set to true to include unbounded and isolated into computations, false otherwise
	 */
	public MutationStatisticsCollecter(boolean includeBadCases) {
		clear(includeBadCases);
	}

	public MutationStatisticsCollecter(boolean includeBadCases, MutantTestProcessingListener<MutationStatisticsCollecter> mtpl) {
		this(includeBadCases);
		addListener(mtpl);
	}

	public MutationStatisticsCollecter(boolean includeBadCases, List<MutantTestProcessingListener<MutationStatisticsCollecter>> mtpl) {
		this(includeBadCases);
		this.mtpl.addAll(mtpl);
	}

	public Iterator<MutantTestProcessingListener<MutationStatisticsCollecter>> listenerIterator(){
		return mtpl.iterator();
	}

	public void addListener(MutantTestProcessingListener<MutationStatisticsCollecter> mtpl){
		if(mtpl != null){
			this.mtpl.add(mtpl);
		}
	}


	@Override
	public void executionStarting() {

	}

	public void declareNewTime(long propatime){
		times.add(propatime * 1d);
	}

	/**
	 * To declare an unbounded case, use {@link MutationStatisticsCollecter#unboundedFound(String, String)}
	 * To declare an isolated case, use {@link MutationStatisticsCollecter#isolatedFound(String, String)}
	 * 
	 * 
	 * To declare an unbounded case, pass impactedNodes = null and impactedTests = null
	 * To declare an isolated case, pass impactedNodes = null and impactedTests = []
	 */
	@Override
	public void intersectionFound(String id, String in, String[] ais, String[] cis) {
		foundAtLeastOne = true;
		lastIn = in;
		lastId = id;
		lastGraphDetermined = cis;
		lastUnbounded = false;
		lastIsolated  = false;

		soud.cumulate(id, ais, cis);
		prf.cumulate(ais, cis);

		Iterator<MutantTestProcessingListener<MutationStatisticsCollecter>> listenerIterator = listenerIterator();

		while(listenerIterator.hasNext()){
			MutantTestProcessingListener<MutationStatisticsCollecter> mtpl = listenerIterator.next();
			mtpl.aMutantHasBeenProceeded(this);
		}
	}

	@Override
	public void isolatedFound(String id, String in) {
		lastIn = in;
		lastId = id;
		soud.addIsolated(id);
		lastUnbounded = false;
		lastIsolated = true;
		
		if(includeBadCases){
			prf.cumulate(null, null);
		}
	}

	@Override
	public void unboundedFound(String id, String in) {
		lastIn = id;
		lastId = id;
		soud.addUnbounded(in);
		lastUnbounded = true;
		lastIsolated = false;

		if(includeBadCases){
			prf.cumulate(null, null);
		}
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

	public String getLastId() {
		return lastId;
	}

	public String getLastChangeIn() {
		return lastIn;
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

	public void clear(boolean includeBadCases){
		prf = new PRFStatistics();
		soud = new SOUDStatistics(includeBadCases);
		mtpl = new ArrayList<>();

		this.includeBadCases = includeBadCases;
		lastIn = null;
		lastGraphDetermined = null;
		times = new ArrayList<Double>();
		lastUnbounded = false;
		lastIsolated = false;
	}

	public boolean hasFoundAtLeastOne() {
		return foundAtLeastOne;
	}
}
