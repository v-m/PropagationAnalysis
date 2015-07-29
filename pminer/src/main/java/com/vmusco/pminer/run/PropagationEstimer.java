package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.vmusco.pminer.MutantTestProcessingListener;
import com.vmusco.pminer.UseGraph;
import com.vmusco.pminer.analyze.ExploreMutants;
import com.vmusco.pminer.analyze.GraphDisplayAnalyzer;
import com.vmusco.pminer.analyze.GraphDisplayAnalyzerAndExporter;
import com.vmusco.pminer.analyze.MutantTestAnalyzer;
import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.persistence.PropagationStatistics;
import com.vmusco.pminer.persistence.XmlPminerPersister;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.mutation.operators.KingOffutt91.ArithmeticMutatorOperator;
import com.vmusco.smf.run.CreateMutation;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;

public class PropagationEstimer implements MutantTestProcessingListener{
	private static final Class<?> thisclass = PropagationEstimer.class;
	
	private ArrayList<MutantTestAnalyzer> analyzeListeners = new ArrayList<MutantTestAnalyzer>();
	private static final DecimalFormat nf = new DecimalFormat("0.00");

	private PropagationEstimer() {
	}

	public void fireExecutionStarting(){
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionStarting();
		}
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <usegraph> <mutationFile>", options);
			System.exit(0);
		}
		
		PropagationEstimer pe = new PropagationEstimer();
		PropagationStatistics prop = new PropagationStatistics();
		HashMap<String, UseGraph> cache = new HashMap<String, UseGraph>();
		
		// Load mutations and executions informations from the project
		MutationStatistics<?> ms = MutationStatistics.loadState(cmd.getArgs()[1]);
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();
		// Load the mutations in ms here
		String[] allMutations = ms.loadResultsForExecutedTestOnMutants(0);

		// Load the UseGraph
		Graph usegraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		GraphML gml = new GraphML(usegraph);
		gml.load(new FileInputStream(cmd.getArgs()[0]));
		
		// LOADING PHASE FINISHED !
		StatisticsMutantAnalyzer stats = new StatisticsMutantAnalyzer(allMutations.length, pe); 
		pe.analyzeListeners.add(stats);
		GraphDisplayAnalyzer mta = null;
		//pe.analyzeListeners.add(new HistogramForSizesOfSubgraphs(ff.getAbsolutePath(), ff.getName(), 10));
		//pe.analyzeListeners.add(new HistogramRawData(ff));

		System.out.println("# tests = "+ps.getTestCases().length);
		System.out.println("# nodes = "+usegraph.getNbNodes());
		System.out.println("# edges = "+usegraph.getNbEdges());

		printDataHeader("");

		pe.fireExecutionStarting();

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
				propaGraph.propagateMarkersAndTypes(usegraph);
			}

			if(mta != null)
				pe.analyzeListeners.remove(mta);
			mta = new GraphDisplayAnalyzerAndExporter(
					propaGraph.getBasinGraph(), 
					/*new File(visusFolder, mutation).getAbsolutePath()*/null, false, false);

			mta.setBuggyNodeAndOriginalGraph(ifos.getMutationIn(), usegraph);
			pe.analyzeListeners.add(mta);

			// retrieved IS list of tests impacted by the introduced bug (determined using basin)
			String[] retrievedArray = ExploreMutants.getRetrievedTests(propaGraph, ps.getTestCases());

			for(MutantTestAnalyzer aListerner : pe.analyzeListeners){
				//TODO: default value of -1 is set here -- maybe change it further
				aListerner.fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, -1);

				if(!forceStop && aListerner.forceStop()){
					forceStop = true;
					break;
				}
			}

			prop.addPropagationInMutation(mutation, propaGraph, stats.getLastMutstat());

			if(forceStop)
				break;
		}

		pe.fireExecutionEnded();

		String prefForDisplay = "\t\t\t%";

		System.out.println(prefForDisplay+"-------------------------------------------------------------------------");
		//System.out.println(prefForDisplay+ff.getName());

		System.out.println(prefForDisplay+"-------------------------------------------------------------------------");

		printDataHeader(prefForDisplay);


		System.out.print(prefForDisplay+"MEDIAN \t");

		//System.out.print("\u001b[94m"+value+"\u001b[0m"+")\t");
		System.out.print("("+nf.format(stats.getMinGraphSize())+"/"+nf.format(stats.getMedianGraphSize())+"/"+nf.format(stats.getMaxGraphSize())+")"+"\t"+
				nf.format(stats.getMedianCasesFoundByGraph())+"\t"+
				nf.format(stats.getMedianCasesFoundByMutation())+"\t"+
				nf.format(stats.getMedianCasesFoundByBoth())+"\t"+
				nf.format(stats.getMedianCasesFoundOnlyByGraph())+"\t"+
				nf.format(stats.getMedianCasesFoundOnlyByMutation())+"\t");

		System.out.println(nf.format(stats.getMedianPrecision())+"\t"+
				nf.format(stats.getMedianRecall())+"\t"+
				nf.format(stats.getMedianFScore())+"\t");

		System.out.print(prefForDisplay+"AVERAGE \t");
		//System.out.print("\u001b[94m"+value+"\u001b[0m"+")\t");
		System.out.print("("+nf.format(stats.getMinGraphSize())+"/"+nf.format(stats.getAvgGraphSize())+"/"+nf.format(stats.getMaxGraphSize())+")"+"\t"+
				nf.format(stats.getAvgCasesFoundByGraph())+"\t"+
				nf.format(stats.getAvgCasesFoundByMutation())+"\t"+
				nf.format(stats.getAvgCasesFoundByBoth())+"\t"+
				nf.format(stats.getAvgCasesFoundOnlyByGraph())+"\t"+
				nf.format(stats.getAvgCasesFoundOnlyByMutation())+"\t");

		System.out.println(nf.format(stats.getAvgPrecision())+"\t"+
				nf.format(stats.getAvgRecall())+"\t"+
				nf.format(stats.getAvgFScore())+"\t");

		System.out.println(prefForDisplay+"-------------------------------------------------------------------------");

		System.out.println(prefForDisplay+"Perfect: "+stats.getNbPerfect()+"/"+stats.getNbTotal()+" ("+nf.format(stats.getPartPerfect()*100)+"%)");
		System.out.println(prefForDisplay+"More: "+stats.getNbMore()+"/"+stats.getNbTotal()+" ("+nf.format(stats.getPartMore()*100)+"%)");
		System.out.println(prefForDisplay+"Both (soundness): "+(stats.getNbPerfect()+stats.getNbMore())+"/"+stats.getNbTotal()+" ("+nf.format((stats.getPartPerfect()+stats.getPartMore())*100)+"%)");

		System.out.println(prefForDisplay+"Less: "+stats.getNbLess()+"/"+stats.getNbTotal()+" ("+nf.format(stats.getPartLess()*100)+"%)");
		System.out.println(prefForDisplay+"M&L : "+stats.getNbMoreLess()+"/"+stats.getNbTotal()+" ("+nf.format(stats.getPartMoreLess()*100)+"%)");


		System.out.println("Persisting results...");
		File ugxml = new File(ms.resolveName("pminer.xml"));
		XmlPminerPersister.persistAll(prop, ugxml);
	}



	private void fireExecutionEnded() {
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionEnded();
		}
	}

	public void aMutantHasBeenProceeded(MutantTestAnalyzer a) {
		if(a instanceof StatisticsMutantAnalyzer){
			StatisticsMutantAnalyzer stats = (StatisticsMutantAnalyzer) a;

			System.out.print(stats.getLastMutationId());
			System.out.print("\t");
			System.out.print(stats.getLastGraphSize()+"\t"+
					stats.getLastNumberOfCasesDeterminedByGraphs()+"\t"+
					stats.getLastNumberOfCasesDeterminedByMutation()+"\t"+
					stats.getLastNumberOfCasesDeterminedByBoth()+"\t");

			System.out.print(stats.getLastNumberOfCasesDeterminedOnlyByGraphs()+"\t"+
					stats.getLastNumberOfCasesDeterminedOnlyByMutation()+"\t");

			System.out.println(nf.format(stats.getLastPrecision())+"\t"+
					nf.format(stats.getLastRecall())+"\t"+
					nf.format(stats.getLastFScore())+"\t"+
					"=> "+stats.getLastMutationInsertionPoint());

		}
	}

	private static void printDataHeader(String before){
		System.out.println();
		System.out.println(before+"-------------------------------------------------------------------------");
		System.out.print(before+"Mutant id \t");
		System.out.print("sizes"+"\t"+"G    "+"\t"+"M    "+"\t"+"G&M"+"\t"+"G-M"+"\t"+"M-G"+"\t");
		System.out.println("prec"+"\t"+"recall"+"\t"+"fscore"+"\t");
		System.out.println(before+"-------------------------------------------------------------------------");
	}

}
