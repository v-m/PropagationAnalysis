package com.vmusco.pminer.tests.faultloc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;

public class NbPathsFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getPassingTestsConnectionsPaths(flt.getStats());
	}
	
	@Test
	public void testWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForTest2();

		System.out.println(flt.getStats());
		
		ScoreAndWastedEffort ret = processLocator(flt);
		
		Assert.assertEquals(4, ret.score, 0);
		Assert.assertEquals(0, ret.wastedeffort);
		
		Assert.assertEquals(1, ret.allscores.get("a"), 0);
		Assert.assertEquals(2, ret.allscores.get("b"), 0);
		Assert.assertEquals(1, ret.allscores.get("c"), 0);
		Assert.assertEquals(1, ret.allscores.get("d"), 0);
		Assert.assertEquals(4, ret.allscores.get("e"), 0);
		Assert.assertEquals(1, ret.allscores.get("f"), 0);
		Assert.assertEquals(1, ret.allscores.get("g"), 0);
		Assert.assertEquals(0, ret.allscores.get("y"), 0);
		Assert.assertEquals(0, ret.allscores.get("z"), 0);
		Assert.assertEquals(0, ret.allscores.get("t1"), 0);
		Assert.assertEquals(0, ret.allscores.get("t2"), 0);
		Assert.assertEquals(0, ret.allscores.get("t3"), 0);
	}
	
}
