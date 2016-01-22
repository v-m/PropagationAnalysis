package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmusco.smf.exceptions.MalformedSourcePositionException;
import com.vmusco.smf.utils.SpoonHelpers;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtGenericElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.reference.CtTypeParameterReferenceImpl;

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

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(src_txt, dst_txt, getExecutableType(src), NodeTypes.FIELD, EdgeTypes.READ_OPERATION, src.getPosition());
	}

	@Override
	public void newWriteFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess) {
		String src_txt = getNodeForItemKey(src);
		String dst_txt = anAccess.getSignature().split(" ")[1];

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(dst_txt, src_txt, NodeTypes.FIELD, getExecutableType(src), EdgeTypes.WRITE_OPERATION, src.getPosition());
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

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(src_txt, dst_txt, getExecutableType(src), getExecutableType(aReferenceExecutable), EdgeTypes.METHOD_CALL, src.getPosition());
	}

	@Override
	public void newIfceImplementation(CtExecutable<?> src, CtMethod<?> exo) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = getNodeForItemKey(exo);

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(dst_txt, src_txt, getExecutableType(exo), getExecutableType(src), EdgeTypes.INTERFACE_IMPLEMENTATION, src.getPosition());
	}

	@Override
	public void newInheritenceDeclaration(CtExecutable<?> src, CtMethod<?> exo) {
		String src_txt = getNodeForItemKey(src); 
		String dst_txt = getNodeForItemKey(exo);

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(dst_txt, src_txt, getExecutableType(exo), getExecutableType(src), EdgeTypes.METHOD_OVERRIDE, src.getPosition());
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

		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(src_txt, dst_txt, NodeTypes.FIELD, getExecutableType(declaration), EdgeTypes.INLINE_CONSTRUCTOR_CALL, src.getPosition());
	}

	@Override
	public void methodVisited(CtExecutable<?> execElement) {
		String node_txt = getNodeForItemKey(execElement);

		String n = ProcessorCommunicator.addNodeAndIgoreOriginatorErrors(node_txt, getExecutableType(execElement), execElement.getPosition());
		
		if(execElement instanceof CtGenericElement){
			List<CtTypeReference<?>> formalParameters =  ((CtGenericElement)execElement).getFormalTypeParameters();
			if(formalParameters.size() == 0 && execElement.getParent(CtClass.class) != null){
				formalParameters = execElement.getParent(CtClass.class).getFormalTypeParameters();
			}

			if(formalParameters.size() > 0){
				Map<String, String> mapper = new HashMap<>();

				for(CtTypeReference<?> el : formalParameters){
					if(el instanceof CtTypeParameterReference){
						CtTypeParameterReference pel = (CtTypeParameterReference) el;

						String newft = null;

						if(pel.getBounds().size() > 0){
							newft = "";
							for(CtTypeReference<?> r : pel.getBounds()){
								newft += ((newft.length()==0)?"":"&")+ r.getQualifiedName();
							}
						}

						mapper.put(el.getSimpleName(), newft);
					}else{
						// Instance of CtTypeReference
						// Nothing to do here..
						continue;
					}
				}

				List<String> ft = new ArrayList<>();

				for(CtParameter<?> p : execElement.getParameters()){
					CtTypeReference<?> ref = p.getType();

					while(ref instanceof CtArrayTypeReference<?>){
						CtArrayTypeReference<?> arr = (CtArrayTypeReference<?>) ref;
						ref = arr.getComponentType();
					}
					if(ref instanceof CtTypeParameterReference){
						CtTypeParameterReference el = (CtTypeParameterReference) ref;

						String newft = null;
						if(!mapper.containsKey(el.getSimpleName()) || mapper.get(el.getSimpleName()) == null){
							newft = el.getSimpleName();
						}else{
							newft = el.getSimpleName()+":"+mapper.get(el.getSimpleName());
						}

						if(!ft.contains(newft)){
							//System.out.println("+"+newft);
							ft.add(newft);
						}
					}
				}

				if(ft.size()>0){
					ProcessorCommunicator.outputgraph.setNodeFormalTypes(n, ft);
				}
			}
		}
		
	}

	private static NodeTypes getExecutableType(CtExecutable e){
		if(e instanceof CtConstructor)
			return NodeTypes.CONSTRUCTOR;
		else
			return NodeTypes.METHOD;
	}

	@Override
	public void newImplicitlyInheritedFromParent(CtExecutable exec, CtClass currentClass) {
		String dst_txt = getNodeForItemKey(exec); 
		String src_txt = currentClass.getQualifiedName()+"."+SpoonHelpers.notFullyQualifiedName((CtTypeMember)exec);

		
		ProcessorCommunicator.addEdgeIfAllowedAndIgnoreOriginatorExceptions(dst_txt, src_txt, getExecutableType(exec), getExecutableType(exec), EdgeTypes.METHOD_OVERRIDE, exec.getPosition());
	}
}
