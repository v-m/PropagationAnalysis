package com.vmusco.smf.run;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.TargetObtainer;
import com.vmusco.smf.utils.ConsoleTools;

/**
 * This entry point is used to obtain informations about a mutation project
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ProjectTools {
	private static final Class<?> thisclass = ProjectTools.class;

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
				int maxmutat = 0;

				Factory factory = Mutation.obtainFactory();
				CtElement[] mutations = Mutation.getMutations(ps, ms, factory);

				for(CtElement e : mutations){
					HashMap<CtElement, TargetObtainer> mutatedEntriesWithTargets = Mutation.obtainsMutationCandidates(ms, e, factory, false);
					if(mutatedEntriesWithTargets != null)
						maxmutat += mutatedEntriesWithTargets.size();
				}

				int treated = 0;
				int not_viable = 0;
				int alivemut  = 0;

				for(String mid : ms.listMutants()){
					try{
						ms.loadMutationStats(mid);
						treated += 1;
						
						if(ms.isMutantAlive(mid)){
							alivemut++;
						}
					}catch(MutationNotRunException e){
						// If mutation not run yet, just skip it from the count process
					}
					
					MutantIfos mutationStats = ms.getMutationStats(mid);
					if(!mutationStats.isViable()){
						not_viable++;
					}
				}

				ConsoleTools.write("....."+ms.getMutationsSize()+" mutants (max: "+maxmutat+") - "+treated+" treated, "+not_viable+" unviables. [Remaining] To generate: "+(maxmutat-ms.getMutationsSize())+". To test:"+(maxmutat-treated-not_viable)+". Alive mutants: "+alivemut);
				ConsoleTools.endLine();
			}
		}

	}
}
