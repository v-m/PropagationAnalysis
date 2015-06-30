package com.vmusco.softminer.graphs.persistance;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.io.InputStream;
import java.io.OutputStream;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.NodesNamesForEdge;

public class GraphPersistFileCategory /*implements GraphPersistanceDirector*/{
	
	//TODO: Persistance has to be reviewed...
/*
	private Graph g;

	public GraphPersistFileCategory(Graph g) {
		this.g = g;
	}

	@Override
	public void read(InputStream in) throws IOException {
		HashMap<Integer, String> maps = new HashMap<Integer, String>();

		DataInputStream dis = new DataInputStream(in);
		String line;
		while((line = dis.readLine()) != null){
			if(line.length() > 0){
				boolean pat1 = line.contains("][");
				boolean pat2 = line.contains("}{");
				boolean pat3 = line.contains("[");
				boolean pat4 = line.contains("{");

				if(!pat1 && pat3){
					//NODE
					String rem = line.substring(1).trim();
					int poscut = rem.indexOf("]");
					int id = Integer.parseInt(rem.substring(0, poscut));
					String name = rem.substring(poscut+1, rem.length());
					maps.put(id, name);
					g.addNode(name, true);
				}else if(pat1){
					// EDGE
					String rem = line.substring(1).trim();
					int poscut = rem.indexOf("][");
					int id1 = Integer.parseInt(rem.substring(0, poscut));
					int id2 = Integer.parseInt(rem.substring(poscut+2, rem.length()-1));
					g.addDirectedEdge(maps.get(id1), maps.get(id2));
				}else if(!pat2 && pat4){
					// NODE TAG
					String rem = line.substring(1).trim();
					int poscut = rem.indexOf("}");
					int id = Integer.parseInt(rem.substring(0, poscut));
					String name = rem.substring(poscut+1, rem.length());
					g.tagNode(maps.get(id), name);
				}else if(pat2){
					// EDGE TAG
					String rem = line.substring(1).trim();
					int poscut = rem.indexOf("}{");
					int id1 = Integer.parseInt(rem.substring(0, poscut));
					rem = rem.substring(poscut + 2);
					poscut = rem.indexOf("}");
					int id2 = Integer.parseInt(rem.substring(0, poscut));
					String tg = rem.substring(poscut + 1, rem.length());
					g.tagEdge(maps.get(id1), maps.get(id2), tg);
				}

			}
		}
	}

	@Override
	public void write(OutputStream out) throws IOException {
		HashMap<String, Integer> maps = new HashMap<String, Integer>();
		String nodesOutput = "";
		String edgesOutput = "";

		String nodeTagsOutput = "";
		String edgesTagsOutput = "";

		int cpt = 0;
		for(String node:g.getNodesNames()){
			//TODO: Write here
			nodesOutput += "["+cpt+"]"+node+"\n";

			String ntag = g.getNodeTag(node);
			if(ntag != null){
				nodeTagsOutput += "{"+cpt+"}"+ntag+"\n";
			}

			maps.put(node, cpt++);
		}

		for(NodesNamesForEdge nnfe : g.getEdges()){
			int frint = maps.get(nnfe.from);
			int toint = maps.get(nnfe.to);

			nodeTagsOutput += "["+frint+"]["+toint+"]\n";

			String etag = g.getEdgeTag(nnfe.from, nnfe.to);
			if(etag != null){
				edgesTagsOutput += "{"+frint+"}{"+toint+"}"+etag+"\n";
			}
		}

		out.write(nodesOutput.getBytes());
		out.write(edgesOutput.getBytes());
		out.write(nodeTagsOutput.getBytes());
		out.write(edgesTagsOutput.getBytes());
	}
*/
}
