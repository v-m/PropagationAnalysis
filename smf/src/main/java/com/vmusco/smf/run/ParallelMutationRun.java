package com.vmusco.smf.run;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.testing.TestingFunctions;
import com.vmusco.smf.testing.TestingNotification;
import com.vmusco.smf.utils.ConsoleTools;

/**
 * This main point is used to execute ont unit test per project and per operator.
 * The purpose of this program is to test each project to gather more mutants. 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ParallelMutationRun implements TestingNotification{
	private static final Class<?> thisclass = ParallelMutationRun.class;

	Set<String> ops = new HashSet<String>();
	Set<String> prs = new HashSet<String>();

	int cur = -1;
	
	Map<String, Map<String, MutationStatistics<?>>> files = new HashMap<String, Map<String, MutationStatistics<?>>>();
	Map<String, Map<String, List<String>>> remains = new HashMap<String, Map<String, List<String>>>();
	Map<String, Map<String, Integer>> viables = new HashMap<String, Map<String, Integer>>();

	private int slicesize;

	public void process(File f) throws Exception{
		MutationStatistics<?> ms = MutationStatistics.loadState(f.getAbsolutePath());
		
		String op = ms.getMutationId();
		File ff = new File(ms.getRelatedProcessStatisticsObject().getWorkingDir());
		int cpt = 0;

		while(ff.getName().equals("..")){
			cpt++;
			ff = ff.getParentFile();
		}

		for(int i=0; i<cpt; i++){
			ff = ff.getParentFile();
		}

		String pro = ff.getName();
		
		if(!files.containsKey(pro)){
			Map<String, MutationStatistics<?>> r = new HashMap<String, MutationStatistics<?>>();
			Map<String, List<String>> r2 = new HashMap<String, List<String>>();
			Map<String, Integer> r3 = new HashMap<String, Integer>();
			files.put(pro, r);
			remains.put(pro, r2);
			viables.put(pro, r3);
		}
		
		files.get(pro).put(op, ms);
		//remains.get(pro).put(op, ms.statsMutations()[2] > 0);
		List<String> unfinishedCollection = TestingFunctions.getUnfinishedCollection(ms, true);
		int size = TestingFunctions.getViableCollection(ms).size();
		
		remains.get(pro).put(op, unfinishedCollection);
		viables.get(pro).put(op, size);

		int alreadydone = size - unfinishedCollection.size();

		if(cur == -1 || (unfinishedCollection.size() > 0 && cur >= alreadydone)){
			cur = alreadydone;
		}
		
		ops.add(op);
		prs.add(pro);
	}
	
	private ParallelMutationRun(){
	}

	public static void main(String[] args) throws Exception {
		ParallelMutationRun pmr = new ParallelMutationRun();
		Options options = new Options();

		Option opt;

		opt = new Option("s", "slicesize", true, "specify the number of tests to execute for each project before switching to the next one (default: 1)");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <mutationFiles>", options);
			/*
			 * <mutationFiles> can be 
			 *  - files, 
			 *  - root folder containing the mutations.xml or even 
			 *  - the root folder containing folders of mutations with each containing mutation.xml
			 */
			
			System.exit(0);
		}
		
		pmr.slicesize = (cmd.hasOption("slicesize"))?Integer.parseInt(cmd.getOptionValue("slicesize")):1;

		String basefile = MutationStatistics.DEFAULT_CONFIGFILE;

		String[] parts = cmd.getArgs()[0].split(File.pathSeparator); 

		for(String p : parts){
			File f = new File(p);

			if(f.isFile()){
				pmr.process(f);
			}else{
				File f2 = new File(f, basefile);

				if(f2.exists() && f2.isFile()){
					pmr.process(f2);
				}else{
					for(File ff : f.listFiles()){
						ff = new File(ff, basefile);

						if(ff.getName().equals(basefile) && ff.isFile()){
							pmr.process(ff);
						}
					}
				}
			}
		}

		while(true){
			for(String s : pmr.files.keySet()){
				for(String ss : pmr.files.get(s).keySet()){
					pmr.process(ss, s);
				}
			}
			
			pmr.cur += 1;
		}
	}

	private boolean firstPrinting = true;

	private void process(String cop, String cpr) throws InterruptedException, PersistenceException {
		int nbviable = viables.get(cpr).get(cop);
		List<String> al = remains.get(cpr).get(cop);
		int alreadydone = nbviable - al.size(); 
		
		if(alreadydone > cur)
			return;
		
		printStats(cop, cpr);
		
		MutationStatistics<?> ms = files.get(cpr).get(cop);

		if(al.size()<=0)
			return;
		
		TestingFunctions.processMutants(ms, al, alreadydone, slicesize, this, false);
	}
	
	private void printStats(String cop, String cpr) {
		if(!firstPrinting){
			ConsoleTools.rewindLine(prs.size()+1);
		}else{
			firstPrinting = false;
		}

		for(int i=0; i<25; i++){
			ConsoleTools.write(" ");
		}
		
		for(String s : ops){
			ConsoleTools.write(" | ");
			ConsoleTools.write(s, ConsoleTools.BOLD);

			for(int i=0; i<6-s.length(); i++){
				ConsoleTools.write(" ");
			}

		}

		ConsoleTools.endLine();

		for(String s : prs){
			
			if(s.length() < 25){
				ConsoleTools.write(s);
				for(int i=0; i<25-s.length(); i++){
					ConsoleTools.write(" ");
				}
			}else{
				ConsoleTools.write(s.substring(0, 25));
			}

			for(String ss : ops){
				//MutationStatistics<?> ms = files.get(s).get(ss);
				int rm = remains.get(s).get(ss).size();
				
				int v = rm;
				ConsoleTools.write(" | ");
				
				if(cop.equals(ss) && cpr.equals(s)){
					ConsoleTools.write(Integer.toString(v), ConsoleTools.BG_CYAN);
				}else if(remains.get(s).get(ss).size() <= 0){
					ConsoleTools.write(Integer.toString(v), ConsoleTools.FG_RED);
				}else{
					ConsoleTools.write(Integer.toString(v));
				}
				
				for(int i=0; i<6-Integer.toString(v).length(); i++){
					ConsoleTools.write(" ");
				}
			}

			ConsoleTools.endLine();
		}
	}

	@Override
	public void testSuiteExecutionStart(int nbtest, int length, String cmd) {
	}

	@Override
	public void testCaseException(int nbtest, String readLine, String[] executedCommand) {
	}

	@Override
	public void testCaseExecutionFinished(int cpt, String[] all, String[] fail, String[] ignored, String[] hang) {
	}

	@Override
	public void testCaseNewFail(int cpt, String line) {
	}

	@Override
	public void testCaseNotPermitted(int cpt, String line) {
	}

	@Override
	public void testCaseNewIgnored(int cpt, String line) {
	}

	@Override
	public void testCaseEntered(int cpt, String line) {
	}

	@Override
	public void testCaseUndeterminedTest(int cpt, String line) {
	}

	@Override
	public void testCaseOtherCase(int cpt, String line) {
	}

	@Override
	public void testCaseNewLoop(int cpt, String line) {
	}

	@Override
	public void testCaseFailureInfos(int cpt, String line) {
	}

	@Override
	public void testSuiteUnrunnable(int cpt, String aTest, String line) {
	}

	@Override
	public void mutantStarted(String id) {
	}

	@Override
	public void mutantPersisting(String id) {
	}

	@Override
	public void mutantSkippedDueToException(String id) {
	}

	@Override
	public void mutantEnded(String id) {
	}

	@Override
	public void mutantLocked() {
	}

	@Override
	public void mutantException(Exception e) {
	}

	@Override
	public void mutantAlreadyDone() {
	}

	@Override
	public void currentTimeout(int timeout) {
	}

	@Override
	public void newTimeout(int timeout) {
	}
}
