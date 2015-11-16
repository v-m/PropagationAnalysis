package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmusco.smf.exceptions.MalformedSourcePositionException;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class SimpleFeaturesProcessor extends AbstractFeaturesProcessor {
	private Map<String, List<SourcePosition>> tagAsReflexion = new HashMap<String, List<SourcePosition>>();
	
	@Override
	public void newReadFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = anAccess.getSignature().split(" ")[1];
		
		try {
			ProcessorCommunicator.addEdgeIfAllowed(src_txt, dst_txt, NodeTypes.METHOD, NodeTypes.FIELD, EdgeTypes.READ_OPERATION, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void newWriteFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = anAccess.getSignature().split(" ")[1];
		
		try {
			ProcessorCommunicator.addEdgeIfAllowed(dst_txt, src_txt, NodeTypes.FIELD, NodeTypes.METHOD, EdgeTypes.WRITE_OPERATION, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void newReflexionUsage(CtExecutable<?> src) {
		String src_txt = getNodeForItemKey(src);
		if(!tagAsReflexion.containsKey(src_txt)){
			tagAsReflexion.put(src_txt, new ArrayList<SourcePosition>());
		}
		tagAsReflexion.get(src_txt).add(src.getPosition());
	}

	@Override
	public void newMethodCall(CtExecutable<?> src, CtExecutable<?> aReferenceExecutable) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = getNodeForItemKey(aReferenceExecutable);
		
		try {
			ProcessorCommunicator.addEdgeIfAllowed(src_txt, dst_txt, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void newIfceImplementation(CtExecutable<?> src, CtMethod<?> exo) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = getNodeForItemKey(exo);
		
		try {
			ProcessorCommunicator.addEdgeIfAllowed(dst_txt, src_txt, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void newAbstractImplementation(CtExecutable<?> src, CtMethod<?> exo) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = getNodeForItemKey(exo);

		try {
			ProcessorCommunicator.addEdgeIfAllowed(dst_txt, src_txt, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void processingDone() {
		tagNodesAsReflexion();
		super.processingDone();
	}

	private void tagNodesAsReflexion(){
		for(String node : tagAsReflexion.keySet()){
			if(ProcessorCommunicator.outputgraph.hasNode(node)){
				ProcessorCommunicator.markNode(node, NodeMarkers.USES_REFLEXION);
			}else{
				System.out.println("Node "+node+" missing !!!");
			}
		}
	}

	@Override
	public void newDeclarationMethodCall(CtField<?> src, CtExecutable<?> declaration) {
		String src_txt = src.getReference().getQualifiedName();
		String dst_txt = getNodeForItemKey(declaration);
		
		try {
			ProcessorCommunicator.addEdgeIfAllowed(src_txt, dst_txt, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INLINE_CONSTRUCTOR_CALL, src.getPosition());
		} catch (MalformedSourcePositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void methodVisited(CtExecutable<?> execElement) {
		String node_txt = getNodeForItemKey(execElement);
		try {
			ProcessorCommunicator.addNode(node_txt, NodeTypes.METHOD, execElement.getPosition());
		} catch (MalformedSourcePositionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
