package com.vmusco.softminer.graphs.persistance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;

@Deprecated
public class XmlGraph implements GraphPersistence{

	private Graph target;

	public XmlGraph(Graph aGraph) {
		this.target = aGraph;
	}


	public static void insertXmlNodes(Graph aGraph, Element root){
		Element nodes = new Element("nodes");
		root.addContent(nodes);
		Attribute attr;

		for(String n : aGraph.getNodesNames()){
			NodeTypes nodeType = aGraph.getNodeType(n);
			String nodename = "node";

			attr = null;

			if(nodeType != null){
				switch(nodeType){
				case METHOD:
					nodename = "method";
					break;
				case FIELD:
					nodename = "variable";
					break;
				default:
					System.out.println("XmlGraph error : Unknown node type ("+nodeType.name()+")");
					break;
				}
			}

			Element aNode = new Element(nodename);
			nodes.addContent(aNode);
			attr = new Attribute("id", n);
			aNode.setAttribute(attr);

			for(NodeMarkers nm : aGraph.getNodeMarkers(n)){
				attr = null;

				switch(nm){
				case USES_REFLEXION:
					attr = new Attribute("use-reflexion", "1");
					break;
				default:
					System.out.println("XmlGraph error : Unknown node marker ("+nm.name()+")");
					break;
				}

				if(attr != null)
					aNode.setAttribute(attr);
			}
		}

		Element edges = new Element("edges");
		root.addContent(edges);

		for(Graph.NodesNamesForEdge e : aGraph.getEdges()){
			EdgeTypes edgeType = aGraph.getEdgeType(e.from, e.to);
			String edgename = "edge";

			if(edgeType != null){
				switch(edgeType){
				case ABSTRACT_METHOD_IMPLEMENTATION:
					edgename = "abstract-method-implementation";
					break;
				case INTERFACE_IMPLEMENTATION:
					edgename = "interface-implementation";
					break;
				case METHOD_CALL:
					edgename = "call";
					break;
				case READ_OPERATION:
					edgename = "read";
					break;
				case WRITE_OPERATION:
					edgename = "write";
					break;
				default:
					System.out.println("XmlGraph error : Unknown edge type ("+edgeType.name()+")");
				}
			}


			Element anEdge = new Element(edgename);
			edges.addContent(anEdge);
			attr = new Attribute("source", e.from);
			anEdge.setAttribute(attr);
			attr = new Attribute("target", e.to);
			anEdge.setAttribute(attr);

			for(EdgeMarkers em : aGraph.getEdgeMarkers(e.from, e.to)){
				attr = null;

				switch(em){
				default:
					System.out.println("XmlGraph error : Unknown node marker ("+em.name()+")");
					break;
				}

				if(attr != null)
					anEdge.setAttribute(attr);
			}
		}
	}


	@Override
	public void save(OutputStream os) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void load(InputStream is) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
