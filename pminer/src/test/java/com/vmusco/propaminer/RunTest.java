package com.vmusco.propaminer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.vmusco.pminer.run.SetsSizeForAllMutators;

public class RunTest {
	public static void main(String[] args) throws Exception {
		String[] h = new String[]{
				"/home/vince/Experiments/bugimpact/mutants/commons-io",
				"/home/vince/Experiments/bugimpact/mutants/ug/commons-codec",
				"/home/vince/Experiments/bugimpact/mutants/ug/commons-collections4",
				"/home/vince/Experiments/bugimpact/mutants/ug/gson",
				"/home/vince/Experiments/bugimpact/mutants/ug/shindig",
				"/home/vince/Experiments/bugimpact/mutants/ug/commons-lang",
				"/home/vince/Experiments/bugimpact/mutants/spojo",
				"/home/vince/Experiments/bugimpact/mutants/joda-time",
				"/home/vince/Experiments/bugimpact/mutants/ug/jgit",
				"/home/vince/Experiments/bugimpact/mutants/ug/sonar"
		};

		String ug_loc = "/home/vince/Experiments/bugimpact/usegraphs";
		
		Map<String, String> homes = new HashMap<String, String>();
		for(String s : h){
			homes.put((new File(s)).getName(), s);
		}

		if(true){
			for(String homekey: new TreeSet<String>(homes.keySet())){
				String home = homes.get(homekey);

				System.out.println(home);
				for(char c = 'A'; c<'E'; c++){

					String[] s = new String[]{
							home+"/mutations/main/",
							ug_loc + File.separator + new File(home).getName() +"/usegraph_"+c+".xml",
							"-s", "impact_"+c+".xml",
							"-z"
					};

					SetsSizeForAllMutators.main(s);
				}
			}
		}


		if(false){

			for(char c = 'A'; c<'D'; c++){
				
				System.out.println("Graph "+c);
				
				for(String homek : new TreeSet<String>(homes.keySet())){
					String home = homes.get(homek);

					String[] s = new String[]{
							home+"/mutations/main/",
							"-l", "impact_"+c+".xml",
					};
					SetsSizeForAllMutators.main(s);
				}

				System.out.println("**********");
				
			}

		}
	}
}