package com.vmusco.pminer.tests.faultloc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;

public class OchiaiFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getOchiai(flt.getStats());
	}
	
	@Test
	public void testWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForTest();
		ScoreAndWastedEffort ret = processLocator(flt);

		Assert.assertEquals(2/Math.sqrt(6), ret.score, 0);
		Assert.assertEquals(0, ret.wastedeffort);
		
		Assert.assertEquals(11, ret.allscores.size());
		Assert.assertEquals(1/Math.sqrt(2), ret.allscores.get("a"), 0);
		Assert.assertEquals(1/Math.sqrt(2), ret.allscores.get("b"), 0);
		Assert.assertEquals(1/Math.sqrt(2), ret.allscores.get("c"), 0);
		Assert.assertEquals(1/Math.sqrt(2), ret.allscores.get("d"), 0);
		Assert.assertEquals(0d, ret.allscores.get("y"), 0);
		Assert.assertEquals(0d, ret.allscores.get("z"), 0);
		Assert.assertEquals(2/Math.sqrt(6), ret.allscores.get("e"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t1"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t2"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t3"), 0);
		Assert.assertEquals(-1d, ret.allscores.get("t4"), 0);
	}
	
	@Test
	public void testWithGraph() throws BadStateException, MutationNotRunException, TargetNotFoundException{
		FaultLocatorTester flt = getFltForTest();
		ScoreAndWastedEffort ret = processLocatorWithGraph(flt);
		
		Assert.assertEquals(2/Math.sqrt(6), ret.score, 0);
		Assert.assertEquals(0, ret.wastedeffort);
		
		Assert.assertEquals(1, ret.allscores.size());
		Assert.assertEquals(2/Math.sqrt(6), ret.allscores.get("e"), 0);
	}
}
