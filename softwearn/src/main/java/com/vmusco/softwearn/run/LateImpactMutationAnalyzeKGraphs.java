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

public class LateImpactMutationAnalyzeKGraphs {
	private static final float _DEFAULT_THRESHOLD = 0.2f;

	/**
	 * This program generate k-set of weights for a software graphs
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		options.addOption(new Option("t", "threshold", true, "The threshold over which (>=) mutant are kept (any float from 0 to 1, default: "+_DEFAULT_THRESHOLD+")."));
		options.addOption(new Option("h", "help", false, "print this message"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgList().size() < 1 || cmd.hasOption('h')){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(LateImpactMutationAnalyzeKGraphs.class.getCanonicalName()+" [GENERATED_FILE]", options);

			System.exit(1);
		}

		LateMutationGraphKFold thefold = new LateMutationGraphKFold();
		MutationGraphKFoldPersistence per = new MutationGraphKFoldPersistence(thefold);

		per.load(new FileInputStream(cmd.getArgs()[0]));

		float threshold = _DEFAULT_THRESHOLD;
		if(cmd.hasOption("threshold")){
			threshold = Float.parseFloat(cmd.getOptionValue("threshold"));
		}
		
		final MutationStatisticsCollecter msc = new MutationStatisticsCollecter(true){
			@Override
			public void executionEnded() {
				PRFStatistics precisionRecallFscore = getPrecisionRecallFscore();
				/*System.out.println("P = "+precisionRecallFscore.getCurrentMeanPrecision()+
						" / R = "+precisionRecallFscore.getCurrentMeanRecall()+
						" / F = "+precisionRecallFscore.getCurrentMeanFscore());*/
				clear(true);
			}
		};

		final MutationStatisticsCollecter mscall = new MutationStatisticsCollecter(true);
		
		run(thefold, threshold, new MutationStatisticsCollecter[]{ msc, mscall });
		
		PRFStatistics precisionRecallFscore = mscall.getPrecisionRecallFscore();
		System.out.println("[[[ P = "+precisionRecallFscore.getCurrentMeanPrecision()+
				" / R = "+precisionRecallFscore.getCurrentMeanRecall()+
				" / F = "+precisionRecallFscore.getCurrentMeanFscore()+" ]]]");
	}

	public static void run(LateMutationGraphKFold thefold, float threshold, MutationStatisticsCollecter msc){
		LateImpactMutationAnalyzeKGraphs.run(thefold, threshold, new MutationStatisticsCollecter[]{ msc });
	}

	public static void run(LateMutationGraphKFold thefold, float threshold, MutationStatisticsCollecter[] mscs){
		if(mscs != null){
			for(MutationStatisticsCollecter msc : mscs){
				msc.addListener(new MutantTestProcessingAdapter());
				thefold.addTestListener(msc);
			}
		}

		//LearningKGraph g = new LearningKGraphStream(initW, k);
		thefold.testKFold(threshold);		
	}
}
