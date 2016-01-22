package com.vmusco.pminer.tests.faultloc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;

public class NoAlgoFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getIntersectionMaxSizeApporach(flt.getStats());
	}
	
	@Test
	public void testWithGraph() throws BadStateException, MutationNotRunException, TargetNotFoundException{
		FaultLocatorTester flt = getFltForTest();

		//System.out.println(flt.getStats());
		
		ScoreAndWastedEffort ret = processLocatorWithGraph(flt);
		
		Assert.assertEquals(1, ret.score, 1);
		Assert.assertEquals(0, ret.wastedeffort, 0);
		Assert.assertEquals(1, ret.allscores.size());
		Assert.assertEquals(1, ret.allscores.get("e"), 0);

		flt.getStats().changeTestingNode("b");
		
		try{
			ret = processLocatorWithGraph(flt);
			Assert.fail();
		}catch(TargetNotFoundException ex){
			// Expect to fail because the target is not there !
		}
	}
}
