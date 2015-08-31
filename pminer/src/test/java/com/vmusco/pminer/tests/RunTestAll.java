package com.vmusco.pminer.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.run.SetsSizeForAllMutators;
import com.vmusco.smf.utils.ConsoleTools;

public class RunTestAll {

	public static void main(String[] args) throws FileNotFoundException, IOException {
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

		Map<String, String> homes = new HashMap<String, String>();
		for(String s : h){
			homes.put((new File(s)).getName().replaceAll("commons-", ""), s);
		}

		for(String homekey: new TreeSet<String>(homes.keySet())){
			String home = homes.get(homekey);
			File f = new File(home+"/mutations/main/");
			String projectname = homekey;

			boolean first = true;
			
			ArrayList<String> fff = new ArrayList<String>();
			for(String f1 : f.list()){
				fff.add(f1);
			}
			
			Collections.sort(fff);
			
			for(String fff1 : fff){
				File ff = new File(f.getAbsoluteFile(), fff1);
				if(!ff.isDirectory())
					continue;

				String ope = ff.getName();
				// FOR EACH OPERATOR !!!!!!!!!
				boolean infirst = true;
				
				List<StatisticsMutantAnalyzer> items = new ArrayList<StatisticsMutantAnalyzer>(); 
				int maxMedFSco = 0;
				
				boolean same = true;
				
				
				for(char c = 'A'; c<'E'; c++){
					String o = "impact_"+c+".xml";
					
					Map<String, StatisticsMutantAnalyzer> loadFile = SetsSizeForAllMutators.loadFile(f, o);
					StatisticsMutantAnalyzer sma = loadFile.get(ope);
					items.add(sma);
					
					DecimalFormat nf2 = new DecimalFormat("0.00");
					
					if(items.size() > 1){
						Double d = Double.valueOf(nf2.format(items.get(maxMedFSco).getMedianFScore()));
						Double d2 = Double.valueOf(nf2.format(sma.getMedianFScore()));
						
						if(same && d != d2){
							same = false;
						}
						if(!same && d < d2){
							maxMedFSco = items.size() - 1;
						}
					}
				}
				
				for(int i = 0; i<items.size(); i++){
					StatisticsMutantAnalyzer sma = items.get(i);
					
					if(i>0){
						ConsoleTools.write(" && ");
					}else{
						SetsSizeForAllMutators.displayHeader(first, projectname, ope, sma.getAllMutatObjs().size());
						ConsoleTools.write(" & ");
						first = false;
					}
					
					if(i == maxMedFSco && !same){
						SetsSizeForAllMutators.display2(sma, true);
					}else{
						SetsSizeForAllMutators.display2(sma);
					}
				}
				
				ConsoleTools.write("\\\\\n");
			}
			
			ConsoleTools.write("\n\\midrule\n");
			
			

			
		}
	}
}
