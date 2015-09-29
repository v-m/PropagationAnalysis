package com.vmusco.smf.mutation;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.support.reflect.code.CtLiteralImpl;
import spoon.support.reflect.code.CtVariableReadImpl;
import spoon.support.reflect.code.CtVariableWriteImpl;

import com.vmusco.smf.exceptions.HashClashException;
import com.vmusco.smf.exceptions.NotValidMutationException;
import com.vmusco.smf.mutation.operators.KingOffutt91.AbsoluteValueInsertionMutator;
import com.vmusco.smf.mutation.testclasses.Class1;

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
		Factory factory = Mutation.obtainFactory();
		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), AbsoluteValueInsertionMutator.class.getCanonicalName(), factory);

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
		
		Factory factory = Mutation.obtainFactory();
		MutationOperator<?> mo = new AbsoluteValueInsertionMutator();

		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), mo.getClass().getCanonicalName(), factory);

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
		MutationOperator<?> mo = new AbsoluteValueInsertionMutator();
		
		CtElement[] mutations = Mutation.getMutations(TestingTools.getTestClassForCurrentProject(Class1.class), TestingTools.getCurrentCp(), mo.getClass().getCanonicalName(), factory);
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
		Factory factory = Mutation.obtainFactory();
		Object[] r = getMutationTestingObject(factory);
		
		Set<String> hash = new HashSet<String>();
		hash.add("ab595c23fc91e4d344484f2cb6e8af14");
		
		try {
			Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, hash, TestingTools.getCurrentCp());
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
		Factory factory = Mutation.obtainFactory();
		Object[] r = getMutationTestingObject(factory);

		Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, null, TestingTools.getCurrentCp());
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
		Factory factory = Mutation.obtainFactory();
		Object[] r = getMutationTestingObject(factory);
		
		Set<String> hash = new HashSet<String>();
		hash.add("ab595c23fc91e4d344484f2cb6e8af13");
		
		Mutation.probeMutant((CtElement)r[0], (CtElement)r[1], (TargetObtainer)r[2], factory, hash, TestingTools.getCurrentCp());
	}
}
