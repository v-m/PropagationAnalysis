package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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

import com.vmusco.pminer.impact.JavapdgPropagationExplorer;
import com.vmusco.pminer.impact.PropagationExplorer;
import com.vmusco.pminer.impact.SoftMinerPropagationExplorer;
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
		opt = new Option("a", "include-alives", false, "include all mutants mutants in the analysis even if they are not killed");
		options.addOption(opt);
		opt = new Option("o", "include-nulls", false, "include nulls values in precision, recalls and fscores medians computation");
		options.addOption(opt);
		opt = new Option("u", "include-unbounded", false, "include mutant which have no entry point in the graph");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", true, "filter out if more than n mutants are present");
		options.addOption(opt);
		opt = new Option("c", "csv", true, "export in csv format with such a separator");
		options.addOption(opt);
		opt = new Option("s", "smfrun-filename", true, "specify the smf running config file if different (default: "+ProcessStatistics.DEFAULT_CONFIGFILE+")");
		options.addOption(opt);
		opt = new Option("m", "mutation-filename", true, "specify the mutation summary file if different (default: "+MutationStatistics.DEFAULT_CONFIGFILE+")");
		options.addOption(opt);
		opt = new Option("v", "average", false, "compute the average/mean (default: median)");
		options.addOption(opt);
		opt = new Option("p", "mutation-project", true, "specify the mutation project if different (default: "+MutationStatistics.DEFAULT_ID_NAME+")");
		options.addOption(opt);
		opt = new Option("j", "javapdg", false, "<graph> is a javapdg database instead of a softminer call-graph");
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
							"The graphs used are those supplied by <relativepathtograph> which are path relatives to project folder. If the --javapdg option is supplied, <relativepathtograph> must point to one folder which contains subfolders, one for each considered project (with the same folder name)",
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

		System.out.println(getHeader(sep));

		for(String aFile : files){
			File f = new File(aFile);
			File fp = new File(f, smfrun);

			Map<String, PropagationExplorer> explorers = null;
			
			if(fp.exists()){
				// This is a project
				if(cmd.hasOption("javapdg")){
					explorers = getExplorers(fp, graphs[0]);
				}else{
					explorers = getExplorers(fp, graphs);
				}
				
				processProject(f.getName(), fp, explorers, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("include-alives"), cmd.hasOption("include-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"), cmd.hasOption("include-unbounded"));
			}else{
				for(File ff : f.listFiles()){
					fp = new File(ff, smfrun);
					if(ff.isDirectory() && fp.exists()){
						// This is a project
						if(cmd.hasOption("javapdg")){
							explorers = getExplorers(fp, graphs[0]);
						}else{
							explorers = getExplorers(fp, graphs);
						}
						
						String name = f.getName()+"-"+ff.getName();
						if(cmd.hasOption("short-names"))
							name = ff.getName();
						processProject(name, fp, explorers, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("include-alives"), cmd.hasOption("include-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"), cmd.hasOption("include-unbounded"));
					}
				}
			}
		}
	}

	private static Map<String, PropagationExplorer> getExplorers(File f, String[] graphs) throws IOException{
		/*************
		 * Load graphs
		 */
		Map<String, PropagationExplorer> explorers = new HashMap<>();

		Graph[] allGraphs = new Graph[graphs.length];

		for(int i = 0; i<graphs.length; i++){
			File gf = new File(f.getParentFile(), graphs[i]);
			if(!gf.exists()){
				throw new FileNotFoundException("Unable to locate the graph file "+gf.getAbsolutePath());
			}else{
				explorers.put(gf.getName(), new SoftMinerPropagationExplorer(MutationStatsRunner.loadGraph(gf.getAbsolutePath())));
			}
		}
		
		return explorers;
	}


	private static Map<String, PropagationExplorer> getExplorers(File projPath, String javapdgroot) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		/*************
		 * Load graphs
		 */
		Map<String, PropagationExplorer> explorers = new HashMap<>();

		File gf = new File(javapdgroot, projPath.getParentFile().getName());

		if(!gf.exists()){
			throw new FileNotFoundException("Unable to locate the javapdg database "+gf.getAbsolutePath());
		}else{
			explorers.put("pdg_"+gf.getName(), new JavapdgPropagationExplorer(gf.getAbsolutePath()));
		}
		
		return explorers;
	}

	private static String getHeader(Character sep) {
		if(sep == null){
			return String.format("%25s %25s    %s", "Project", "Graph", MutationStatsRunner.getDataHeader(sep).substring(24));
		}else{
			return String.format("\"Project\"%c\"graph\"%c%s", sep, sep, MutationStatsRunner.getDataHeader(sep).substring(8));
		}
	}

	private static void processProject(String name, File f, Map<String, PropagationExplorer> explorers, int nbmut, boolean includeAlives, boolean includeNulls, Character sep, String mutationrun, String projectrun, boolean average, boolean includeUnbounded) throws IOException, PersistenceException, MutationNotRunException {

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
		for(String graphTitle: explorers.keySet()){
			PropagationExplorer pgp = explorers.get(graphTitle);

			for(MutationStatistics<?> ms : mss){
				//String[] allMutations = MutationStatsRunner.selectMutations(ms, nbmut, onlyKilled);
				//String[] ret = MutationStatsRunner.processMutants(ms, allMutations, aGraph, sep, removeNulls, null);
				String[] ret = MutationStatsRunner.processMutants(ms, pgp, sep, includeNulls, null, includeUnbounded, nbmut, includeAlives);

				int display = average?1:0;
				if(sep==null){
					System.out.printf("%25s %25s %s", (name.length()>25?"..."+name.substring(name.length()-22):name), graphTitle.endsWith(".xml")?graphTitle.substring(0, graphTitle.length()-4):graphTitle, ret[display]);
				}else{
					System.out.printf("\"%s\"%c\"%s\"%c%s", name, sep, graphTitle, sep, ret[display]);
				}
			}
		}
	}
}
