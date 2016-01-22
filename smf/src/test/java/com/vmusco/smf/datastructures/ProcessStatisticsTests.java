package com.vmusco.smf.datastructures;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.testing.Testing;

public class ProcessStatisticsTests {
	public static ProcessStatistics createTestObject() throws IOException{
		File f = File.createTempFile("fake", "project");
		f.delete();
		
		System.out.println(f);
		
		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, f.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(ps.getClass().getClassLoader().getResource("fakeproj/").getFile());		
		ps.setProjectName("My Fake Project");
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ps.createLocalCopies(ProcessStatistics.SOURCES_COPY, ProcessStatistics.CLASSPATH_PACK);
		
		for(File aFile : FileUtils.listFiles(new File(ps.getProjectIn(true)), new String[]{"j"}, true)){
			String nameWithoutExtension = aFile.getName().substring(0, aFile.getName().lastIndexOf('.'));
			File dst = new File(aFile.getParentFile(), nameWithoutExtension+".java");
			aFile.renameTo(dst);
		}
		
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tests"});
		
		return ps;
	}
	
	@Test
	public void processStatisticCreationTest() throws IOException, PersistenceException, BadStateException, MutationNotRunException{
		ProcessStatistics ps = createTestObject();
		
		Testing.INC_TEST_TIMEOUT = 1;
		Testing.MIN_TEST_TIMEOUT = 1;
		Testing.MAX_TEST_TIMEOUT = 3;
		
		
		Assert.assertNull(ps.getTestExecutionResult());
		Assert.assertTrue(ps.compileWithSpoon());
		
		ps.performFreshTesting(null);		
		
		List<String> tc = Arrays.asList(ps.getTestCases());
		
		Assert.assertEquals(9, tc.size());
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.exceptionTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.failingTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.hangingTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest1()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest2()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest3()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.ignoredTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest2.test1()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest2.test2()"));
		
		tc = Arrays.asList(ps.getTestClasses());
		Assert.assertEquals(2, ps.getTestClasses().length);
		Assert.assertTrue(tc.contains("my.tests.FakeTest1"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest2"));
		
		Assert.assertNotNull(ps.getTestExecutionResult());
		
		TestsExecutionIfos tei = ps.getTestExecutionResult();
		Assert.assertEquals(0, tei.getRawErrorOnTestSuite().length);
		Assert.assertEquals(4, tei.getRawFailingTestCases().length);
		Assert.assertEquals(1, tei.getRawHangingTestCases().length);
		Assert.assertEquals(1, tei.getRawIgnoredTestCases().length);

		Assert.assertEquals(0, ps.getCoherentMutantFailAndHangTestCases(tei).length);
		Assert.assertEquals(0, ps.getCoherentMutantFailingTestCases(tei).length);
		Assert.assertEquals(0, ps.getCoherentMutantHangingTestCases(tei).length);
		Assert.assertEquals(0, ps.getCoherentMutantIgnoredTestCases(tei).length);
		
		TestsExecutionIfos coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[0]);
		coherenceTest.setFailingTestCases(new String[]{
				"my.tests.FakeTest1.failingTest()",
				"my.tests.FakeTest2.test1()",
				"my.tests.FakeTest2.test2()",
				"my.tests.FakeTest1.passingTest1()"
		});
		coherenceTest.setHangingTestCases(new String[]{
				"my.tests.FakeTest1.hangingTest()"
		});
		coherenceTest.setIgnoredTestCases(new String[]{
				"my.tests.FakeTest1.ignoredTest()"
		});
		
		Assert.assertEquals(1, ps.getCoherentMutantFailAndHangTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest1()", ps.getCoherentMutantFailAndHangTestCases(coherenceTest)[0]);
		Assert.assertEquals(1, ps.getCoherentMutantFailingTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest1()", ps.getCoherentMutantFailingTestCases(coherenceTest)[0]);
		Assert.assertEquals(0, ps.getCoherentMutantHangingTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantIgnoredTestCases(coherenceTest).length);
		
		coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[0]);
		coherenceTest.setFailingTestCases(new String[]{
				"my.tests.FakeTest1.failingTest()",
				"my.tests.FakeTest2.test1()",
				"my.tests.FakeTest2.test2()"
		});
		coherenceTest.setHangingTestCases(new String[]{
				"my.tests.FakeTest1.hangingTest()",
				"my.tests.FakeTest1.passingTest2()"
		});
		coherenceTest.setIgnoredTestCases(new String[]{
				"my.tests.FakeTest1.ignoredTest()"
		});
		
		Assert.assertEquals(1, ps.getCoherentMutantFailAndHangTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest2()", ps.getCoherentMutantFailAndHangTestCases(coherenceTest)[0]);
		Assert.assertEquals(0, ps.getCoherentMutantFailingTestCases(coherenceTest).length);
		Assert.assertEquals(1, ps.getCoherentMutantHangingTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest2()", ps.getCoherentMutantHangingTestCases(coherenceTest)[0]);
		Assert.assertEquals(0, ps.getCoherentMutantIgnoredTestCases(coherenceTest).length);
		
		coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[0]);
		coherenceTest.setFailingTestCases(new String[]{
				"my.tests.FakeTest1.failingTest()",
				"my.tests.FakeTest1.passingTest1()",
				"my.tests.FakeTest2.test1()",
				"my.tests.FakeTest2.test2()",
		});
		coherenceTest.setHangingTestCases(new String[]{
				"my.tests.FakeTest1.hangingTest()",
				"my.tests.FakeTest1.passingTest2()"
		});
		coherenceTest.setIgnoredTestCases(new String[]{
				"my.tests.FakeTest1.ignoredTest()"
		});
		
		Assert.assertEquals(2, ps.getCoherentMutantFailAndHangTestCases(coherenceTest).length);
		tc = Arrays.asList(ps.getCoherentMutantFailAndHangTestCases(coherenceTest));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest1()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest2()"));
		Assert.assertEquals("my.tests.FakeTest1.passingTest2()", ps.getCoherentMutantFailAndHangTestCases(coherenceTest)[0]);
		Assert.assertEquals(1, ps.getCoherentMutantFailingTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest1()", ps.getCoherentMutantFailingTestCases(coherenceTest)[0]);
		Assert.assertEquals(1, ps.getCoherentMutantHangingTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest2()", ps.getCoherentMutantHangingTestCases(coherenceTest)[0]);
		Assert.assertEquals(0, ps.getCoherentMutantIgnoredTestCases(coherenceTest).length);
		
		coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[0]);
		coherenceTest.setFailingTestCases(new String[]{
				"my.tests.FakeTest1.failingTest()",
				"my.tests.FakeTest2.test1()",
				"my.tests.FakeTest2.test2()",
		});
		coherenceTest.setHangingTestCases(new String[]{
				"my.tests.FakeTest1.hangingTest()"
		});
		coherenceTest.setIgnoredTestCases(new String[]{
				"my.tests.FakeTest1.ignoredTest()",
				"my.tests.FakeTest1.passingTest3()"
		});
		
		Assert.assertEquals(0, ps.getCoherentMutantFailAndHangTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantFailingTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantHangingTestCases(coherenceTest).length);
		Assert.assertEquals(1, ps.getCoherentMutantIgnoredTestCases(coherenceTest).length);
		Assert.assertEquals("my.tests.FakeTest1.passingTest3()", ps.getCoherentMutantIgnoredTestCases(coherenceTest)[0]);
		
		// Testing error on testsuite by forcing value
		ps.getTestExecutionResult().setErrorOnTestSuite(new String[]{
				"my.tests.FakeTest2"
		});
		List<String> items = new ArrayList<String>();
		for(String test : ps.getTestExecutionResult().getRawFailingTestCases()){
			if(!test.startsWith("my.tests.FakeTest2"))
				items.add(test);
		}
		ps.getTestExecutionResult().setFailingTestCases(items.toArray(new String[items.size()]));
		
		//////////
		coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[]{
				"my.tests.FakeTest2"
		});
		coherenceTest.setFailingTestCases(items.toArray(new String[items.size()]));
		coherenceTest.setHangingTestCases(new String[]{
				"my.tests.FakeTest1.hangingTest()"
		});
		coherenceTest.setIgnoredTestCases(new String[]{
				"my.tests.FakeTest1.ignoredTest()"
		});
		
		Assert.assertEquals(0, ps.getCoherentMutantFailAndHangTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantFailingTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantHangingTestCases(coherenceTest).length);
		Assert.assertEquals(0, ps.getCoherentMutantIgnoredTestCases(coherenceTest).length);
		
		tc = Arrays.asList(ps.includeTestSuiteGlobalFailingCases(ps.getErrorOnTestSuite(), ps.getFailingTestCases()));
		Assert.assertEquals(4, tc.size());
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.failingTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.exceptionTest()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest2.test1()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest2.test2()"));
		
		coherenceTest = new TestsExecutionIfos();
		coherenceTest.setErrorOnTestSuite(new String[]{
				"my.tests.FakeTest1",
				"my.tests.FakeTest2"
		});
		coherenceTest.setFailingTestCases(new String[0]);
		coherenceTest.setHangingTestCases(new String[0]);
		coherenceTest.setIgnoredTestCases(new String[0]);
		tc = Arrays.asList(ps.getCoherentMutantFailAndHangTestCases(coherenceTest));
		Assert.assertEquals(4, tc.size());
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest1()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest2()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.passingTest3()"));
		Assert.assertTrue(tc.contains("my.tests.FakeTest1.ignoredTest()"));
	}
	
}
