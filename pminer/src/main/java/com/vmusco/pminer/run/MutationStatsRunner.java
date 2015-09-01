package com.vmusco.pminer.run;

import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.analyze.ExploreMutants;
import com.vmusco.pminer.analyze.MutantTestProcessingListener;
import com.vmusco.pminer.analyze.StatisticsDisplayer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;

/**
 * Compute performances for a mutants of a soft and op
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationStatsRunner{
	private static final Class<?> thisclass = MutationStatsRunner.class;

	private static final DecimalFormat nf = new DecimalFormat("0.00");

	private MutationStatsRunner() {
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("k", "only-killed", false, "include only killed mutants in the analysis");
		options.addOption(opt);
		opt = new Option("r", "remove-nulls", false, "if set, remove nulls from the medians");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", false, "filter out if more than n mutants are present");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <usegraph> <mutationFile>", options);
			System.exit(0);
		}

		HashMap<String, UseGraph> cache = new HashMap<String, UseGraph>();

		// Load mutations and executions informations from the project
		MutationStatistics<?> ms = MutationStatistics.loadState(cmd.getArgs()[1]); 
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();
		// Load the mutations in ms here

		String[] allMutations;
		if(cmd.hasOption("only-killed")){
			allMutations = ms.listViableButKilledMutants();
		}else{
			allMutations = ms.listViableAndRunnedMutants(true);
		}
		
		if(cmd.hasOption("nb-mutants")){
			int nbm = Integer.parseInt(cmd.getOptionValue("nb-mutants"));
			
			if(allMutations.length > nbm){
				String[] tmp = new String[nbm];
				
				for(int i=0; i<nbm; i++){
					tmp[i] = allMutations[i];
				}
				
				allMutations = tmp;
			}
		}

		// Load the UseGraph
		Graph usegraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		GraphML gml = new GraphML(usegraph);
		gml.load(new FileInputStream(cmd.getArgs()[0]));

		printDataHeader();
		StatisticsDisplayer sd = new StatisticsDisplayer(new MutantTestProcessingListener<StatisticsDisplayer>() {
			@Override
			public void aMutantHasBeenProceeded(StatisticsDisplayer a) {
				System.out.printf("%20s %7d %7d %7d %7d %7d %7.2f %7.2f %7.2f\n",
						a.getLastMutantId(),
						(int)a.getSoud().getLastCandidateImpactSetSize(),
						(int)a.getSoud().getLastActualImpactSetSize(),
						(int)a.getSoud().getLastIntersectedImpactSetSize(),
						(int)a.getSoud().getLastFalsePositiveImpactSetSize(),
						(int)a.getSoud().getLastDiscoveredImpactSetSize(),
						a.getPrecisionRecallFscore().getLastPrecision(),
						a.getPrecisionRecallFscore().getLastRecall(),
						a.getPrecisionRecallFscore().getLastFscore());
				
			}
		});

		for(String mutation : allMutations){											// For each mutant...
			boolean forceStop = false;
			MutantIfos ifos = (MutantIfos) ms.getMutationStats(mutation);

			// relevant IS list of tests impacted by the introduced bug (determined using mutation)
			String[] relevantArray = ExploreMutants.purifyFailAndHangResultSetForMutant(ps, ifos);

			if(relevantArray == null)
				continue;

			UseGraph propaGraph;

			if(cache.containsKey(ifos.getMutationIn())){
				propaGraph = cache.get(ifos.getMutationIn());
			}else{
				propaGraph = new UseGraph(usegraph);
				usegraph.visitTo(propaGraph, ifos.getMutationIn());
			}

			
			// retrieved IS list of tests impacted by the introduced bug (determined using basin)
			String[] retrievedArray = ExploreMutants.getRetrievedTests(propaGraph, ps.getTestCases());

			sd.fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, -1);
			
			if(!forceStop && sd.forceStop()){
				forceStop = true;
				break;
			}

			if(forceStop)
				break;
		}

		sd.fireExecutionEnded();

		printLine();
		
		if(cmd.hasOption("remove-nulls")){
			sd.getPrecisionRecallFscore().removesNulls();
		}
		
		System.out.printf("%20s %7d %7d %7d %7d %7d %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
				"MEDIAN",
				(int)sd.getSoud().getCurrentMedianCandidateImpactSetSize(),
				(int)sd.getSoud().getCurrentMedianActualImpactSetSize(),
				(int)sd.getSoud().getCurrentMedianIntersectedImpactSetSize(),
				(int)sd.getSoud().getCurrentMedianFalsePositiveImpactSetSize(),
				(int)sd.getSoud().getCurrentMedianDiscoveredImpactSetSize(),
				sd.getPrecisionRecallFscore().getCurrentMedianPrecision(),
				sd.getPrecisionRecallFscore().getCurrentMedianRecall(),
				sd.getPrecisionRecallFscore().getCurrentMedianFscore(),
				sd.getSoud().getNbSameProportion(),
				sd.getSoud().getNbSameProportion() + sd.getSoud().getNbOverestimatedProportion(),
				sd.getSoud().getNbOverestimatedProportion(),
				sd.getSoud().getNbUnderestimatedProportion(),
				sd.getSoud().getNbDifferentProportion());
		
		System.out.printf("%20s %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
				"MEAN",
				sd.getSoud().getCurrentMeanCandidateImpactSetSize(),
				sd.getSoud().getCurrentMeanActualImpactSetSize(),
				sd.getSoud().getCurrentMeanIntersectedImpactSetSize(),
				sd.getSoud().getCurrentMeanFalsePositiveImpactSetSize(),
				sd.getSoud().getCurrentMeanDiscoveredImpactSetSize(),
				sd.getPrecisionRecallFscore().getCurrentMeanPrecision(),
				sd.getPrecisionRecallFscore().getCurrentMeanRecall(),
				sd.getPrecisionRecallFscore().getCurrentMeanFscore(),
				sd.getSoud().getNbSameProportion(),
				sd.getSoud().getNbSameProportion() + sd.getSoud().getNbOverestimatedProportion(),
				sd.getSoud().getNbOverestimatedProportion(),
				sd.getSoud().getNbUnderestimatedProportion(),
				sd.getSoud().getNbDifferentProportion());
		
		printDataHeader();
	}

	private static void printDataHeader(){
		printLine();
		System.out.println("           Mutant id     CIS     AIS   C^AIS    FPIS     DIS    prec  recall  fscore       S       C       O       U       D");	
		printLine();
	}

	private static void printLine() {
		System.out.println("----------------------------------------------------------------------------------------------------------------------------");
		
	}

}
