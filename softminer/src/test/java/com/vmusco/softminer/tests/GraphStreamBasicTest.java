package com.vmusco.softminer.tests;


import org.junit.Assert;
import org.junit.Test;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.GraphTools;

public class GraphStreamBasicTest {
	@Test
	public void testRename(){
		Graph g = new GraphStream();
		
		g.addDirectedEdgeAndNodeIfNeeded("b", "a");
		g.addDirectedEdgeAndNodeIfNeeded("c", "a");
		g.addDirectedEdgeAndNodeIfNeeded("d", "a");
		g.addDirectedEdgeAndNodeIfNeeded("a", "e");
		g.addDirectedEdgeAndNodeIfNeeded("a", "f");
		g.addDirectedEdgeAndNodeIfNeeded("b", "h");
		g.addDirectedEdgeAndNodeIfNeeded("g", "b");
		
		Assert.assertEquals(g.getNbNodes(), 8);
		Assert.assertEquals(g.getNbEdges(), 7);
		Assert.assertEquals(g.getOutDegreeFor("a"), 2);
		Assert.assertEquals(g.getInDegreeFor("a"), 3);
		
		Assert.assertTrue(g.hasNode("a"));
		Assert.assertFalse(g.hasNode("mynewa"));
		
		g.renameNode("a", "mynewa");
		Assert.assertEquals(g.getNbNodes(), 8);
		Assert.assertEquals(g.getNbEdges(), 7);
		Assert.assertFalse(g.hasNode("a"));
		Assert.assertTrue(g.hasNode("mynewa"));
		Assert.assertEquals(g.getOutDegreeFor("mynewa"), 2);
		Assert.assertEquals(g.getInDegreeFor("mynewa"), 3);
		
		//g.bestDisplay();
	}
	
	@Test
	public void testConnectedComponent(){
		Graph g = new GraphStream();
		
		GraphTools.fastInsertion(g, "a->b->c->d->e->f->a");
		GraphTools.fastInsertion(g, "g->h->i->g");
		GraphTools.fastInsertion(g, "j->k->j");

		Assert.assertEquals(6, g.getNbGiantComponents());
		Assert.assertEquals(3, g.getNbConnectedComponents());
	}
}
