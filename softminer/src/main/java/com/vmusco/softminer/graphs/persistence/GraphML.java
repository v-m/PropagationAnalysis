package com.vmusco.softminer.graphs.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.SourceReference;
import com.vmusco.softminer.graphs.EdgeIdentity;
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
	private static final Logger logger = LogManager.getFormatterLogger(GraphML.class.getSimpleName());
	
	private static final Namespace xmlns = Namespace.getNamespace("http://graphml.graphdrawing.org/xmlns");
	private Graph target;
	
	public GraphML(Graph aGraph){
		this.target = aGraph;
	}

	private static Element generateElementForSourcecode(SourceReference[] sourcePosition){
		String sourcetxt="";
		Set<String> files = new HashSet<>();
		
		for(SourceReference sr : sourcePosition){
			files.add(sr.getFile());
		}
		
		for(String filename : files){
			sourcetxt += filename+";[";
			
			boolean first = true;
			for(SourceReference sr : sourcePosition){
				if(sr.getFile().equals(filename)){
					if(!first){
						sourcetxt += "/";
					}else{
						first = false;
					}
					
					sourcetxt += sr.getSourceRange()+";"+sr.getLineRange()+";"+sr.getColumnRange();
				}
			}
			
			sourcetxt += "];";
		}
		
		if(sourcetxt.length() > 0){
			Element data = new Element("data", xmlns);
			Attribute attr = new Attribute("key", "sourcecode");
			data.setAttribute(attr);
			data.setText(sourcetxt);
			return data;
		}
		
		return null;
	}
	
	private static SourceReference[] generateSourceCodeFromElement(Element ee){
		String srcs = ee.getText();
		String[] srcs_grps = srcs.split("];");
		List<SourceReference> ret = new ArrayList<>();
		
		for(String src : srcs_grps){
			String[] prt = src.split(";\\[");
			String[] ent = prt[1].split("/");
			
			for(String entt : ent){
				SourceReference sr = new SourceReference();
				String[] vals = entt.split(";");
				
				sr.setFile(prt[0]);
				
				// Source
				int pos1 = Integer.valueOf(vals[0].split("-")[0]);
				int pos2 = Integer.valueOf(vals[0].split("-")[1]);
				sr.setSourceRange(pos1, pos2);
				
				// Line
				pos1 = Integer.valueOf(vals[1].split("-")[0]);
				pos2 = Integer.valueOf(vals[1].split("-")[1]);
				sr.setLineRange(pos1, pos2);
				
				
				// Column
				pos1 = Integer.valueOf(vals[2].split("-")[0]);
				pos2 = Integer.valueOf(vals[2].split("-")[1]);
				sr.setColumnRange(pos1, pos2);
				
				ret.add(sr);
			}
			
		}
		
		return ret.toArray(new SourceReference[0]);
	}
	
	public static Element generateXmlNodes(Graph aGraph, Namespace xmlns){
		int nbnodes = 0;
		int nbedges = 0;
		
		Element g = new Element("graph", xmlns);
		Attribute attr;

		attr = new Attribute("id", "G");
		g.setAttribute(attr);

		attr = new Attribute("edgedefault", "directed");
		g.setAttribute(attr);
		
		attr = new Attribute("buildtime", Long.toString(aGraph.getBuildTime()));
		g.setAttribute(attr);

		for(String n : aGraph.getNodesNames()){
			nbnodes++;
			Element aNode = new Element("node", xmlns);
			g.addContent(aNode);

			NodeTypes nodeType = aGraph.getNodeType(n);
			
			Element data;
			
			if((data = generateElementForSourcecode(aGraph.getSourcePositionForNode(n))) != null){
				aNode.addContent(data);
			}

			if(nodeType != null){
				data = new Element("data", xmlns);
				attr = new Attribute("key", "type");
				data.setAttribute(attr);
				data.setText(nodeType.name());
				aNode.addContent(data);
			}

			attr = new Attribute("id", n);
			aNode.setAttribute(attr);

			for(NodeMarkers nm : aGraph.getNodeMarkers(n)){
				data = new Element("data", xmlns);
				attr = new Attribute("key", nm.name());
				data.setAttribute(attr);
				data.setText("true");
				aNode.addContent(data);
			}
			
			String ftstring = "";
			for(String ft : aGraph.getNodeFormalTypes(n)){
				ftstring+=((ftstring.length()==0)?"":",")+ft;
			}
			
			if(ftstring.length()>0){
				data = new Element("data", xmlns);
				attr = new Attribute("key", "formaltype");
				data.setAttribute(attr);
				data.setText(ftstring);
				aNode.addContent(data);
			}
		}

		int i = 1;

		for(EdgeIdentity e : aGraph.getEdges()){
			nbedges++;
			Element anEdge = new Element("edge", xmlns);
			g.addContent(anEdge);
			
			EdgeTypes edgeType = aGraph.getEdgeType(e.getFrom(), e.getTo());
			
			if(edgeType != null){
				Element data = new Element("data", xmlns);
				attr = new Attribute("key", "type");
				data.setAttribute(attr);
				data.setText(edgeType.name());
				anEdge.addContent(data);
			}

			Element data;
			
			if((data = generateElementForSourcecode(aGraph.getSourcePositionForEdge(e.getFrom(), e.getTo()))) != null){
				anEdge.addContent(data);
			}

			attr = new Attribute("id", "e"+i++);
			anEdge.setAttribute(attr);
			attr = new Attribute("source", e.getFrom());
			anEdge.setAttribute(attr);
			attr = new Attribute("target", e.getTo());
			anEdge.setAttribute(attr);

			for(EdgeMarkers em : aGraph.getEdgeMarkers(e.getFrom(), e.getTo())){
				data = new Element("data", xmlns);
				attr = new Attribute("key", em.name());
				data.setAttribute(attr);
				data.setText("true");
				anEdge.addContent(data);
				
				if(attr != null)
					anEdge.setAttribute(attr);
			}
		}

		logger.info("Write %d nodes and %d edges", nbnodes, nbedges);
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
		
		// <key id="type" for="edge" attr.name="sourcecode" attr.type="string" />
		tmp = new Element("key", xmlns);
		tmp.setAttribute(new Attribute("id", "type"));
		tmp.setAttribute(new Attribute("for", "edge"));
		tmp.setAttribute(new Attribute("attr.name", "sourcecode"));
		tmp.setAttribute(new Attribute("attr.type", "string"));
		root.addContent(tmp);
		
		// <key id="type" for="node" attr.name="formaltype" attr.type="string" />
		tmp = new Element("key", xmlns);
		tmp.setAttribute(new Attribute("id", "type"));
		tmp.setAttribute(new Attribute("for", "node"));
		tmp.setAttribute(new Attribute("attr.name", "formaltype"));
		tmp.setAttribute(new Attribute("attr.type", "string"));
		root.addContent(tmp);
		
		//<key id="uses_reflexion" for="node" attr.name="uses_reflexion" attr.type="boolean">
		//<default>false</default>
		//</key>
		for(NodeMarkers m : NodeMarkers.values()){
			tmp = new Element("key", xmlns);
			tmp.setAttribute(new Attribute("id", "type"));
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

		Format format = Format.getPrettyFormat();
		format.setLineSeparator(LineSeparator.UNIX);
		XMLOutputter output = new XMLOutputter(format);
		output.output(d, os);
		
	}



	@Override
	public void load(InputStream is) throws IOException {
		int nbnodes = 0;
		int nbedges = 0;
		
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
			nbnodes++;
			String nodename = e.getAttribute("id").getValue();
			g.addNode(nodename);
			
			for(Element ee : e.getChildren("data", xmlns)){
				String tmp = ee.getAttribute("key").getValue();
				if(tmp.equals("type")){
					// This is the node type declaration
					String type = ee.getValue();
					g.setNodeType(nodename, NodeTypes.valueOf(type));
				}else if(tmp.equals("marker")){
					// This is a node marker
					String isEnabled = ee.getText().toLowerCase();
					if(!isEnabled.equals("true"))
						continue;
					String marker = tmp; 
					g.markNode(nodename, NodeMarkers.valueOf(marker));
				}else if(tmp.equals("sourcecode")){
					SourceReference[] sc = generateSourceCodeFromElement(ee);
					
					for(SourceReference sr : sc){
						g.bindNodeToSourcePosition(nodename, sr);
					}
				}else if(tmp.equals("formaltype")){
					List<String> ft = new ArrayList<>();
					
					StringTokenizer st = new StringTokenizer(ee.getValue(), ",");
					while(st.hasMoreElements()){
						ft.add(st.nextToken());
					}
					
					if(ft.size()>0){
						g.setNodeFormalTypes(nodename, ft);
					}
				}
			}
		}
		
		for(Element e : graph.getChildren("edge", xmlns)){
			nbedges++;
			String source = e.getAttribute("source").getValue();
			String target = e.getAttribute("target").getValue();

			g.addDirectedEdge(source, target);
			
			for(Element ee : e.getChildren("data", xmlns)){
				String tmp = ee.getAttribute("key").getValue();
				if(tmp.equals("type")){
					// This is the edge type declaration
					String type = ee.getValue();
					g.setEdgeType(source, target, EdgeTypes.valueOf(type));
				}else if(tmp.equals("marker")){
					// This is an edge marker
					String isEnabled = ee.getText().toLowerCase();
					if(!isEnabled.equals("true"))
						continue;
					String marker = tmp; 
					g.markEdge(source, target, EdgeMarkers.valueOf(marker));
				}else if(tmp.equals("sourcecode")){
					SourceReference[] sc = generateSourceCodeFromElement(ee);
					
					for(SourceReference sr : sc){
						g.bindEdgeToSourcePosition(source, target, sr);
					}
				}
			}
		}
		
		logger.info("Read %d nodes and %d edges", nbnodes, nbedges);
	}

}
