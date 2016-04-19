package com.vmusco.softwearn.persistence;

import com.vmusco.smf.utils.SourceReference;
import com.vmusco.softminer.graphs.*;
import com.vmusco.softminer.graphs.persistence.GraphML;
import com.vmusco.softminer.graphs.persistence.GraphPersistence;
import com.vmusco.softwearn.learn.LearningGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import static com.vmusco.softminer.graphs.persistence.GraphML.xmlns;

/**
 * Created by Vinc on 4/18/16.
 */
public class LearningGraphPersistence implements GraphPersistence{
    private static final Logger logger = LogManager.getFormatterLogger(LearningGraphPersistence.class.getSimpleName());
    private final LearningGraph aGraph;

    public LearningGraphPersistence(LearningGraph aGraph){
        this.aGraph = aGraph;
    }

    public static Element generateRootDocument(float defaultThreshold) {
        Element root = GraphML.generateRootDocument();

        // Edit the root doc
        // <key id="weight" for="edge" attr.name="weight" attr.type="float">
        //<default>...</default>
        //</key>
        Element tmp = new Element("key", xmlns);
        tmp.setAttribute(new Attribute("id", "weight"));
        tmp.setAttribute(new Attribute("for", "edge"));
        tmp.setAttribute(new Attribute("attr.name", "weight"));
        tmp.setAttribute(new Attribute("attr.type", "float"));

        Element tmp2 = new Element("default", xmlns);
        tmp2.setText(Float.toString(defaultThreshold));
        tmp.addContent(tmp2);

        root.addContent(tmp);

        return root;
    }

    public static Element generateXmlNodes(LearningGraph aGraph, Namespace xmlns){
        Element e = GraphML.generateXmlNodes(aGraph.graph(), xmlns);

        // Adding edge informations
        for(Element ee : e.getChildren("edge", xmlns)){
            float thr = aGraph.getEdgeThreshold(ee.getAttributeValue("source"), ee.getAttributeValue("target"));

            Element data = new Element("data", xmlns);
            Attribute attr = new Attribute("key", "weight");
            data.setAttribute(attr);
            data.setText(Float.toString(thr));
            ee.addContent(data);
        }

        return e;
    }

    public static void populateFromGraphMl(Element root, LearningGraph g){
        GraphML.populateFromGraphMl(root, g.graph());

        for(Element e : root.getChildren("key", xmlns)){
            if(e.getAttributeValue("id").equals("weight")){
                g.setDefaultTreshold(Float.parseFloat(e.getChild("default", xmlns).getText()));
            }
        }

        // Load weights also...
        for(Element e : root.getChild("graph", xmlns).getChildren("edge", xmlns)){
            String source = e.getAttribute("source").getValue();
            String target = e.getAttribute("target").getValue();

            for(Element ee : e.getChildren("data", xmlns)){
                String tmp = ee.getAttribute("key").getValue();

                if(tmp.equals("weight")){
                    float thr = Float.parseFloat(ee.getText());
                    g.setEdgeThreshold(source, target, thr);
                }
            }
        }
    }

    @Override
    public void save(OutputStream os) throws IOException {
        Element root = generateRootDocument(aGraph.getDefaultTreshold());
        Element graphml = generateXmlNodes(aGraph, xmlns);
        System.out.println(aGraph.getNbThresholdNotNull());
        root.addContent(graphml);

        Document d = new Document(root);
        Format format = Format.getPrettyFormat();
        format.setLineSeparator(LineSeparator.UNIX);
        XMLOutputter output = new XMLOutputter(format);
        output.output(d, os);
    }

    @Override
    public void load(InputStream is) throws IOException {
        SAXBuilder sxb = new SAXBuilder();
        Document document;
        try {
            document = sxb.build(is);
        } catch (JDOMException e1) {
            e1.printStackTrace();
            return;
        }

        Element root = document.getRootElement();
        populateFromGraphMl(root, this.aGraph);
    }
}
