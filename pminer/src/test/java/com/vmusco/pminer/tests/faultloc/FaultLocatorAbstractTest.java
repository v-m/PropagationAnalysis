package com.vmusco.pminer.tests.faultloc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.GraphTools;

public abstract class FaultLocatorAbstractTest {
	public abstract FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt);
	
	public ScoreAndWastedEffort processLocator(FaultLocatorTester flt) throws BadStateException{
		ScoreAndWastedEffort ret = new ScoreAndWastedEffort();
		
		FaultLocalizationScore sel = getFaultLocalizationScore(flt);
		sel.computeScore(flt.getChangePoint());

		ret.score = sel.getScore();
		ret.wastedeffort = sel.wastedEffort(flt.getChangePoint()); 
		ret.allscores = sel.getWastedEffortList(flt.getStats().getUUTs()); 
		
		return ret;
	}
	
	public ScoreAndWastedEffort processLocatorWithGraph(FaultLocatorTester flt) throws BadStateException, TargetNotFoundException{
		ScoreAndWastedEffort ret = new ScoreAndWastedEffort();
		
		FaultLocalizationScore sel = getFaultLocalizationScore(flt);
		sel.computeScore(flt.getChangePoint());

		ret.score = sel.getScore();
		ret.wastedeffort = sel.wastedEffort(flt.getInterNodesList(), flt.getChangePoint()); 
		ret.allscores = sel.getWastedEffortList(flt.getInterNodesList()); 
		
		return ret;
	}
	
	public FaultLocatorTester getFltForTest() throws MutationNotRunException, BadStateException {
		Graph g = new GraphStream();

		GraphTools.fastInsertion(g, "t1->a->b->e");
		GraphTools.fastInsertion(g, "t2->c->d->e");
		GraphTools.fastInsertion(g, "t3->z->y");
		GraphTools.fastInsertion(g, "t4->e");
		
		String[] testcases = new String[]{"t1", "t2", "t3", "t4"};

		String changePoint = "e";
		
		String[] fails = new String[]{"t1", "t2"};
		
		Map<String, Set<String>> calledTest = new HashMap<String, Set<String>>();
		for(String k : g.getNodesNames()){
			calledTest.put(k, new HashSet<String>());
		}
		
		calledTest.get("a").add("t1");
		calledTest.get("b").add("t1");
		calledTest.get("c").add("t2");
		calledTest.get("d").add("t2");
		calledTest.get("y").add("t3");
		calledTest.get("z").add("t3");

		calledTest.get("e").add("t1");
		calledTest.get("e").add("t2");
		calledTest.get("e").add("t4");

		return new FaultLocatorTester(g, testcases, changePoint , calledTest, fails);
	}
	
	public FaultLocatorTester getFltForPaperTestCase() throws MutationNotRunException, BadStateException {
		Graph g = new GraphStream();

		GraphTools.fastInsertion(g, "t1->m1->m3->m5->m7->m6->m5");
		GraphTools.fastInsertion(g, "t2->m4->m3");
		GraphTools.fastInsertion(g, "m1->m2");
		GraphTools.fastInsertion(g, "t3->m7");
		GraphTools.fastInsertion(g, "t4->m7");
		GraphTools.fastInsertion(g, "t5->m7");
		
		String[] testcases = new String[]{"t1", "t2", "t3", "t4", "t4"};

		String changePoint = "m6";
		
		String[] fails = new String[]{"t1", "t2", "t4"};
		
		Map<String, Set<String>> calledTest = new HashMap<String, Set<String>>();
		for(String k : g.getNodesNames()){
			calledTest.put(k, new HashSet<String>());
		}
		
		calledTest.get("m1").add("t1");
		calledTest.get("m2").add("t1");
		calledTest.get("m3").add("t1");
		calledTest.get("m5").add("t1");
		calledTest.get("m7").add("t1");
		calledTest.get("m4").add("t2");
		calledTest.get("m3").add("t2");
		calledTest.get("m5").add("t2");
		calledTest.get("m7").add("t2");
		calledTest.get("m5").add("t2");

		
		calledTest.get("m7").add("t3");
		calledTest.get("m7").add("t4");
		calledTest.get("m7").add("t5");
		
		calledTest.get("m6").add("t3");
		calledTest.get("m6").add("t4");
		calledTest.get("m6").add("t5");
		
		calledTest.get("m5").add("t5");
		
		return new FaultLocatorTester(g, testcases, changePoint , calledTest, fails);
	}
	
	public FaultLocatorTester getFltForTest2() throws MutationNotRunException, BadStateException {
		Graph g = new GraphStream();

		GraphTools.fastInsertion(g, "t1->a->b->e");
		GraphTools.fastInsertion(g, "t1->c->b");
		GraphTools.fastInsertion(g, "t2->d->f->e");
		GraphTools.fastInsertion(g, "d->g->e");
		GraphTools.fastInsertion(g, "t3->z->y");
		
		String[] testcases = new String[]{"t1", "t2", "t3"};

		String changePoint = "e";
		
		String[] fails = new String[]{"t3"};
		
		Map<String, Set<String>> calledTest = new HashMap<String, Set<String>>();
		for(String k : g.getNodesNames()){
			calledTest.put(k, new HashSet<String>());
		}
		
		calledTest.get("e").addAll(Arrays.asList(new String[]{"t1", "t2"}));
		
		calledTest.get("a").add("t1");
		calledTest.get("c").add("t1");
		calledTest.get("b").add("t1");
		
		calledTest.get("d").add("t2");
		calledTest.get("f").add("t2");
		calledTest.get("g").add("t2");

		calledTest.get("y").add("t3");
		calledTest.get("z").add("t3");

		return new FaultLocatorTester(g, testcases, changePoint , calledTest, fails);
	}
}
