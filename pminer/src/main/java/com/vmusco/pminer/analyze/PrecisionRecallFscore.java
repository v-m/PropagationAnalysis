package com.vmusco.pminer.analyze;

import java.util.ArrayList;

import com.vmusco.smf.utils.MutationsSetTools;
import com.vmusco.softminer.utils.Tools;

public class PrecisionRecallFscore {
	private ArrayList<Double> precisions = new ArrayList<Double>();
	private ArrayList<Double> recalls = new ArrayList<Double>();
	private ArrayList<Double> fscores = new ArrayList<Double>();

	private ArrayList<Double> cis = new ArrayList<Double>();
	private ArrayList<Double> ais = new ArrayList<Double>();
	private ArrayList<Double> fpis = new ArrayList<Double>();
	private ArrayList<Double> dis = new ArrayList<Double>();
	private ArrayList<Double> int_ais_cis = new ArrayList<Double>();

	public void newRun(String[] ais, String[] cis){
		this.cis.add(cis.length * 1.0d);
		this.ais.add(ais.length * 1.0d);

		this.fpis.add(MutationsSetTools.setDifference(cis, ais).length * 1.0d);
		this.dis.add(MutationsSetTools.setDifference(ais, cis).length * 1.0d);

		this.int_ais_cis.add(MutationsSetTools.setIntersection(ais, cis).length * 1.0d);

		this.precisions.add(precision(ais, cis));
		this.recalls.add(recall(ais, cis));
		this.fscores.add(fscore(ais, cis));
	}

	public static double precision(String[] ais, String[] cis){
		int nbinter = MutationsSetTools.setIntersection(ais, cis).length;

		if(cis.length == 0){
			return 1;
		}else{
			return nbinter/cis.length;
		}
	}

	public static double recall(String[] ais, String[] cis){
		int nbinter = MutationsSetTools.setIntersection(ais, cis).length;

		if(ais.length == 0){
			return 1;
		}else{
			return nbinter/ais.length;
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
		return Tools.average(this.precisions);
	}
	
	public double getCurrentMeanRecall(){
		return Tools.average(this.recalls);
	}
	
	public double getCurrentMeanFscore(){
		return Tools.average(this.fscores);
	}

	public double getCurrentMedianPrecision(){
		return Tools.median(this.precisions);
	}
	
	public double getCurrentMedianRecall(){
		return Tools.median(this.recalls);
	}
	
	public double getCurrentMedianFscore(){
		return Tools.median(this.fscores);
	}
}
