package com.vmusco.pminer.analyze;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;

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

	public void fillIn(ProcessStatistics ps, String mutationId, MutantIfos mi, String[] graphDetermined, UseGraph basin, long propagraphtime) throws MutationNotRunException {
		this.mutationId = mutationId;
		mutationInsertionPoint = mi.getMutationIn();

		//System.out.print(this.mutationId+"\t");
		
		data_basin = new HashSet<String>();
		for(String node : basin.getBasinGraph().getNodesNames()){
			data_basin.add(node);
		}
		data_basin_testnodes = new HashSet<String>();
		for(String node : basin.getTestNodes(ps.getTestCases())){
			data_basin_testnodes.add(node);
		}
		
		graphsize = data_basin.size();
		graphsizeonlytests = data_basin_testnodes.size();
		
		data_relevant = new HashSet<String>();
		for(String t : ExploreMutants.purifyFailAndHangResultSetForMutant(ps, mi)){
			data_relevant.add(t);
		}

		data_retrieved = new HashSet<String>();
		for(String t : graphDetermined){
			data_retrieved.add(t);
		}

		// inter IS list of tests impacted by the introduced bug (determined by BOTH)
		data_inter = new HashSet<String>();
		for(String tt : MutationsSetTools.setIntersection(graphDetermined, ExploreMutants.purifyFailAndHangResultSetForMutant(ps, mi))){
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
		//System.out.print(nb_graph+"\t");
		//System.out.print(nb_mutat+"\t");
		//System.out.print(nb_boths+"\t");
		//System.out.print(nb_mores+"\t");
		//System.out.print(nb_lesss+"\t");
		
		prediction_time = propagraphtime;
		
		calculate();
		//System.out.print(precision+"\t");
		//System.out.print(recall+"\t");
		//System.out.print(fscore+"\t");
		calculateAndExcludeNulls();
		//System.out.print(precision_notnull+"\t");
		//System.out.print(recall_notnull+"\t");
		//System.out.print(fscore_notnull+"\t");
		//System.out.print("\n");
	}
}