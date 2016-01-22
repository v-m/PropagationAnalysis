package com.vmusco.pminer.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public abstract class Tools {
	public static double median(double[] alist){
		Arrays.sort(alist);
		
		if(alist.length == 0)
			return -1d;
		
		if(alist.length % 2 == 0){
			double a = alist[alist.length / 2];
			double b = alist[(alist.length / 2) - 1];
			
			return (a + b) / 2;
		}else{
			return alist[(int)Math.ceil(alist.length/2)];
		}
	}
	
	public static double median(int[] alist){
		//Collections.sort(alist);
		Arrays.sort(alist);
		
		if(alist.length == 0)
			return -1;
		
		if(alist.length % 2 == 0){
			int a = alist[alist.length / 2];
			int b = alist[(alist.length / 2) - 1];
			
			return ((a + b) * 1d) / 2;
		}else{
			return alist[(int)Math.ceil(alist.length/2)];
		}
	}
	
	public static TypeWithInfo<?>[] medianWithInfo(List<TypeWithInfo> alist){
		Collections.sort(alist);
		
		if(alist.size() == 0)
			return null;
		
		if(alist.size()%2==0){
			TypeWithInfo<?> a = alist.get(alist.size()/2);
			TypeWithInfo<?> b = alist.get((alist.size()/2)-1);
			
			return new TypeWithInfo[]{a, b};
		}else{
			return new TypeWithInfo[]{alist.get((int)Math.ceil(alist.size()/2))};
		}
	}
	
	public static double average(double[] alist){
		if(alist.length == 0)
			return -1d;
		
		double ret = 0.0;
		
		for(double i : alist){
			ret += i;
		}
		
		return ret/alist.length;
	}
	
	public static double average(int[] alist){
		if(alist.length == 0)
			return -1;
		
		double ret = 0.0;
		
		for(int i : alist){
			ret += i;
		}
		
		ret = ret * 1d;
		
		return ret/alist.length;
	}
	
	/**
	 * Convert to an array. Makes a copy, thus sorting the array is safe.
	 * @param list
	 * @return
	 */
	public static double[] toDoubleArray(List<Double> list){
		double[] ret = new double[list.size()];
		
		int i = 0;
		for(Double d : list){
			ret[i++] = d;
		}
		
		return ret;
	}
	
	/**
	 * Convert to an array. Makes a copy, thus sorting the array is safe.
	 * @param list
	 * @return
	 */
	public static int[] toIntArray(List<Integer> list){
		int[] ret = new int[list.size()];
		
		int i = 0;
		for(Integer d : list){
			ret[i++] = d;
		}
		
		return ret;
	}
	
	public static <T extends Double,Integer> int countEqualsOrLower(T[] intArray, T searchingNumber) {
		int cpt = 0;
		
		for(T i : intArray){
			if(i <= searchingNumber)
				cpt++;
		}
		
		return cpt;
	}
	
	
}
