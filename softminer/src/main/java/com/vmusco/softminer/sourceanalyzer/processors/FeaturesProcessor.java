package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.reflect.visitor.filter.AbstractReferenceFilter;

import com.vmusco.smf.mutation.Mutation;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

@SuppressWarnings({"rawtypes","unchecked"})
public class FeaturesProcessor extends AbstractProcessor<CtNamedElement>{
	private HashSet<String> tagAsReflexion = new HashSet<String>();

	private static String getNodeForItemKey(CtExecutable anExecutable){
		return Mutation.resolveName((CtTypeMember)anExecutable);
	}
	
	@Override
	public void process(CtNamedElement element){
		if(!preliminarTests(element))
			return;

		CtExecutable execElement = (CtExecutable) element;
		String src = getNodeForItemKey(execElement);

		/**********************************
		 * Here we create a bridge between 
		 * a declared method and its abstract signature
		 */
		bridgeMethodAndAbstract(execElement, src);

		/**********************************
		 * Treating executable references
		 */
		List<CtExecutableReference> refs = Query.getReferences(element, new AbstractReferenceFilter<CtExecutableReference>(CtExecutableReference.class) {
			public boolean matches(CtExecutableReference reference) {
				return true;
			}
		});

		for(CtExecutableReference aReference : refs){
			try{
				treatExecutableReferences(aReference, src);
			}catch(Exception ex){
				exceptionOccured(ex);
			}
		}

		/**********************************
		 * Treating fields references
		 */

		if(ProcessorCommunicator.includesFields){
			AbstractFilter af = new AbstractFilter<CtFieldAccess>(CtFieldAccess.class) {
				public boolean matches(CtFieldAccess element) {
					return true;
				}
			};
			List<CtFieldAccess> refs2 = Query.getElements(element, af);
	
			for(CtFieldAccess anAccess : refs2){
				treatFieldReferences(anAccess, src);
			}
		}
	}

	private void exceptionOccured(Exception ex){
		System.err.println("Exception has occured");
		ex.printStackTrace();
	}

