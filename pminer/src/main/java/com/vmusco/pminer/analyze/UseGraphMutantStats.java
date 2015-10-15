package com.vmusco.pminer.analyze;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class UseGraphMutantStats {
	public String mutationId;
	public String mutationInsertionPoint;
	public int graphsize;
	public int graphsizeonlytests;

	public int nb_graph;	// # determined using graph
	public int nb_mutat;	// # determined using testing
	public int nb_boths;	// # determined by both
	public int nb_mores;	// # determined by usegraph only
	public int nb_lesss;	// # determined by test execution only
	
	public long prediction_time = -1;

	public double interd;
	public double precision;
	public double recall;
	public double fscore;
	
	public double precision_notnull;
	public double recall_notnull;
	public double fscore_notnull;
	
	public Set<String> data_basin;
	/**
	 * Mutation determined
	 */
	public Set<String> data_relevant;
	/**
	 * Graph determined
	 */
	public Set<String> data_retrieved;
	public Set<String> data_inter;
	public Set<String> data_graphOnly;
	public Set<String> data_mutationOnly;
	public Set<String> data_basin_testnodes;

	public void calculate() {
		this.interd = nb_boths*1.0;

		// PRECISION
		if(nb_graph == 0){
			this.precision = 1;
		}else{
			this.precision = interd/nb_graph;
		}

		// RECALL
		if(nb_mutat == 0){
			this.recall = 1;
		}else{
			this.recall = interd/nb_mutat;
		}

		//FSCORE
		if((precision + recall) == 0){
			this.fscore = 0;
		}else{
			this.fscore = 2 * ((precision*recall) / (precision+recall));
		}
	}
	
	public void calculateAndExcludeNulls() {
		this.interd = nb_boths*1.0;

		// PRECISION
		if(nb_graph == 0){
			this.precision_notnull = -1;
		}else{
			this.precision_notnull = interd/nb_graph;
		}

		// RECALL
		if(nb_mutat == 0){
			this.recall_notnull = -1;
		}else{
			this.recall_notnull = interd/nb_mutat;
		}

		//FSCORE
		if(precision_notnull == -1 || recall_notnull == -1){
			this.fscore_notnull = -1;
		}else{
			this.fscore_notnull = 2 * ((precision_notnull*recall_notnull) / (precision_notnull+recall_notnull));
		}
	}

	public void fillIn(ProcessStatistics ps, MutantIfos mi, String[] impactedNodes, String[] impactedTests, long propagraphtime) throws MutationNotRunException {
		this.mutationId = mi.getId();
		mutationInsertionPoint = mi.getMutationIn();
		
		data_basin = new HashSet<String>();
		for(String node : impactedNodes){
			data_basin.add(node);
		}
		data_basin_testnodes = new HashSet<String>();
		for(String node : impactedTests){
			data_basin_testnodes.add(node);
		}
		
		graphsize = data_basin.size();
		graphsizeonlytests = data_basin_testnodes.size();
		
		data_relevant = new HashSet<String>();
		for(String t : ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults())){
			data_relevant.add(t);
		}

		data_retrieved = new HashSet<String>();
		for(String t : impactedTests){
			data_retrieved.add(t);
		}

		// inter IS list of tests impacted by the introduced bug (determined by BOTH)
		data_inter = new HashSet<String>();
		for(String tt : MutationsSetTools.setIntersection(impactedTests, ps.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults()))){
			data_inter.add(tt);
		}

		// FALSE POSITIVES (only detected using mutation)
		data_graphOnly = new HashSet<String>();
		data_graphOnly.addAll(data_retrieved);
		data_graphOnly.removeAll(data_inter);

		// FALSE NEGATIVES (only detected using mutation)
		data_mutationOnly = new HashSet<String>();
		data_mutationOnly.addAll(data_relevant);
		data_mutationOnly.removeAll(data_inter);

		nb_graph = data_retrieved.size();
		nb_mutat = data_relevant.size();
		nb_boths = data_inter.size();
		nb_mores = data_graphOnly.size();
		nb_lesss = data_mutationOnly.size();
		
		prediction_time = propagraphtime;
		
		calculate();
		calculateAndExcludeNulls();
	}
}