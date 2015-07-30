package com.vmusco.pminer.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.persistence.UseGraphStatsXml;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;

public class DatasetStats {
	public static int totalLOC = 0;
	public static int totalNodes = 0;
	public static int totalEdges = 0;
	public static int totalNodesCHA = 0;
	public static int totalEdgesCHA = 0;
	
	public static void main(String[] args) throws IOException {

		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(1);

		File fold = new File(args[1]).getParentFile();
		ProcessStatistics ps = ProcessStatistics.rawLoad(args[1]);

		Graph usegraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		GraphML gml = new GraphML(usegraph);
		gml.load(new FileInputStream(args[2]));
		
		Graph usegraph2 = null;
		if(args.length > 3){
			usegraph2 = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
			GraphML gml2 = new GraphML(usegraph2);
			gml2.load(new FileInputStream(args[3]));
		}

		String[] nv = getProjectNameAndVersion(ps);

		if(args[0].equals("0")){
			ConsoleTools.write(nv[0]+" & ");
				
			ConsoleTools.write(nf.format(ps.getRunTestsOriginalTime()/1000f)+"s & ");
			ConsoleTools.write(nf.format(usegraph.getBuildTime()/1000f)+"s & ");
			
			// TIMES
			File muts = new File(fold, "mutations/main");
			int nb = 0;
			double med = 0;
			
			for(File f : muts.listFiles()){
				if(f.isDirectory()){
					File fimp = new File(f, "impact_A.xml");
					StatisticsMutantAnalyzer sma = UseGraphStatsXml.restoreResults(new FileInputStream(fimp));
					med =+ sma.getMedianExecTime();
					nb++;
				}
			}
			
			med /= nb;
			
			ConsoleTools.write(nf.format(med/1000f)+"ms \\\\");
			/*ConsoleTools.write(ps.testCases.length+" & ");
			ConsoleTools.write((ps.runTestsOriginalTime/ps.testCases.length)+"ms \\\\");*/
			ConsoleTools.endLine();
		}else{
			ConsoleTools.write(nv[0]+" & ");
			ConsoleTools.write(nv[1].split("-")[0]+" & ");
	
			if(ps.isGitProject())
				ConsoleTools.write("\\#"+getGitCurrentCommit(ps)+" (git) && ");
			else
				ConsoleTools.write(getSvnCurrentRevision(ps)+" (svn) && ");
			
			DatasetStats.totalLOC += getNbLineOfCode(ps);
			ConsoleTools.write(getNbLineOfCode(ps)+" && ");
			DatasetStats.totalNodes += usegraph.getNbNodes(NodeTypes.METHOD);
			DatasetStats.totalEdges += usegraph.getNbEdges(EdgeTypes.METHOD_CALL);
			ConsoleTools.write(usegraph.getNbNodes(NodeTypes.METHOD)+" & ");
			ConsoleTools.write(usegraph.getNbEdges(EdgeTypes.METHOD_CALL)+" && ");
			
			if(usegraph2 != null){
				DatasetStats.totalNodesCHA += usegraph2.getNbNodes();
				DatasetStats.totalEdgesCHA += usegraph2.getNbEdges();
				ConsoleTools.write(usegraph2.getNbNodes()+" & ");
				ConsoleTools.write(usegraph2.getNbEdges()+"");
			}
			
			ConsoleTools.write("\\\\");
			ConsoleTools.endLine();
		}
	}

	private static String[] getProjectNameAndVersion(ProcessStatistics ps) throws IOException {
		String[] ret = new String[2];

		ret[0] = getProjectInfoExpression(ps, "project.name");
		ret[1] = getProjectInfoExpression(ps, "project.version");

		return ret;
	}

	private static String getProjectInfoExpression(ProcessStatistics ps, String expr) throws IOException{
		String[] cmd = new String[]{"mvn", "help:evaluate", "-Dexpression="+expr};
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(ps.getProjectIn(true)));
		pb.redirectErrorStream(true);
		Process proc = pb.start();

		final InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String line;
		String lastvalidline = null;

		while((line = br.readLine()) != null){
			if(line.charAt(0) == '[')
				continue;
			else
				lastvalidline = line;
		}

		return lastvalidline;
	}

	private static int getNbLineOfCode(ProcessStatistics ps) throws IOException{
		int nb = 0;
		
		for(String sf : ps.getSrcToCompile(true)){
			int ret = getNbLineOfCode(sf);
			if(ret > 0)
				nb += ret;
			else{
				System.out.println("Nothing found in root folder");
			}
		}

		for(String sf : ps.getSrcTestsToTreat(true)){
			int ret = getNbLineOfCode(sf);
			if(ret > 0)
				nb += ret;
			else{
				System.out.println("Nothing found in root folder");
			}
		}
		
		return nb;
	}

	private static int getNbLineOfCode(String forFolder) throws IOException{
		String[] cmd = new String[]{"cloc", "--quiet", forFolder};
		ProcessBuilder pb = new ProcessBuilder(cmd);
		//pb.directory(new File(ps.getProjectIn(true)));
		pb.redirectErrorStream(true);
		Process proc = pb.start();

		final InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String line;
		boolean startanalyze = false;

		while((line = br.readLine()) != null){
			if(!startanalyze && line.contains("---------------------")){
				startanalyze = true;
				continue;
			}

			if(line.startsWith("Java")){
				int pos = line.lastIndexOf(' ');
				return Integer.parseInt(line.substring(pos+1));
			}
		}

		return -1;
	}

	private static String getSvnCurrentRevision(ProcessStatistics ps) throws IOException{
		String[] cmd = new String[]{"svn", "info"};
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(ps.getProjectIn(true)));
		pb.redirectErrorStream(true);
		Process proc = pb.start();

		final InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String line;

		while((line = br.readLine()) != null){
			if(line.startsWith("Revision: ")){
				return "r"+line.substring("Revision: ".length());
			}
		}

		return null;
	}

	private static String getGitCurrentCommit(ProcessStatistics ps) throws IOException{
		String[] cmd = new String[]{"git", "log", "-1", "--pretty=\"%h\""};
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(ps.getProjectIn(true)));
		pb.redirectErrorStream(true);
		Process proc = pb.start();

		final InputStream is = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		final BufferedReader br = new BufferedReader(isr);

		String l = br.readLine();
		if(l.charAt(0) == '"')
			l = l.substring(1);
		if(l.charAt(l.length()-1) == '"')
			l = l.substring(0, l.length()-1);
		
		return l;
	}

}
