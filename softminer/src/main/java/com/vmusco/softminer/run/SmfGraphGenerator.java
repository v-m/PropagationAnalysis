package com.vmusco.softminer.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.vmusco.smf.analysis.ProcessStatistics;

public class SmfGraphGenerator implements GraphGeneratorLogic{
	private ArrayList<String> sources = new ArrayList<String>();
	private String projname = null;
	private String[] classpath = null;
	private ProcessStatistics ps;
	private String output_file = null;
	
	public SmfGraphGenerator(String workingDir) throws IOException {
		this.ps = ProcessStatistics.rawLoad(workingDir);
	}
	
	public static void main(String[] args) throws Exception {
		SmfGraphGenerator g = new SmfGraphGenerator(args[0]);
		GraphGenerator.main(args, g);
	}

	@Override
	public boolean process(CommandLine cmd) {
		for(String s : ps.getSrcToCompile(true)){
			sources.add(s);
		}

		for(String s : ps.getSrcTestsToTreat(true)){
			sources.add(s);
		}
		
		projname = ps.getProjectName();
		classpath = ps.getClasspath();
		
		if(cmd.hasOption("output-file")){
			output_file = cmd.getOptionValue("output-file");
		}
		
		return true;
	}

	@Override
	public String getProjectName() {
		return projname;
	}

	@Override
	public String[] getClasspath() {
		return classpath;
	}

	@Override
	public String[] getSources() {
		return sources.toArray(new String[sources.size()]);
	}

	@Override
	public File updateOutputPath(File output) {
		File output_path = new File(ps.getWorkingDir(), output.getName());
		
		if(output_file != null){
			String s = output_file;
			if(s.startsWith("/"))
				output_path = new File(s);
			else
				output_path = new File(ps.getWorkingDir(), s);
		}
		
		return output_path;
	}

	@Override
	public void updateComandLineOptions(Options options) {
		options.addOption(new Option("o", "output-file", true, "Describes the output file absolute if start with / else relative from <working-dir>. This parameter is used only in case of using a smf project as input."));
	}

	@Override
	public boolean verifyCommandParameters(CommandLine cmd) {
		return cmd.getArgs().length != 1;
	}

	@Override
	public String getSignatureHelp() {
		return "<smf-project-dir>";
	}

	@Override
	public String getHeaderHelp() {
		return "Generate a call graph for the smf project pointed in <smf-project-dir> and output it to this project folder.";
	}

	@Override
	public String getFooterHelp() {
		return "";
	}
}
