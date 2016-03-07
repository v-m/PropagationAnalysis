package com.vmusco.smf.mutation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtVariableReadImpl;
import spoon.support.reflect.code.CtVariableWriteImpl;

import com.vmusco.smf.TestingTools;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.XMLPersistence;
import com.vmusco.smf.buildtest.BuildingTest;
import com.vmusco.smf.compilation.compilers.JavaxCompilation;
import com.vmusco.smf.exceptions.HashClashException;
import com.vmusco.smf.exceptions.NotValidMutationException;
import com.vmusco.smf.instrumentation.AbstractInstrumentationProcessor;
import com.vmusco.smf.instrumentation.MethodInInstrumentationProcessor;
import com.vmusco.smf.instrumentation.MethodOutInstrumentationProcessor;
import com.vmusco.smf.mutation.operators.KingOffutt91.AbsoluteValueInsertionMutator;
import com.vmusco.smf.mutation.operators.KingOffutt91.ArithmeticMutatorOperator;
import com.vmusco.smf.testclasses.simple.Class1;
import com.vmusco.smf.testing.Testing;
import com.vmusco.smf.utils.SpoonHelpers;

/**
 * Tests for mutation code
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationTest {
	/**
	 * Test the list of elements selected for an ABS mutation
	 */
	@Test
	public void testElementsToMutateABS(){
		Factory factory = SpoonHelpers.obtainFactory();
		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), new AbsoluteValueInsertionMutator(), factory);

		Map<String, Class<?>> matches = new HashMap<String, Class<?>>();
		Map<String, String> types = new HashMap<String, String>();

		matches.put("14", CtLiteralImpl.class);
		matches.put("92.0F", CtLiteralImpl.class);
		matches.put("95.0", CtLiteralImpl.class);
		matches.put("215L", CtLiteralImpl.class);
		matches.put("i", CtVariableReadImpl.class);
		matches.put("j", CtVariableReadImpl.class);
		matches.put("k", CtVariableWriteImpl.class);
		matches.put("5.25", CtLiteralImpl.class);
		matches.put("l", CtVariableWriteImpl.class);
		matches.put("2", CtLiteralImpl.class);

		types.put("i", "int");
		types.put("j", "float");
		types.put("k", "double");
		types.put("l", "long");

		Assert.assertEquals(10, mutations.length);

		for(CtElement e : mutations){
			Assert.assertTrue(matches.containsKey(e.toString()));
			Class<?> c = matches.remove(e.toString());
			Assert.assertTrue(c.equals(e.getClass()));

			if(e instanceof CtVariableAccess){
				CtVariableAccess<?> v = (CtVariableAccess<?>) e;
				Assert.assertTrue(types.containsKey(e.toString()));
				String t = types.remove(e.toString());

				Assert.assertTrue(v.getVariable().getType().getSimpleName().equals(t));
			}
		}

		Assert.assertEquals(0, matches.size());
		Assert.assertEquals(0, types.size());
	}
	
	/**
	 * Test the candidates proposed for the ABS mutation operator 
	 */
	@Test
	public void testMutationCandidatesObtainerABS(){
		Map<String, String> matches = new HashMap<String, String>();
		Map<String, String> matches2 = new HashMap<String, String>();

		matches.put("14", "-1 * 14");
		matches.put("92.0F", "-1 * 92.0F");
		matches.put("95.0", "-1 * 95.0");
		matches.put("215L", "-1 * 215L");
		matches.put("5.25", "-1 * 5.25");
		matches.put("2", "-1 * 2");
		
		matches.put("i", "i >= 0 ? i : i * -1");
		matches2.put("i", "i >= 0 ? i * -1 : i");
		matches.put("j", "j >= 0 ? j * -1 : j");
		matches2.put("j", "j >= 0 ? j : j * -1");
		matches.put("k", "k >= 0 ? k * -1 : k"); 
		matches2.put("k", "k >= 0 ? k : k * -1");
		matches.put("l", "l >= 0 ? l * -1 : l");
		matches2.put("l", "l >= 0 ? l : l * -1");
		
		Factory factory = SpoonHelpers.obtainFactory();
		SmfMutationOperator<?> mo = new AbsoluteValueInsertionMutator();

		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), mo, factory);

		for(CtElement mutation : mutations){
			HashMap<CtElement, TargetObtainer> candid = Mutation.obtainsMutationCandidates(mo, mutation, factory);

			Iterator<CtElement> iterator = candid.keySet().iterator();

			while(iterator.hasNext()){
				CtElement m = iterator.next();
				TargetObtainer to = candid.get(m);

				if(matches.containsKey(mutation.toString()) && matches.get(mutation.toString()).equals(m.toString())){
					matches.remove(mutation.toString());
				}else if(matches2.containsKey(mutation.toString()) && matches2.get(mutation.toString()).equals(m.toString())){
					matches2.remove(mutation.toString());
				}else{
					Assert.fail("Unexpected element: "+m.toString());
				}
				
				Assert.assertTrue(mutation == to.determineTarget(mutation));
			}
		}
		
		Assert.assertEquals(0, matches.size());
		Assert.assertEquals(0, matches2.size());
	}

	private static Object[] getMutationTestingObject(Factory factory){
		SmfMutationOperator<?> mo = new AbsoluteValueInsertionMutator();
		
		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), mo, factory);
		CtElement target = null;
		
		for(CtElement e : mutations){
			if(e.toString().equals("i")){
				if(target != null){
					Assert.fail("Several CtElements with same name for target!");
				}else{
					target = e;
				}
			}
		}
		
		
		CtElement target2_elem = null;
		TargetObtainer target2_to = null;
		HashMap<CtElement, TargetObtainer> candid = Mutation.obtainsMutationCandidates(mo, target, factory);
		
		Iterator<CtElement> iterator = candid.keySet().iterator();

		while(iterator.hasNext()){
			CtElement e = iterator.next();
			
			if(e.toString().equals("i >= 0 ? i : i * -1")){
				if(target2_elem != null || target2_to != null){
					Assert.fail("Several CtElements with same name for target2!");
				}else{
					target2_elem = e;
					target2_to = candid.get(e);
				}
			}
		}
		
		return new Object[]{
				target,
				target2_elem,
				target2_to
		};
	}
	
	/**
	 * Test the mutant probe process with hash clash
	 * @throws NotValidMutationException 
	 * @throws HashClashException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	public void testMutantProberHashClash() throws NoSuchAlgorithmException, IOException, NotValidMutationException{
		Factory factory = SpoonHelpers.obtainFactory();
		Object[] r = getMutationTestingObject(factory);
		
		Set<String> hash = new HashSet<String>();
		hash.add("8c4c7c6d6449d208a7451f9aeb224818");
		
		try {
			Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, hash, TestingTools.getCurrentCp(), 7);
		} catch (HashClashException e) {
			return;
		}
		
		Assert.fail("Should have failed due to hash clash !");
	}

	/**
	 * Test the mutant probe process with a null element
	 * @throws NotValidMutationException 
	 * @throws HashClashException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 */
	@Test
	public void testMutantProberNullElement() throws NoSuchAlgorithmException, IOException, HashClashException, NotValidMutationException{
		Factory factory = SpoonHelpers.obtainFactory();
		Object[] r = getMutationTestingObject(factory);

		Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, null, TestingTools.getCurrentCp(), 7);
	}

	/**
	 * Test the mutant probe process with a successfull mutation
	 * @throws NotValidMutationException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws HashClashException 
	 */
	@Test
	public void testMutantProberSuccess() throws NoSuchAlgorithmException, IOException, NotValidMutationException, HashClashException{
		Factory factory = SpoonHelpers.obtainFactory();
		Object[] r = getMutationTestingObject(factory);
		
		Set<String> hash = new HashSet<String>();
		hash.add("ab595c23fc91e4d344484f2cb6e8af13");
		
		Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, hash, TestingTools.getCurrentCp(), 7);
	}
	
	/**
	 * Test the mutant generated object
	 * @throws NotValidMutationException 
	 * @throws IOException 
	 * @throws NoSuchAlgorithmException 
	 * @throws HashClashException 
	 */
	@Test
	public void testMutantIfosContent() throws NoSuchAlgorithmException, IOException, NotValidMutationException, HashClashException{
		Factory factory = SpoonHelpers.obtainFactory();
		Object[] r = getMutationTestingObject(factory);
		
		MutantIfos pm = Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, null, TestingTools.getCurrentCp(), 7);
		Assert.assertEquals("8c4c7c6d6449d208a7451f9aeb224818", pm.getHash());
		Assert.assertEquals("i", pm.getMutationFrom());
		Assert.assertEquals("i >= 0 ? i : i * -1", pm.getMutationTo());
		Assert.assertEquals("com.vmusco.smf.testclasses.simple.Class1.main(java.lang.String[])", pm.getMutationIn());
		Assert.assertTrue(pm.isViable());
		Assert.assertEquals(6, pm.getSourceReference().getColumnStart());
		Assert.assertEquals(6, pm.getSourceReference().getColumnEnd());
		Assert.assertEquals(10, pm.getSourceReference().getLineStart());
		Assert.assertEquals(10, pm.getSourceReference().getLineEnd());
		Assert.assertEquals(0, pm.getSourceReference().getParentSearch());
		Assert.assertEquals(184, pm.getSourceReference().getSourceStart());
		Assert.assertEquals(184, pm.getSourceReference().getSourceEnd());
		Assert.assertEquals(TestingTools.getTestClassForCurrentProject(Class1.class, false)[0]+".java", pm.getSourceReference().getFile());
	}
	
	@Test
	public void testMutationWithPMStatisticsObject() throws Exception{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = BuildingTest.prepareProjectWithTests();

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(proj.getAbsolutePath());

		// Setting ps configuration
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tst"});
		ps.setProjectName("my test");
		ps.setComplianceLevel(6);
		
		// Setting classpath
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ProcessStatistics.saveState(ps);

		// Build project
		System.out.print("Building.....");
		ps.build(new JavaxCompilation());
		ProcessStatistics.saveState(ps);

		System.out.println("Running tests...");
		ps.performFreshTesting(null);
		ProcessStatistics.saveState(ps);
		
		MutationStatistics ms = new MutationStatistics(ps, new ArithmeticMutatorOperator());
		ms.loadOrCreateMutants(true);
		

		for(String s : ms.listMutants()){
			MutantIfos sta = ms.getMutationStats(s);
			System.out.println(sta.getMutationFrom()+" -> "+sta.getMutationTo());
		}
		System.out.println(ms.listMutants().length);
		
		//TODO: test mutation result

		FileUtils.deleteDirectory(proj);
		FileUtils.deleteDirectory(src);
	}
	
	@Test
	public void testMutationWithPMStatisticsObjectAndSelectiveMethods() throws Exception{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = BuildingTest.prepareProjectWithTests();

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(proj.getAbsolutePath());

		// Setting ps configuration
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tst"});
		ps.setProjectName("my test");
		ps.setComplianceLevel(6);
		
		// Setting classpath
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ProcessStatistics.saveState(ps);

		// Build project
		System.out.print("Building.....");
		ps.build(new JavaxCompilation());
		ProcessStatistics.saveState(ps);

		System.out.println("Running tests...");
		ps.performFreshTesting(null);
		ProcessStatistics.saveState(ps);
		
		MutationStatistics ms = new MutationStatistics(ps, new ArithmeticMutatorOperator());
		ms.setMethodSignaturesToMutate(new String[]{"hello.you.Class2.arithmeticTest()"});
		ms.loadOrCreateMutants(true);
		
		for(String s : ms.listMutants()){
			MutantIfos sta = ms.getMutationStats(s);
			System.out.println(sta.getMutationFrom()+" -> "+sta.getMutationTo());
		}
		System.out.println(ms.listMutants().length);
		//TODO: test mutation result

		FileUtils.deleteDirectory(proj);
		FileUtils.deleteDirectory(src);
	}
	
	@Test
	public void testMutationWithPMStatisticsObjectAndInstrumentation() throws Exception{
		File src = File.createTempFile(this.getClass().getCanonicalName(), Long.toString(System.currentTimeMillis()));
		src.delete();
		System.out.println(src.getAbsolutePath());

		File proj = BuildingTest.prepareProjectWithTests();
		System.out.println(proj);

		ProcessStatistics ps = new ProcessStatistics(ProcessStatistics.SOURCES_COPY, src.getAbsolutePath());
		ps.createWorkingDir();
		ps.setProjectIn(proj.getAbsolutePath());

		// Setting ps configuration
		ps.setSrcToCompile(new String[]{"src"});
		ps.setSrcTestsToTreat(new String[]{"tst"});
		ps.setProjectName("my test");

		// Setting classpath
		ps.setOriginalClasspath(Testing.getCurrentVMClassPath());
		ps.setComplianceLevel(6);
		ps.createLocalCopies(ProcessStatistics.SOURCES_COPY, ProcessStatistics.CLASSPATH_PACK);
		ProcessStatistics.saveState(ps);

		// Build project
		System.out.print("Building.....");
		ps.instrumentAndBuildProjectAndTests(new JavaxCompilation(),
				new AbstractInstrumentationProcessor[]{ 
					new MethodInInstrumentationProcessor(),
					new MethodOutInstrumentationProcessor(),
				}
				);
		ProcessStatistics.saveState(ps);

		System.out.println("Running tests...");
		System.out.println("...");
		ps.performFreshTesting(null);
		System.out.println("...");
		ProcessStatistics.saveState(ps);
		System.out.println("...");
		
		MutationStatistics ms = new MutationStatistics(ps, new ArithmeticMutatorOperator());
		System.out.println("...");
		ms.loadOrCreateMutants(true, null, -1, 0);
		System.out.println("...");
		
		TestsExecutionIfos runTestCases = Testing.runTestCases(ps.getProjectIn(true), ms.getRunningClassPath("mutant_0"), ps.getTestClasses(), null, ps.getAlternativeJre());
		Assert.assertEquals(1, runTestCases.getCalledNodes().size());
		
		String[] calledNodes = runTestCases.getCalledNodes("hello.you.tests.Test1.mytest()");
		List<String> calledNodesList = Arrays.asList(calledNodes);
		Assert.assertEquals(8, calledNodes.length);
		Assert.assertTrue(calledNodesList.contains("hello.you.Class1()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class2()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class3()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.returnTrue()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.returnFalse()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class1.recursiveMethod(int)"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.doNotReturn()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class2.arithmeticTest()"));
		
		ms.getMutationStats("mutant_0").setExecutedTestsResults(runTestCases);
		File ff = new File(ms.getMutantFileResolved("mutation_0"));
		ff.getParentFile().mkdirs();
		MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(ms.getMutationStats("mutant_0"), ff);
		pers.setFileLock(new FileOutputStream(ff));
		XMLPersistence.save(pers);
		
		pers = new MutantInfoXMLPersisitence(new MutantIfos(), ff, true);
		XMLPersistence.load(pers);
		MutantIfos linkedObject = pers.getLinkedObject();
		
		calledNodes = linkedObject.getExecutedTestsResults().getCalledNodes("hello.you.tests.Test1.mytest()");
		calledNodesList = Arrays.asList(calledNodes);
		Assert.assertEquals(8, calledNodes.length);
		Assert.assertTrue(calledNodesList.contains("hello.you.Class1()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class2()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class3()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.returnTrue()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.returnFalse()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class1.recursiveMethod(int)"));
		Assert.assertTrue(calledNodesList.contains("hello.you.AClass.doNotReturn()"));
		Assert.assertTrue(calledNodesList.contains("hello.you.Class2.arithmeticTest()"));
		
		FileUtils.deleteDirectory(proj);
		FileUtils.deleteDirectory(src);
	}
}
