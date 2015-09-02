package com.vmusco.pminer.run;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.analyze.ConsoleDisplayAnalyzer;
import com.vmusco.pminer.analyze.GraphDisplayAnalyzer;
import com.vmusco.pminer.analyze.MutantTestAnalyzer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.softminer.graphs.Graph;

/**
 * Compute performances for mutants of a soft and op
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationVisualizationRunner{
	private static final Class<?> thisclass = MutationVisualizationRunner.class;

	private MutationVisualizationRunner() {
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("c", "console", false, "console type display");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 3 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <mutationFile> <mutantId> <graphFile>", 
					"",
					options,
					"");
			System.exit(0);
		}

		// Load mutations and executions informations from the project
		MutationStatistics<?> ms = MutationStatistics.loadState(cmd.getArgs()[0]);
		MutantIfos mi = ms.loadMutationStats(cmd.getArgs()[1]);

		// Load a graph
		Graph aGraph = MutationStatsRunner.loadGraph(cmd.getArgs()[2]);
		UseGraph propaGraph = new UseGraph(aGraph);
		aGraph.visitTo(propaGraph, mi.getMutationIn());

		MutantTestAnalyzer mta;
		
		if(cmd.hasOption("console")){
			mta = new ConsoleDisplayAnalyzer();
		}else{
			mta = new GraphDisplayAnalyzer(propaGraph.getBasinGraph());
		}

		mta.fireIntersectionFound(ms.getRelatedProcessStatisticsObject(), mi, propaGraph);
		mta.fireExecutionEnded();
		System.exit(0);
	}
}

