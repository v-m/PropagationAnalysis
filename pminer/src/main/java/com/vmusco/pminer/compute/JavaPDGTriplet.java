package com.vmusco.pminer.compute;

public class JavaPDGTriplet {
	String method;
	String signature;
	String packg;

	public String getMethodName(){
		return packg.substring(packg.lastIndexOf("/") + 1);
	}

	public String getPackageName(){
		return packg.substring(0, packg.lastIndexOf("/")).replaceAll("/", "\\.");
	}

	public String[] getParameters(){
		String ss = signature.substring(1, signature.lastIndexOf(')'));

		if(ss.equals(""))
			return new String[]{""};

		String[] ps = ss.split(";");

		for(int i = 0; i<ps.length; i++){
			int tablecounter = 0;
			
			while(ps[i].charAt(0) == '['){
				tablecounter++;
				ps[i] = ps[i].substring(1);
			}
				
			
			if(ps[i].length() == 1){
				ps[i] = resolveJvmType(ps[i].charAt(0));
			}else{
				ps[i] = ps[i].substring(1).replaceAll("/", "\\.");
			}
			
			for(int j=0; j < tablecounter; j++){
				ps[i] = ps[i]+"[]"; 
			}
		}

		return ps;
	}
	
	/**
	 * Convert a specific JVM type character in Java full type
	 * @param aChar
	 * @return
	 */
	public static String resolveJvmType(char aChar) {
		if(aChar == 'B')
			return "byte";
		else if(aChar == 'C')
			return "char";
		else if(aChar == 'D')
			return "double";
		else if(aChar == 'F')
			return "float";
		else if(aChar == 'I')
			return "int";
		else if(aChar == 'J')
			return "long";
		else if(aChar == 'S')
			return "short";
		else if(aChar == 'Z')
			return "boolean";

		System.err.println("Resolving failed !!");
		return "???";
	}
}