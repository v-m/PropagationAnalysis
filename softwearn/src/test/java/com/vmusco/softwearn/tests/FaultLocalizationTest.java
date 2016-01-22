package com.vmusco.softwearn.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.GraphFaultLocalizationByIntersection;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.learner.BinaryAlgorithm;
import com.vmusco.softwearn.learn.learner.NoAlgorithm;

public class FaultLocalizationTest {

	@Test
	public void testFaultLocBinary(){
		// LEARNING
		LearningGraph g = TestTools.runTestFor(new BinaryAlgorithm());
		
		// TESTING
		g.setThreshold(1);
		GraphFaultLocalizationByIntersection t = new GraphFaultLocalizationByIntersection(g.graph());
		t.visit(new String[]{"f", "e", "g"});
		String[] computeTestingResult = t.getLastConsequenceNodes();

		String[] expRes = new String[]{};
		Assert.assertEquals(expRes.length, computeTestingResult.length);
		Assert.assertArrayEquals(expRes, computeTestingResult);
	}
	
	@Test
	public void testFaultLocNoAlgo(){
		LearningGraph g = TestTools.runTestFor(new NoAlgorithm());
		GraphFaultLocalizationByIntersection t = new GraphFaultLocalizationByIntersection(g.graph());
		g.setThreshold(1);
		t.visit(new String[]{"f", "e", "g"});
		String[] computeTestingResult = t.getLastConsequenceNodes();
		 
		String[] expRes = new String[]{"a"};
		Assert.assertEquals(expRes.length, computeTestingResult.length);
		Assert.assertArrayEquals(expRes, computeTestingResult);
	}
	
}
