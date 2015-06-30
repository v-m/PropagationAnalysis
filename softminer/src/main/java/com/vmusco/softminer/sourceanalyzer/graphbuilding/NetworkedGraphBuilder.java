package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.graphstream.stream.gephi.JSONSender;

import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphStream;
import com.vmusco.softminer.graphs.Graph.GraphApi;
import com.vmusco.softminer.graphs.persistance.GraphPersistFileCategory;
import com.vmusco.softminer.graphs.persistance.GraphPersistanceDirector;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.compiler.SpoonCompiler;
import spoon.reflect.factory.Factory;
import spoon.reflect.factory.FactoryImpl;
import spoon.support.DefaultCoreFactory;
import spoon.support.StandardEnvironment;
import spoon.support.compiler.jdt.JDTBasedSpoonCompiler;

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
	
	public NetworkedGraphBuilder(String projectName, String[] inputSources, ClassPathObtainer cpo) {
		super(projectName, inputSources, cpo);
	}
	
	public NetworkedGraphBuilder(String projectName, String[] inputSources, ClassPathObtainer cpo, String host, int port, String workspace) {
		super(projectName, inputSources, cpo);
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
