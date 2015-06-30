package com.vmusco.smf.mutation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import junit.framework.Assert;

import org.junit.Test;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.ProjectDefinition;
import com.vmusco.smf.analysis.ProcessStatistics.STATE;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.MutationXMLPersisitence;
import com.vmusco.smf.compilation.Compilation;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.operators.KingOffutt91.*;
import com.vmusco.smf.projects.*;
import com.vmusco.smf.testing.Testing;

public class Base {
	private static final boolean RESET = false;

	//private Class moc = ArithmeticMutatorOperator.class;
	private Class moc = AbsoluteValueInsertionMutator.class;
	//private Class moc = LogicalConnectorReplacementOperator.class;
	//private Class moc = UnaryOperatorInsertionMutator.class;

	private static ProcessStatistics createDefProject(ProjectDefinition pd, String workingproj){
		try {
			return ProcessStatistics.createOrLoad(TestContants.DATASET, TestContants.WORKINGDIR + workingproj, RESET, pd);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static ProcessStatistics getActiveProject(){
		//return createDefProject(new CommonsCollections4TreeBidiMapConfig(), "commons-collections4");
		return createDefProject(new MyMiniProject(), "myminiproj");
		//return createDefProject(new CommonsCodecBase64Config(), "commons-codec");
		//return createDefProject(new CommonsIoFileUtilsConfig(), "commons-io");				// FAILS !!!
		//return createDefProject(new CommonsLangStringUtilsConfig(), "commons-lang");
		//return createDefProject(new GsonGsonClassConfig(), "gson");
		//return createDefProject(new GuavaHashBiMapConfig(), "guava");							// WAITING...
		//return createDefProject(new JGitCommitCommandConfig(), "jgit");
		//return createDefProject(new HadoopConfig(), "hadoop");									// WAITING

	}

	private ProcessStatistics ps = getActiveProject();

	@Test
	public void testBoth() throws Exception{
		testPreparation();
		testMutationGeneration();
	}

	/**
	 * Testing case
	 * Primer preparation:
	 * 	mvn pakage (to get a consisten package and ensure comEpilation works)
	 *  mvn install	(to find on compilation time !?)
	 *  mvn dependency:build-classpath
	 *  
	 * @throws Exception
	 */
	@Test
	public void testPreparation() throws Exception{
		System.out.println();

		System.out.println("Current state is: " + ps.currentState);

		System.out.print("Preparing object.....");
		if(ps.currentStateIsBefore(STATE.READY)){
			System.out.println();

			ps.determineClassPathOnAll();

			ps.createWorkingDir();
			if(!ps.changeState(STATE.READY)){
				System.out.println("Error changing state !");
				return;
			}

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}


		System.out.print("Building project.....");
		if(ps.currentStateIsBefore(STATE.BUILD)){
			System.out.println();
			if(!Compilation.compileProjectUsingSpoon(ps)){
				System.out.println("Building failed. Didn't you forget to run 'mvn install' priorly ?");
				Assert.fail("Compilation of source failed!");
				return;
			}

			if(!ps.changeState(STATE.BUILD)){
				System.out.println("Error changing state !");
				return;
			}

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}


		System.out.print("Building tests.....");
		if(ps.currentStateIsBefore(STATE.BUILD_TESTS)){
			System.out.println();
			if(!Compilation.compileTestsDissociatedUsingSpoon(ps)){
				System.out.println("Building failed. Didn't you forget to run 'mvn install' priorly ?");
				Assert.fail("Compilation of tests failed!");
			}

			if(!ps.changeState(STATE.BUILD_TESTS)){
				System.out.println("Error changing state !");
				return;
			}
			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}



		System.out.print("Running tests.....");
		if(ps.currentStateIsBefore(STATE.DRY_TESTS)){
			System.out.println();
			Testing.findTestClassesString(ps);
			Testing.runTestCases(ps);

			if(!ps.changeState(STATE.DRY_TESTS)){
				System.out.println("Error changing state !");
				return;
			}

			System.out.println("\n********************\n");
		}else{
			System.out.println("SKIP");
		}
	}

	@Test
	public void testMutationGeneration() throws Exception{
		System.out.println("Generating mutations, please wait...");
		MutationStatistics<?> ms = Mutation.createMutationElement(ps, moc, "toto", new String[]{"src/main/java/test/test/MySum.java"});
		ms.loadOrCreateMutants(true);
	}

	@Test
	public void testMutationExecution() throws Exception{
		//System.out.println("Current state: "+ps.currentState);

		MutationStatistics<?> ms = Mutation.createMutationElement(ps, moc);

		File f = new File(ms.getConfigFileResolved());

		if(f.exists()){
			MutationXMLPersisitence per = new MutationXMLPersisitence(f);
			ms = per.loadState();

			for(String mut : ms.listMutants()){
				MutantIfos ifos = ms.mutations.get(mut);
				
				if(!ifos.viable){
					continue;
				}
				
				File ff = new File(ms.getMutantFileResolved(mut));
				
				System.out.print("... Mutant ..."+mut+"...");
				if(!ff.exists() || ff.length() == 0){
					FileOutputStream fos = new FileOutputStream(ff);

					FileLock lock = fos.getChannel().tryLock();
					if(lock != null){
						Testing.runTestCases(ms, mut);
						System.out.println();

						MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(fos, mut);
						pers.saveState(ms.mutations.get(mut));
						
						lock.release();
						fos.close();
					}else{
						System.out.println("SKIP (another thread working on)");
					}
				}else{
					System.out.println("SKIP (already done)");
				}
			}
		}
	}

	/*System.out.print("Running mutants tests.....\n");


		//for(String mutant : processThoses){
		for(String mutant : ms.listMutants()){
			System.out.print("... Mutant ..."+mutant+"...");

			if(ms.mutantsFailingTestCases.containsKey(mutant) || ms.mutantsIgnoredTestCases.containsKey(mutant)){
				System.out.println("SKIP");
				continue;
			}else{
				System.out.println();
			}

			Testing.runTestCases(ps, mo.operatorId(), mutant);

			if(!ps.changeState(STATE.DRY_TESTS)){
				System.out.println("Error changing state !");
				return;
			}
		}


















		// This is a part of the code intended to generate the MM requested file
		File f = new File(ps.workingDir, "test_martin");

		if(f.exists())
			f.delete();

		f.createNewFile();

		FileOutputStream fos = new FileOutputStream(f);

		fos.write("# mutation_point impacted_test1 ... impacted_testn".getBytes());
		for(String mutid : ms.listMutants()){
			if(ps.mutations.get(mo.operatorId()).mutantsHangingTestCases.get(mutid) == null)
				continue;

			String[] all = ps.purifyFailAndHangResultSetForMutant(mo.operatorId(), mutid);
			String pt = ms.mutationIn.get(mutid);

			String line = "\n"+pt;
			for(String a : all)
				line += " " + a + "()"; 


			fos.write(line.getBytes());
		}

		fos.close();*/
}

/*
 *  NOTE: To test a code with injecting change just add the directory
 *  to the beginning of the class path:
 *  i.e:   java -cp mutantfolder:remainingcp package.testclass
 *  		if i produced a mutant file class in mutant project
 *  		but the remain of the code is on remaining cp
 *  		
 *  		mutantfolder contains:
 *  		my/pkg/AClass		(mutated)
 *  		
 *  		remainingcp contains:
 *  		my/pkg/AClass		(original)
 *  		(...)
 *  		mytests/pkg/TestClass (orinial, uses AClass)
 *  		==> In this scenario, we uses the mutated one
 */

