package com.vmusco.pminer.faultlocalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;

/**
 * This class defines elements to compute FL scores for a specific node
 * Note that the method {@link FaultLocalizationScore#getScore()} return simply a score computed.
 * This score must be computed prior calling the method using the {@link FaultLocalizationScore#computeScore()} method (called
 * also by {@link FaultLocalizationScore#computeScore(String)}).
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see FaultLocators
 */
public abstract class FaultLocalizationScore {
	protected FaultLocalizationStats stats;
	private double score;

	public FaultLocalizationScore(FaultLocalizationStats stats) {
		this.stats=stats;
	}

	protected abstract void computeScore();

	protected  void setScore(double score){
		this.score = score;
	}

	public double getScore(){
		return score;
	}

	public void computeScore(String node) throws BadStateException{
		stats.changeTestingNode(node);
		computeScore();
	}

	public static int wastedEffortForList(List<Double> scoreOfUnits, double scoreOfSearchedUnit) {
		Collections.sort(scoreOfUnits);
		List<Double> revlist = Lists.reverse(scoreOfUnits);
		Iterator<Double> iterator = revlist.iterator();

		int pos = 0;
		int startpos = -1;
		int endpos = -1;

		while(iterator.hasNext()){
			double currentScore = iterator.next();

			if(currentScore == scoreOfSearchedUnit){
				if(startpos == -1){
					startpos = pos;
				}
				endpos = pos;
			}

			if(currentScore < scoreOfSearchedUnit)
				break;

			pos += 1;
		}

		if(startpos == -1 || endpos == -1){
			System.out.println("MAX !!!");
			return revlist.size()-1;
		}else{
			int mid = (endpos - startpos +1) / 2;
			return startpos+mid;
		}
	}

	public Map<String, Double> getWastedEffortList(String[] entries) throws BadStateException{
		Map<String, Double> ret = new HashMap<String, Double>();
		
		for(String uut : entries){
			stats.changeTestingNode(uut);
			computeScore();
			double s = getScore();

			if(Double.isNaN(s))
				s = -1;

			ret.put(uut, s);
		}
		
		return ret;
	}
	
	public int wastedEffort(String[] entries, String effectiveMutationPoint) throws BadStateException, TargetNotFoundException {
		Map<String, Double> scores = getWastedEffortList(entries);
		
		if(!scores.containsKey(effectiveMutationPoint)){
			throw new TargetNotFoundException(effectiveMutationPoint);
		}
		
		return wastedEffortForList(new ArrayList<Double>(scores.values()), scores.get(effectiveMutationPoint));
	}


	public int wastedEffort(String effectiveMutationPoint) throws BadStateException {
		try{
			return wastedEffort(stats.getUUTs(), effectiveMutationPoint);
		}catch(TargetNotFoundException e){
			// Never occurs
			//logger.error("Exception should never been thrown !");
			e.printStackTrace();
			System.exit(1);
		}

		return -1;
	}


}
