package com.vmusco.softminer.tests.cases.real.testPracticalApacheCommonLang3StrSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Test class for StrSubstitutor.
 *
 * @version $Id: StrSubstitutorTest.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class StrSubstitutorTest extends TestCase {

	private void doTestReplace(String expectedResult, String replaceTemplate, boolean substring) {
    	StrSubstitutor sub = new StrSubstitutor();

    	// replace using String
        assertEquals(expectedResult, sub.replace(replaceTemplate));

        // replace using char[]
        char[] chars = replaceTemplate.toCharArray();
        assertEquals(expectedResult, sub.replace(chars));

        assertEquals(expectedResult, sub.replace(new Integer(5)));
    }

    private void doTestNoReplace(String replaceTemplate) {
        StrSubstitutor sub = new StrSubstitutor();

        if (replaceTemplate == null) {
            assertEquals(null, sub.replace((String) null));
            assertEquals(null, sub.replace((char[]) null));
            assertEquals(null, sub.replace((Integer) null));
        }
    }

}
