package com.vmusco.softminer.run;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
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
import com.vmusco.smf.testing.TestingInstrumentedCodeHelper;
import com.vmusco.smf.testing.TestsExecutionListener;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.MavenTools;
import com.vmusco.softminer.graphs.DynamicCallGraphGenerator;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.graphs.persistance.GraphPersistence;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuildLogic;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.GraphBuilder;
import com.vmusco.softminer.sourceanalyzer.graphbuilding.SpoonGraphBuilder;

/**
* Generate call graphs
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class DynamicGraphGenerator {
	public static Graph generatedGraph = null;

	static final String def_filename = "dynamic_callgraph.xml";
	
	public static void main(String[] args) throws Exception {
		generatedGraph = null;

		Options options = new Options();

		options.addOption(new Option("h", "help", false, "print this message"));
		options.addOption(new Option("d", "overwrite", false, "Delete the file if it already exists."));
		options.addOption(new Option("F", "out-format", true, "set the output format. Set help or h as a type to get the list of types"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		String out_format = "graphml";

		if(cmd.hasOption("out-format")){
			String val = cmd.getOptionValue("out-format");
			if(val.equals("graphml") /* || ... */){
				out_format = val;
			}else{
				ConsoleTools.write("Available formats: \n", ConsoleTools.BOLD);
				ConsoleTools.write("\tgraphml", ConsoleTools.BOLD);
				ConsoleTools.write(": graphml XML file (default)");

				ConsoleTools.endLine();
				System.exit(0);
			}
		}

		String type_graph = null;

		if(cmd.getArgs().length != 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			String header = "Generate a dynamic call graph for the smf project pointed in <smf-project-dir> and output it to this project folder.";
			String footer = "";
			formatter.printHelp(" [options] <smf-project-dir>", header, options, footer);


			System.exit(0);
		}

		ProcessStatistics ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);
		if(!ps.isInstrumented()){
			System.out.println("Sorry, the project have to be instrumented in order to create a dynamic call graph !");
			System.exit(1);
		}
		
		if(ps.getCurrentState() != STATE.READY){
			System.out.println("Sorry, the project have to be in READY state in order to create a dynamic call graph !");
			System.exit(1);
		}
		
		File output_path = new File(ps.getWorkingDir(), def_filename);
		if(output_path.exists()){
			if(!cmd.hasOption("overwrite")){
				System.out.println("The file "+output_path.getAbsolutePath()+" already exist. To force its overwriting use -d flag.");
				return;
			}else{
				output_path.delete();
			}
		}
		
		// Building graph part
		DynamicCallGraphGenerator tel = new DynamicCallGraphGenerator();
		TestingInstrumentedCodeHelper.setEnteringPrinting(true);
		TestingInstrumentedCodeHelper.setLeavingPrinting(true);
		
		ps.performFreshTesting(tel);
		Graph aGraph = tel.getGraph();
		
		// Persistence part
		GraphPersistence gp = null;

		if(out_format.equals("graphml")){
			gp = new GraphML(aGraph);
		}

		gp.save(new FileOutputStream(output_path));

		generatedGraph = aGraph;
	}
}
