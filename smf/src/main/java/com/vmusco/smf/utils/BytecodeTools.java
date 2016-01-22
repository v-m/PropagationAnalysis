package com.vmusco.smf.utils;

public class BytecodeTools {

	public static String signatureConverter(String sign){
		String ret = "";
		String tmp = sign.substring(0, sign.lastIndexOf(')'));
		String params = tmp.substring(tmp.lastIndexOf('(')+1);
		tmp = tmp.substring(0, tmp.lastIndexOf('('));

		ret = tmp.replaceAll(":", ".");

		return ret+"("+parseParams(params)+")";
	}

	public static String parseParams(String params) {
		String ret = "";
		String parsing = params;
		int array_cpt = 0;

		while(parsing.length() > 0){
			char nextChar = parsing.charAt(0);
			
			parsing = parsing.substring(1);

			if(nextChar == '['){
				array_cpt++;
				continue;
			}
			
			if(nextChar == 'L'){
				// This is a class...
				String classType = parsing.substring(0, parsing.indexOf(';'));
				parsing = parsing.substring(parsing.indexOf(';')+1);

				ret += ((ret.length()>0)?",":"")+classType.replaceAll("/", ".");
			}else{
				String type = parseBytecodeSimpleType(nextChar);
				

				ret += ((ret.length()>0)?",":"")+type;
			}
			
			while(array_cpt > 0){
				ret += "[]";
				array_cpt--;
			}
		}

		return ret;
	}

	public static String parseBytecodeSimpleType(char nextChar) {
		switch(nextChar){
		case 'B':
			return "byte";
		case 'C':
			return "char";
		case 'D':
			return "double";
		case 'F':
			return "float";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'S':
			return "short";
		case 'V':
			return "void";
		case 'Z':
			return "boolean";
		default:
			return "?";
		}
	}
}
