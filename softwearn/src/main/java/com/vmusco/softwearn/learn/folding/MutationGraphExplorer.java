package com.vmusco.softwearn.learn.folding;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softminer.graphs.Graph;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MutationGraphExplorer {
	protected Set<MutationStatisticsCollecter> listeners = new HashSet<MutationStatisticsCollecter>();
	protected Graph g;
	
	public abstract void test(MutantIfos[] mutants, int k);
	
	public MutationGraphExplorer(Graph g) {
		this.g = g;
	}
	
	public void addTestListener(MutationStatisticsCollecter aListener){
		listeners.add(aListener);
	}
	
	public void removeTestListener(MutationStatisticsCollecter aListener){
		listeners.remove(aListener);
	}
	
	public void fireExecutionStarting(){
		for(MutationStatisticsCollecter l : listeners){
			l.executionStarting();
		}
	}
	
	public void fireExecutionEnded(){
		for(MutationStatisticsCollecter l : listeners){
			l.executionEnded();
		}
	}
	
	public void fireIntersectionFound(MutantIfos mi, String[] ais, String[] cis){
		for(MutationStatisticsCollecter l : listeners){
			l.intersectionFound(mi.getId(), mi.getMutationIn(), ais, cis);
		}
	}
	
	public void fireUnboundedFound(MutantIfos mi){
		for(MutationStatisticsCollecter l : listeners){
			l.unboundedFound(mi.getId(), mi.getMutationIn());
		}
	}
	
	public void fireIsolatedFound(MutantIfos mi){
		for(MutationStatisticsCollecter l : listeners){
			l.isolatedFound(mi.getId(), mi.getMutationIn());
		}
	}
	
	public Graph getGraph() {
		return g;
	}
}
