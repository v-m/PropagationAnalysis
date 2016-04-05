package com.vmusco.softwearn.tests;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.Assert;
import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softwearn.learn.LearningGraphStream;
import com.vmusco.softwearn.learn.LearningKGraphStream;
import com.vmusco.softwearn.learn.folding.LateMutationGraphKFold;
import com.vmusco.softwearn.learn.folding.MutationGraphKFold;
import com.vmusco.softwearn.learn.learner.BinaryAlgorithm;
import com.vmusco.softwearn.learn.learner.DichotomicAlgorithm;
import com.vmusco.softwearn.learn.learner.late.BinaryLateImpactLearning;
import com.vmusco.softwearn.learn.learner.late.DichoLateImpactLearning;

/**
 * Those tests ensures that the kfold learning process works good
 * Two applications are taken into consideration: inline and late (deferred)
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class KLearningTest {
	@Test
	public void inlineBinaryKTesting(){
		LearningGraphStream lg = new LearningGraphStream(0f);
		generateLearningGraph(lg);
		
		MutationGraphKFold fold = new MutationGraphKFold(lg);
		fold.setLearner(new BinaryAlgorithm());
		fold.learn("a", new String[]{"b", "c"}, 0);
		
		lg.setThreshold(1f);
		
		Assert.assertTrue(lg.isThereAtLeastOnePath("b", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("d", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("e", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("f", "a"));
		
		lg.resetLearnedInformations();
		
		fold.learn("a", new String[]{"e", "f"}, 1);
		
		lg.setThreshold(1f);
		
		Assert.assertTrue(lg.isThereAtLeastOnePath("e", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("f", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("d", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("b", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("c", "a"));
	}

	@Test
	public void lateBinaryKTesting(){
		// Log4j2: Force to trace level
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(Level.TRACE);
		ctx.updateLoggers(); 
		// -----
		
		LearningKGraphStream lg = new LearningKGraphStream(0f, 3);
		generateLearningGraph(lg);
		
		MutationGraphKFold fold = new MutationGraphKFold(lg);
		
		fold.setLearner(new BinaryLateImpactLearning(3));
		fold.learn("a", new String[]{"b", "c"}, 0);
		fold.learn("a", new String[]{"e", "f"}, 1);
		fold.learn("a", new String[]{"d"}, 2);
		fold.learningRoundFinished();
		
		lg.setThreshold(1f);
		lg.setK(0);
		
		Assert.assertFalse(lg.isThereAtLeastOnePath("b", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("d", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("e", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("f", "a"));
		
		lg.setK(1);
		
		Assert.assertTrue(lg.isThereAtLeastOnePath("b", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("d", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("e", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("f", "a"));
		
		lg.setK(2);
		
		Assert.assertTrue(lg.isThereAtLeastOnePath("b", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertFalse(lg.isThereAtLeastOnePath("d", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("e", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("f", "a"));
	}
	
	@Test
	public void lateDichoKTesting(){
		// Log4j2: Force to trace level
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(Level.TRACE);
		ctx.updateLoggers(); 
		// -----
		
		LearningGraphStream lgold = new LearningGraphStream(0f);
		LearningKGraphStream lg = new LearningKGraphStream(0f, 3);
		
		generateLearningGraph(lgold);
		generateLearningGraph(lg);
		
		MutationGraphKFold fold = new MutationGraphKFold(lgold);
		LateMutationGraphKFold fnew = new LateMutationGraphKFold(lg);
		
		DichotomicAlgorithm learold = new DichotomicAlgorithm();
		DichoLateImpactLearning learner = new DichoLateImpactLearning(3);
		
		fold.setLearner(learold);
		fnew.setLearner(learner);
		
		learold.postDeclareAnImpact("a", new String[]{"b", "c", "d", "e"});
		learold.postDeclareAnImpact("a", new String[]{"b", "d"});
		learold.postDeclareAnImpact("a", new String[]{"c"});
		learold.postDeclareAnImpact("a", new String[]{"c"});
		learold.postDeclareAnImpact("a", new String[]{"c"});
		
		learner.postDeclareAnImpact("a", new String[]{"b", "c", "d", "e"});
		learner.postDeclareAnImpact("a", new String[]{"b", "d"});
		learner.postDeclareAnImpact("a", new String[]{"c"});
		learner.postDeclareAnImpact("a", new String[]{"c"});
		learner.postDeclareAnImpact("a", new String[]{"c"});
		
		fnew.learn("a", new String[]{"b", "c"}, 0);
		fnew.learn("a", new String[]{"e"}, 1);
		fnew.learn("a", new String[]{"d"}, 2);

		Assert.assertEquals(learner.getEmpiricalWeight("a", "b"), learold.getEmpiricalWeight("a", "b"), 0);
		Assert.assertEquals(learner.getEmpiricalWeight("a", "c"), learold.getEmpiricalWeight("a", "c"), 0);
		Assert.assertEquals(learner.getEmpiricalWeight("a", "d"), learold.getEmpiricalWeight("a", "d"), 0);
		Assert.assertEquals(learner.getEmpiricalWeight("a", "e"), learold.getEmpiricalWeight("a", "e"), 0);
		Assert.assertEquals(learner.getEmpiricalWeight("a", "f"), learold.getEmpiricalWeight("a", "f"), 0);

		fnew.learningRoundFinished();

		lg.setThreshold(0.2f);
		
		lg.setK(0);
		lgold.resetLearnedInformations();
		lgold.switchToLearningPhase();
		fold.learn("a", new String[]{"e"}, 1);
		fold.learn("a", new String[]{"d"}, 2);
		lgold.setThreshold(0.2f);
		testTwoVersionsOfGraph(lgold, lg, 0);
		
		lg.setK(1);
		lgold.resetLearnedInformations();
		lgold.switchToLearningPhase();
		fold.learn("a", new String[]{"b", "c"}, 0);
		fold.learn("a", new String[]{"d"}, 2);
		lgold.setThreshold(0.2f);
		testTwoVersionsOfGraph(lgold, lg, 1);
		
		lg.setK(2);
		lgold.resetLearnedInformations();
		lgold.switchToLearningPhase();
		fold.learn("a", new String[]{"b", "c"}, 0);
		fold.learn("a", new String[]{"e"}, 1);
		lgold.setThreshold(0.2f);
		testTwoVersionsOfGraph(lgold, lg, 2);
	}

	private void testTwoVersionsOfGraph(LearningGraphStream lgold, LearningKGraphStream lg, int k) {
		System.out.println("K = "+k);
		
		System.out.println(String.format("b %f-%f", lg.getEdgeThreshold("b", "a"), lgold.getEdgeThreshold("b", "a")));
		System.out.println(String.format("c %f-%f", lg.getEdgeThreshold("c", "a"), lgold.getEdgeThreshold("c", "a")));
		System.out.println(String.format("d %f-%f", lg.getEdgeThreshold("d", "a"), lgold.getEdgeThreshold("d", "a")));
		System.out.println(String.format("e %f-%f", lg.getEdgeThreshold("e", "a"), lgold.getEdgeThreshold("e", "a")));
		System.out.println(String.format("f %f-%f", lg.getEdgeThreshold("f", "a"), lgold.getEdgeThreshold("f", "a")));
		
		Assert.assertEquals(lg.getEdgeThreshold("b", "a"), lgold.getEdgeThreshold("b", "a"), 0);
		Assert.assertEquals(lg.getEdgeThreshold("c", "a"), lgold.getEdgeThreshold("c", "a"), 0);
		Assert.assertEquals(lg.getEdgeThreshold("d", "a"), lgold.getEdgeThreshold("d", "a"), 0);
		Assert.assertEquals(lg.getEdgeThreshold("e", "a"), lgold.getEdgeThreshold("e", "a"), 0);
		Assert.assertEquals(lg.getEdgeThreshold("f", "a"), lgold.getEdgeThreshold("f", "a"), 0);
		
		Assert.assertEquals(lg.isThereAtLeastOnePath("b", "a"), lgold.isThereAtLeastOnePath("b", "a"));
		Assert.assertEquals(lg.isThereAtLeastOnePath("c", "a"), lgold.isThereAtLeastOnePath("c", "a"));
		Assert.assertEquals(lg.isThereAtLeastOnePath("d", "a"), lgold.isThereAtLeastOnePath("d", "a"));
		Assert.assertEquals(lg.isThereAtLeastOnePath("e", "a"), lgold.isThereAtLeastOnePath("e", "a"));
		Assert.assertEquals(lg.isThereAtLeastOnePath("f", "a"), lgold.isThereAtLeastOnePath("f", "a"));
	}
	
	@Test
	public void inlineBinaryKTesting2(){
		LearningGraphStream lg = new LearningGraphStream(0f);
		generateLearningGraph2(lg);
		
		MutationGraphKFold fold = new MutationGraphKFold(lg);
		fold.setLearner(new BinaryAlgorithm());
		fold.learn("a", new String[]{"e", "c"}, 0);
		
		lg.setThreshold(1f);
		
		Assert.assertTrue(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("e", "a"));
	}
	
	@Test
	public void lateBinaryKTesting2(){
		// Log4j2: Force to trace level
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration config = ctx.getConfiguration();
		LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME); 
		loggerConfig.setLevel(Level.TRACE);
		ctx.updateLoggers(); 
		// -----
		
		LearningKGraphStream lg = new LearningKGraphStream(0f, 1);
		generateLearningGraph2(lg);
		
		MutationGraphKFold fold = new MutationGraphKFold(lg);
		
		fold.setLearner(new BinaryLateImpactLearning(1));
		fold.learn("a", new String[]{"e"}, 1);
		fold.learningRoundFinished();
		
		Assert.assertEquals(4, lg.getNbEdges());
		
		lg.setThreshold(1f);
		lg.setK(0);

		Assert.assertEquals(2, lg.getNbEdges());
		
		Assert.assertFalse(lg.isThereAtLeastOnePath("c", "a"));
		Assert.assertTrue(lg.isThereAtLeastOnePath("e", "a"));
	}
	
	private static void generateLearningGraph(Graph g) {
		g.addDirectedEdgeAndNodeIfNeeded("b", "a");
		g.addDirectedEdgeAndNodeIfNeeded("c", "a");
		g.addDirectedEdgeAndNodeIfNeeded("d", "a");
		g.addDirectedEdgeAndNodeIfNeeded("e", "a");
		g.addDirectedEdgeAndNodeIfNeeded("f", "a");
	}
	
	private static void generateLearningGraph2(Graph g) {
		g.addDirectedEdgeAndNodeIfNeeded("c", "b");
		g.addDirectedEdgeAndNodeIfNeeded("b", "a");
		g.addDirectedEdgeAndNodeIfNeeded("d", "a");
		g.addDirectedEdgeAndNodeIfNeeded("e", "d");
	}
}
