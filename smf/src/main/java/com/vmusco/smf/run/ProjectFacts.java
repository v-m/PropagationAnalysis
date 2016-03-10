package com.vmusco.smf.run;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.CollectionsTools;

public class ProjectFacts {
	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		
		opt = new Option("m", "mutation", false, "configFile is a mutation file");
		options.addOption(opt);
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);

		// For project
		OptionGroup og = new OptionGroup();
		opt = new Option("null", "whichmethods", false, "determin in which method the mutation occurs");
		og.addOption(opt);
		options.addOptionGroup(og);
		
		// For mutation
		og = new OptionGroup();
		
		options.addOptionGroup(og);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(" [options] <configFile>", options);
			System.exit(0);
		}
		
		if(cmd.hasOption("mutation")){
			MutationStatistics ms = MutationStatistics.loadState((String) cmd.getArgList().get(0));
			
			
			if(cmd.hasOption("whichmethods")){
				Map<String, Integer> cpt = new HashMap<>();
				
				for(String mutid : ms.listViableMutants()){
					MutantIfos stats = ms.getMutationStats(mutid);
					
					if(!cpt.containsKey(stats.getMutationIn())){
						cpt.put(stats.getMutationIn(), 1);
					}else{
						cpt.put(stats.getMutationIn(), cpt.get(stats.getMutationIn())+1);
					}
				}
				
				Map<String, Integer> sortMapByValue = CollectionsTools.sortMapByValue(cpt);
				
				for(String key : sortMapByValue.keySet()){
					System.out.println(String.format("%5d %s", sortMapByValue.get(key), key));
				}
			}
		}else{
			ProcessStatistics ps = ProcessStatistics.rawLoad((String) cmd.getArgList().get(0));
			
		}
	}
}
