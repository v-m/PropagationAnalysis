package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softminer.graphs.Graph;

/**
 * Compute performances for all mutants of all softs and all op
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class AllMutationsStatsRunner{
	private static final Class<?> thisclass = AllMutationsStatsRunner.class;

	private static final DecimalFormat nf = new DecimalFormat("0.00");

	private AllMutationsStatsRunner() {
	}

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("k", "only-killed", false, "include only killed mutants in the analysis");
		options.addOption(opt);
		opt = new Option("r", "remove-nulls", false, "if set, remove nulls from the medians");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", true, "filter out if more than n mutants are present");
		options.addOption(opt);
		opt = new Option("c", "csv", true, "export in csv format with such a separator");
		options.addOption(opt);
		opt = new Option("s", "smfrun-filename", true, "specify the smf running config file if different (default: "+ProcessStatistics.DEFAULT_CONFIGFILE+")");
		options.addOption(opt);
		opt = new Option("m", "mutation-filename", true, "specify the mutation summary file if different (default: "+MutationStatistics.DEFAULT_CONFIGFILE+")");
		options.addOption(opt);
		opt = new Option("a", "average", true, "compute the average/mean (default: median)");
		options.addOption(opt);
		opt = new Option("p", "mutation-project", true, "specify the mutation project if different (default: "+MutationStatistics.DEFAULT_ID_NAME+")");
		options.addOption(opt);
		opt = new Option("t", "short-names", false, "in case of reccursive folder exploration, do not merge the parent-project name");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <software["+File.pathSeparator+"...]> <relativepathtograph["+File.pathSeparator+"...]>", 
					"Run statistics on all softwares describes in <software> separated by "+File.pathSeparator+". <software> can be a software directly containing a "+ProcessStatistics.DEFAULT_CONFIGFILE+" file or folder containing projects folders which contains a "+ProcessStatistics.DEFAULT_CONFIGFILE+" file. "+
					"The name of the folder is used as project name. "+
					"The graphs used are those supplied by <relativepathtograph> which are path relatives to project folder",
					options,
					"");
			System.exit(0);
		}
		
		String smfrun = ProcessStatistics.DEFAULT_CONFIGFILE;
		String mutationrun = MutationStatistics.DEFAULT_CONFIGFILE;
		String projectrun = MutationStatistics.DEFAULT_ID_NAME;

		if(cmd.hasOption("smfrun-filename"))
			smfrun = cmd.getOptionValue("smfrun-filename");

		if(cmd.hasOption("mutation-filename"))
			mutationrun = cmd.getOptionValue("mutation-filename");
		
		if(cmd.hasOption("mutation-project"))
			projectrun = cmd.getOptionValue("mutation-project");
		
		Character sep = null;
		
		if(cmd.hasOption("csv")){
			if(cmd.getOptionValue("csv").length() == 1)
				sep = cmd.getOptionValue("csv").charAt(0);
		}

		String[] graphs = cmd.getArgs()[1].split(File.pathSeparator);
		
		String[] files = cmd.getArgs()[0].split(File.pathSeparator);
		
		printHeader(sep);
		
		for(String aFile : files){
			File f = new File(aFile);
			File fp = new File(f, smfrun);
			
			if(fp.exists()){
				// This is a project
				processProject(f.getName(), fp, graphs, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("only-killed"), cmd.hasOption("remove-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"));
			}else{
				for(File ff : f.listFiles()){
					fp = new File(ff, smfrun);
					if(ff.isDirectory() && fp.exists()){
						// This is a project
						String name = f.getName()+"-"+ff.getName();
						if(cmd.hasOption("short-names"))
							name = ff.getName();
						processProject(name, fp, graphs, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("only-killed"), cmd.hasOption("remove-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"));
					}
				}
			}
		}
	}

	private static void printHeader(Character sep) {
		if(sep == null){
			System.out.printf("%25s %25s    Op  #mut     CIS     AIS   C^AIS    FPIS     DIS    prec  recall  fscore       S       C       O       U       D\n", "Project", "Graph");
		}else{
			System.out.printf("Project%cgraph%cOp%cnbmut%cCIS%cAIS%cCAIS%cFPIS%cDIS%cprec%crecall%cfscore%cS%cC%cO%cU%cD\n", sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep, sep);
		}
	}

	private static void processProject(String name, File f, String[] graphs, int nbmut, boolean onlyKilled, boolean removeNulls, Character sep, String mutationrun, String projectrun, boolean average) throws IOException, PersistenceException, MutationNotRunException {
		/*************
		 * Load graphs
		 */
		Map<String, Graph> allGraphs = new HashMap<>();
		
		//Graph[] allGraphs = new Graph[graphs.length];
		
		for(int i = 0; i<graphs.length; i++){
			File gf = new File(f.getParentFile(), graphs[i]);
			if(!gf.exists()){
				throw new FileNotFoundException("Unable to locate the graph file "+gf.getAbsolutePath());
			}else{
				allGraphs.put(gf.getName(), MutationStatsRunner.loadGraph(gf.getAbsolutePath()));
			}
		}
		
		/****************
		 * Load mutations
		 */
		ProcessStatistics ps = ProcessStatistics.loadState(f.getAbsolutePath());
		File ops = new File(ps.resolveThis(ps.getMutantsOpsBaseDir(projectrun)));
		List<MutationStatistics<?>> mss = new ArrayList<MutationStatistics<?>>();
		
		for(File op : ops.listFiles()){
			File resolved = new File(op, mutationrun);
			
			if(resolved.exists()){
				mss.add(MutationStatistics.loadState(resolved.getAbsolutePath()));
			}
		}
		
		/*****************
		 * Logic iteration
		 */
		for(String graphTitle: allGraphs.keySet()){
			Graph aGraph = allGraphs.get(graphTitle);
			
			for(MutationStatistics<?> ms : mss){
				String[] allMutations = MutationStatsRunner.selectMutations(ms, nbmut, onlyKilled);
				String[] ret = MutationStatsRunner.processMutants(ms, allMutations, aGraph, sep, removeNulls, null);
				
				int display = average?1:0;
				if(sep==null){
					System.out.printf("%25s %25s %s", name, graphTitle.endsWith(".xml")?graphTitle.substring(0, graphTitle.length()-4):graphTitle, ret[display]);
				}else{
					System.out.printf("\"%s\"%c\"%s\"%c%s", name, sep, graphTitle, sep, ret[display]);
				}
			}
		}
	}
}
