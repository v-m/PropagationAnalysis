package com.vmusco.softminer.graphs.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class GraphML implements GraphPersistence{
	private static final Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
	private Graph target;
	
	public GraphML(Graph aGraph){
		this.target = aGraph;
	}

	public static Element generateXmlNodes(Graph aGraph, Namespace xmlns){
		Element g = new Element("graph", xmlns);
		Attribute attr;

		attr = new Attribute("id", "G");
		g.setAttribute(attr);

		attr = new Attribute("edgedefault", "directed");
		g.setAttribute(attr);
		
		attr = new Attribute("buildtime", Long.toString(aGraph.getBuildTime()));
		g.setAttribute(attr);

		for(String n : aGraph.getNodesNames()){
			Element aNode = new Element("node", xmlns);
			g.addContent(aNode);

			NodeTypes nodeType = aGraph.getNodeType(n);

			if(nodeType != null){
				Element data = new Element("data", xmlns);
				attr = new Attribute("key", "type");
				data.setAttribute(attr);
				data.setText(nodeType.name());
				aNode.addContent(data);
			}

			attr = new Attribute("id", n);
			aNode.setAttribute(attr);

			for(NodeMarkers nm : aGraph.getNodeMarkers(n)){
				Element data = new Element("data", xmlns);
				attr = new Attribute("key", nm.name());
				data.setAttribute(attr);
				data.setText("true");
				aNode.addContent(data);
			}
		}

		int i = 1;

		for(Graph.NodesNamesForEdge e : aGraph.getEdges()){
			Element anEdge = new Element("edge", xmlns);
			g.addContent(anEdge);
			
			EdgeTypes edgeType = aGraph.getEdgeType(e.from, e.to);
			
			if(edgeType != null){
				Element data = new Element("data", xmlns);
				attr = new Attribute("key", "type");
				data.setAttribute(attr);
				data.setText(edgeType.name());
				anEdge.addContent(data);
			}

			attr = new Attribute("id", "e"+i++);
			anEdge.setAttribute(attr);
			attr = new Attribute("source", e.from);
			anEdge.setAttribute(attr);
			attr = new Attribute("target", e.to);
			anEdge.setAttribute(attr);

			for(EdgeMarkers em : aGraph.getEdgeMarkers(e.from, e.to)){
				Element data = new Element("data", xmlns);
				attr = new Attribute("key", em.name());
				data.setAttribute(attr);
				data.setText("true");
				anEdge.addContent(data);
				
				if(attr != null)
					anEdge.setAttribute(attr);
			}
		}
		
		return g;
	}



	@Override
	public void save(OutputStream os) throws IOException {
		Namespace xsi = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");

		Element root = new Element("graphml", xmlns);
		root.addNamespaceDeclaration(xsi);
		Document d = new Document(root);

		Element tmp;
		// <key id="type" for="node" attr.name="type" attr.type="string"/>
		tmp = new Element("key", xmlns);
		tmp.setAttribute(new Attribute("id", "type"));
		tmp.setAttribute(new Attribute("for", "node"));
		tmp.setAttribute(new Attribute("attr.name", "type"));
		tmp.setAttribute(new Attribute("attr.type", "string"));
		root.addContent(tmp);

		// <key id="type" for="edge" attr.name="type" attr.type="string"/>
		tmp = new Element("key", xmlns);
		tmp.setAttribute(new Attribute("id", "type"));
		tmp.setAttribute(new Attribute("for", "edge"));
		tmp.setAttribute(new Attribute("attr.name", "type"));
		tmp.setAttribute(new Attribute("attr.type", "string"));
		root.addContent(tmp);
		
		//<key id="uses_reflexion" for="node" attr.name="uses_reflexion" attr.type="boolean">
		//<default>false</default>
		//</key>
		for(NodeMarkers m : NodeMarkers.values()){
			tmp = new Element("key", xmlns);
			tmp.setAttribute(new Attribute("id", m.name()));
			tmp.setAttribute(new Attribute("for", "node"));
			tmp.setAttribute(new Attribute("attr.name", m.name()));
			tmp.setAttribute(new Attribute("attr.type", "boolean"));
			
			Element tmp2 = new Element("default", xmlns);
			tmp2.setText("false");
			tmp.addContent(tmp2);
			root.addContent(tmp);
		}
		
		//<key id="uses_reflexion" for="edge" attr.name="uses_reflexion" attr.type="boolean">
        //	<default>false</default>
        //</key>
		for(EdgeMarkers m : EdgeMarkers.values()){
			tmp = new Element("key", xmlns);
			tmp.setAttribute(new Attribute("id", m.name()));
			tmp.setAttribute(new Attribute("for", "edge"));
			tmp.setAttribute(new Attribute("attr.name", m.name()));
			tmp.setAttribute(new Attribute("attr.type", "boolean"));
			
			Element tmp2 = new Element("default", xmlns);
			tmp2.setText("false");
			tmp.addContent(tmp2);
			root.addContent(tmp);
		}
		
		Element graphml = generateXmlNodes(target, xmlns);
		root.addContent(graphml);

		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
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
		Element graph = root.getChild("graph", xmlns);
		Graph g = this.target;
		
		Attribute attribute = graph.getAttribute("buildtime");
		if(attribute != null)
			g.setBuildTime(Long.valueOf(attribute.getValue()));
		
		for(Element e : graph.getChildren("node", xmlns)){
			String nodename = e.getAttribute("id").getValue();
			g.addNode(nodename);
			
			for(Element ee : e.getChildren("data", xmlns)){
				String tmp = ee.getAttribute("key").getValue();
				if(tmp.equals("type")){
					// This is the node type declaration
					String type = ee.getValue();
					g.setNodeType(nodename, NodeTypes.valueOf(type));
				}else{
					// This is a node marker
					String isEnabled = ee.getText().toLowerCase();
					if(!isEnabled.equals("true"))
						continue;
					String marker = tmp; 
					g.markNode(nodename, NodeMarkers.valueOf(marker));
				}
			}
		}
		
		for(Element e : graph.getChildren("edge", xmlns)){
			String source = e.getAttribute("source").getValue();
			String target = e.getAttribute("target").getValue();

			g.addDirectedEdge(source, target);
			
			for(Element ee : e.getChildren("data", xmlns)){
				String tmp = ee.getAttribute("key").getValue();
				if(tmp.equals("type")){
					// This is the edge type declaration
					String type = ee.getValue();
					g.setEdgeType(source, target, EdgeTypes.valueOf(type));
				}else{
					// This is an edge marker
					String isEnabled = ee.getText().toLowerCase();
					if(!isEnabled.equals("true"))
						continue;
					String marker = tmp; 
					g.markEdge(source, target, EdgeMarkers.valueOf(marker));
				}
			}
		}
	}

}
