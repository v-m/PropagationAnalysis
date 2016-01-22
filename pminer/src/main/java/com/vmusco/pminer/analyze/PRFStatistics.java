package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.pminer.utils.Tools;
import com.vmusco.smf.utils.SetTools;

/**
 * Class used to compute precision, recall and fscores
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class PRFStatistics {
	private List<Double> precisions = new ArrayList<Double>();
	private List<Double> recalls = new ArrayList<Double>();
	private List<Double> fscores = new ArrayList<Double>();

	public void cumulate(String[] ais, String[] cis){
		double computed;

		if(cis == null){
			this.precisions.add(0d);
			this.recalls.add(0d);
			this.fscores.add(0d);
		}else{
			computed = precision(ais, cis);
			this.precisions.add(computed);
	
			computed = recall(ais, cis);
			this.recalls.add(computed);
	
			computed = fscore(ais, cis);
			this.fscores.add(computed);
		}
	}
	
	public void removesNulls(){
		while(this.precisions.contains(0))
			this.precisions.remove(new Double(0));
		while(this.recalls.contains(new Double(0)))
			this.recalls.remove(new Double(0));
		while(this.fscores.contains(new Double(0)))
			this.fscores.remove(new Double(0));
	}

	public static double precision(String[] ais, String[] cis){
		int nbinter = SetTools.setIntersection(ais, cis).length;

		if(cis.length == 0){
			return (ais.length>0)?1:0;
		}else{
			return (nbinter * 1d) /cis.length;
		}
	}

	public static double recall(String[] ais, String[] cis){
		int nbinter = SetTools.setIntersection(ais, cis).length;

		if(ais.length == 0){
			return (cis.length>0)?1:0;
		}else{
			return (nbinter * 1d)/ais.length;
		}
	}

	public static double fscore(String[] ais, String[] cis){
		double p = precision(ais, cis);
		double r = recall(ais, cis);

		if(p+r == 0){
			return 0;
		}else{
			return 2 * ((p*r) / (p+r));
		}
	}

	public double getCurrentMeanPrecision(){
		return Tools.average(Tools.toDoubleArray(this.precisions));
	}

	public double getCurrentMeanRecall(){
		return Tools.average(Tools.toDoubleArray(this.recalls));
	}

	public double getCurrentMeanFscore(){
		return Tools.average(Tools.toDoubleArray(this.fscores));
	}

	public double getCurrentMedianPrecision(){
		return Tools.median(Tools.toDoubleArray(this.precisions));
	}

	public double getCurrentMedianRecall(){
		return Tools.median(Tools.toDoubleArray(this.recalls));
	}

	public double getCurrentMedianFscore(){
		return Tools.median(Tools.toDoubleArray(this.fscores));
	}

	public double getLastPrecision(){
		return precisions.get(precisions.size() - 1).doubleValue();
	}

	public double getLastRecall(){
		return recalls.get(recalls.size() - 1).doubleValue();
	}

	public double getLastFscore(){
		return fscores.get(fscores.size() - 1).doubleValue();
	}
}
