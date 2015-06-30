package com.vmusco.smf.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.utils.ConsoleTools;

public class ParallelMutationRunTests extends MutationRunTests{
	private static final Class<?> thisclass = ParallelMutationRunTests.class;

	private ParallelMutationRunTests() {
	}
	
	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		Option opt;

		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);
		opt = new Option("s", "no-shuffle", false, "indicated the mutants selection order should not be shuffled (they order is system dependent).");
		options.addOption(opt);
		opt = new Option("n", "nbmax", true, "Set a maxium number of mutant to execute (default: all)");
		options.addOption(opt);
		opt = new Option("R", "reset", false, "Restart from zero all mutations checks");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 2){
			HelpFormatter formatter = new HelpFormatter();
			String header = "Run <slicesize> tests on each mutants described on subfolders of <mutationroot> which are path to mutation xml config files (or path to folders and will be completed with "+MutationStatistics.DEFAULT_CONFIGFILE+").";
			header += "Once run <slicesize> tests on each mutation operator, start again for <slicesize> tests until all tests are done on each mutant.";
			String footer = "";	
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <slicesize> <mutationroot>", header, options, footer);
			System.exit(2);
		}else{
			int slicesize = Integer.parseInt(cmd.getArgs()[0]);
			File f = new File(cmd.getArgs()[1]);
			
			List<String> tmp = new ArrayList<String>();
			List<String> ops = new ArrayList<String>();
			for(File ft : f.listFiles()){
				if(ft.isDirectory()){
					tmp.add(ft.getAbsolutePath());
					ops.add(ft.getName());
				}
			}
			
			MutationStatistics<?>[] mss = new MutationStatistics<?>[tmp.size()];
			List<String>[] mutants = new List[tmp.size()];
			int[] totalForEach = new int[tmp.size()];
			int[] doneForEach = new int[tmp.size()];

			ConsoleTools.write("Loading files...\n");
			int i = 0;
			for(String cur : tmp){
				MutationStatistics<?> ms = null;

				try{
					ms = MutationRunTests.openConfig(cur);
					
					if(cmd.hasOption("reset")){
						File ff = new File(ms.getExecutionFileResolved());
						FileUtils.deleteDirectory(ff);
					}
					
					ms.createExecutionFolderIfNeeded();
				}catch(Exception ex){
					ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED, ConsoleTools.BOLD);
					ConsoleTools.write("Unable to load mutation config from "+cur);
					ConsoleTools.endLine();
					System.exit(-1);
				}

				mss[i] = ms;
				mutants[i] = MutationRunTests.getUnfinishedCollection(ms, !cmd.hasOption("no-shuffle"));
				totalForEach[i] = MutationRunTests.getViableCollection(ms).size();
				doneForEach[i] = totalForEach[i] - mutants[i].size();
				i++;
			}

			int cur = 0;

			int nbmax = -1;
			if(cmd.hasOption("nbmax")){
				nbmax = Integer.parseInt(cmd.getOptionValue("nbmax"));
			}
			
			int nbdone = slicesize;
			
			ConsoleTools.write("\n\n");
			
			while((cur = nextItems(cur, mutants)) != -1){
				boolean skip = false;
				
				if(doneForEach[cur] > nbdone)
					skip=true;
				if(cur==0)
					nbdone += slicesize;
				if(skip)
					continue;
				
				int nextslice = slicesize - (doneForEach[cur] % slicesize);
				printStatus(cur, ops, doneForEach, totalForEach, nextslice, mutants);
				MutationRunTests mrt = new ParallelMutationRunTests();

				doneForEach[cur] += MutationRunTests.processMutants(mss[cur], mutants[cur], nextslice, mrt);
				
				if(nbmax != -1 && checkIfMaxIsReached(doneForEach, nbmax)){
					System.out.println("Interrupted because did "+nbmax+" on each operator :)");
				}
			}

		}
	}
	
	private static void printStatus(int cur, List<String> ops, int[] doneForEach, int[] totalForEach, int sliceSize, List<String>[] mutants){
		ConsoleTools.rewindLine(2);
		String line = "";
		int i = 0;
		for(String op : ops){
			if(i==cur){
				line += ConsoleTools.format("["+op+"]", ConsoleTools.BOLD, ConsoleTools.FG_CYAN)+" ";
			}else{
				line += op+" ";
			}
			i++;
		}
		line += "(done: "+doneForEach[cur]+" (+"+sliceSize+")/"+totalForEach[cur]+")";
		ConsoleTools.write(line+"\n\n");
	}

	private static boolean checkIfMaxIsReached(int[] stats, int max) {
		for(int i=0; i<stats.length; i++){
			if(stats[i] < max)
				return false;
		}
		
		return true;
	}

	private static int nextItems(int curpos, List<String>[] entries){
		for(int i=1; i<=entries.length; i++){
			List<String> e = entries[(i+curpos)%(entries.length)];
			if(e.size() > 0){
				return (i+curpos)%(entries.length);
			}
		}

		return -1;
	}

}