package com.vmusco.softwearn.tests;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.impact.GraphPropagationExplorerForTests;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.learner.BinaryAlgorithm;
import com.vmusco.softwearn.learn.learner.NoAlgorithm;

public class LearningAlgorithmTest {

	@Test
	public void testNoAlgo(){
		LearningGraph g = TestTools.runTestFor(new NoAlgorithm());

		Assert.assertEquals(1, g.getEdgeThreshold("f", "b"), 0);
		Assert.assertEquals(1, g.getEdgeThreshold("b", "a"), 0);
		Assert.assertEquals(1, g.getEdgeThreshold("e", "c"), 0);
		Assert.assertEquals(1, g.getEdgeThreshold("c", "a"), 0);
		Assert.assertEquals(1, g.getEdgeThreshold("g", "c"), 0);
	}

	@Test
	public void testBinary(){
		LearningGraph g = TestTools.runTestFor(new BinaryAlgorithm());

		Assert.assertEquals(1, g.getEdgeThreshold("f", "b"), 0);
		Assert.assertEquals(1, g.getEdgeThreshold("b", "a"), 0);
		Assert.assertEquals(0, g.getEdgeThreshold("e", "c"), 0);
		Assert.assertEquals(0, g.getEdgeThreshold("c", "a"), 0);
		Assert.assertEquals(0, g.getEdgeThreshold("g", "c"), 0);
	}

	@Test
	public void testImpactTesterNoAlgo() throws SpecialEntryPointException{
		LearningGraph g = TestTools.runTestFor(new NoAlgorithm());
		GraphPropagationExplorerForTests t = new GraphPropagationExplorerForTests(g.graph(), new String[]{"f", "e", "g"});
		g.setThreshold(1);
		t.visit(new String[]{"a"});
		String[] computeTestingResult = t.getLastConsequenceNodes(); 

		String[] expRes = new String[]{"f", "e", "g"};
		Assert.assertEquals(expRes.length, computeTestingResult.length);
		Assert.assertArrayEquals(expRes, computeTestingResult);
	}

	@Test
	public void testImpactTesterBinary() throws SpecialEntryPointException{
		LearningGraph g = TestTools.runTestFor(new BinaryAlgorithm());
		GraphPropagationExplorerForTests t = new GraphPropagationExplorerForTests(g.graph(), new String[]{"f", "e", "g"});
		g.setThreshold(1);
		t.visit(new String[]{"a"});
		String[] computeTestingResult = t.getLastConsequenceNodes(); 

		String[] expRes = new String[]{"f"};
		Assert.assertEquals(expRes.length, computeTestingResult.length);
		Assert.assertArrayEquals(expRes, computeTestingResult);
	}
}
