package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import org.graphstream.stream.gephi.JSONSender;

import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class NetworkedGraphBuilder extends GraphBuilder{

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	private String host = "localhost";
	private int port = 8080;
	private String workspace = "workspace0";

	public NetworkedGraphBuilder(String projectName, String[] inputSources) {
		super(projectName, inputSources);
	}
	
	public NetworkedGraphBuilder(String projectName, String[] inputSources, String host, int port, String workspace) {
		this(projectName, inputSources);
		this.host = host;
		this.port = port;
		this.workspace = workspace;
	}

	public NetworkedGraphBuilder(String projectName, String[] inputSources, String[] cp) {
		super(projectName, inputSources, cp);
	}
	

	public NetworkedGraphBuilder(String projectName, String[] inputSources, String[] cp, String host, int port, String workspace) {
		super(projectName, inputSources, cp);
		this.host = host;
		this.port = port;
		this.workspace = workspace;
	}
	
	public void afterGraphInstatiation() {		
		JSONSender sender = new JSONSender(this.host, this.port, this.workspace);
		((org.graphstream.graph.Graph) ProcessorCommunicator.outputgraph.getGraph()).addSink(sender);
	};

}
