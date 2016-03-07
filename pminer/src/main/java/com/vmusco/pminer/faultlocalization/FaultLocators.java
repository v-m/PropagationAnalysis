package com.vmusco.pminer.faultlocalization;

import java.util.Arrays;
import java.util.List;

/**
 * This class defines factory methods for getting FL techniques to use
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class FaultLocators {
	private FaultLocators() {
	}

	/*
	 * ALL STATS:
	    double failed = this.stats.getNbExecutingFailed();
		double nonexec_failed = this.stats.getNbNonExecutingFailed();
		double passed = this.stats.getNbExecutingPassed();
		double nonexec_passed = this.stats.getNbNonExecutingPassed();
		double totalfailed = this.stats.getNbTotalFailed();
		double totalpassed = this.stats.getNbTotalPassed();
	 */
	
	/*****
	 * EXECUTION BASED
	 */
	public static FaultLocalizationScore getTarantula(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats){
			@Override
			protected void computeScore() {
				double failed = this.stats.getNbExecutingFailed();
				double passed = this.stats.getNbExecutingPassed();
				double totalfailed = this.stats.getNbTotalFailed();
				double totalpassed = this.stats.getNbTotalPassed();

				setScore( (failed/totalfailed) /
						((passed/totalpassed) + (failed/totalfailed)) );
			}
		};
	}

	public static FaultLocalizationScore getOchiai(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats){
			@Override
			protected void computeScore() {
				double failed = this.stats.getNbExecutingFailed();
				double nonexec_failed = this.stats.getNbNonExecutingFailed();
				double passed = this.stats.getNbExecutingPassed();

				setScore((failed)/(Math.sqrt((failed+passed)*(failed+nonexec_failed))));

				/*setScore( totalpassed /
						Math.sqrt(totalfailed * (failed+passed)) );*/
			}
		};
	}

	public static FaultLocalizationScore getNaish(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats){

			@Override
			protected void computeScore() {
				double nonexec_failed = this.stats.getNbNonExecutingFailed();
				double nonexec_passed = this.stats.getNbNonExecutingPassed();
				
				if(nonexec_failed > 0)	setScore(-1);
				else					setScore(nonexec_passed);
			}
		};
	}
	
	public static FaultLocalizationScore getTarantulaStar(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats){

			@Override
			protected void computeScore() {
				double failed = this.stats.getNbExecutingFailed();
				double passed = this.stats.getNbExecutingPassed();
				double totalfailed = this.stats.getNbTotalFailed();
				double totalpassed = this.stats.getNbTotalPassed();
				
				FaultLocalizationScore tarantula = getTarantula(this.stats);
				tarantula.computeScore();
				setScore(tarantula.getScore() * Math.max((passed/totalpassed), (failed/totalfailed)));
			}
		};
	}
	
	
	/*****
	 * GRAPH BASED
	 */
	
	/**
	 * This approach takes into consideration the size of the intersected graph.
	 * size of the intersected if in the set, 0 otherwise
	 * @param stats
	 * @return
	 */
	public static FaultLocalizationScore getIntersectionMaxSizeApporach(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats) {
			@Override
			protected void computeScore() {
				boolean contains = Arrays.asList(this.stats.getLastIntersectedNodes()).contains(this.stats.getCurrentTestingNode());
				
				setScore(contains?1:0);
			}
		};
	}
	
	
	
	
	/*****
	 * GRAPH + EXECUTION BASED
	 */
	
	/**
	 * Score = # of passing test for which there is at least one path from the test and the analyzed node
	 * @param stats
	 * @return
	 */
	public static FaultLocalizationScore getPassingTestsConnectionsMethod(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats) {
			@Override
			protected void computeScore() {
				
				int cpt = 0;

				for(String test : stats.getExecutingPassedTests()){
					cpt += stats.getBaseGraph().isThereAtLeastOnePath(test, stats.getCurrentTestingNode())?1:0;
				}

				setScore( cpt );
			}
		};
	}
	
	/**
	 * Score = # of paths existing between all passing tests and the analyzed node
	 * @param stats
	 * @return
	 */
	public static FaultLocalizationScore getPassingTestsConnectionsPaths(FaultLocalizationStats stats){
		return new FaultLocalizationScore(stats) {
			@Override
			protected void computeScore() {
				
				int cpt = 0;

				for(String test : stats.getExecutingPassedTests()){
					List<String[]> pths = stats.getBaseGraph().getPaths(test, stats.getCurrentTestingNode());
					cpt += (pths==null)?0:pths.size();
				}

				setScore( cpt );
			}
		};
	}

	/**
	 * Zoltar implementation
	 * @param stats
	 * @return
	 */
	public static FaultLocalizationScore getZoltar(FaultLocalizationStats stats) {
		return new FaultLocalizationScore(stats) {
			@Override
			protected void computeScore() {
				double failed = this.stats.getNbExecutingFailed();
				double nonexec_failed = this.stats.getNbNonExecutingFailed();
				double passed = this.stats.getNbExecutingPassed();

				setScore(failed / (failed + passed + nonexec_failed + ( (10000 * passed  * nonexec_failed) / failed ) ) );
			}
		};
	}
	
}
