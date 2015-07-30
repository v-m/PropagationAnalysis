package com.vmusco.softminer.utils;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Tools {
	public static Double median(ArrayList<Double> alist){
		if(alist.size() == 0)
			return -1d;
		
		if(alist.size()%2==0){
			Double a = alist.get(alist.size()/2);
			Double b = alist.get((alist.size()/2)-1);
			
			return (a + b) / 2;
		}else{
			return alist.get((int)Math.ceil(alist.size()/2));
		}
	}
	
	public static Integer medianInt(ArrayList<Integer> alist){
		if(alist.size() == 0)
			return -1;
		
		if(alist.size()%2==0){
			Integer a = alist.get(alist.size()/2);
			Integer b = alist.get((alist.size()/2)-1);
			
			return (a + b) / 2;
		}else{
			return alist.get((int)Math.ceil(alist.size()/2));
		}
	}
	
	public static TypeWithInfo[] medianWithInfo(ArrayList<TypeWithInfo> alist){
		if(alist.size() == 0)
			return null;
		
		if(alist.size()%2==0){
			TypeWithInfo a = alist.get(alist.size()/2);
			TypeWithInfo b = alist.get((alist.size()/2)-1);
			
			return new TypeWithInfo[]{a, b};
		}else{
			return new TypeWithInfo[]{alist.get((int)Math.ceil(alist.size()/2))};
		}
	}
	
	public static Double average(ArrayList<Double> alist){
		if(alist.size() == 0)
			return -1d;
		
		double ret = 0.0;
		
		Iterator<Double> iterator = alist.iterator();
		
		while(iterator.hasNext()){
			ret += iterator.next();
		}
		
		return ret/alist.size();
	}
}
