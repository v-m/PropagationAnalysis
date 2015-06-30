package com.vmusco.softminer.run;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.graphs.persistance.GraphPersistence;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuildLogic;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;
import com.vmusco.softminer.sourceanalyzer.processors.FeaturesProcessor;

public class GraphGenerator {
	private static final String DEFAULT_USEGRAPH_FILENAME = "usegraph.xml";
	public static Graph generatedGraph = null;

	public static void main(String[] args) throws Exception {
		generatedGraph = null;

		Options options = new Options();

		options.addOption(new Option("h", "help", false, "print this message"));
		options.addOption(new Option("O", "show-out-format", false, "list the available output format"));
		options.addOption(new Option("o", "output-file", true, "Describes the output file absolute if start with / else relative from <working-dir> (default: working-dir/"+DEFAULT_USEGRAPH_FILENAME+")"));
		Option delprev = new Option("d", "delete-previous", false, "Delete the file if it already exists.");
		options.addOption(delprev);
		options.addOption(new Option("F", "out-format", true, "set the output format"));
		options.addOption(new Option("c", "cha", false, "resolve interfaces and classes"));
		options.addOption(new Option("f", "fields", false, "resolve interfaces and classes"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.hasOption("show-out-format")){
			ConsoleTools.write("Available formats: \n", ConsoleTools.BOLD);
			ConsoleTools.write("\tgraphml", ConsoleTools.BOLD);
			ConsoleTools.write(": graphml XML file (default)");

			ConsoleTools.endLine();
			System.exit(0);
		}

		String out_format = "graphml";

		if(cmd.hasOption("out-format")){
			String val = cmd.getOptionValue("out-format");
			if(!val.equals("graphml") /* && ... */){
				out_format = val;
			}else{
				ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED);
				ConsoleTools.write("Unknown output format "+val+"\n");
				System.exit(1);
			}
		}

		if(cmd.getArgs().length < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			String header = "Generate a graph for the project pointed in <working-dir>.";
			String footer = "";
			formatter.printHelp(GraphGenerator.class.getCanonicalName()+" [options] <working-dir> <output-file>", header, options, footer);


			System.exit(0);
		}

		ProcessStatistics ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);

		File output_path = new File(ps.getWorkingDir(), DEFAULT_USEGRAPH_FILENAME);
		if(cmd.hasOption("output-file")){
			String s = cmd.getOptionValue("output-file");
			if(s.startsWith("/"))
				output_path = new File(s);
			else
				output_path = new File(ps.getWorkingDir(), s);
		}

		if(output_path.exists()){
			if(!cmd.hasOption("delete-previous")){
				System.out.println("The file "+output_path.getAbsolutePath()+" already exist. To force its overwriting use "+delprev.getOpt()+" flag.");
				return;
			}else{
				output_path.delete();
			}
		}

		GraphBuildLogic builder = SpoonGraphBuilder.getFeatureGranularityGraphBuilder();

		ArrayList<String> sources = new ArrayList<String>();

		for(String s : ps.srcToCompile){
			sources.add(ps.getProjectIn(true) + File.separator + s);
		}

		for(String s : ps.srcTestsToTreat){
			sources.add(ps.getProjectIn(true) + File.separator + s);
		}

		GraphBuilder gb;
		if(cmd.hasOption("cha")){
			if(cmd.hasOption("fields")){
				gb = GraphBuilder.newGraphBuilderWithFieldsAndInheritence(ps.projectName, sources.toArray(new String[0]), ps.getClasspath());
			}else{
				gb = GraphBuilder.newGraphBuilderWithInheritence(ps.projectName, sources.toArray(new String[0]), ps.getClasspath());
			}
		}else{
			if(cmd.hasOption("fields")){
				gb = GraphBuilder.newGraphBuilderWithFields(ps.projectName, sources.toArray(new String[0]), ps.getClasspath());
			}else{
				gb = GraphBuilder.newGraphBuilderOnlyWithDependencies(ps.projectName, sources.toArray(new String[0]), ps.getClasspath());
			}
		}
		 
		Graph aGraph = gb.generateDependencyGraph(builder);

		GraphPersistence gp = null;

		if(out_format.equals("graphml")){
			gp = new GraphML(aGraph);
		}


		gp.save(new FileOutputStream(output_path));

		generatedGraph = aGraph;
	}
}
