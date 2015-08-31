package com.vmusco.smf.run;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.testing.TestingFunctions;
import com.vmusco.smf.testing.TestingNotification;
import com.vmusco.smf.utils.ConsoleTools;

/**
 * This entry point is used to run the tests for mutations 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationRunTests extends GlobalTestRunning implements TestingNotification {
	private static final Class<?> thisclass = MutationRunTests.class;

	protected MutationRunTests() {
		super();
	}

	private MutationRunTests(int nbmutants) {
		super();
		this.nbmutants = nbmutants;
	}
	
	@SuppressWarnings("unused")
	private int nbmutants = 0;

	public static void main(String[] args) throws ParseException, PersistenceException {
		Options options = new Options();

		Option opt;

		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);
		opt = new Option("n", "proceed-nb", true, "number of mutants to proceed (default: all).");
		options.addOption(opt);
		opt = new Option("k", "only-killed", false, "if proceed-nb is set, defines if the number proceeded must be killed mutants (default: false)");
		options.addOption(opt);
		opt = new Option("m", "mutants-id", true, "mutation id to proceed separated by "+File.pathSeparator+" (default: all mutants).");
		options.addOption(opt);
		opt = new Option("s", "no-shuffle", false, "indicated the mutants selection order should not be shuffled (the order is system dependent).");
		options.addOption(opt);


		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length == 0){
			HelpFormatter formatter = new HelpFormatter();
			String header = "Run tests on mutants described on <mutationFile> which is a path to mutation xml config file (or path to folder and will be completed with "+MutationStatistics.DEFAULT_CONFIGFILE+").";
			String footer = "Return: the number of mutant proceeded. 0 if no mutants remaining. -1 in case of error. -2 if this message is displayed.";
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <mutationFile>", header, options, footer);
			System.exit(-2);
		}else{
			MutationStatistics<?> ms = null;

			try{
				ms = openConfig(cmd.getArgs()[0]);
				ms.createExecutionFolderIfNeeded();
			}catch(Exception ex){
				ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED, ConsoleTools.BOLD);
				ConsoleTools.write("Unable to load mutation config from "+cmd.getArgs()[0]);
				ConsoleTools.endLine();
				System.exit(-1);
			}

			String[] mutations = null;
			
			if(cmd.hasOption("mutants-id")){
				mutations = cmd.getOptionValue("mutants-id").split(":");
				List<String> al = new ArrayList<String>();
				
				for(String m : mutations){
					al.add(m);
				}
				
				if(!cmd.hasOption("no-shuffle")){
					Collections.shuffle(al);
				}
				
				MutationRunTests tel = new MutationRunTests(mutations.length);
				
				TestingFunctions.processMutants(ms, al, 0, mutations.length, tel, false);
			}else{
				mutations = ms.listViableMutants();

				MutationRunTests tel = new MutationRunTests(mutations.length);
				
				int nbtodo = mutations.length;
				
				boolean onlykilled = false;
				
				if(cmd.hasOption("proceed-nb")){
					nbtodo = Integer.parseInt(cmd.getOptionValue("proceed-nb"));
					
					onlykilled = cmd.hasOption("only-killed");
				}
				
				int nbviable = TestingFunctions.getViableCollection(ms).size();

				List<String> al = TestingFunctions.getUnfinishedCollection(ms, !cmd.hasOption("no-shuffle"));
				int alreadydone = nbviable - al.size(); 
						
				TestingFunctions.processMutants(ms, al, alreadydone, nbtodo, tel, onlykilled);
			}
		}
	}

	public static MutationStatistics<?> openConfig(String path) throws Exception{
		File f = new File(path);
		String fb = path;

		if(f.isDirectory()){
			fb = path + File.separator + MutationStatistics.DEFAULT_CONFIGFILE; 
		}

		MutationStatistics<?> loadState = MutationStatistics.loadState(fb);
		
		if(loadState == null)
			throw new Exception("Unable to load data, an error has occured");
		else
			return loadState;
	}

	public void mutantException(Exception e) {
		e.printStackTrace();
		System.exit(-1);
	}

	public void mutantPersisting(String mut) {
		this.state = "Storing results...";
		printMutantStat();
	}

	public void mutantStarted(String id){
		this.execname = id;

		nbfail = 0;	
		nbignored = 0;
		nbhang = 0;
		nbnotpermitted = 0;
		nbtotaltc = 0;
		nbunrunnable = 0;
		
		
		this.state = "Starting...";
		printMutantStat();
	}
	
	public void mutantSkippedDueToException(String id){
		ConsoleTools.write("\n !!! Mutant "+id+" has beed skipped due to exception !!! \n", ConsoleTools.BG_RED);
		ConsoleTools.endLine();
		printMutantStat();
	}

	public void mutantEnded(String id){
		this.state = "Done.";
		printMutantStat();
	}

	public void mutantLocked(){
		this.state = "Locked by another thread !";
		printMutantStat();
	}

	public void mutantAlreadyDone(){
		this.state = "Already done.";
		printMutantStat();
	}

}
