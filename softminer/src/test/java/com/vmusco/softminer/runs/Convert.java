package com.vmusco.softminer.runs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphML;
import com.vmusco.softminer.graphs.persistance.GraphPersistence;

public class Convert {

	public static void main(String[] args) throws Exception {

		String[] h = new String[]{
				"/home/vince/Experiments/bugimpact/mutants/commons-io",
				"/home/vince/Experiments/bugimpact/ug/commons-codec",
				"/home/vince/Experiments/bugimpact/ug/commons-collections4",
				"/home/vince/Experiments/bugimpact/ug/gson",
				"/home/vince/Experiments/bugimpact/ug/shindig",
				"/home/vince/Experiments/bugimpact/ug/commons-lang",
				"/home/vince/Experiments/bugimpact/mutants/joda-time",
				"/home/vince/Experiments/bugimpact/mutants/spojo"
		};
		
		
		for(String home : h){
			File f = new File(home, "newusegraph.xml");
	
			Graph usegraph = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
			GraphML gml = new GraphML(usegraph);
			gml.load(new FileInputStream(f));
	
			Graph aNewGraph = usegraph.keepOnly(
					new NodeTypes[]{NodeTypes.METHOD}, 
					new NodeMarkers[]{NodeMarkers.USES_REFLEXION}, 
					new EdgeTypes[]{EdgeTypes.METHOD_CALL}, 
					new EdgeMarkers[]{});
			
			aNewGraph.setBuildTime(usegraph.getBuildTime());
	
			GraphPersistence gp = null;
			gp = new GraphML(aNewGraph);
			File ff = new File(home, "newusegraphA.xml");
			gp.save(new FileOutputStream(ff));
		}
	}
}
