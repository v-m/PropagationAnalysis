package com.vmusco.softwearn.tests;

import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.LearningGraphStream;
import com.vmusco.softwearn.persistence.LearningGraphPersistence;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class LearningGraphPersistenceTest {

    @Test
    public void testPersistedGraph() throws IOException {
        LearningGraph lg = new LearningGraphStream(0.35f);
        lg.graph().addNode("1");
        lg.graph().addNode("2");
        lg.graph().addNode("3");
        lg.graph().addNode("4");

        lg.addDirectedEdge("1", "2", 0.24f);
        lg.addDirectedEdge("3", "4", 0.45f);
        lg.addDirectedEdge("1", "4", 0.01f);
        lg.addDirectedEdge("1", "2", 0.75f);

        Assert.assertEquals(4, lg.graph().getNbNodes());
        Assert.assertEquals(3, lg.graph().getNbEdges());
        Assert.assertEquals(0.35f, lg.getDefaultTreshold(), 0.001f);
        Assert.assertEquals(0.75f, lg.getEdgeThreshold("1", "2"), 0.001f);
        Assert.assertEquals(0.01f, lg.getEdgeThreshold("1", "4"), 0.001f);
        Assert.assertEquals(0.45f, lg.getEdgeThreshold("3", "4"), 0.001f);

        File f = File.createTempFile("persistence", "testing");
        f.deleteOnExit();

        LearningGraphPersistence lgp = new LearningGraphPersistence(lg);
        lgp.save(new FileOutputStream(f));

        lg = new LearningGraphStream(0f);
        lgp = new LearningGraphPersistence(lg);

        lgp.load(new FileInputStream(f));

        Assert.assertEquals(4, lg.graph().getNbNodes());
        Assert.assertEquals(3, lg.graph().getNbEdges());
        Assert.assertEquals(0.35f, lg.getDefaultTreshold(), 0.001f);
        Assert.assertEquals(0.75f, lg.getEdgeThreshold("1", "2"), 0.001f);
        Assert.assertEquals(0.01f, lg.getEdgeThreshold("1", "4"), 0.001f);
        Assert.assertEquals(0.45f, lg.getEdgeThreshold("3", "4"), 0.001f);
    }
}
