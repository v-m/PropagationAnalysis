package com.vmusco.softminer.run;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public class SourceGraphGenerator implements GraphGeneratorLogic{

	private List<String> sources = new ArrayList<>();
	private String output_file;

	public static void main(String[] args) throws Exception {
		SourceGraphGenerator g = new SourceGraphGenerator();
		GraphGenerator.main(args, g);
	}
	
	@Override
	public boolean process(CommandLine cmd) {
		for(String s : cmd.getArgs()[0].split(File.pathSeparator)){
			sources.add(s);
		}
		
		for(String s : cmd.getArgs()[1].split(File.pathSeparator)){
			sources.add(s);
		}
		
		output_file = cmd.getArgs()[2]; 
				
		return true;
	}

	@Override
	public String getProjectName() {
		return null;
	}

	@Override
	public String[] getClasspath() {
		return new String[0];
	}

	@Override
	public String[] getSources() {
		return sources.toArray(new String[sources.size()]);
	}

	@Override
	public File updateOutputPath(File output) {
		return new File(output_file);
	}

	@Override
	public void updateComandLineOptions(Options options) {
	}

	@Override
	public boolean verifyCommandParameters(CommandLine cmd) {
		return cmd.getArgList().size() == 3;
	}

	@Override
	public String getSignatureHelp() {
		return "<src> <tst> <out>";
	}

	@Override
	public String getHeaderHelp() {
		return "Generate a call graph for sources <src> and tests <tst> (both eventually separated by "+File.pathSeparator+") and produce the result in <out> file";
	}

	@Override
	public String getFooterHelp() {
		return "";
	}

}
