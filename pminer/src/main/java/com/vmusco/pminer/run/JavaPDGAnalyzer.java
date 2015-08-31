package com.vmusco.pminer.run;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.analyze.PrecisionRecallFscore;
import com.vmusco.pminer.compute.ImpactPredictionListener;
import com.vmusco.pminer.compute.JavaPDGImpactPredictionScore;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;

public class JavaPDGAnalyzer {
	public static void main(String[] args) throws Exception {
		Class thisclass = JavaPDGAnalyzer.class;
		Options options = new Options();

		Option opt;
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <java_pdg_db> <mutationFile>", "Run precision, recall and fscores using the mutation estimated set for a specific mutation designated by <mutationFile> and the JavaPDG Derby CallGraph database <java_pdg_db>.", options, "");
			System.exit(0);
		}

		MutationStatistics<?> ms = MutationStatistics.loadState(cmd.getArgs()[1]);
		
		PrecisionRecallFscore prf = JavaPDGImpactPredictionScore.runOverMutants(cmd.getArgs()[0], ms , new ImpactPredictionListener() {
			@Override
			public void fireTestIntersection(String string) {
			}
			
			@Override
			public void fireOneMutantStarting(String mutant, MutantIfos mi) {
				System.out.println("Starting mutant "+mutant+" (error in "+mi.getMutationIn()+").");
			}
			
			@Override
			public void fireOneMutantResults(double p, double r, double f) {
				System.out.println(" > Local scrores are: "+
						"P = "+p+
						" / R = "+r+
						" / F = "+f+".");
			}
		});
		
		System.out.println("Average scrores are: "+
			"P = "+prf.getCurrentMeanPrecision()+
			" / R = "+prf.getCurrentMeanRecall()+
			" / F = "+prf.getCurrentMeanFscore()+".");

		System.out.println("Median scrores are: "+
			"P = "+prf.getCurrentMedianPrecision()+
			" / R = "+prf.getCurrentMedianRecall()+
			" / F = "+prf.getCurrentMedianFscore()+".");
	}
}
