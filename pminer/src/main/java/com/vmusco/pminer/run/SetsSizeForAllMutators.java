package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import com.vmusco.pminer.analyze.ExploreMutants;
import com.vmusco.pminer.analyze.MutantTestAnalyzer;
import com.vmusco.pminer.analyze.MutantTestProcessingListener;
import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.persistence.UseGraphStatsXml;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.MutationsSetTools;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;

public class SetsSizeForAllMutators implements MutantTestProcessingListener<MutantTestAnalyzer> {
	private static final Class<?> thisclass = PropagationEstimer.class;


	public static void main(String[] args) throws Exception {
		Options options = new Options();

		String projectname = "?";

		Option opt;
		opt = new Option("z", "silent", false, "Do not ouput results to console");
		options.addOption(opt);
		opt = new Option("k", "only-killed", false, "include only killed mutants in the analysis");
		options.addOption(opt);
		opt = new Option("s", "save", true, "Persist results in mutation subfolder with specified name");
		options.addOption(opt);
		opt = new Option("l", "load", true, "Read statistics from file in mutation subfolder");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 1
				|| (cmd.getArgs().length == 1 && !cmd.hasOption("load"))
				||  cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <mutationRootFolder> [<usegraph>]", options);
			return;
		}

		File f = new File(cmd.getArgs()[0]);
		Map<String, StatisticsMutantAnalyzer> produced = new HashMap<String, StatisticsMutantAnalyzer>();
		
		if(cmd.getArgs().length == 1){
			//System.out.println("Loading statistics from "+f.getAbsolutePath());

			produced = loadFile(f, cmd.getOptionValue("load"));
		}else{
			//System.out.println("Generating new statistics");

			boolean persist = cmd.hasOption("save");

			for(File ff : f.listFiles()){
				if(!ff.isDirectory())
					continue;

				// Load mutations and executions informations from the project
				MutationStatistics<?> ms = MutationStatistics.loadState(ff.getAbsolutePath());
				//ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();
				// Load the mutations in ms here
				String[] allMutationsLoaded;
				if(cmd.hasOption("only-killed")){
					allMutationsLoaded = ms.listViableButKilledMutants();
				}else{
					allMutationsLoaded = ms.listViableAndRunnedMutants(true);
				}
				
				
				String[] allMutations = MutationsSetTools.shuffleAndSlice(allMutationsLoaded, 600);
				
				// Load the UseGraph
				Graph usegraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
				GraphML gml = new GraphML(usegraph);
				gml.load(new FileInputStream(cmd.getArgs()[1]));

				ExploreMutants em = new ExploreMutants(ms, usegraph);
				StatisticsMutantAnalyzer mta = new StatisticsMutantAnalyzer(-1, null);
				em.addMutantTestAnalyzeListener(mta);

				em.start(allMutations/*, cmd.hasOption("invert-variable-read")*/);

				if(persist){
					File fdir = new File(ff, cmd.getOptionValue("save"));

					System.out.println("Saving in: "+fdir.getAbsolutePath());

					if(fdir.exists() && fdir.isDirectory())
						FileUtils.deleteDirectory(fdir);
					else if(fdir.exists() && !fdir.isDirectory())
						fdir.delete();

					FileOutputStream fos = new FileOutputStream(fdir);
					UseGraphStatsXml.persistResults(mta, fos);
					fos.close();
				}

				produced.put(ff.getName(), mta);
			}
		}

		projectname = f.getParentFile().getParentFile().getName();

		if(cmd.getArgs().length >= 2 &&  cmd.hasOption("save") && cmd.hasOption("silent"))
			return;

		boolean first = true;

		ConsoleTools.write("\\midrule\n");

