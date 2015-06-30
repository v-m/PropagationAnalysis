package com.vmusco.softminer.tests.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphPersistFileCategory;

public class FilePersistance {
	@Test
	public void aTest() throws IOException{
		/*Graph g = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		g.addDirectedEdgeAndNodeIfNeeded("node1", "node2", true, false);
		g.addDirectedEdgeAndNodeIfNeeded("node3", "node4", true, false);
		g.addDirectedEdgeAndNodeIfNeeded("node1", "node4", true, false);
		g.addNode("node5");
		g.tagNode("node1", "tag1");
		g.tagEdge("node3", "node4", "tag2");
		
		GraphPersistFileCategory dir = new GraphPersistFileCategory(g);
		File f = new File("/tmp/"+System.currentTimeMillis());
		f.createNewFile();
		
		FileOutputStream fos = new FileOutputStream(f);
		dir.write(fos);
		fos.close();
		
		Graph g2 = Graph.getNewGraph(GraphApi.GRAPH_STREAM);
		dir = new GraphPersistFileCategory(g2);
		
		FileInputStream in = new FileInputStream(f);
		dir.read(in);
		in.close();
		
		Assert.assertTrue(g2.hasNode("node1"));
		Assert.assertTrue(g2.hasNode("node2"));
		Assert.assertTrue(g2.hasNode("node3"));
		Assert.assertTrue(g2.hasNode("node4"));
		Assert.assertTrue(g2.hasNode("node5"));
		
		Assert.assertTrue(g2.hasDirectedEdge("node1", "node2"));
		Assert.assertTrue(g2.hasDirectedEdge("node3", "node4"));
		Assert.assertTrue(g2.hasDirectedEdge("node1", "node4"));
		
		Assert.assertEquals(g2.getNodeTag("node1"), "tag1");
		Assert.assertEquals(g2.getEdgeTag("node3", "node4"), "tag2");
		
		f.delete();*/
	}
}
