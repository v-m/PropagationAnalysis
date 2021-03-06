package com.vmusco.pminer.run;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.analyze.MutantTestProcessingListener;
import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException.TYPE;
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.pminer.impact.GraphPropagationExplorerForTests;
import com.vmusco.pminer.impact.JavapdgPropagationExplorerForTests;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.GraphTools;
import com.vmusco.softminer.graphs.persistence.GraphML;

/**
 * Compute performances for mutants of a soft and op
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationStatsRunner{
	private static final Class<?> thisclass = MutationStatsRunner.class;
	public static final boolean CLASSLEVEL = false;

	private MutationStatsRunner() {
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("a", "include-alives", false, "include all mutants mutants in the analysis even if they are not killed");
		options.addOption(opt);
		opt = new Option("o", "exclude-nulls", false, "exclude nulls values in precision, recalls and fscores medians computation");
		options.addOption(opt);
		opt = new Option("u", "exclude-unbounded", false, "exclude mutant which have no entry point in the graph");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", true, "filter out if more than n mutants are present");
		options.addOption(opt);
		opt = new Option("j", "javapdg", false, "<graph> is a javapdg database root folder instead of a softminer call-graph. If an archive, end with : and input the database path in the archive");
		options.addOption(opt);
		opt = new Option("c", "csv", true, "export in csv format with such a separator");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <graph> <mutationFile>", 
					"Run detailed statistics (ie at mutant level) on software related to a <mutationFile> using the graphml file <graph>. "+
							"The name of the folder is used as project name. "+
							"The graphs used are those supplied by <relativepathtograph> which are path relatives to project folder",
							options,
					"");
			System.exit(0);
		}

		Character sep = null;

		if(cmd.hasOption("csv")){
			if(cmd.getOptionValue("csv").length() == 1)
				sep = cmd.getOptionValue("csv").charAt(0);
		}

		// Load mutations and executions informations from the project
		MutationStatistics ms = MutationStatistics.loadState(cmd.getArgs()[1]); 

		// Load the graph

		System.out.println(getDataHeader(sep));
		printLine(sep);

		final Character ssep = sep;
		MutantTestProcessingListener<MutationStatisticsCollecter> listener = new MutantTestProcessingListener<MutationStatisticsCollecter>() {
			@Override
			public void aMutantHasBeenProceeded(MutationStatisticsCollecter a) {
				if(ssep == null){
					System.out.printf("%20s%s...............................%7d %7d %7d %7d %7d %7.2f %7.2f %7.2f %s\n",
							a.getLastId(),
							(a.isLastUnbounded()?" (unbounded)":((a.isLastIsolated())?"  (isolated)":"............")),
							(int)a.getSoud().getLastCandidateImpactSetSize(),
							(int)a.getSoud().getLastActualImpactSetSize(),
							(int)a.getSoud().getLastIntersectedImpactSetSize(),
							(int)a.getSoud().getLastFalsePositiveImpactSetSize(),
							(int)a.getSoud().getLastDiscoveredImpactSetSize(),
							a.getPrecisionRecallFscore().getLastPrecision(),
							a.getPrecisionRecallFscore().getLastRecall(),
							a.getPrecisionRecallFscore().getLastFscore(),
							((a.isLastUnbounded()||a.isLastIsolated())?a.getLastChangeIn():""));
				}else{
					System.out.printf("\"%s\"%c%c%c%c%c%c%c%d%c%d%c%d%c%d%c%d%c%f%c%f%c%f%c%c%c%c%c\n",
							a.getLastId()+(a.isLastUnbounded()?" (unbounded)":((a.isLastIsolated())?" (isolated)":"")),
							ssep,ssep,ssep,ssep,ssep, ssep, ssep,
							(int)a.getSoud().getLastCandidateImpactSetSize(),ssep,
							(int)a.getSoud().getLastActualImpactSetSize(),ssep,
							(int)a.getSoud().getLastIntersectedImpactSetSize(),ssep,
							(int)a.getSoud().getLastFalsePositiveImpactSetSize(),ssep,
							(int)a.getSoud().getLastDiscoveredImpactSetSize(),ssep,
							a.getPrecisionRecallFscore().getLastPrecision(),ssep,
							a.getPrecisionRecallFscore().getLastRecall(),ssep,
							a.getPrecisionRecallFscore().getLastFscore(),ssep,ssep,ssep,ssep,ssep);

				}
			}
		};

		ConsequencesExplorer pgp;
		String pth = cmd.getArgs()[0];
		
		String[] tests = ms.getTestCases();
		
		if(cmd.hasOption("javapdg")){
			String[] parts = pth.split(":");
			
			if(parts.length > 1)
				pgp = new JavapdgPropagationExplorerForTests(parts[0], parts[1], tests);
			else
				pgp = new JavapdgPropagationExplorerForTests(parts[0], tests);
		}else{
			pgp = new GraphPropagationExplorerForTests(loadGraph(pth), tests);
		}
		
		String[] ret = processMutants(ms, pgp, sep, cmd.hasOption("exclude-nulls"), listener, cmd.hasOption("include-unbounded"), cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("include-alives")); 

		printLine(sep);

		if(ssep == null){
			System.out.printf("%20s %s", "MEDIAN", ret[0]);
			System.out.printf("%20s %s", "MEAN", ret[1]);
		}else{
			System.out.printf("\"%s\"%c%s", "MEDIAN", ssep, ret[0]);
			System.out.printf("\"%s\"%c%s", "MEAN", ssep, ret[1]);
		}



		if(sep == null){
			System.out.println(getDataHeader(sep));
			printLine(sep);
		}
	}

	public static Graph loadGraph(String graphPath) throws FileNotFoundException, IOException {
		Graph aGraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		GraphML gml = new GraphML(aGraph);
		FileInputStream is = new FileInputStream(graphPath);
		gml.load(is);
		is.close();
		
		if(CLASSLEVEL)
			aGraph = GraphTools.exportClassDependencyGraph(aGraph);
		
		return aGraph;
	}

	protected static String getDataHeader(Character sep){
		printLine(sep);
		if(sep == null){
			return String.format("           Mutant id    Op  #mut #aliv #unbo  #iso #node #edge     CIS     AIS   C^AIS    FPIS     DIS    prec  recall  fscore       S       C       O       U       D      time");
		}else{
			return String.format("\"MutId\"%c\"Op\"%c\"nbmut\"%c\"nbalives\"%c\"nunbound\"%c\"nisolated\"%c\"nbnodes\"%c\"nbedges\"%c\"CIS\"%c\"AIS\"%c\"CAIS\"%c\"FPIS\"%c\"DIS\"%c\"prec\"%c\"recall\"%c\"fscore\"%c\"S\"%c\"C\"%c\"O\"%c\"U\"%c\"D\"%c\"time\"", sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep,sep);
		}
	}

	private static void printLine(Character sep) {
		if(sep == null){
			System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------");
		}
	}

	public static String[] processMutants(MutationStatistics ms, ConsequencesExplorer pgp, 
			Character sep, boolean excludeNulls, MutantTestProcessingListener<MutationStatisticsCollecter> listener, 
			boolean excludeUnbounded, int nb, boolean includeAlives) throws MutationNotRunException, PersistenceException{
		/**
		 * Compute the mutants entry set...
		 */
		
		String[] allMutations;
		allMutations = ms.listViableAndRunnedMutants(true);

		return processMutants(ms, pgp, sep, excludeNulls, listener, excludeUnbounded, nb, includeAlives, allMutations);
	}
	
	public static String[] processMutants(MutationStatistics ms, ConsequencesExplorer pgp, 
			Character sep, boolean excludeNulls, MutantTestProcessingListener<MutationStatisticsCollecter> listener, 
			boolean excludeUnbounded, int nb, boolean includeAlives, String[] allMutations) throws MutationNotRunException, PersistenceException{
		String[] ret = new String[2];
		//int nbunbounded = 0;

		MutationStatisticsCollecter sd = new MutationStatisticsCollecter(true, listener);
		
		int nbentry = (nb > 0 && allMutations.length>nb)?nb:allMutations.length;
		int cpt = 0;
		int nbalives = 0;

		for(String mutation : allMutations){											// For each mutant...
			boolean forceStop = false;
			
			MutantIfos ifos = (MutantIfos) ms.getMutationStats(mutation);

			if(ms.isMutantAlive(mutation)){
				nbalives++;
				
				if(!includeAlives){
					continue;
				}
			}
			
			// relevant IS list of tests impacted by the introduced bug (determined using mutation)
			String[] relevantArray = ms.getCoherentMutantFailAndHangTestCases(ifos.getExecutedTestsResults());

			if(relevantArray == null)
				continue;

			try{
				long time = System.currentTimeMillis();
				pgp.visit(ms, ifos);
				time = System.currentTimeMillis()-time;
				String[] ais = ms.getCoherentMutantFailAndHangTestCases(ifos.getExecutedTestsResults());
				String[] cis = pgp.getLastConsequenceNodes();
				
				sd.intersectionFound(ifos.getId(), ifos.getMutationIn(), ais, cis);
				sd.declareNewTime(time);
				cpt++;

			}catch(SpecialEntryPointException e){
				// No entry point here !

				if(excludeUnbounded){
					continue;
				}else{
					if(e.getType().equals(TYPE.NOT_FOUND)){
						sd.unboundedFound(ifos.getId(), ifos.getMutationIn());
					}else{
						sd.isolatedFound(ifos.getId(), ifos.getMutationIn());
					}
					cpt++;
				}
			}

			if(!forceStop && sd.forceStop()){
				forceStop = true;
				break;
			}

			if(cpt >= nbentry){
				forceStop = true;
			}
			
			if(forceStop){
				break;
			}
		}

		sd.executionEnded();

		if(excludeNulls){
			sd.getPrecisionRecallFscore().removesNulls();
		}
		

		if(sep == null){
			ret[0] = String.format("%5s %5d %5d %5d %5d %5d %5d %7d %7d %7d %7d %7d %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
					ms.getMutationOperator().operatorId(),
					cpt,
					nbalives,
					sd.getSoud().getNbUnbounded(),
					sd.getSoud().getNbIsolated(),
					pgp.getBaseGraphNodesCount(),
					pgp.getBaseGraphEdgesCount(),
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
					sd.getSoud().getNbDifferentProportion(),
					sd.getMedianTimes());

			ret[1] = String.format("%5s %5d %5d %5d %5d %5d %5d %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f %7.2f\n",
					ms.getMutationOperator().operatorId(),
					cpt,
					nbalives,
					sd.getSoud().getNbUnbounded(),
					sd.getSoud().getNbIsolated(),
					pgp.getBaseGraphNodesCount(),
					pgp.getBaseGraphEdgesCount(),
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
					sd.getSoud().getNbDifferentProportion(),
					sd.getMeanTimes());
		}else{
			ret[0] = String.format("\"%s\"%c%d%c%d%c%d%c%d%c%d%c%d%c%d%c%d%c%d%c%d%c%d%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f\n",
					ms.getMutationOperator().operatorId(),sep,
					cpt,sep,
					nbalives,sep,
					sd.getSoud().getNbUnbounded(),sep,
					sd.getSoud().getNbIsolated(),sep,
					pgp.getBaseGraphNodesCount(),sep,
					pgp.getBaseGraphEdgesCount(),sep,
					(int)sd.getSoud().getCurrentMedianCandidateImpactSetSize(),sep,
					(int)sd.getSoud().getCurrentMedianActualImpactSetSize(),sep,
					(int)sd.getSoud().getCurrentMedianIntersectedImpactSetSize(),sep,
					(int)sd.getSoud().getCurrentMedianFalsePositiveImpactSetSize(),sep,
					(int)sd.getSoud().getCurrentMedianDiscoveredImpactSetSize(),sep,
					sd.getPrecisionRecallFscore().getCurrentMedianPrecision(),sep,
					sd.getPrecisionRecallFscore().getCurrentMedianRecall(),sep,
					sd.getPrecisionRecallFscore().getCurrentMedianFscore(),sep,
					sd.getSoud().getNbSameProportion(),sep,
					sd.getSoud().getNbSameProportion() + sd.getSoud().getNbOverestimatedProportion(),sep,
					sd.getSoud().getNbOverestimatedProportion(),sep,
					sd.getSoud().getNbUnderestimatedProportion(),sep,
					sd.getSoud().getNbDifferentProportion(), sep,
					sd.getMedianTimes());

			ret[1] = String.format("\"%s\"%c%d%c%d%c%d%c%d%c%d%c%d%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f%c%f\n",
					ms.getMutationOperator().operatorId(),sep,
					cpt,sep,
					nbalives,sep,
					sd.getSoud().getNbUnbounded(),sep,
					sd.getSoud().getNbIsolated(),sep,
					pgp.getBaseGraphNodesCount(),sep,
					pgp.getBaseGraphEdgesCount(),sep,
					sd.getSoud().getCurrentMeanCandidateImpactSetSize(),sep,
					sd.getSoud().getCurrentMeanActualImpactSetSize(),sep,
					sd.getSoud().getCurrentMeanIntersectedImpactSetSize(),sep,
					sd.getSoud().getCurrentMeanFalsePositiveImpactSetSize(),sep,
					sd.getSoud().getCurrentMeanDiscoveredImpactSetSize(),sep,
					sd.getPrecisionRecallFscore().getCurrentMeanPrecision(),sep,
					sd.getPrecisionRecallFscore().getCurrentMeanRecall(),sep,
					sd.getPrecisionRecallFscore().getCurrentMeanFscore(),sep,
					sd.getSoud().getNbSameProportion(),sep,
					sd.getSoud().getNbSameProportion() + sd.getSoud().getNbOverestimatedProportion(),sep,
					sd.getSoud().getNbOverestimatedProportion(),sep,
					sd.getSoud().getNbUnderestimatedProportion(),sep,
					sd.getSoud().getNbDifferentProportion(), sep,
					sd.getMeanTimes());
		}

		return ret;
	}
}

