package com.vmusco.smf.utils;

public class SignatureTools {
	/**
	 * For a graph which respect software engineering rules (i.e. upper first letter of a class name), return the name of the class for a method signature
	 * @param method
	 * @return
	 */
	public static String getClassOf(String method){
		String[] split = method.split("\\(");
		split = split[0].split("\\.");

		String last = split[split.length - 1];

		String ret = method.split("\\(")[0];
		if(last.charAt(0)>= 'A' && last.charAt(0)<= 'Z'){
			// This is a constructor
			if(ret.lastIndexOf('#') > 0){
				ret = ret.substring(0, ret.lastIndexOf('#'));
			}
		}else{
			// This is a method
			ret = ret.substring(0, ret.lastIndexOf('.'));
		}
		return ret;
	}
	
	/**
	 * For a graph which respect software engineering rules (i.e. upper first letter of a class name), return the name of the package for a method signature
	 * @param method
	 * @return
	 */
	public static String getPackageOf(String method){
		String ret = getClassOf(method);
		
		return ret.substring(0, ret.lastIndexOf('.'));
	}
}
