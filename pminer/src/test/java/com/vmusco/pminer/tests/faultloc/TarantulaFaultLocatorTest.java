package com.vmusco.pminer.tests.faultloc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;

public class TarantulaFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getTarantula(flt.getStats());
	}
	
	@Test
	public void testWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForTest();
		ScoreAndWastedEffort ret = processLocator(flt);

		Assert.assertEquals(1/1.5, ret.score, 0);
		Assert.assertEquals(4, ret.wastedeffort);
		
		Assert.assertEquals(11, ret.allscores.size());
		Assert.assertEquals(1d, ret.allscores.get("a"), 0);
		Assert.assertEquals(1d, ret.allscores.get("b"), 0);
		Assert.assertEquals(1d, ret.allscores.get("c"), 0);
		Assert.assertEquals(1d, ret.allscores.get("d"), 0);
		Assert.assertEquals(0d, ret.allscores.get("y"), 0);
		Assert.assertEquals(0d, ret.allscores.get("z"), 0);
		Assert.assertEquals(1/1.5, ret.allscores.get("e"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t1"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t2"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t3"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t4"), 0);
	}
	
	@Test
	public void testWithGraph() throws BadStateException, MutationNotRunException, TargetNotFoundException{
		FaultLocatorTester flt = getFltForTest();
		ScoreAndWastedEffort ret = processLocatorWithGraph(flt);
		
		Assert.assertEquals(1/1.5, ret.score, 0);
		Assert.assertEquals(0, ret.wastedeffort);
		
		Assert.assertEquals(1, ret.allscores.size());
		Assert.assertEquals(1/1.5, ret.allscores.get("e"), 0);
	}
	
	@Test
	public void paperTestWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForPaperTestCase();
		ScoreAndWastedEffort ret = processLocator(flt);

		System.out.println(ret);
	}
}
