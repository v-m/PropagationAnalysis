package com.vmusco.softminer.runs;

import java.io.File;

import com.vmusco.softminer.run.GraphGenerator;

public class Test1 {
	public static void main(String[] args) throws Exception {
		String[] proj = new String[]{
			//"/home/vince/Experiments/bugimpact/mutants/joda-time",
			//"/home/vince/Experiments/bugimpact/mutants/commons-io",
			//"/home/vince/Experiments/bugimpact/mutants/ug/commons-codec",
			//"/home/vince/Experiments/bugimpact/mutants/ug/commons-collections4",
			//"/home/vince/Experiments/bugimpact/mutants/ug/gson",
			//"/home/vince/Experiments/bugimpact/mutants/ug/shindig",
			//"/home/vince/Experiments/bugimpact/mutants/ug/commons-lang",
			//"/home/vince/Experiments/bugimpact/mutants/spojo",
			"/home/vince/Experiments/bugimpact/mutants/ug/jgit",
			"/home/vince/Experiments/bugimpact/mutants/ug/sonar"
		};
		
		String dstfold = "/home/vince/Experiments/bugimpact/usegraphs";

		for(String p : proj){
			File f = new File(dstfold, new File(p).getName());
			f.mkdirs();
			
			String[] ug1 = new String[]{p, 					    "-o", f.getAbsolutePath() + File.separator +"usegraph_A.xml", "-d"};
			String[] ug2 = new String[]{p, "-fields", 		    "-o", f.getAbsolutePath() + File.separator +"usegraph_B.xml", "-d"};
			String[] ug3 = new String[]{p, 			   "--cha", "-o", f.getAbsolutePath() + File.separator +"usegraph_C.xml", "-d"};
			String[] ug4 = new String[]{p, "--fields", "--cha", "-o", f.getAbsolutePath() + File.separator +"usegraph_D.xml", "-d"};

			System.out.println(p);
			System.out.println("Ver A");
			GraphGenerator.main(ug1);
			System.out.println("NbEdges: "+GraphGenerator.generatedGraph.getNbEdges());

			System.out.println(p);
			System.out.println("Ver B");
			GraphGenerator.main(ug2);
			System.out.println("NbEdges: "+GraphGenerator.generatedGraph.getNbEdges());
			System.out.println(p);

			System.out.println("Ver C");
			GraphGenerator.main(ug3);
			System.out.println("NbEdges: "+GraphGenerator.generatedGraph.getNbEdges());

			System.out.println("Ver D");
			GraphGenerator.main(ug4);
			System.out.println("NbEdges: "+GraphGenerator.generatedGraph.getNbEdges());
		}
	}
}
