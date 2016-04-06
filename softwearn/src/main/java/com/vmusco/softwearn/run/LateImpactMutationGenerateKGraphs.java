package com.vmusco.softwearn.run;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.analyze.MutantTestProcessingAdapter;
import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.analyze.PRFStatistics;
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.pminer.impact.GraphPropagationExplorerForTests;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.persistence.GraphML;
import com.vmusco.softminer.graphs.persistence.GraphPersistence;
import com.vmusco.softwearn.learn.LearningKGraph;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.LateMutationGraphKFold;
import com.vmusco.softwearn.learn.learner.late.BinaryLateImpactLearning;
import com.vmusco.softwearn.learn.learner.late.DichoLateImpactLearning;
import com.vmusco.softwearn.learn.learner.late.LateImpactLearner;
import com.vmusco.softwearn.learn.learner.late.NoLateImpactLearning;
import com.vmusco.softwearn.persistence.MutationGraphKFoldPersistence;

public class LateImpactMutationGenerateKGraphs {
	private static final int _DEFAULT_KSP = 10;
	private static final int _DEFAULT_KFOLD = 10;

	/**
	 * This program generate k-set of weights for a software graphs
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		options.addOption(new Option("U", "list-update-algorithms", false, "list the available updating algorithms"));
		options.addOption(new Option("k", "kfolds", true, "The number of folds to run (default: "+_DEFAULT_KFOLD+")"));
		options.addOption(new Option("K", "ksp", true, "The number of shortest paths to compute. Use 0 to consider all paths --  can be quite slow. (default: "+_DEFAULT_KSP+")"));
		options.addOption(new Option("w", "init-weight", true, "The initialization value of the weigths (any float between 0 to 1, default: algorithm dependent)."));
		options.addOption(new Option("n", "nb-mutants", true, "The number of mutants to consider or MAX if > nb mutants (default: max)."));
		options.addOption(new Option("h", "help", false, "print this message"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		LateImpactLearner il = null;

		if(cmd.hasOption("list-update-algorithms")){
			algoHelp();
		}
		
		if(cmd.getArgList().size() < 4 || cmd.hasOption('h')){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(LateImpactMutationGenerateKGraphs.class.getCanonicalName()+" [GRAPH_FILE] [MUTATION_FILE] [UPDATE_ALGO] [GENERATED_FILE]", options);

			System.exit(1);
		}


		int k = _DEFAULT_KFOLD;
		if(cmd.hasOption("kfolds")){
			k = Integer.parseInt(cmd.getOptionValue("kfolds"));
		}
		
		int kspnr = _DEFAULT_KSP;
		if(cmd.hasOption("ksp")){
			kspnr = Integer.parseInt(cmd.getOptionValue("ksp"));
		}
		
		if(cmd.getArgs()[2].equals("no")){
			il = new NoLateImpactLearning(k, kspnr);
		}else if(cmd.getArgs()[2].equals("binary")){
			il = new BinaryLateImpactLearning(k, kspnr);
		}else if(cmd.getArgs()[2].equals("dicho")){
			il = new DichoLateImpactLearning(k, kspnr);
		}

		if(il == null){
			algoHelp();
		}
		
		final MutationStatistics ms = MutationStatistics.loadState(cmd.getArgs()[1]);
		float initW = il.defaultInitWeight();
		if(cmd.hasOption("init-weight")){
			initW = Float.parseFloat(cmd.getOptionValue("init-weight"));
		}

		LearningKGraph g = new LearningKGraphStream(initW, k);
		GraphPersistence gp = new GraphML(g.graph());
		gp.load(new FileInputStream(cmd.getArgs()[0]));

		String[] tests = ms.getTestCases();
		ConsequencesExplorer t = new GraphPropagationExplorerForTests(g.graph(), tests);
		
		int nbmut = 0;

		if(cmd.hasOption("nb-mutants")){
			nbmut = Integer.parseInt(cmd.getOptionValue("nb-mutants"));
		}

		LateMutationGraphKFold tenfold = LateMutationGraphKFold.instantiateKFold(ms, g, k, nbmut, il, t);
		tenfold.learnKFold();
		
		MutationGraphKFoldPersistence persist = new MutationGraphKFoldPersistence(tenfold);
		persist.save(new FileOutputStream(cmd.getArgs()[3]), cmd.getArgs()[0], cmd.getArgs()[1], cmd.getArgs()[2]);
	}

	private static void algoHelp() {
		ConsoleTools.write("Available algorithms: \n", ConsoleTools.BOLD);
		ConsoleTools.write("\tno", ConsoleTools.BOLD);
		ConsoleTools.write(": Use no algorithm (weight remains unchanged) [default init weight = 1]\n");
		ConsoleTools.write("\tdicho", ConsoleTools.BOLD);
		ConsoleTools.write(": Use the Dichotomic Algorithm (add the empirical probability to the weight) [default init weight = 0]\n");
		ConsoleTools.write("\tbinary", ConsoleTools.BOLD);
		ConsoleTools.write(": Use the Binary Algorithm (set to 1 all impacted weights at least once)\n [default init weight = 0]");

		ConsoleTools.endLine();
		System.exit(0);
	}

}
