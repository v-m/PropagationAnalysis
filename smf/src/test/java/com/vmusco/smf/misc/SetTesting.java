package com.vmusco.smf.misc;

import org.junit.Assert;
import org.junit.Test;

import com.vmusco.smf.utils.SetTools;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class SetTesting {

	private static String[] set1 = new String[]{"a", "b", "c"};
	private static String[] set2 = new String[]{"c", "d", "e"};
	private static String[] set3 = new String[]{"a", "b", "c"};
	private static String[] set4 = new String[]{"c", "d", "e"};
	private static String[] set5 = new String[]{"c", "d"};
	
	@Test
	public void testDifference(){

		String[] res = SetTools.setDifference(set1, set2);
		Assert.assertEquals(res.length, 2);
		Assert.assertEquals(res[0], "a");
		Assert.assertEquals(res[1], "b");
		

		res = SetTools.setDifference(set2, set1);
		Assert.assertEquals(res.length, 2);
		Assert.assertEquals(res[0], "d");
		Assert.assertEquals(res[1], "e");
	}

	@Test
	public void testIntersection(){
		String[] res = SetTools.setIntersection(set1, set2);
		Assert.assertEquals(res.length, 1);
		Assert.assertEquals(res[0], "c");
	}

	@Test
	public void testEquals(){
		Assert.assertFalse(SetTools.areSetsSimilars(set1, set2));
		Assert.assertFalse(SetTools.areSetsSimilars(set2, set1));
		
		Assert.assertTrue(SetTools.areSetsSimilars(set1, set3));
	}
	
	@Test
	public void testMutantAlive(){
		Assert.assertTrue(SetTools.isMutantAlive(set1, set2, set3, set4));
		Assert.assertFalse(SetTools.isMutantAlive(set1, set2, set3, set5));
	}
	
	
}
