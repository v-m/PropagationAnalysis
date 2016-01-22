package com.vmusco.pminer.tests.faultloc;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalizationOLD.FaultLocalizationAnalyzerOnExecution;

public class FaultLocalizationTests {

	private static List<Double> generateBasicData(){
		List<Double> scores = new ArrayList<>();
		
		scores.add(1d);
		scores.add(2d);
		scores.add(3d);
		scores.add(4d);
		scores.add(5d);
		
		return scores;
	}
	
	@Test
	public void testFirstScoreWithOneOccurence(){
		List<Double> scores = generateBasicData();
		Assert.assertEquals(0, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 5d));
	}
	
	@Test
	public void testFirstScoreWithSeveralOccurences(){
		List<Double> scores = generateBasicData();
		scores.add(5d);
		scores.add(5d);
		scores.add(5d);

		Assert.assertEquals(2, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 5d));
	}
	
	@Test
	public void testLastScoreWithOneOccurence(){
		List<Double> scores = generateBasicData();
		Assert.assertEquals(4, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 1d));
	}
	
	@Test
	public void testLastScoreWithSeveralOccurences(){
		List<Double> scores = generateBasicData();
		scores.add(1d);
		scores.add(1d);
		scores.add(1d);
		Assert.assertEquals(6, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 1d));
	}
	

	@Test
	public void testMidScoreWithOneOccurence(){
		List<Double> scores = generateBasicData();
		Assert.assertEquals(2, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 3d));
	}
	
	@Test
	public void testMidScoreWithSeveralOccurences(){
		List<Double> scores = generateBasicData();
		scores.add(3d);
		scores.add(3d);
		scores.add(3d);
		Assert.assertEquals(4, FaultLocalizationAnalyzerOnExecution.wastedEffortForList(scores, 3d));
	}
}
