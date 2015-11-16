package com.vmusco.softminer.run;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

public interface GraphGeneratorLogic {
	boolean process(CommandLine cmd);
	String getProjectName();
	String[] getClasspath();
	String[] getSources();
	File updateOutputPath(File output);
	void updateComandLineOptions(Options options);
	boolean verifyCommandParameters(CommandLine cmd);
	String getSignatureHelp();
	String getHeaderHelp();
	String getFooterHelp();
}