	private boolean preliminarTests(CtElement element) {
		if(!(element instanceof CtExecutable) && !(element instanceof CtField))
			return false;

		if(element instanceof CtField){
			CtField field = (CtField) element;
			String src = field.getReference().getQualifiedName();

			if(field.getDefaultExpression() != null){
				if(field.getDefaultExpression() instanceof CtInvocation){
					CtInvocation invok = (CtInvocation) field.getDefaultExpression();
					if(invok.getExecutable().getDeclaration() != null){
						String dst = getNodeForItemKey(invok.getExecutable().getDeclaration());
						ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL);
					}

				}else if(field.getDefaultExpression() instanceof CtConstructorCall){
					CtConstructorCall ccc = (CtConstructorCall) field.getDefaultExpression();
					if(ccc.getExecutable().getDeclaration() != null){
						String dst = getNodeForItemKey(ccc.getExecutable().getDeclaration());
						ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL);
					}
				}
			}
			return false;
		}

		return true;
	}

	private void treatFieldReferences(CtFieldAccess anAccess, String src) {
		EdgeTypes et = null;
		
		if(anAccess instanceof CtFieldRead){
			et = EdgeTypes.READ_OPERATION;
		}else if(anAccess instanceof CtFieldWrite){
			et = EdgeTypes.WRITE_OPERATION;
		}else{
			return;
		}
		
		String dst = anAccess.getSignature().split(" ")[1];
		
		if(et == EdgeTypes.WRITE_OPERATION)
			ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.FIELD, et);
		else
			ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.FIELD, et);
	}

	private void treatExecutableReferences(CtExecutableReference aReference, String src) {
		String dst = null;

		try{
			if(aReference.getDeclaration() == null){
				// This is an exodependency => not useful
	
				//But before, let's see if there is reflexion in it...
				if(aReference.toString().startsWith("java.lang.reflect.") ||
						aReference.toString().startsWith("java.lang.Class.") ||
						aReference.toString().startsWith("java.lang.Class<?>.")){
					tagAsReflexion.add(src);
				}
	
				return;
			}
		}catch(Exception ex){
			// I think there is a bug in spoon here...
			// Let's skip this case
			return;
		}

		CtExecutable aReferenceExecutable = aReference.getDeclaration();

		dst = getNodeForItemKey(aReferenceExecutable);
		ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL);
	}
	
	private CtMethod<?> isMethodComingFrom(CtMethod aMethod, CtTypeReference anElement){
		List<CtTypeReference<?>> resolveFrom = anElement.getDeclaration().getFormalTypeParameters();
		List<CtTypeReference<?>> resolveTo = anElement.getActualTypeArguments();
		if(resolveFrom.size() != resolveTo.size() && aMethod.getParent(CtClass.class) != null){
			resolveTo = (aMethod.getParent(CtClass.class)).getFormalTypeParameters();
		}else if(resolveFrom.size() != resolveTo.size()){
			// Maybe other cases can be implied here ?!
			return null;
		}
		
		for(CtMethod<?> s : (Set<CtMethod<?>>)anElement.getDeclaration().getMethods()){
			if(s.getParameters().size() != aMethod.getParameters().size()){
				continue;
			}
			
			CtTypeReference[] array = new CtTypeReference[s.getParameters().size()];
			int i=0;
			for(CtParameter<?> pr : s.getParameters()){
				array[i++] = pr.getType();
			}
			
			for(i=0; i<array.length; i++){
				for(int j=0; j<resolveFrom.size(); j++){
					if(array[i].equals(resolveFrom.get(j))){
						array[i] = resolveTo.get(j);
						break;
					}
				}
			}
			
			if(s.getSimpleName().equals(aMethod.getSimpleName())){
				List<CtParameter> meth_params = aMethod.getParameters();
				
				if(meth_params.size() == array.length){
					boolean found = true;
					for(i=0; i<meth_params.size(); i++){
						if(!meth_params.get(i).getType().equals(array[i])){
							found = false;
							break;
						}
					}
					
					if(found)
						return s;
				}
			}
		}
		
		return null;
	}
	
	
	private void bridgeMethodAndAbstract(CtExecutable execElement, String src) {
		String dst = "";

		if(ProcessorCommunicator.resolveInterfacesAndClasses && execElement instanceof CtMethod){
			CtMethod casted = (CtMethod) execElement;

			CtType<?> declaringType = ((CtTypeMember) execElement).getDeclaringType();

			if(declaringType instanceof CtClass){
				List<CtTypeReference> analyzeThoseIfces = new ArrayList<CtTypeReference>();
				List<CtTypeReference> analyzeThoseClasses = new ArrayList<CtTypeReference>();
				
				if(declaringType.getSuperclass() != null && declaringType.getSuperclass().getDeclaration() != null)
					analyzeThoseClasses.add(declaringType.getSuperclass());
				
				if(declaringType.getSuperInterfaces().size() > 0)
					analyzeThoseIfces.addAll(declaringType.getSuperInterfaces());
				
				
				while(analyzeThoseIfces.size() > 0 || analyzeThoseClasses.size() > 0){
					// We first check if an interface is the responsible of a potential overriding
					while(analyzeThoseIfces.size() > 0){
						CtTypeReference ifce = analyzeThoseIfces.remove(0);
						
						if(ifce instanceof CtTypeReference){
							CtTypeReference ctr = ((CtTypeReference) ifce);
	
							if(ctr.getDeclaration() == null)
								continue;
							
							CtMethod<?> exo = isMethodComingFrom(casted, ctr);
							
							if(exo != null){
								dst = getNodeForItemKey(exo);
								if(ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION)){
									//System.out.println(dst+" --I--> "+src);
								}
							}
						}
						
						analyzeThoseIfces.addAll(ifce.getSuperInterfaces());
					}
	
					while(analyzeThoseClasses.size() > 0){
						CtTypeReference parentClass = analyzeThoseClasses.remove(0);
	
						if(parentClass != null){
							CtMethod<?> exo = isMethodComingFrom(casted, parentClass);
							
							if(exo != null){
								dst = getNodeForItemKey(exo);
								if(ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION)){
									//System.out.println(dst+" --A--> "+src);
								}
							}
							
							if(parentClass.getSuperclass() != null && parentClass.getSuperclass().getDeclaration() != null)
								analyzeThoseClasses.add(parentClass.getSuperclass());
							
							analyzeThoseIfces.addAll(parentClass.getSuperInterfaces());
						}
					}
				}
			}
		}
	}

	@Override
	public void processingDone() {
		tagNodesAsReflexion();
		super.processingDone();
	}

	private void tagNodesAsReflexion(){
		for(String node : tagAsReflexion){
			if(ProcessorCommunicator.outputgraph.hasNode(node)){
				ProcessorCommunicator.markNode(node, NodeMarkers.USES_REFLEXION);
			}else{
				System.out.println("Node "+node+" missing !!!");
			}
		}
	}
}