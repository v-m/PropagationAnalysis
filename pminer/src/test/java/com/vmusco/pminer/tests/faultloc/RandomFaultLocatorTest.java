package com.vmusco.pminer.tests.faultloc;


import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;

public class RandomFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getRandomBased(flt.getStats(), 200);
	}
	
	@Test
	public void testWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForTest();
		ScoreAndWastedEffort ret = processLocator(flt);
		System.out.println(flt.getStats());
		
		flt = getFltForTest();
		ScoreAndWastedEffort ret2 = processLocator(flt);
		System.out.println(flt.getStats());
		
		System.out.println(ret.score);
		System.out.println(ret2.score);

		System.out.println(ret.wastedeffort);
		System.out.println(ret2.wastedeffort);
		
		for(String k : ret.allscores.keySet()){
			System.out.println(k+" -> "+ret.allscores.get(k));
			System.out.println(k+" -> "+ret2.allscores.get(k));
		}
		
		Assert.assertEquals(ret.score, ret2.score, 0);
		Assert.assertEquals(ret.wastedeffort, ret2.wastedeffort);

		for(String k : ret.allscores.keySet()){
			Assert.assertEquals(ret.allscores, ret2.allscores);
		}
	}
}
