package com.vmusco.smf.run;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
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

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.utils.ConsoleTools;

public class MutationRunTests extends GlobalTestRunning {
	private static final Class<?> thisclass = MutationRunTests.class;

	protected MutationRunTests() {
		super();
	}

	private MutationRunTests(int nbmutants) {
		super();
		this.nbmutants = nbmutants;
	}
	private int nbmutants = 0;

	public static void main(String[] args) throws ParseException {
		Options options = new Options();

		Option opt;

		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);
		opt = new Option("n", "proceed-nb", true, "number of mutants to proceed (default: all).");
		options.addOption(opt);
		opt = new Option("m", "mutants-id", true, "mutation id to proceed separated by "+File.pathSeparator+" (default: all mutants).");
		options.addOption(opt);
		opt = new Option("s", "no-shuffle", false, "indicated the mutants selection order should not be shuffled (they order is system dependent).");
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
				
				processMutants(ms, al, 0, mutations.length, tel);
			}else{
				mutations = ms.listMutants();

				MutationRunTests tel = new MutationRunTests(mutations.length);
				
				int nbtodo = mutations.length;
				
				if(cmd.hasOption("proceed-nb")){
					nbtodo = Integer.parseInt(cmd.getOptionValue("proceed-nb"));
				}
				
				int nbviable = getViableCollection(ms).size();

				List<String> al = getUnfinishedCollection(ms, !cmd.hasOption("no-shuffle"));
				int alreadydone = nbviable - al.size(); 
						
				processMutants(ms, al, alreadydone, nbtodo, tel);
			}
		}
	}

	public static MutationStatistics<?> openConfig(String path) throws Exception{
		File f = new File(path);
		String fb = path;

		if(f.isDirectory()){
			fb = path + File.separator + MutationStatistics.DEFAULT_CONFIGFILE; 
		}

		MutationStatistics<?> ms = null;

		return MutationStatistics.loadState(fb);
	}

	public static List<String> getViableCollection(MutationStatistics<?> ms){
		ArrayList<String> al = new ArrayList<String>();
		for(String mut : ms.getAllMutationsId()){
			MutantIfos ifos = ms.getMutationStats(mut);

			if(!ifos.isViable()){
				continue;
			}

			al.add(mut);
		}

		return al;
	}

	public static List<String> getUnfinishedCollection(MutationStatistics<?> ms, boolean shuffle){
		ArrayList<String> al = new ArrayList<String>();
		for(String mut : getViableCollection(ms)){
			MutantIfos ifos = ms.getMutationStats(mut);
			File ff = new File(ms.getMutantFileResolved(mut));

			if(!ff.exists()){
				al.add(mut);
			}else if(ff.length() == 0){
				try{
					FileOutputStream fos = new FileOutputStream(ff);

					FileLock lock = fos.getChannel().tryLock();
					if(lock != null){
						lock.release();
						fos.close();
						al.add(mut);
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}

		if(shuffle)
			Collections.shuffle(al);
		
		return al;
	}

	public static int processMutants(MutationStatistics<?> ms, List<String> mutantIds, int nbdone, int nbmax, MutationRunTests tel){
		int nbproc = nbdone;

		while(mutantIds.size() > 0){
			String mut = mutantIds.remove(0); 

			if(tel != null)	tel.mutantStarted(mut);
			MutantIfos ifos = ms.getMutationStats(mut);

			if(!ifos.isViable()){
				continue;
			}

			File ff = new File(ms.getMutantFileResolved(mut));

			if(!ff.exists() || ff.length() == 0){

				try{
					FileOutputStream fos = new FileOutputStream(ff);

					FileLock lock = fos.getChannel().tryLock();
					if(lock != null){
						
						Testing.runTestCases(ms, mut, tel);

						if(tel != null)	tel.mutantPersisting(mut);

						MutantIfos mi = ms.getMutationStats(mut);
						
						if(mi.isExecutedTests()){
							MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(fos, mut);
							pers.saveState(ms.getMutationStats(mut));
						}else{
							if(tel != null)	tel.mutantSkippedDueToException(mut);
						}
						
						lock.release();
						fos.close();

						if(tel != null)	tel.mutantEnded(mut);

						nbproc++;
					}else{
						if(tel != null)	tel.mutantLocked();
						nbproc++;
					}
				} catch (IOException | InterruptedException | ClassNotFoundException e) {
					if(tel != null)	tel.mutantException(e);
				}
			}else{
				if(tel == null)	tel.mutantAlreadyDone();
			}

			if(nbproc >= nbmax){
				return nbproc;
			}
		}

		return nbproc;
	}
	
	public static int processMutants(MutationStatistics<?> ms, List<String> mutantIds, int nbmax, MutationRunTests tel){
		return processMutants(ms, mutantIds, 0, nbmax, tel);
	}

	private void mutantException(Exception e) {
		e.printStackTrace();
		System.exit(-1);
	}

	private void mutantPersisting(String mut) {
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
