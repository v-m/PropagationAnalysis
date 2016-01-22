package com.vmusco.softwearn.tests;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.softminer.graphs.EdgeIdentity;
import com.vmusco.softwearn.learn.LearningGraph;

public class LearningGraphTest {
	@Test
	public void testLearningPhase(){
		LearningGraph g = TestTools.getATestingGraph(0.5f);
		
		for(EdgeIdentity ei : TestTools.edgesDataset()){
			List<EdgeIdentity> asList = Arrays.asList(g.getEdges());
			Assert.assertTrue(asList.contains(ei));
		}
		
		g.setThreshold(0.7f);
		Assert.assertEquals(0, g.getEdges().length);

		g.setEdgeThreshold("b", "a", 0.75f);
		Assert.assertEquals(1, g.getEdges().length);
		Assert.assertEquals(new EdgeIdentity("b", "a"), g.getEdges()[0]);
	}
}
