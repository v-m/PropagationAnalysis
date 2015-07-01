package com.vmusco.pminer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.run.SetsSizeForAllMutators;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.Tools;

public class RunTestAllWithInfos {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String[] h = new String[]{
				"/home/vince/Experiments/bugimpact/mutants/commons-io",
				"/home/vince/Experiments/bugimpact/ug/commons-codec",
				"/home/vince/Experiments/bugimpact/ug/commons-collections4",
				"/home/vince/Experiments/bugimpact/ug/gson",
				"/home/vince/Experiments/bugimpact/ug/shindig",
				"/home/vince/Experiments/bugimpact/ug/commons-lang",
				"/home/vince/Experiments/bugimpact/mutants/spojo",
				"/home/vince/Experiments/bugimpact/mutants/joda-time",
		};

		Map<String, String> homes = new HashMap<String, String>();
		for(String s : h){
			homes.put((new File(s)).getName().replaceAll("commons-", ""), s);
		}
		
		ArrayList<Double>[] al = new ArrayList[]{
				new ArrayList<Double>(),
				new ArrayList<Double>(),
				new ArrayList<Double>()
		};
		int i = 0;
		
		for(String homekey: new TreeSet<String>(homes.keySet())){
			String home = homes.get(homekey);
			File f = new File(home+"/mutations/main/");
			String projectname = homekey;
			
			System.out.println("***"+projectname+"***");

			String[] op = new String[]{"impact0.xml", "impactVar.xml", "impactCha.xml", "impactChaVar.xml"};
			StatisticsMutantAnalyzer fi = null;
			
			for(File ff : f.listFiles()){
				if(!ff.isDirectory())
					continue;

				String ope = ff.getName();
				// FOR EACH OPERATOR !!!!!!!!!
				boolean infirst = true;
				
				System.out.print(ope+" : ");
				for(String o : op){
					Map<String, StatisticsMutantAnalyzer> loadFile = SetsSizeForAllMutators.loadFile(f, o);
					StatisticsMutantAnalyzer sma = loadFile.get(ope);
					
					if(infirst){
						infirst = false;
						fi = sma;
					}
					
					if(fi != sma){
						double completeness1 = sma.getPartMore()+sma.getPartPerfect();
						double completeness2 = fi.getPartMore()+fi.getPartPerfect();
						double fcplt = (completeness1 - completeness2)*100;
						System.out.print("[[[[["+fcplt+"]]]]]");
						al[i].add(fcplt);
						i = (i+1)%3;
					}
					
				}
				
				System.out.println();
			}

		}
		

		Collections.sort(al[0]);
		Collections.sort(al[1]);
		Collections.sort(al[2]);
		System.out.println("MEDIAN 0 = "+Tools.median(al[0]));
		System.out.println("MEDIAN 1 = "+Tools.median(al[1]));
		System.out.println("MEDIAN 2 = "+Tools.median(al[2]));
	}
}
