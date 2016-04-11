package com.vmusco.softminer.run;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.vmusco.smf.utils.MavenTools;

public class MavenGraphGenerator implements GraphGeneratorLogic {

	private File output_path;
	private File output_final;
	private String[] classpath;
	private HashSet<String> sources = new HashSet<>();

	public static void main(String[] args) throws Exception {
		MavenGraphGenerator g = new MavenGraphGenerator();
		GraphGenerator.main(args, g);
	}
	
	@Override
	public boolean process(CommandLine cmd) {
		output_path = new File(cmd.getArgs()[2]);

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

		try {
			for(File fp : MavenTools.findAllPomsFiles(mvn_dir)){
				String fromthisfile = MavenTools.extractClassPathUsingMvnV2(fp.getParentFile().getAbsolutePath(), false);
				for(String item : fromthisfile.split(":")){
					if(item != null && item.length() > 0)
						finalcp.add(item);
				}
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		classpath = finalcp.toArray(new String[0]);

		for(String s : srcs){
			File f = new File(mvn_dir, s);

			if(!f.exists()){
				System.err.println("Unable to locate "+f.getAbsolutePath());
			}

			sources.add(f.getAbsolutePath());
		}
		
		return true;
	}

	@Override
	public String getProjectName() {
		return null;
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
		if(output_final != null)
			return output_final;

		if(output_path.exists()){
			if(output_path.isDirectory()){
				output_final = new File(output_path, output.getName());
			}else{
				output_final = output_path;
			}
		}

		return output_final;
	}

	@Override
	public void updateComandLineOptions(Options options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean verifyCommandParameters(CommandLine cmd) {
		return cmd.getArgs().length == 3;
	}

	@Override
	public String getSignatureHelp() {
		return "<maven-dir> <source-dir> <output-file>";
	}

	@Override
	public String getHeaderHelp() {
		return "Generate a call graph for a source code described in maven project <maven-dir> with resolving dependencies and using sources relatives a maven dir in <source-dir> (eventually separated by "+File.pathSeparator+", can be xxx@ to indicate the defaults xxx/src/main|xxx/test/java folders) and output it in <output-file>.";
	}

	@Override
	public String getFooterHelp() {
		return "";
	}

}
