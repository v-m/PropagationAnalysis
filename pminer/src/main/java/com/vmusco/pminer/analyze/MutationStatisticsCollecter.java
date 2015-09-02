package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.utils.Tools;

public class MutationStatisticsCollecter extends MutantTestAnalyzer {

	private PRFStatistics prf = new PRFStatistics();
	private SOUDStatistics soud = new SOUDStatistics();
	private MutantTestProcessingListener<MutationStatisticsCollecter> mtpl = null;

	// Last values retainer
	private String lastMutantId = null;
	private UseGraph lastUseGraph = null;
	private ProcessStatistics lastProcessStatistics = null;
	private MutantIfos lastMutantIfos = null;
	private String[] lastGraphDetermined = null;
	private List<Double> times = new ArrayList<Double>();
	
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
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, UseGraph graph) throws MutationNotRunException {

		String[] cis = ExploreMutants.getRetrievedTests(graph, ps.getTestCases());
		
		lastMutantId = mi.getId();
		lastUseGraph = graph;
		lastProcessStatistics = ps;
		lastMutantIfos = mi;
		lastGraphDetermined = cis;

		String[] ais = mi.getExecutedTestsResults().getCoherentMutantFailAndHangTestCases(ps);

		prf.cumulate(ais, cis);
		soud.cumulate(mi.getId(), ais, cis);

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

	public ProcessStatistics getLastProcessStatistics() {
		return lastProcessStatistics;
	}

	public UseGraph getLastUseGraph() {
		return lastUseGraph;
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
