package com.vmusco.softminer.tests;

import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.algorithms.ShortestPath;

public class ShortestPathsTests {
	
	private static Graph createGraph(){
		Graph g = new GraphStream();

		g.addDirectedEdgeAndNodeIfNeeded("Z", "A");
		g.addDirectedEdgeAndNodeIfNeeded("A", "B");
		g.addDirectedEdgeAndNodeIfNeeded("A", "C");
		g.addDirectedEdgeAndNodeIfNeeded("B", "D");
		g.addDirectedEdgeAndNodeIfNeeded("C", "D");
		g.addDirectedEdgeAndNodeIfNeeded("D", "E");
		
		g.addDirectedEdgeAndNodeIfNeeded("Z", "E");
		
		g.addDirectedEdgeAndNodeIfNeeded("A", "E");
		g.addDirectedEdgeAndNodeIfNeeded("B", "E");
		g.addDirectedEdgeAndNodeIfNeeded("C", "E");
		
		return g;
	}
	
	/**
	 * This test should be checked
	 * TODO fixit
	 */
	@Test
	@Ignore
	public void testKShortestPaths(){
		Graph g = createGraph();
		
		ShortestPath sp = new ShortestPath(g);
		List<String[]> paths = sp.yen("Z", "E", 10);
		
		Assert.assertEquals(6, paths.size());
		
		Assert.assertEquals(2, paths.get(0).length);
		Assert.assertEquals(3, paths.get(1).length);
		Assert.assertEquals(4, paths.get(2).length);
		Assert.assertEquals(4, paths.get(3).length);
		Assert.assertEquals(5, paths.get(4).length);
		Assert.assertEquals(5, paths.get(5).length);
		
		for(int i=0; i<paths.size(); i++){
			Assert.assertEquals("Z", paths.get(i)[0]);
		}
		
		Assert.assertEquals("E", paths.get(0)[1]);
		
		for(int i=1; i<paths.size(); i++){
			Assert.assertEquals("A", paths.get(i)[1]);
		}
		
		Assert.assertEquals("E", paths.get(1)[2]);

		Assert.assertEquals("B", paths.get(3)[2]);
		Assert.assertEquals("C", paths.get(2)[2]);
		Assert.assertEquals("E", paths.get(3)[3]);
		Assert.assertEquals("E", paths.get(2)[3]);
		
		Assert.assertEquals("B", paths.get(5)[2]);
		Assert.assertEquals("C", paths.get(4)[2]);
		Assert.assertEquals("D", paths.get(5)[3]);
		Assert.assertEquals("D", paths.get(4)[3]);
		Assert.assertEquals("E", paths.get(5)[4]);
		Assert.assertEquals("E", paths.get(4)[4]);
	}
}
