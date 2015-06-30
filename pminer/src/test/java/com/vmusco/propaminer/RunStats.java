package com.vmusco.propaminer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.vmusco.pminer.run.DatasetStats;
import com.vmusco.smf.utils.ConsoleTools;

public class RunStats {
	public static void main(String[] args) throws Exception {
		String[] h = new String[]{
				"/home/vince/Experiments/bugimpact/mutants/commons-io",
				"/home/vince/Experiments/bugimpact/ug/commons-codec",
				"/home/vince/Experiments/bugimpact/ug/commons-collections4",
				"/home/vince/Experiments/bugimpact/ug/gson",
				"/home/vince/Experiments/bugimpact/ug/shindig",
				"/home/vince/Experiments/bugimpact/ug/commons-lang",
				"/home/vince/Experiments/bugimpact/mutants/joda-time",
				"/home/vince/Experiments/bugimpact/mutants/spojo",
				"/home/vince/Experiments/bugimpact/ug/jgit",
				"/home/vince/Experiments/bugimpact/ug/sonar"
		};

		Map<String, String> homes = new HashMap<String, String>();
		for(String s : h){
			homes.put((new File(s)).getName(), s);
		}
		

		String type="1";
		
		
		if(true){
			for(String homekey : new TreeSet<String>(homes.keySet())){
				String home = homes.get(homekey);
				
				String[] s = new String[]{
						type,
						home+"/smf.skitty.run.xml",
						home+"/usegraph_A.xml",
						home+"/usegraph_D.xml",
				};
				
				DatasetStats.main(s);
			}
			
			if(type.equals("1"))
				ConsoleTools.write("\\textbf{Total} & & & & \\textbf{"+DatasetStats.totalLOC+"} && \\textbf{"+DatasetStats.totalNodes+"} & \\textbf{"+DatasetStats.totalEdges+"} && \\textbf{"+DatasetStats.totalNodesCHA+"} & \\textbf{"+DatasetStats.totalEdgesCHA+"}\\\\");
		}

	}
}