package com.vmusco.softwearn.run;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.pminer.faultlocalization.GraphFaultLocalizationByIntersection;
import com.vmusco.pminer.run.FaultLocalizationSteimann;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.persistence.GraphML;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.MutationGraphKFold;
import com.vmusco.softwearn.learn.learner.Learner;
import com.vmusco.softwearn.learn.learner.late.BinaryLateImpactLearning;

public class LearningFaultLocalization extends FaultLocalizationSteimann{

	protected static final Logger logger = LogManager.getFormatterLogger(LearningFaultLocalization.class.getSimpleName());
	
	private int K_FOLD = 10;
	private Learner LEARN_ALGO = new BinaryLateImpactLearning(K_FOLD);
	private MutationGraphKFold kfold = null;
	private int currentIteration = 0;
	private int nbedgeswithlearn;
	private long learningtime = -1;

	static private File persistToFolder = null; 
	static private File persistToFile = null;
	
	protected LearningFaultLocalization() {
		super();
	}
	
	public static void main(String[] args) throws Exception {
		Options options = FaultLocalizationSteimann.prepareOptions();

		Option opt;
		opt = new Option("k", "kfold", true, "Set the number of fold (for learning only -- default: 10)");
		options.addOption(opt);
		opt = new Option("a", "algorithm", true, "Select a learning algorithm (default: binary) -- currently the only choice.");
		options.addOption(opt);
		opt = new Option("K", "ksp", true, "Enter the number of shortest path used for learning (default: 10)");
		options.addOption(opt);
		opt = new Option("persistcg", true, "Set a folder where persist generated causal graphs");
		options.addOption(opt);
		opt = new Option("persiststats", true, "Set a file to write the number of edge learnt by each mutant");
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
		
		if(cmd.hasOption("persistcg")){
			persistToFolder = new File(cmd.getOptionValue("persistcg"));
			
			if(!persistToFolder.mkdirs()){
				System.out.println("The persistance diretory cannot be created !");
				System.exit(1);
			}
		}
		
		if(cmd.hasOption("persiststats")){
			persistToFile = new File(cmd.getOptionValue("persiststats"));
			
			if(persistToFile.exists())
				persistToFile.delete();
			
			if(!persistToFile.createNewFile()){
				System.out.println("The persistance file cannot be created !");
				System.exit(1);
			}
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
		
		// Here starts the learning phase time
		learningtime = System.currentTimeMillis();
		logger.info("Starting learning at %d", learningtime);
		
		while(ite < K_FOLD){
			kfold.learn(ite);
			ite++;
		}
		
		kfold.learningRoundFinished();
		// Here ends the learning phase time
		learningtime = System.currentTimeMillis() - learningtime;
		logger.info("Finished learning at %d (duration: +/-%d)", System.currentTimeMillis(), learningtime);
		
		// Persist generated graphs...
		if(persistToFolder != null){
			logger.info("Persisting causal graph...");
			for(int i=0; i<K_FOLD; i++){
				String name = String.format("causal_graph_%d.graphml", i);
				File persistTo = new File(persistToFolder, name);
				lg.setK(i);
				lg.setThreshold(1f);
				GraphML gml = new GraphML(lg);
				gml.save(new FileOutputStream(persistTo));
			}
		}
		
		// Display stats
		if(persistToFile != null){
			FileOutputStream fos = new FileOutputStream(persistToFile);
			
			for(int i = 0; i<K_FOLD; i++){
				MutantIfos[] testingSubset = kfold.getTestingSubset(i);
				
				for(MutantIfos mi : testingSubset){
					fos.write(String.format("%s;%d\n", mi.getId(), LEARN_ALGO.getLearnedPathForChange(mi.getId())).getBytes());
				}
			}
			
			fos.close();
		}
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
		String result = "count;max;mutid;#fold;learntime;#E;predtime;#Eon";

		result += String.format(";#inter");

		for(DisplayData dd : notifier){
			result += String.format(";%s", dd.getHeader());
		}
		result += '\n';
		return result;
	}
	
	@Override
	protected String printEntry(int cpt, int nbedges, int interNodesList, String m, String intermBuffer, long predictTime) {
		String result = String.format("%d;%d;%s;%d;%d;%d;%d;%d", cpt, maxsize, m, currentIteration, learningtime, nbedges, predictTime, nbedgeswithlearn);
		result += ";"+interNodesList;
		result += intermBuffer;
		result += '\n';
		
		return result;
	}
}