		for(String k : new TreeSet<String>(produced.keySet())){
			StatisticsMutantAnalyzer mta = produced.get(k);

			displayHeader(first, projectname, k, mta.getAllMutatObjs().size());
			display2(mta);
			first = false;
			
			ConsoleTools.write("\\\\");
			ConsoleTools.endLine();
		}
	}

	public static void displayHeader(boolean first, String projectname, String k, int nbmut) {
		if(first){				
			ConsoleTools.write(projectname+" & ");
		}else{
			ConsoleTools.write("\t & ");
		}

		ConsoleTools.write(k+" & ");
		
		ConsoleTools.write(nbmut+" & ");
	}

	public static Map<String, StatisticsMutantAnalyzer> loadFile(File f, String loadPos) throws FileNotFoundException, IOException {
		Map<String, StatisticsMutantAnalyzer> produced = new HashMap<String, StatisticsMutantAnalyzer>();

		for(File ff : f.listFiles()){
			if(!ff.isDirectory())
				continue;

			File fff = new File(ff, loadPos);
			String op = ff.getName();
			produced.put(op, UseGraphStatsXml.restoreResults(new FileInputStream(fff)));
		}
		
		return produced;
	}

	public static void display1(StatisticsMutantAnalyzer mta) {
		DecimalFormat nf = new DecimalFormat("0.00");

		ConsoleTools.write(mta.getAllMutatObjs().size()+" & ");

		ConsoleTools.write(((int)mta.getMedianGraphSize())+" ("+((int)mta.getNbTestsInMedianGraphSize())+") & ");
		ConsoleTools.write(((int)mta.getMaxGraphSize())+" ("+((int)mta.getNbTestsInMaxGraphSize())+") & ");
		ConsoleTools.write(nf.format(mta.getPartPerfect()*100)+"\\% & ");
		ConsoleTools.write(nf.format(mta.getPartMore()*100)+"\\% & ");

		int t = ((int)mta.getMedianMoreSizes());
		ConsoleTools.write((t==-1?"---":t)+" & ");
		ConsoleTools.write(nf.format((mta.getPartPerfect()+mta.getPartMore())*100)+"\\% & ");
		ConsoleTools.write(nf.format(mta.getPartLess()*100)+"\\% & ");
		t = ((int)mta.getMedianLessSizes());
		ConsoleTools.write((t==-1?"---":t)+" & ");
		ConsoleTools.write(nf.format(mta.getPartMoreLess()*100)+"\\% & ");
		ConsoleTools.write(nf.format(mta.getMedianPrecision()*100)+"\\% & ");
		ConsoleTools.write(nf.format(mta.getMedianRecall()*100)+"\\% & ");
		ConsoleTools.write(nf.format(mta.getMedianFScore()*100)+"\\%");
	}

	public static void display2(StatisticsMutantAnalyzer mta, boolean bold) {
		DecimalFormat nf = new DecimalFormat("0");
		DecimalFormat nf2 = new DecimalFormat("0.00");

		Double tmp = mta.getPartPerfect()*100;
		ConsoleTools.write(nf.format(tmp));
		ConsoleTools.write("\\% & ");

		tmp = (mta.getPartPerfect()+mta.getPartMore())*100;
		ConsoleTools.write(nf.format(tmp));
		ConsoleTools.write("\\% & ");
		
		tmp = mta.getMedianPrecision();
		ConsoleTools.write(nf2.format(tmp));
		ConsoleTools.write(" & ");
		
		tmp = mta.getMedianRecall();
		ConsoleTools.write(nf2.format(tmp));
		ConsoleTools.write(" & ");
		
		tmp = mta.getMedianFScore();
		if(bold){
			ConsoleTools.write("\\textbf{");
			ConsoleTools.write(nf2.format(tmp));
			ConsoleTools.write("}");
		}else{
			ConsoleTools.write(nf2.format(tmp));
		}
		//ConsoleTools.write(" & ");
		
	}
	
	public static void display2(StatisticsMutantAnalyzer mta) {
		display2(mta, false);
	}

	public void aMutantHasBeenProceeded(MutantTestAnalyzer a) {
		
	}
}
