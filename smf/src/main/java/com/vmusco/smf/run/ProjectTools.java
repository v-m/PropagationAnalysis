package com.vmusco.smf.run;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.utils.ConsoleTools;

public class ProjectTools {
	private static final Class<?> thisclass = CreateMutation.class;

	public static void main(String[] args) throws Exception {

		Options options = new Options();

		Option opt;
		opt = new Option("f", "fix", false, "fix the working dir with the current working directory");
		options.addOption(opt);

		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <configFile>", options);
			System.exit(0);
		}

		ConsoleTools.write("GENERAL FACTS\n", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);
		ConsoleTools.write("=============\n", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);

		ProcessStatistics ps = null;
		ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);

		ConsoleTools.write("Project name: ", ConsoleTools.BOLD);
		ConsoleTools.write(ps.getProjectName());
		ConsoleTools.endLine();

		ConsoleTools.write("Project root: ", ConsoleTools.BOLD);
		ConsoleTools.write(ps.getWorkingDir());

		File f = new File(ps.getWorkingDir());
		File ff = new File(cmd.getArgs()[0]);
		if(!ff.isDirectory())	ff = ff.getParentFile();

		if(!f.equals(ff)){
			ConsoleTools.write(" (may require fixing)", ConsoleTools.BOLD, ConsoleTools.FG_RED);
		}
		ConsoleTools.endLine();

		ConsoleTools.write("Dataset root: ", ConsoleTools.BOLD);
		ConsoleTools.write(ps.getProjectIn(false));
		ConsoleTools.endLine();

		ConsoleTools.endLine();
		ConsoleTools.write("MUTATIONS\n", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);
		ConsoleTools.write("=========\n", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);

		ConsoleTools.write("Mutation working dir: ", ConsoleTools.BOLD);
		ConsoleTools.write(ps.getMutantsBasedir());
		ConsoleTools.endLine();

		ConsoleTools.write("\nAvailable mutants\n", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);

		File ftmp = new File(ps.resolveThis(ps.getMutantsIdsBaseDir()));

		for(File fftmp : ftmp.listFiles()){
			if(!fftmp.isDirectory())
				continue;

			ConsoleTools.write("  "+fftmp.getName(), ConsoleTools.FG_CYAN);
			ConsoleTools.endLine();

			File ffftmp = new File(ps.resolveThis(ps.getMutantsOpsBaseDir(fftmp.getName())));

			for(File fffftmp : ffftmp.listFiles()){
				if(!fftmp.isDirectory())
					continue;

				ConsoleTools.write("    "+fffftmp.getName(), ConsoleTools.FG_YELLOW);

				String pth = ps.resolveThis(ps.getMutantsBasedir());
				pth = pth.replace("{id}", fftmp.getName());
				pth = pth.replace("{op}", fffftmp.getName());

				MutationStatistics<?> ms = MutationStatistics.loadState(pth);

				//int treated = ms.loadResultsForExecutedTestOnMutants(0).length;
				
				int treated = 0;
				int not_viable = 0;

				for(String mid : ms.getAllMutationsId()){
					MutantIfos mutationStats = ms.getMutationStats(mid);

					if(!mutationStats.isViable())
						not_viable++;
					else{
						if(!mutationStats.isExecutionKnown()){
							mutationStats.setExecutedTests(ms.checkIfExecutionExists(mid));
						}
						
						treated += mutationStats.isExecutedTests()?1:0;
					}
				}

				ConsoleTools.write("....."+ms.getMutationsSize()+" mutants - "+treated+" treated, "+not_viable+" unviables");
				ConsoleTools.endLine();
			}
		}

	}
}
