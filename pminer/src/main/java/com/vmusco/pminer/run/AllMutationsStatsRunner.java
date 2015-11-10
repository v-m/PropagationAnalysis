package com.vmusco.pminer.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.pminer.impact.SoftMinerPropagationExplorer;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;

/**
 * Compute performances for all mutants of all softs and all op
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class AllMutationsStatsRunner{
	private static final Class<?> thisclass = AllMutationsStatsRunner.class;

	private AllMutationsStatsRunner() {
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
		opt = new Option("i", "intersect-mutants", true, "consider only killed and bounded mutants in all graphs (ovverride -u and -a) using the mutation.kb file");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", true, "filter out if more than n mutants are present");
		options.addOption(opt);
		opt = new Option("c", "csv", true, "export in csv format with such a separator");
		options.addOption(opt);
		opt = new Option("v", "average", false, "compute only the average");
		options.addOption(opt);
		opt = new Option("m", "median", false, "compute only the median");
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
							"The graphs used are those supplied by <relativepathtograph> which are path relatives to project folder. If the --javapdg option is supplied, <relativepathtograph> must point to one folder which contains subfolders, one for each considered project (with the same folder name) or a jar/zip archive file",
							options,
					"");
			System.exit(0);
		}

		boolean avg = cmd.hasOption("average");
		boolean med = cmd.hasOption("median");

		String smfrun = ProcessStatistics.DEFAULT_CONFIGFILE;
		String mutationrun = MutationStatistics.DEFAULT_CONFIGFILE;
		String projectrun = MutationStatistics.DEFAULT_ID_NAME;

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

			Map<String, ConsequencesExplorer> explorers = null;

			if(fp.exists()){
				// This is a project
				if(cmd.hasOption("javapdg")){
					explorers = getExplorers(fp, graphs[0]);
				}else{
					explorers = getExplorers(fp, graphs);
				}

				processProject(f.getName(), fp, explorers, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("include-alives"), cmd.hasOption("exclude-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"), cmd.hasOption("exclude-unbounded"), avg, med);
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
						processProject(name, fp, explorers, cmd.hasOption("nb-mutants")?Integer.parseInt(cmd.getOptionValue("nb-mutants")):-1, cmd.hasOption("include-alives"), cmd.hasOption("exclude-nulls"), sep, mutationrun, projectrun, cmd.hasOption("average"), cmd.hasOption("exclude-unbounded"), avg, med);
					}
				}
			}
		}
	}

	public static Map<String, ConsequencesExplorer> getExplorers(File f, String[] graphs) throws IOException{
		/*************
		 * Load graphs
		 */
		Map<String, ConsequencesExplorer> explorers = new HashMap<>();

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


	public static Map<String, ConsequencesExplorer> getExplorers(File projPath, String javapdgroot) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		/*************
		 * Load graphs
		 */
		Map<String, ConsequencesExplorer> explorers = new HashMap<>();

		File gf = new File(javapdgroot);
		boolean isarchive = true;
		String projname = projPath.getParentFile().getName();
		
		if(gf.isDirectory()){
			gf = new File(javapdgroot, projname);
			isarchive = false;
		}

		if(!gf.exists()){
			throw new FileNotFoundException("Unable to locate the javapdg database "+gf.getAbsolutePath());
		}else{
			if(isarchive)
				explorers.put("pdg_"+gf.getName(), new JavapdgPropagationExplorer(gf.getAbsolutePath(), projname));
			else
				explorers.put("pdg_"+gf.getName(), new JavapdgPropagationExplorer(gf.getAbsolutePath()));
		}

		return explorers;
	}

	private static String getHeader(Character sep) {
		if(sep == null){
			return String.format("%25s %25s %3s   %s", "Project", "Graph", "Met", MutationStatsRunner.getDataHeader(sep).substring(24));
		}else{
			return String.format("\"Project\"%c\"graph\"%c\"Type\"%c%s", sep, sep, sep, MutationStatsRunner.getDataHeader(sep).substring(8));
		}
	}

	private static void processProject(String name, File f, Map<String, ConsequencesExplorer> explorers, int nbmut, boolean includeAlives, boolean excludeNulls, Character sep, String mutationrun, String projectrun, boolean average, boolean excludeUnbounded, boolean avg, boolean med) throws IOException, PersistenceException, MutationNotRunException {

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
			ConsequencesExplorer pgp = explorers.get(graphTitle);

			for(MutationStatistics<?> ms : mss){
				//String[] allMutations = MutationStatsRunner.selectMutations(ms, nbmut, onlyKilled);
				//String[] ret = MutationStatsRunner.processMutants(ms, allMutations, aGraph, sep, removeNulls, null);
				String[] ret = MutationStatsRunner.processMutants(ms, pgp, sep, excludeNulls, null, excludeUnbounded, nbmut, includeAlives);

				if(avg || (!avg && !med)){
					if(sep==null){
						System.out.printf("%25s %25s %3s %s", (name.length()>25?"..."+name.substring(name.length()-22):name), graphTitle.endsWith(".xml")?graphTitle.substring(0, graphTitle.length()-4):graphTitle, "avg", ret[1]);
					}else{
						System.out.printf("\"%s\"%c\"%s\"%c\"%s\"%c%s", name, sep, graphTitle, sep, "avg", sep, ret[1]);
					}
				}

				if(med || (!avg && !med)){
					if(sep==null){
						System.out.printf("%25s %25s %3s %s", (name.length()>25?"..."+name.substring(name.length()-22):name), graphTitle.endsWith(".xml")?graphTitle.substring(0, graphTitle.length()-4):graphTitle, "med", ret[0]);
					}else{
						System.out.printf("\"%s\"%c\"%s\"%c\"%s\"%c%s", name, sep, graphTitle, sep, "med", sep, ret[0]);
					}
				}
			}
		}
	}
}
