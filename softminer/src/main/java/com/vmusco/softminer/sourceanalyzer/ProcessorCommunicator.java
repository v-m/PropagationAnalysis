package com.vmusco.softminer.sourceanalyzer;

import java.util.ArrayList;

import spoon.reflect.cu.SourcePosition;

import com.vmusco.smf.utils.SourceReference;
import com.vmusco.softminer.graphs.EdgeMarkers;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.processors.GraphItemRenamer;

/**
 * This class is used as a pipe of communication with the spoon processor
 * This is the easiest way to proceed with spoon as the processor
 * is instantiated by the spoon launcher, we can pass no parameter to this.
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class ProcessorCommunicator {
	public static String inpackage = ""; 
	public static Graph outputgraph = null;
	public static boolean hasErrors = false;
	public static boolean features_variables = true;
	public static ArrayList<String> excludedItems = new ArrayList<String>();
	public static DependenciesType granularityGraph = DependenciesType.FEATURES;
	public static String pattern;
	public static PatternBehavior patternBehavior;
	public static GraphItemRenamer nodesRenamer = null;
	/**
	 * Change this string to remove (shorten) the file path by removing a common prefix (eg. project directory)
	 */
	public static String prefixSourceCodeToRemove = null;
	
	public static String formatSourceCodeFilePath(String path){
		if(prefixSourceCodeToRemove == null)
			return path;
		
		if(path.startsWith(prefixSourceCodeToRemove))
			return path.substring(prefixSourceCodeToRemove.length());
		else
			return path;
	}
	
	/**
	 * OPTIONS FOR USE GRAPH
	 */
	public static boolean resolveInterfacesAndClasses = false;
	public static boolean includesFields = false;
	public static boolean removeOverridenMethods = false;
	
	// Include all nodes (even isolated ones)
	public static boolean includeAllNodes = true;
	
	public static void reset(){
		inpackage = "";
		pattern = null;
		patternBehavior = PatternBehavior.NO_PATTERN;
		outputgraph = null;
		hasErrors = false;
		excludedItems = new ArrayList<String>();
		granularityGraph = DependenciesType.FEATURES;
		nodesRenamer = null;
		resolveInterfacesAndClasses = false;
	}
	
	public enum PatternBehavior{
		NO_PATTERN, INCLUDE_IF_NONE, INCLUDE_IF_BOTH, INCLUDE_IF_AT_LEAST_ONE, INCLUDE_IF_AT_LEAST_SRC, INCLUDE_IF_AT_LEAST_DST, INCLUDE_IF_ONLY_SRC, INCLUDE_IF_ONLY_DST
	}
	
	private static boolean allowedByPattern(String src, String dst) {
		boolean patternpass = true;
		
		if(ProcessorCommunicator.pattern != null && ProcessorCommunicator.patternBehavior != ProcessorCommunicator.PatternBehavior.NO_PATTERN){
			boolean srcmatch = src.matches(ProcessorCommunicator.pattern);
			boolean dstmatch = dst.matches(ProcessorCommunicator.pattern);
			
			if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_NONE)
				patternpass = !srcmatch && !dstmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_BOTH)
				patternpass = srcmatch && dstmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_ONLY_SRC)
				patternpass = srcmatch && !dstmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_ONLY_DST)
				patternpass = !srcmatch && dstmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_AT_LEAST_ONE)
				patternpass = srcmatch || dstmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_AT_LEAST_SRC)
				patternpass = srcmatch;
			else if(ProcessorCommunicator.patternBehavior == ProcessorCommunicator.PatternBehavior.INCLUDE_IF_AT_LEAST_DST)
				patternpass = dstmatch;
		}
		
		return patternpass;
	}
	
	public static boolean addNode(String src, NodeTypes src_type, SourcePosition originator){
		if(outputgraph != null){
			String finalsrc = src;
			
			if(nodesRenamer != null){
				finalsrc = nodesRenamer.renamed(src);
			}
			
			if(!outputgraph.hasNode(finalsrc)){
				outputgraph.addNode(finalsrc);
			}
			
			outputgraph.setNodeType(finalsrc, src_type);
			
			if(originator != null){
				SourceReference sp = new SourceReference(originator);
				sp.setFile(formatSourceCodeFilePath(originator.getFile().getAbsolutePath()));
				outputgraph.bindNodeToSourcePosition(finalsrc, sp);
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean addEdgeIfAllowed(String src, String dst, NodeTypes src_type, NodeTypes dst_type, EdgeTypes edgeType, SourcePosition originator){
		if(allowedByPattern(src, dst)){
			if(outputgraph != null){
				String finalsrc = src;
				String finaldst = dst;
				
				if(nodesRenamer != null){
					finalsrc = nodesRenamer.renamed(src);
					finaldst = nodesRenamer.renamed(dst);
				}
				
				outputgraph.addDirectedEdgeAndNodeIfNeeded(finalsrc, finaldst, true, false);
				outputgraph.setNodeType(finalsrc, src_type);
				outputgraph.setNodeType(finaldst, dst_type);
				outputgraph.setEdgeType(finalsrc, finaldst, edgeType);
				
				if(originator != null){
					SourceReference sp = new SourceReference(originator);
					sp.setFile(formatSourceCodeFilePath(originator.getFile().getAbsolutePath()));
					outputgraph.bindEdgeToSourcePosition(finalsrc, finaldst, sp);
				}
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean markEdge(String src, String dst, EdgeMarkers aMarker){
		if(outputgraph != null){
			String finalsrc = src;
			String finaldst = dst;
			
			if(nodesRenamer != null){
				finalsrc = nodesRenamer.renamed(src);
				finaldst = nodesRenamer.renamed(dst);
			}
			
			if(outputgraph.hasDirectedEdge(finalsrc, finaldst)){
				outputgraph.markEdge(finalsrc, finaldst, aMarker);
				return true;
			}
		}

		return false;
	}

	public static boolean markNode(String node, NodeMarkers aMarker){
		if(outputgraph != null){
			String finalnode = node;
			
			if(nodesRenamer != null){
				finalnode = nodesRenamer.renamed(node);
			}
			
			if(outputgraph.hasNode(finalnode)){
				outputgraph.markNode(finalnode, aMarker);
				return true;
			}
		}

		return false;
	}
}
