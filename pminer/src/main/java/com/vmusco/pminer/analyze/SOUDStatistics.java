package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.utils.MutationsSetTools;
import com.vmusco.softminer.utils.Tools;

/**
 * Class used to compute 
 *  - size of sets;
 *  - the S (same) O (overestimated) U (underestimated) D (different) sets.
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class SOUDStatistics {
	private List<Double> cis = new ArrayList<Double>();
	private List<Double> ais = new ArrayList<Double>();
	private List<Double> fpis = new ArrayList<Double>();
	private List<Double> dis = new ArrayList<Double>();
	private List<Double> inter = new ArrayList<Double>();
	
	private List<String> same = new ArrayList<String>();
	private List<String> overestimate = new ArrayList<String>();
	private List<String> underestimate = new ArrayList<String>();
	private List<String> different = new ArrayList<String>();
	
	private int cases = 0;
	
	public void cumulate(String mutationid, String[] ais, String[] cis){
		int cis_size = cis.length;
		int ais_size = ais.length;
		
		int fpis_size = MutationsSetTools.setDifference(cis, ais).length;
		int dis_size = MutationsSetTools.setDifference(ais, cis).length;
		int inter_size = MutationsSetTools.setIntersection(ais, cis).length;
		
		this.cis.add(cis_size * 1.0d);
		this.ais.add(ais_size * 1.0d);

		this.fpis.add(fpis_size * 1.0d);
		this.dis.add(dis_size * 1.0d);
		this.inter.add(inter_size * 1.0d);

		if(fpis_size == 0 && dis_size == 0)
			same.add(mutationid);
		else if(fpis_size > 0 && dis_size <= 0)
			overestimate.add(mutationid);
		else if(fpis_size <= 0 && dis_size > 0)
			underestimate.add(mutationid);
		else
			different.add(mutationid);
		
		cases++;
	}
	
	public double getCurrentMeanActualImpactSetSize(){
		return Tools.average(Tools.toDoubleArray(this.ais));
	}
	
	public double getCurrentMedianActualImpactSetSize(){
		return Tools.median(Tools.toDoubleArray(this.ais));
	}
	
	public double getCurrentMeanCandidateImpactSetSize(){
		return Tools.average(Tools.toDoubleArray(this.cis));
	}
	
	public double getCurrentMedianCandidateImpactSetSize(){
		return Tools.median(Tools.toDoubleArray(this.cis));
	}
	
	public double getCurrentMeanFalsePositiveImpactSetSize(){
		return Tools.average(Tools.toDoubleArray(this.fpis));
	}
	
	public double getCurrentMedianFalsePositiveImpactSetSize(){
		return Tools.median(Tools.toDoubleArray(this.fpis));
	}
	
	public double getCurrentMeanDiscoveredImpactSetSize(){
		return Tools.average(Tools.toDoubleArray(this.dis));
	}
	
	public double getCurrentMedianDiscoveredImpactSetSize(){
		return Tools.median(Tools.toDoubleArray(this.dis));
	}
	
	public double getCurrentMeanIntersectedImpactSetSize(){
		return Tools.average(Tools.toDoubleArray(this.inter));
	}
	
	public double getCurrentMedianIntersectedImpactSetSize(){
		return Tools.median(Tools.toDoubleArray(this.inter));
	}

	public int getNbSame(){
		return same.size();
	}

	public int getNbOverestimated(){
		return overestimate.size();
	}
	
	public int getNbUnderestimated(){
		return underestimate.size();
	}

	public int getNbDifferent(){
		return different.size();
	}
	
	public double getNbSameProportion(){
		return (getNbSame()*1d) / cases;
	}
	
	public double getNbOverestimatedProportion(){
		return (getNbOverestimated()*1d) / cases;
	}
	
	public double getNbUnderestimatedProportion(){
		return (getNbUnderestimated()*1d) / cases;
	}
	
	public double getNbDifferentProportion(){
		return (getNbDifferent()*1d) / cases;
	}
	
	public int getNbCases(){
		return cases;
	}
	
	public int getLastCandidateImpactSetSize(){
		return (int) cis.get(cis.size() - 1).doubleValue();
	}

	public int getLastActualImpactSetSize(){
		return (int) ais.get(ais.size() - 1).doubleValue();
	}

	public int getLastFalsePositiveImpactSetSize(){
		return (int) fpis.get(fpis.size() - 1).doubleValue();
	}

	public int getLastDiscoveredImpactSetSize(){
		return (int) dis.get(dis.size() - 1).doubleValue();
	}
	
	public int getLastIntersectedImpactSetSize(){
		return (int) inter.get(inter.size() - 1).doubleValue();
	}
}

