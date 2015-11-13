package com.vmusco.smf.buildtest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import spoon.reflect.declaration.CtClass;

import com.vmusco.smf.TestingTools;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.instrumentation.AbstractInstrumentationProcessor;
import com.vmusco.smf.instrumentation.EntryMethodInstrumentationProcessor;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.utils.SpoonHelpers;

public class BuildingTest {

	/**
	 * Test the Spoon compilation. Test only if it create the hierarchy and all needed files on compilation (content not tests)
	 * @throws IOException
	 */
	@Test
	public void simpleSpoonBuildTest() throws IOException{
		File src = prepareSourceFolder();
		System.out.println(src.getAbsolutePath());
		System.out.println(TestingTools.getTestClassForCurrentProject(com.vmusco.smf.testclasses.simple.Class1.class)[0]);

		File f = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		File tmpf = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		f.deleteOnExit();
		tmpf.deleteOnExit();

		Compilation.compileUsingSpoon(new String[]{src.getAbsolutePath()}, new String[]{}, f.getAbsolutePath(), tmpf.getAbsolutePath());
		File test = new File(f, TestingTools.getTestPackageFolders(com.vmusco.smf.testclasses.simple.Class1.class, true));
		Assert.assertTrue(test.exists());
		Assert.assertTrue(test.isDirectory());
		Assert.assertEquals(1, test.list().length);

		Set<String> s = new HashSet<>();
		for(String fn : test.list()){
			s.add(fn);
		}

		String[] exp = new String[]{"Class1.class"};

		for(String fn : exp){
			Assert.assertTrue(s.remove(fn));
		}

		Assert.assertEquals(0, s.size());
	}

	/**
	 * Test the JavaX on the fly compilation. Test only if it create the bytes (not testing the exact content)
	 * @throws IOException
	 */
	@Test
	public void simpleJavaXBuildTest() throws IOException{
		File src = prepareSourceFolder();

		System.out.println(src.getAbsolutePath());
		System.out.println(TestingTools.getTestClassForCurrentProject(com.vmusco.smf.testclasses.simple.Class1.class)[0]);

		CtClass<?> ce = SpoonHelpers.getClassElement(new String[]{src.getAbsolutePath()}, null, com.vmusco.smf.testclasses.simple.Class1.class.getCanonicalName());

		Assert.assertNotNull(ce);
		Map<String, byte[]> comp = Compilation.compilesUsingJavax(ce, SpoonHelpers.generateAssociatedClassContent(ce), new String[]{});

		Assert.assertEquals(1, comp.size());
		String k = comp.keySet().iterator().next();
		Assert.assertEquals(com.vmusco.smf.testclasses.simple.Class1.class.getCanonicalName(), k);
		Assert.assertTrue(comp.get(k).length > 0);
	}

	@Test
	public void testProcessStatistics() throws Exception{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = prepareProjectWithTests();

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
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

		// Build project
		System.out.print("Building.....");
		ps.compileWithSpoon();
		ProcessStatistics.saveState(ps);

		System.out.println("Running tests...");
		ps.performFreshTesting(null);
		ProcessStatistics.saveState(ps);
	}

	@Test
	public void instrumentCode() throws IOException, PersistenceException, BadStateException{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = prepareProjectWithTests();

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
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

		ps.performFreshTesting(null);
	}

	@Test
	public void testProcessStatisticsInstrumentation() throws Exception{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = prepareProjectWithTests();

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(proj.getAbsolutePath());

		// Setting ps configuration
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tst"});
		ps.setProjectName("my test");

		// Setting classpath
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ProcessStatistics.saveState(ps);

		// Build project
		System.out.print("Building.....");
		ps.compileWithSpoon();
		ProcessStatistics.saveState(ps);

		System.out.println("Running tests...");
		ps.performFreshTesting(null);
		ProcessStatistics.saveState(ps);
	}

	private File prepareSourceFolder() throws IOException {
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		src.mkdirs();
		File src2 = new File(src, TestingTools.getTestPackageFolders(com.vmusco.smf.testclasses.simple.Class1.class, true));
		src2.mkdirs();
		FileUtils.copyDirectory(new File(TestingTools.getTestClassForCurrentProject(com.vmusco.smf.testclasses.simple.Class1.class)[0]), src2);
		src.deleteOnExit();

		return src;
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
