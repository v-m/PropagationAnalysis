package com.vmusco.softminer.run;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.persistence.GraphML;

public class GraphFacts {
	
	public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
		Options options = new Options();

		options.addOption(new Option("h", "help", false, "display this message"));

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if(cmd.getArgList().size() < 1 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(String.format(" [options] <graphfile>"), options);

			System.exit(0);
		}		
		Graph g = new GraphStream();
		GraphML mgl = new GraphML(g);
		mgl.load(new FileInputStream((String) cmd.getArgList().get(0)));
		
		System.out.println(String.format("Graph %s", cmd.getArgList().get(0)));
		System.out.println(String.format("Graph structure: %d nodes, %d edges.", g.getNbNodes(), g.getNbEdges()));
		
		System.out.println(String.format("Strongly connected components: %d (giant size= %d)", g.getNbConnectedComponents(), g.getNbGiantComponents()));
	}

}
