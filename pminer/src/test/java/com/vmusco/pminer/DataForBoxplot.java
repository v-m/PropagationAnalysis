package com.vmusco.pminer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import com.vmusco.pminer.analyze.StatisticsMutantAnalyzer;
import com.vmusco.pminer.analyze.UseGraphMutantStats;
import com.vmusco.pminer.persistence.UseGraphStatsXml;
import com.vmusco.softminer.Tools;

public class DataForBoxplot {


	public static void main(String[] args) throws Exception {
		String[] h = new String[]{
				"/home/vince/Experiments/bugimpact/ug/commons-codec",
				"/home/vince/Experiments/bugimpact/ug/commons-collections4",
				"/home/vince/Experiments/bugimpact/ug/gson",
				"/home/vince/Experiments/bugimpact/ug/jgit",
				"/home/vince/Experiments/bugimpact/mutants/commons-io",
				"/home/vince/Experiments/bugimpact/mutants/joda-time",
				"/home/vince/Experiments/bugimpact/ug/commons-lang",
				"/home/vince/Experiments/bugimpact/ug/shindig",
				"/home/vince/Experiments/bugimpact/mutants/spojo",
				"/home/vince/Experiments/bugimpact/ug/sonar"
		};

		String plots = "";
		
		for(String home : h){
			//System.out.println("===== "+home+" =====");
			
			File ff = new File(home);
			
			File muts = new File(home, "mutations/main/ABS/impact_A.xml");
			File t = new File(muts.getParentFile(), "boxplot");
			
			if(t.exists())
				t.delete();
			
			StatisticsMutantAnalyzer sma = UseGraphStatsXml.restoreResults(new FileInputStream(muts));
			
			//FileOutputStream fos = new FileOutputStream(t);
			String projname = ff.getName().replaceAll("commons-", "");
			plots += "rep (\""+projname+"\", times = "+sma.getAllMutatObjs().size()+"),\n";
			
			ArrayList<Integer> al = new ArrayList<Integer>();
			
			for(UseGraphMutantStats s : sma.getAllMutatObjs()){
				al.add(s.graphsize);
				
				//System.out.println(Integer.toString(s.graphsize));
				//fos.write(Integer.toString(s.graphsize).getBytes());
				//fos.write('\n');
			}
			
			
			Collections.sort(al);
			System.out.println(projname+" => "+Tools.medianInt(al)+" - max = "+al.get(al.size()-1));
			
			for(Integer i : al)
				System.out.println(i);
			//fos.close();
		}
		
		System.out.println(plots);

	}
}
