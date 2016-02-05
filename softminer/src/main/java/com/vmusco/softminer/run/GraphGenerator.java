package com.vmusco.softminer.run;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.Graph;
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
public class GraphGenerator {
	public static Graph generatedGraph = null;

	private static String defaultFilename(String type_graph){
		String def_filename = "callgraph%s.xml";
		
		if(type_graph == null){
			return String.format(def_filename, "");
		}else if(type_graph.equals("cha")){
			return String.format(def_filename, "_cha");
		}else if(type_graph.equals("f")){
			return String.format(def_filename, "_f");
		}else if(type_graph.equals("chaf")){
			return String.format(def_filename, "_f_cha");
		}else if(type_graph.equals("m")){
			return String.format(def_filename, "_m");
		}else{
			return String.format(def_filename, "");
		}
	}
	
	public static void main(String[] args, GraphGeneratorLogic g) throws Exception {
		generatedGraph = null;

		Options options = new Options();

		options.addOption(new Option("h", "help", false, "print this message"));
		options.addOption(new Option("d", "overwrite", false, "Delete the file if it already exists."));
		options.addOption(new Option("F", "out-format", true, "set the output format. Set help or h as a type to get the list of types"));
		options.addOption(new Option("C", "cp", true, "add entries in classpath. Can be separated by "+File.pathSeparator));
		options.addOption(new Option("r", "remove-isolated", false, "remove isolated nodes from the final graph"));
		options.addOption(new Option("x", "noclasspath", false, "do not resolve the class path for building the graph"));
		
		
		options.addOption(new Option("t", "type", true, "select a specific type of call graph (override -f and -c). Set help or h as a type to get the list of types."));
		options.addOption(new Option("c", "cha", false, "resolve interfaces and classes"));
		options.addOption(new Option("f", "fields", false, "resolve fields accesses"));
		options.addOption(new Option("o", "no-overridden-calls", false, "resolve calls to overriden methods"));

		g.updateComandLineOptions(options);
		
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

		if(cmd.hasOption("type")){
			String val = cmd.getOptionValue("type");

			if(val.equals("cg") ||
					val.equals("cha") ||
					val.equals("f") ||
					val.equals("chaf") ||
					val.equals("m")){
				type_graph = val;
			}else{
				ConsoleTools.write("Available call graphs formats: \n", ConsoleTools.BOLD);
				ConsoleTools.write("\tcg", ConsoleTools.BOLD);
				ConsoleTools.write(": classic call graph (default)\n");
				ConsoleTools.write("\tcha", ConsoleTools.BOLD);
				ConsoleTools.write(": call graph wich CHA\n");
				ConsoleTools.write("\tf", ConsoleTools.BOLD);
				ConsoleTools.write(": call graph wich fields\n");
				ConsoleTools.write("\tchaf", ConsoleTools.BOLD);
				ConsoleTools.write(": call graph wich CHA and fields\n");
				ConsoleTools.write("\tm", ConsoleTools.BOLD);
				ConsoleTools.write(": classic call graph without overriden methods calls");

				ConsoleTools.endLine();
				System.exit(0);
			}
		}

		if(!g.verifyCommandParameters(cmd) || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(String.format(" [options] %s", g.getSignatureHelp()), g.getHeaderHelp(), options, g.getFooterHelp());


			System.exit(0);
		}

		GraphBuildLogic builder = SpoonGraphBuilder.getFeatureGranularityGraphBuilder();
		
		g.process(cmd);
		File output_path = new File(defaultFilename(type_graph));
		output_path = g.updateOutputPath(output_path);
		
		
		if(output_path.exists()){
			if(!cmd.hasOption("overwrite")){
				System.out.println("The file "+output_path.getAbsolutePath()+" already exist. To force its overwriting use -d flag.");
				return;
			}else{
				output_path.delete();
			}
		}
		
		String[] classpath = g.getClasspath();
		
		if(cmd.hasOption("cp")){
			Set<String> entries = new HashSet<String>();
			
			for(String c : classpath){
				entries.add(c);
			}
			for(String c : cmd.getOptionValue("cp").split(File.pathSeparator)){
				entries.add(c);
			}
			
			classpath = entries.toArray(new String[0]);
		}
		

		/*for(String cp : classpath){
			System.out.println("+CP: "+cp);
		}*/

		GraphBuilder gb;

		if(type_graph != null){
			if(type_graph.equals("cha")){
				gb = GraphBuilder.newGraphBuilderWithInheritence(g.getProjectName(), g.getSources(), classpath);
			}else if(type_graph.equals("f")){
				gb = GraphBuilder.newGraphBuilderWithFields(g.getProjectName(), g.getSources(), classpath);
			}else if(type_graph.equals("chaf")){
				gb = GraphBuilder.newGraphBuilderWithFieldsAndInheritence(g.getProjectName(), g.getSources(), classpath);
			}else if(type_graph.equals("m")){
				gb = GraphBuilder.newGraphBuilderOnlyWithDependenciesWithoutOverriden(g.getProjectName(), g.getSources(), classpath);
			}else{
				gb = GraphBuilder.newGraphBuilderOnlyWithDependencies(g.getProjectName(), g.getSources(), classpath);
			}
		}else{
			gb = GraphBuilder.newGraphBuilderManuallyConfigured(g.getProjectName(), g.getSources(), classpath, cmd.hasOption("cha"), cmd.hasOption("fields"), cmd.hasOption("no-overridden-calls"));
		}

		if(cmd.hasOption("remove-isolated")){
			ProcessorCommunicator.includeAllNodes = false;
		}
		
		ProcessorCommunicator.prefixSourceCodeToRemove = cmd.getArgs()[0];
		
		Graph aGraph = null;
		if(cmd.hasOption("noclasspath")){
			gb.setNoClassPath();
		}
		
		aGraph = gb.generateDependencyGraph(builder);
		GraphPersistence gp = null;

		if(out_format.equals("graphml")){
			gp = new GraphML(aGraph);
		}

		gp.save(new FileOutputStream(output_path));

		generatedGraph = aGraph;
	}
}
