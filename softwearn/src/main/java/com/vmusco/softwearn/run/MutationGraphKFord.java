package com.vmusco.softwearn.run;

import java.io.FileInputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.analyze.PRFStatistics;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.graphs.persistance.GraphPersistence;
import com.vmusco.softwearn.learn.BinaryAlgorithm;
import com.vmusco.softwearn.learn.DichotomicAlgorithm;
import com.vmusco.softwearn.learn.ImpactLearner;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.MutationGraphKFold;
import com.vmusco.softwearn.learn.NoAlgorithm;
import com.vmusco.softwearn.learn.TestListener;

public class MutationGraphKFord {

	private static final float _DEFAULT_THRESHOLD = 0.2f;

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		options.addOption(new Option("U", "list-update-algorithms", false, "list the available updating algorithms"));
		options.addOption(new Option("k", "kfolds", true, "The number of folds to run (default: 10)"));
		options.addOption(new Option("w", "init-weight", true, "The initialization value of the weigths (any float between 0 to 1, default: algorithm dependent)."));
		options.addOption(new Option("n", "nb-mutants", true, "The number of mutants to consider or MAX if > nb mutants (default: max)."));
		options.addOption(new Option("t", "threshold", true, "The threshold over which (>=) mutant are kept (any float from 0 to 1, default: "+_DEFAULT_THRESHOLD+")."));
		options.addOption(new Option("h", "help", false, "print this message"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		ImpactLearner il = null;

		if(cmd.getArgList().size() < 3 || cmd.hasOption('h')){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(MutationGraphKFord.class.getCanonicalName()+" [GRAPH_FILE] [MUTATION_FILE] [UPDATE_ALGO]", options);

			System.exit(1);
		}

		if(cmd.getArgs()[2].equals("no")){
			il = new NoAlgorithm();
		}else if(cmd.getArgs()[2].equals("binary")){
			il = new BinaryAlgorithm();
		}else if(cmd.getArgs()[2].equals("dicho")){
			il = new DichotomicAlgorithm();
		}

		if(il == null || cmd.hasOption("list-update-algorithms")){
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

		Graph g = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		GraphPersistence gp = new GraphML(g);
		gp.load(new FileInputStream(cmd.getArgs()[0]));
		final MutationStatistics<?> ms = MutationStatistics.loadState(cmd.getArgs()[1]);
		String[] testCases = ms.getRelatedProcessStatisticsObject().getTestCases();

		int k = 10;
		if(cmd.hasOption("kfolds")){
			k = Integer.parseInt(cmd.getOptionValue("kfolds"));
		}

		float initW = il.defaultInitWeight();
		if(cmd.hasOption("init-weight")){
			initW = Float.parseFloat(cmd.getOptionValue("init-weight"));
		}

		int nbmut = 0;

		if(cmd.hasOption("nb-mutants")){
			nbmut = Integer.parseInt(cmd.getOptionValue("nb-mutants"));
		}

		float threshold = _DEFAULT_THRESHOLD;
		if(cmd.hasOption("threshold")){
			threshold = Float.parseFloat(cmd.getOptionValue("threshold"));
		}

		MutationGraphKFold tenfold = MutationGraphKFold.instantiateKFold(ms, new LearningGraph(g, initW, testCases), k, nbmut, il);

		tenfold.addTestListener(new TestListener() {
			MutationStatisticsCollecter sma = new MutationStatisticsCollecter(null);
			MutationStatisticsCollecter small = new MutationStatisticsCollecter(null);

			@Override
			public void testResult(MutantIfos mi, String[] impactedTests) {
				try {
					String[] ais = ms.getRelatedProcessStatisticsObject().getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());
					sma.fireIntersectionFound(mi, ais, impactedTests);
					small.fireIntersectionFound(mi, ais, impactedTests);
				} catch (MutationNotRunException e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			@Override
			public void oneFoldEnded() {
				PRFStatistics precisionRecallFscore = sma.getPrecisionRecallFscore();
				System.out.println("P = "+precisionRecallFscore.getCurrentMeanPrecision()+
						" / R = "+precisionRecallFscore.getCurrentMeanRecall()+
						" / F = "+precisionRecallFscore.getCurrentMeanFscore());
				sma = new MutationStatisticsCollecter(null);
			}

			@Override
			public void allFoldsEnded() {
				PRFStatistics precisionRecallFscore = small.getPrecisionRecallFscore();
				System.out.println("P = "+precisionRecallFscore.getCurrentMeanPrecision()+
						" / R = "+precisionRecallFscore.getCurrentMeanRecall()+
						" / F = "+precisionRecallFscore.getCurrentMeanFscore());

				System.out.println("[[[ P = "+precisionRecallFscore.getCurrentMeanPrecision()+
						" / R = "+precisionRecallFscore.getCurrentMeanRecall()+
						" / F = "+precisionRecallFscore.getCurrentMeanFscore()+" ]]]");
			}
		});

		tenfold.kfold(threshold);
	}

}
