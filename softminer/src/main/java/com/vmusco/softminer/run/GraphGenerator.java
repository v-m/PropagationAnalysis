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
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.MavenTools;
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
	
	public static void main(String[] args) throws Exception {
		generatedGraph = null;

		Options options = new Options();

		options.addOption(new Option("h", "help", false, "print this message"));
		options.addOption(new Option("o", "output-file", true, "Describes the output file absolute if start with / else relative from <working-dir>. This parameter is used only in case of using a smf project as input."));
		options.addOption(new Option("d", "overwrite", false, "Delete the file if it already exists."));
		options.addOption(new Option("F", "out-format", true, "set the output format. Set help or h as a type to get the list of types"));
		options.addOption(new Option("c", "cp", true, "add entries in classpath. Can be separated by "+File.pathSeparator));
		options.addOption(new Option("r", "remove-isolated", false, "remove isolated nodes from the final graph"));
		
		
		options.addOption(new Option("t", "type", true, "select a specific type of call graph (override -r, -f and -c). Set help or h as a type to get the list of types."));
		options.addOption(new Option("c", "cha", false, "resolve interfaces and classes"));
		options.addOption(new Option("f", "fields", false, "resolve interfaces and classes"));
		options.addOption(new Option("r", "no-overridden-calls", false, "resolve calls to overriden methods"));

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

		if((cmd.getArgs().length != 1 && cmd.getArgs().length != 3) || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			String header = "Generate a call graph for the smf project pointed in <smf-project-dir> and output it to this project folder or a source code described in maven project <maven-dir> with resolving dependencies and using sources relatives a maven dir in <source-dir> (eventually separated by "+File.pathSeparator+", can be xxx@ to indicate the defaults xxx/src/main|xxx/test/java folders) and output it in <output-file>.";
			String footer = "";
			formatter.printHelp(" [options] <smf-project-dir> | <<maven-dir> <source-dir> <output-file>>", header, options, footer);


			System.exit(0);
		}

		GraphBuildLogic builder = SpoonGraphBuilder.getFeatureGranularityGraphBuilder();
		
		ArrayList<String> sources = new ArrayList<String>();
		String projname = null;
		String[] classpath = null;
		
		File output_path = new File(defaultFilename(type_graph));
		
		if(cmd.getArgs().length == 3){
			output_path = new File(cmd.getArgs()[2]);

			if(output_path.exists()){
				if(output_path.isDirectory()){
					output_path = new File(output_path, defaultFilename(type_graph));
				}
			}
			
			if(output_path.exists()){
				if(!cmd.hasOption("overwrite")){
					System.out.println("The file "+output_path.getAbsolutePath()+" already exist. To force its overwriting use -d flag.");
					return;
				}else{
					output_path.delete();
				}
			}
			
			String mvn_dir = cmd.getArgs()[0];
			
			HashSet<String> srcsh = new HashSet<String>();
			String[] srcs;
			
			srcs = cmd.getArgs()[1].split(File.pathSeparator);
			for(String s: srcs){
				if(s.endsWith("@")){
					if(s.equals("@")){
						srcsh.add("src/main/java");
						srcsh.add("src/test/java");
					}else{
						srcsh.add(s.substring(0, s.length()-1)+"/src/main/java");
						srcsh.add(s.substring(0, s.length()-1)+"/src/test/java");
					}
				}else{
					srcsh.add(s);
				}
			}
			srcs = srcsh.toArray(new String[0]);
			
			Set<String> finalcp = new HashSet<String>();

			for(File fp : MavenTools.findAllPomsFiles(mvn_dir)){
				String fromthisfile = MavenTools.extractClassPathUsingMvnV2(fp.getParentFile().getAbsolutePath(), false);
				for(String item : fromthisfile.split(":")){
					if(item != null && item.length() > 0)
						finalcp.add(item);
				}
			}

			classpath = finalcp.toArray(new String[0]);
			
			for(String s : srcs){
				File f = new File(mvn_dir, s);
				
				if(!f.exists()){
					System.err.println("Unable to locate "+f.getAbsolutePath());
				}
				
				sources.add(f.getAbsolutePath());
			}
		}else{
			ProcessStatistics ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);
			
			for(String s : ps.getSrcToCompile(true)){
				sources.add(s);
			}

			for(String s : ps.getSrcTestsToTreat(true)){
				sources.add(s);
			}
			
			projname = ps.getProjectName();
			classpath = ps.getClasspath();
			
			output_path = new File(ps.getWorkingDir(), defaultFilename(type_graph));
			if(cmd.hasOption("output-file")){
				String s = cmd.getOptionValue("output-file");
				if(s.startsWith("/"))
					output_path = new File(s);
				else
					output_path = new File(ps.getWorkingDir(), s);
			}

			if(output_path.exists()){
				if(!cmd.hasOption("overwrite")){
					System.out.println("The file "+output_path.getAbsolutePath()+" already exist. To force its overwriting use -d flag.");
					return;
				}else{
					output_path.delete();
				}
			}
		}
		
		
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
		

		for(String cp : classpath){
			System.out.println("+CP: "+cp);
		}

		GraphBuilder gb;

		if(type_graph != null){
			if(type_graph.equals("cha")){
				gb = GraphBuilder.newGraphBuilderWithInheritence(projname, sources.toArray(new String[0]), classpath);
			}else if(type_graph.equals("f")){
				gb = GraphBuilder.newGraphBuilderWithFields(projname, sources.toArray(new String[0]), classpath);
			}else if(type_graph.equals("chaf")){
				gb = GraphBuilder.newGraphBuilderWithFieldsAndInheritence(projname, sources.toArray(new String[0]), classpath);
			}else if(type_graph.equals("m")){
				gb = GraphBuilder.newGraphBuilderOnlyWithDependenciesWithoutOverriden(projname, sources.toArray(new String[0]), classpath);
			}else{
				gb = GraphBuilder.newGraphBuilderOnlyWithDependencies(projname, sources.toArray(new String[0]), classpath);
			}
		}else{
			gb = GraphBuilder.newGraphBuilderManuallyConfigured(projname, sources.toArray(new String[0]), classpath, cmd.hasOption("cha"), cmd.hasOption("fields"), cmd.hasOption("no-overridden-calls"));
		}

		if(cmd.hasOption("remove-isolated")){
			ProcessorCommunicator.includeAllNodes = false;
		}
		
		ProcessorCommunicator.prefixSourceCodeToRemove = cmd.getArgs()[0];
		
		Graph aGraph = gb.generateDependencyGraph(builder);

		GraphPersistence gp = null;

		if(out_format.equals("graphml")){
			gp = new GraphML(aGraph);
		}

		gp.save(new FileOutputStream(output_path));

		generatedGraph = aGraph;
	}
}
