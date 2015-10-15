package com.vmusco.softminer.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.instrumentation.AbstractInstrumentationProcessor;
import com.vmusco.smf.instrumentation.EntryMethodInstrumentationProcessor;
import com.vmusco.smf.testing.Testing;
import com.vmusco.softminer.graphs.DynamicCallGraphGenerator;

/**
 * Simple test of dynamic call graph extraction using a ProcessStatistic object 
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class DynamicCallGraphGeneration {
	
	@Test
	public void testGeneration() throws IOException, PersistenceException, BadStateException{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = prepareProjectWithTests();

		ProcessStatistics ps = ProcessStatistics.rawCreateProject(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(proj.getAbsolutePath());

		// Setting ps configuration
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tst"});
		ps.setProjectName("my test");

		// Setting classpath
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ps.createLocalCopies(ProcessStatistics.SOURCES_COPY, ProcessStatistics.CLASSPATH_PACK);
		ProcessStatistics.saveState(ps);

		ps.instrumentAndBuildProjectAndTests(
			new AbstractInstrumentationProcessor[]{ 
				new EntryMethodInstrumentationProcessor(),
			}
		);

		DynamicCallGraphGenerator dcgg = new DynamicCallGraphGenerator();
		ps.performFreshTesting(dcgg);
		dcgg.getGraph().bestDisplay();
	}
	
	public static File prepareProjectWithTests() throws IOException{
		File f = File.createTempFile("BuildingTests", Long.toString(System.currentTimeMillis()));
		f.delete();
		f.mkdirs();

		File ff = new File (System.getProperty("user.dir"));
		ff = new File(ff.getParent(), "testproject");
		
		System.out.println(f);

		// SOURCES
		File srcf = new File(f, "src");
		srcf.mkdirs();

		FileUtils.copyDirectory(new File(ff, "src/main/java/"), srcf);

		// TESTS
		File tstf = new File(f, "tst");
		tstf.mkdirs();

		FileUtils.copyDirectory(new File(ff, "src/test/java/"), tstf);
		tstf.mkdirs();

		return f;
	}
}
