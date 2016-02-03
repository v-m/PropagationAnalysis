package com.vmusco.softminer.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeTypes;

public class SteimannDatasetTools {
	/**
	 * Take a base graph generated with graphminer and adapt it to work with Steimann signatures types.
	 * Differences are:
	 *  - formal types (T) are directly resolved to java.lang.Object types or their basic type (based on extends)
	 *  - inner class mapped with $ symbol in graphminer are mapped with .
	 *  - Constructor invocation in graphminer are not seen as a method where they are here in Steimann (ie. constructor for A is A() in graphminer where it is A.A() in Steimann)  
	 * @param base the graph to update
	 */
	public static void adaptGraph(Graph base) {
		for(String node : base.getNodesNames()){
			Map<String, String> resolveFt = new HashMap<>();

			for(String ft:  base.getNodeFormalTypes(node)){
				String[] parts = ft.split(":");

				if(parts.length == 1){
					resolveFt.put(parts[0], "java.lang.Object");
				}else{
					resolveFt.put(parts[0], parts[1]);
				}
			}
			//List<String> nodeFormalTypes = base.getNodeFormalTypes(node);

			String newfinal = null;

			if(!node.contains("()") && resolveFt.size() > 0){
				String basename = node.split("\\(")[0];
				String params = "";

				StringTokenizer st = new StringTokenizer(node.split("\\(")[1].split("\\)")[0], ",");
				while(st.hasMoreElements()){
					int nbarray = 0;
					String par = st.nextToken();

					while(par.indexOf("[]") >= 0){
						par = par.replaceFirst("\\[\\]", "");
						nbarray++;
					}

					if(resolveFt.containsKey(par)){
						par = resolveFt.get(par);
					}

					params+= ((params.length() == 0)?"":",") + par;
					for(int i=0; i<nbarray; i++){
						params+="[]";
					}
				}

				newfinal = basename+"("+params+")";
			}

			if(node.contains("$")){
				if(newfinal == null)
					newfinal = node;

				newfinal = newfinal.replaceAll("\\$", ".");
			}

			if(base.getNodeType(node) == NodeTypes.CONSTRUCTOR){
				if(newfinal == null)
					newfinal = node;

				String classname = newfinal.split("\\(")[0];
				classname = classname.substring(classname.lastIndexOf('.')+1, classname.length());
				//System.out.println("-----> "+classname);

				String newfinal2 = newfinal.split("\\(")[0]+"."+classname+"("+newfinal.split("\\(")[1];
				newfinal = newfinal2;
			}

			if(newfinal != null){
				base.renameNode(node, newfinal);
				//System.out.println(String.format("Renamed %s => %s", node, newfinal));
			}
		}
	}
}
