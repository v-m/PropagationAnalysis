package com.vmusco.smf.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmusco.smf.mutation.Mutation;

public abstract class MutationsSetTools {
	
	/**
	 * Returns set_one - set_two
	 * @param tests
	 * @return
	 */
	public static String[] setDifference(String[] set_one, String[] set_two){
		Set<String> set = new HashSet<String>();
		
		for(String s: set_one){
			set.add(s);
		}
		
		for(String s: set_two){
			if(set.contains(s))
				set.remove(s);
		}

		return (String[]) set.toArray(new String[0]);
	}
	
	/**
	 * Returns set_one inter set_two
	 * @param tests
	 * @return
	 */
	public static String[] setIntersection(String[] set_one, String[] set_two){
		Set<String> set = new HashSet<String>();
		List<String> ret = new ArrayList<String>();
		
		for(String s: set_one){
			set.add(s);
		}
		
		for(String s: set_two){
			if(set.contains(s))
				ret.add(s);
		}

		return (String[]) ret.toArray(new String[0]);
	}
	
	/**
	 * Returns set_one == set_two
	 * @param tests
	 * @return
	 */
	public static boolean areSetsSimilars(String[] set_one, String[] set_two){
		if(set_one.length != set_two.length)
			return false;
		
		Set<String> set = new HashSet<String>();
		
		for(String s: set_one){
			set.add(s);
		}
		
		for(String s: set_two){
			if(!set.remove(s))
				return false;
		}
		
		return set.size()==0;
	}
	
	public static boolean isMutantAlive(String[] failing, String[] hanging, String[] mutfailing, String[] muthanging){
		return areSetsSimilars(failing, mutfailing) && areSetsSimilars(hanging, muthanging);
	}

	public static String[] shuffle(String[] allMutations) {
		List<String> l = new ArrayList<String>();
		
		for(String m : allMutations){
			l.add(m);
		}
		
		Collections.shuffle(l);
		return (String[]) l.toArray(new String[0]);
	}

	public static String[] shuffleAndSlice(String[] allMutations, int nb) {
		String[] shuffled = shuffle(allMutations);
		return slice(shuffled, nb);
	}

	public static String[] slice(String[] allMutations, int nb) {
		String[] ret = new String[allMutations.length>nb?nb:allMutations.length];
		
		for(int i=0; i<ret.length; i++){
			ret[i] = allMutations[i];
		}
		
		return ret;
	}
}
