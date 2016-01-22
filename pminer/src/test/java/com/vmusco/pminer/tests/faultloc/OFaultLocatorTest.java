package com.vmusco.pminer.tests.faultloc;

import org.junit.Test;

import com.vmusco.pminer.faultlocalization.FaultLocalizationScore;
import com.vmusco.pminer.faultlocalization.FaultLocators;
import com.vmusco.pminer.tests.faultloc.FaultLocatorTester.ScoreAndWastedEffort;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.exceptions.TargetNotFoundException;

public class OFaultLocatorTest extends FaultLocatorAbstractTest{

	@Override
	public FaultLocalizationScore getFaultLocalizationScore(FaultLocatorTester flt) {
		return FaultLocators.getO(flt.getStats());
	}
	
	@Test
	public void testWithoutGraph() throws BadStateException, MutationNotRunException{
		FaultLocatorTester flt = getFltForTest();

		System.out.println(flt.getStats());
		
		ScoreAndWastedEffort ret = processLocator(flt);
		
		System.out.println("Score = "+ret.score);
		System.out.println("Wasted Effort = "+ret.wastedeffort);
		for(String k : ret.allscores.keySet()){
			System.out.println(k+" -> "+ret.allscores.get(k));
		}
	}
	
	@Test
	public void testWithGraph() throws BadStateException, MutationNotRunException, TargetNotFoundException{
		FaultLocatorTester flt = getFltForTest();

		System.out.println(flt.getStats());
		
		ScoreAndWastedEffort ret = processLocatorWithGraph(flt);
		
		System.out.println("Score = "+ret.score);
		System.out.println("Wasted Effort = "+ret.wastedeffort);
		for(String k : ret.allscores.keySet()){
			System.out.println(k+" -> "+ret.allscores.get(k));
		}
	}
}
