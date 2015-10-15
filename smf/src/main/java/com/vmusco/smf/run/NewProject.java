package com.vmusco.smf.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.ProcessStatistics.STATE;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.utils.ConsoleTools;

/**
 * This class defines a main function for generating a new project definition
 * It create all required working resources and attempts a first resolution of 
 * the project maven resource in order to determine the appropriated classpath.
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class NewProject extends GlobalTestRunning {	
	private NewProject(String logging) throws FileNotFoundException{
		super(logging);
	}

	public static void main(String[] args) throws Exception {
		String useAsSepString = "use "+File.pathSeparator+" as separator";

		Options options = new Options();

		Option opt;
		
		// OPTIONS FOR PHASE 1
		opt = new Option("c", "classpath", true, "use this classpath instead of determining it automatically ("+useAsSepString+")");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option(null, "no-classpath", false, "do not determine classpath automatically (no classpath)");
		options.addOption(opt);
		opt = new Option("l", "do-not-copy", false, "do not copy the content in the project (not recommended !)");
		options.addOption(opt);
		opt = new Option("F", "force", false, "Overwritte the working directory if it already exists !");
		options.addOption(opt);
		opt = new Option("j", "just-prepare", false, "Just prepare the project directory. Do not execute any build/tests/...");
		options.addOption(opt);
		
		// OPTIONS FOR PHASE 2
		opt = new Option("R", "reset", false, "reset the execution state (return to build phase)");
		options.addOption(opt);
		opt = new Option("T", "reset-tests", false, "reset without rebuilding (return to test phase)");
		options.addOption(opt);
		
		// OPTIONS FOR BOTH PHASES
		opt = new Option("r", "ressources", true, "ressources used for testing ("+useAsSepString+")");
		options.addOption(opt);
		opt = new Option("s", "sources", true, "sources to compile ("+useAsSepString+" - default: "+ProcessStatistics.DEFAULT_SOURCE_FOLDER+")");
		options.addOption(opt);
		opt = new Option("t", "tests", true, "tests to compile ("+useAsSepString+" - default: "+ProcessStatistics.DEFAULT_TEST_FOLDER+")");
		options.addOption(opt);
		opt = new Option(null, "no-tests", false, "Defines there is no tests in this project (debugging purposes)");
		options.addOption(opt);
		
		
		
		
		
		opt = new Option("h", "help", false, "print this message");
		options.addOption(opt);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if( (cmd.getArgs().length != 1 && cmd.getArgs().length != 3) || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			
			String head = "Create a new project in <working-dir> based on <project-in> named <name>. Copy, resolve dependencies, build source and tests in order to use with further parts.";
			String foot = "";
					
			formatter.printHelp("[options] <working-dir> <name> <project-in>", head, options, foot);
			System.exit(0);
		}
		
		File f = new File(cmd.getArgs()[0]);
		ProcessStatistics ps;
		
		boolean skipRunWithPs = false;
		
		if(cmd.getArgs().length == 3){
			File f2 = new File(cmd.getArgs()[2]);
			
			ps = ProcessStatistics.rawCreateProject(ProcessStatistics.SOURCES_COPY, f.getAbsolutePath());
			
			if(ps.workingDirAlreadyExists()){
				if(!cmd.hasOption("force")){
					ConsoleTools.write("Unable to create this project as it already exists. To overwrite, use --force option.\n");
					System.exit(1);
				}else{
					ConsoleTools.write("Overwritting working directory...\n");
				}
			}
			ps.createWorkingDir();
			ps.setProjectIn(f2.getAbsolutePath());
			
			
			if(!cmd.hasOption("do-not-copy")){
				ConsoleTools.write("Copying files to project-local copies...\n");
				ps.createLocalCopies(ProcessStatistics.SOURCES_COPY, ProcessStatistics.CLASSPATH_PACK);
			}
			
			if(!cmd.hasOption("classpath") && !cmd.hasOption("no-classpath")){
				ps.setCpLocalFolder(ProcessStatistics.CLASSPATH_PACK);
				ps.setSkipMvnClassDetermination(false);
				ps.exportClassPath();
			}else{
				ps.setSkipMvnClassDetermination(true);
				if(cmd.hasOption("classpath")){
					Set<String> set = new HashSet<>();
					
					for(String s : cmd.getOptionValue("classpath").split(File.pathSeparator)){
						set.add(s);
					}
	
					ps.setOriginalClasspath(set.toArray(new String[0]));
				}
				ps.setCpLocalFolder(null);
			}
			
			ps.setProjectName(cmd.getArgs()[1]);
			ProcessStatistics.saveState(ps);
			
			skipRunWithPs = cmd.hasOption("just-prepare");
			
			System.out.printf("Project generated in: %s\n", ps.getWorkingDir());
		}else{
			ps = ProcessStatistics.rawLoad(f.getAbsolutePath());
			
			if(cmd.hasOption("reset")){
				ps.setCurrentState(STATE.FRESH);
			}else if(cmd.hasOption("reset-tests")){
				ps.setCurrentState(STATE.BUILD);
			}
		}
		
		if(cmd.hasOption("ressources")){
			ps.setRessources(cmd.getOptionValue("ressources").split(File.pathSeparator));
		}

		if(cmd.hasOption("sources")){
			ps.setSrcToCompile(cmd.getOptionValue("sources").split(File.pathSeparator));
		}

		if(cmd.hasOption("tests")){
			ps.setSrcTestsToTreat(cmd.getOptionValue("tests").split(File.pathSeparator));
		}else if(cmd.hasOption("no-tests")){
			ps.setSrcTestsToTreat(new String[]{});
		}
		
		if(!skipRunWithPs){
			runWithPs(ps);
		}else{
			ProcessStatistics.saveState(ps);
		}
		
		System.out.println("Done.");
	}

	private static void runWithPs(ProcessStatistics ps) throws Exception{
		NewProject np = new NewProject(ps.buildPath("tests_execution.log"));
		np.execname = "original";

		np.resetAndOpenStream();
		
		System.out.println();
		System.out.println("Current state is: " + ps.getCurrentState());

		System.out.print("Building project.....");
		if(ps.getCurrentState() == STATE.FRESH){
			System.out.println();
			if(ps.compileWithSpoon()){
				ProcessStatistics.saveState(ps);
			}else{
				System.out.println("Building failed. Didn't you forget to run 'mvn clean install' priorly ?");
				System.exit(1);
			}

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}

		System.out.print("Running tests.....");
		if(ps.getCurrentState() == STATE.BUILD){
			System.out.println();
			ps.performFreshTesting(np);

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}

		np.closeStream();
	}
}
