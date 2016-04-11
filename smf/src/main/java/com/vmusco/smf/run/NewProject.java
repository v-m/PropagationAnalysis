package com.vmusco.smf.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.vmusco.smf.compilation.compilers.JavaxCompilation;
import com.vmusco.smf.instrumentation.AbstractInstrumentationProcessor;
import com.vmusco.smf.instrumentation.MethodInInstrumentationProcessor;
import com.vmusco.smf.instrumentation.StackTracePrintingInstrumentationProcessor;
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
		opt = new Option("C", "link-classpath", true, "use this classpath instead of determining it automatically ("+useAsSepString+"). Folders are resolved in all jar contained in root of folder.");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option("c", "copy-classpath", true, "copy the manualy specified classpath instead of determining it automatically ("+useAsSepString+"). Folders are resolved in all jar contained in root of folder.");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option("", "parent-pom", true, "Obtain the dependencies from a parent pom file specified.");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option(null, "no-classpath", false, "do not determine classpath automatically (no classpath)");
		options.addOption(opt);
		opt = new Option("F", "force", false, "Overwritte the working directory if it already exists !");
		options.addOption(opt);
		opt = new Option("i", "instrument", false, "instrument the source code (dynamic informations recolting -- default: false).");
		options.addOption(opt);

		// OPTIONS FOR PHASE 2
		opt = new Option("R", "reset", false, "reset the execution state (return to build phase)");
		options.addOption(opt);
		opt = new Option("", "testsnorun", false, "do not run tests execution");
		options.addOption(opt);
		opt = new Option("T", "reset-tests", false, "reset without rebuilding (return to test phase)");
		options.addOption(opt);

		// OPTIONS FOR BOTH PHASES
		opt = new Option("r", "ressources", true, "ressources used for testing ("+useAsSepString+")");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option("s", "sources", true, "sources to compile ("+useAsSepString+" - default: "+ProcessStatistics.DEFAULT_SOURCE_FOLDER+")");
		opt.setArgName("path");
		options.addOption(opt);
		opt = new Option("t", "tests", true, "tests to compile ("+useAsSepString+" - default: "+ProcessStatistics.DEFAULT_TEST_FOLDER+")");
		options.addOption(opt);
		opt = new Option(null, "no-tests", false, "Defines there is no tests in this project (debugging purposes)");
		options.addOption(opt);
		opt = new Option(null, "compliance", true, "Java source code compliance level (1,2,3,4,5, 6, 7 or 8). (default: 8)");
		opt.setArgName("compliance");
		options.addOption(opt);
		opt = new Option(null, "jre", true, "Set a different jre working folder than the one present in the system classpath (eg. /opt/altjre/bin/)");
		opt.setArgName("path");
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

		if(cmd.getArgs().length == 3){
			File f2 = new File(cmd.getArgs()[2]);

			ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, f.getAbsolutePath());

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

			ConsoleTools.write("Copying files to project-local copies...\n");
			ps.createLocalCopies(ProcessStatistics.SOURCES_COPY, ProcessStatistics.CLASSPATH_PACK);

			if(cmd.hasOption("compliance")){
				int compval = Integer.parseInt(cmd.getOptionValue("compliance"));
				ConsoleTools.write(String.format("Compliance level set to %d...\n", compval));
				ps.setComplianceLevel(compval);
			}

			if(cmd.hasOption("jre")){
				String jre = cmd.getOptionValue("jre");
				ConsoleTools.write(String.format("Using JRE at %s...\n", jre));
				ps.setAlternativeJre(jre);
			}

			if(!cmd.hasOption("link-classpath") && !cmd.hasOption("copy-classpath") && !cmd.hasOption("no-classpath")){
				ps.setCpLocalFolder(ProcessStatistics.CLASSPATH_PACK);
				ps.setSkipMvnClassDetermination(false);
				if(cmd.hasOption("parent-pom")){
					ps.exportClassPath(cmd.getOptionValue("parent-pom"));
				}else{
					ps.exportClassPath(ps.getProjectIn(true));
				}
				
			}else{
				ps.setSkipMvnClassDetermination(true);
				ps.setCpLocalFolder(null);

				if(cmd.hasOption("link-classpath") || cmd.hasOption("copy-classpath")){
					String cpInput = cmd.hasOption("link-classpath")?cmd.getOptionValue("link-classpath"):cmd.getOptionValue("copy-classpath");

					Set<String> set = new HashSet<>();

					for(String s : cpInput.split(File.pathSeparator)){
						File tmpdep = new File(s);

						if(tmpdep.isDirectory()){
							for(File ff : tmpdep.listFiles()){
								if(ff.isFile() && ff.getName().endsWith(".jar")){
									set.add(ff.getAbsolutePath());
								}
							}
						}else{
							set.add(s);
						}
					}

					ps.setOriginalClasspath(set.toArray(new String[0]));

					if(!cmd.hasOption("link-classpath")){
						ps.copyOriginalClasspathIn(ProcessStatistics.CLASSPATH_PACK);
					}
				}
			}

			ps.setProjectName(cmd.getArgs()[1]);
			ProcessStatistics.saveState(ps);

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

		List<AbstractInstrumentationProcessor> aip = new ArrayList<>(); 

		if(cmd.hasOption("instrument")){
			aip.add(new MethodInInstrumentationProcessor());
		}

		runWithPs(ps, aip.toArray(new AbstractInstrumentationProcessor[0]), cmd.hasOption("testsnorun"));
		System.out.println("Done.");
	}

	private static void runWithPs(ProcessStatistics ps, AbstractInstrumentationProcessor[] aips, boolean skipTestsExecution) throws Exception{
		NewProject np = new NewProject(ps.buildPath("tests_execution.log"));
		np.execname = "original";

		np.resetAndOpenStream();

		System.out.println();
		System.out.println("Current state is: " + ps.getCurrentState());

		System.out.print("Building project.....");
		if(ps.getCurrentState() == STATE.FRESH){
			System.out.println();
			boolean ret = false;

			Compilation c = new JavaxCompilation();

			if(aips.length > 0){
				ret = ps.instrumentAndBuildProjectAndTests(c, aips);
			}else{
				ret = ps.build(c);
			}

			if(ret){
				ProcessStatistics.saveState(ps);
			}else{
				System.out.println("Building failed. Error summary:");
				for(int i = 0; i<c.getNumberErrorsWhileLastBuild(); i++){
					String msg = c.getErrorsWhileLastBuild(i);
					System.out.println(msg);
				}
				System.exit(1);
			}

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}

		System.out.print("Running tests.....");

		if(skipTestsExecution){
			System.out.println("Test execution skipped. This project cannot be used for mutation execution as long as tests are not run...");
		}else{
			if(ps.getCurrentState() == STATE.BUILD){
				System.out.println();
				ps.performFreshTesting(np);
				ProcessStatistics.saveState(ps);

				System.out.println("\n********************\n");
			}else{
				System.out.println("SKIP");
			}
		}

		np.closeStream();
	}
}
