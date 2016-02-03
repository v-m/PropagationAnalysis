package com.vmusco.softwearn.run;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.faultlocalization.GraphFaultLocalizationByIntersection;
import com.vmusco.pminer.run.FaultLocalization;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.MutationGraphKFold;
import com.vmusco.softwearn.learn.learner.Learner;
import com.vmusco.softwearn.learn.learner.late.BinaryLateImpactLearning;

public class LearningFaultLocalization extends FaultLocalization{
	private int K_FOLD = 10;
	private Learner LEARN_ALGO = new BinaryLateImpactLearning(K_FOLD);
	private MutationGraphKFold kfold = null;
	private int currentIteration = 0;
	private int nbedgeswithlearn;

	protected LearningFaultLocalization() {
		super();
	}
	
	public static void main(String[] args) throws Exception {
		Options options = FaultLocalization.prepareOptions();

		Option opt;
		opt = new Option("k", "kfold", true, "Set the number of fold (for learning only -- default: 10)");
		options.addOption(opt);
		opt = new Option("a", "algorithm", true, "Select a learning algorithm (default: binary) -- currently the only choice.");
		options.addOption(opt);
		opt = new Option("K", "ksp", true, "Enter the number of shortest path used for learning (default: 10)");
		options.addOption(opt);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if( cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();

			String head = "Compute the wasted effort for each mutation in <mutation-file>. If a metric require graph assistance, use the graph in <graph-file>. Estimation based on graph are computed learning based.";
			String foot = "";

			formatter.printHelp("[options] <mutation-file> [<graph-file>]", head, options, foot);
			System.exit(0);
		}
		
		LearningFaultLocalization lfl = new LearningFaultLocalization();
		
		if(cmd.hasOption("kfold")){
			lfl.K_FOLD = Integer.parseInt(cmd.getOptionValue("kfold")); 
		}
		
		if(cmd.hasOption("algorithm")){
			String requestedAlgo = cmd.getOptionValue("algorithm");
			
			if(requestedAlgo.equals("binary")){
				lfl.LEARN_ALGO = new BinaryLateImpactLearning(lfl.K_FOLD);
			}else{
				System.out.println(String.format("Unknown algorithm: %s.", requestedAlgo));
				System.exit(1);
			}
		}else{
			lfl.LEARN_ALGO = new BinaryLateImpactLearning(lfl.K_FOLD);
		}
		
		lfl.run(cmd);
	}
	
	@Override
	protected Graph giveMeAGraph() {
		return new LearningKGraphStream(0, K_FOLD);
	}
	
	@Override
	protected void preinit(Graph base) throws Exception {
		LearningKGraphStream lg = (LearningKGraphStream)base;
		kfold = MutationGraphKFold.instantiateKFold(ms, lg, K_FOLD, LEARN_ALGO, new GraphFaultLocalizationByIntersection(base));
		int ite = 0;
		
		while(ite < K_FOLD){
			kfold.learn(ite);
			ite++;
		}
		
		kfold.learningRoundFinished();
	}
	
	@Override
	protected boolean moreToProceed() {
		return currentIteration  < K_FOLD;
	}
	
	@Override
	protected String[] giveMeSomeData(Graph base) throws Exception {
		LearningKGraphStream lg = (LearningKGraphStream)base;
		lg.switchToLearningPhase();
		
		maxsize = kfold.getInputDatasetSize();
		
		lg.setK(currentIteration);
		lg.setThreshold(1);

		nbedgeswithlearn = lg.getNbEdges();
		
		MutantIfos[] mts = kfold.getTestingSubset(currentIteration);
		String[] all = new String[mts.length];

		for(int i=0; i<mts.length; i++){
			all[i] = mts[i].getId();
		}
		currentIteration++;
		
		return all;
	}
	
	@Override
	protected String prepareHeader(List<DisplayData> notifier) {
		String result = "count;max;mutid;#fold;#E;#Eon";

		result += String.format(";#inter");

		for(DisplayData dd : notifier){
			result += String.format(";%s", dd.getHeader().split(";")[1]);
		}
		result += '\n';
		return result;
	}
	
	@Override
	protected String printEntry(int cpt, int nbedges, int interNodesList, String m, String intermBuffer) {
		String result = String.format("%d;%d;%s;%d;%d;%d", cpt, maxsize, m, currentIteration, nbedges, nbedgeswithlearn);
		result += ";"+interNodesList;
		result += intermBuffer;
		result += '\n';
		
		return result;
	}
}
