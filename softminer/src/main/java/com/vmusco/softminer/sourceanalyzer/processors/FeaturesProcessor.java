package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.vmusco.smf.mutation.Mutation;
import com.vmusco.softminer.graphs.EdgeTypes;
import com.vmusco.softminer.graphs.NodeMarkers;
import com.vmusco.softminer.graphs.NodeTypes;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtNamedElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.reflect.visitor.filter.AbstractReferenceFilter;
import spoon.support.reflect.declaration.CtParameterImpl;

@SuppressWarnings({"rawtypes","unchecked"})
public class FeaturesProcessor extends AbstractProcessor<CtNamedElement>{
	private static int cpt = 0;
	private HashSet<String> tagAsReflexion = new HashSet<String>();

	private static String getNodeForItemKey(CtExecutable anExecutable){
		return Mutation.resolveName((CtTypeMember)anExecutable);
	}
	
	/**
	 * Do not use anymore this function.
	 * It is not homogeneous with the smf approach
	 * @param anExecutable
	 * @return
	 */
	@Deprecated
	private static String getNodeForItemKeyVersion0(CtExecutable anExecutable){
		String key = anExecutable.getSignature();

		CtType<?> declaringType = ((CtTypeMember) anExecutable).getDeclaringType();

		if(anExecutable instanceof CtMethod){
			CtMethod castedElement = (CtMethod) anExecutable;

			int pos = anExecutable.getSignature().indexOf("(");
			String st = anExecutable.getSignature().substring(0, pos);
			pos = st.lastIndexOf(' ');



			key = declaringType.getQualifiedName()+"."+anExecutable.getSignature().substring(pos+1);
		}else if(anExecutable instanceof CtConstructor){
			CtConstructor castedElement = (CtConstructor) anExecutable;

			key = declaringType.getQualifiedName()+
					"."+
					declaringType.getSimpleName()+
					anExecutable.getSignature().substring(declaringType.getQualifiedName().length());
		}

		return key;
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
		try{
			bridgeMethodAndAbstract(execElement, src);
		}catch(Exception ex){
			exceptionOccured(ex);
		}

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
			AbstractFilter af = new AbstractFilter<CtVariableAccess>(CtVariableAccess.class) {
				public boolean matches(CtVariableAccess element) {
					return true;
				}
			};
			List<CtVariableAccess> refs2 = Query.getElements(element, af);
	
			for(CtVariableAccess anAccess : refs2){
				try{
					treatFieldReferences(anAccess, src);
				}catch(Exception ex){
					exceptionOccured(ex);
				}
			}
		}
	}

	private void exceptionOccured(Exception ex){
		System.err.println("Exception has occured -- skipping for paper rush");
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

	private void treatFieldReferences(CtVariableAccess anAccess, String src) {
		try{
			if(anAccess.getVariable().getDeclaration() instanceof CtField){
				String dst = anAccess.getSignature().split(" ")[1];
				cpt += 1;

				EdgeTypes et = EdgeTypes.READ_OPERATION;

				if(anAccess.getParent() instanceof CtAssignment){
					// This is a writing operation
					CtAssignment casted = (CtAssignment)anAccess.getParent();
					if(casted.getAssigned() == anAccess){
						// This is definitively a writing operation
						et = EdgeTypes.WRITE_OPERATION;
					}

				}

				if(et == EdgeTypes.WRITE_OPERATION)
					ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.FIELD, et);
				else
					ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.FIELD, et);
			}
		}catch(IllegalStateException ex){
			//TODO: ???
			System.out.println("Exception here for: "+anAccess);
		}
	}

	private void treatExecutableReferences(CtExecutableReference aReference, String src) {
		String dst = null;
		cpt += 1;

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

		CtExecutable aReferenceExecutable = aReference.getDeclaration();

		dst = getNodeForItemKey(aReferenceExecutable);
		ProcessorCommunicator.addIfAllowed(src, dst, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.METHOD_CALL);
	}

	private void bridgeMethodAndAbstract(CtExecutable execElement, String src) {
		String dst = "";

		if(ProcessorCommunicator.resolveInterfacesAndClasses && execElement instanceof CtMethod){
			CtMethod casted = (CtMethod) execElement;
			CtTypeReference[] params = extractParams(casted.getParameters());
			//TypeVariable[] formalParams;
			List<CtTypeReference<?>> formalParams2;

			CtType<?> declaringType = ((CtTypeMember) execElement).getDeclaringType();

			if(declaringType instanceof CtClass){
				CtClass exploration = (CtClass)declaringType;

				formalParams2 = exploration.getFormalTypeParameters();
				// formalParams = exploration.getActualClass().getTypeParameters();

				//System.out.println(exploration.getQualifiedName());
				ArrayList analyzeThoseIfces = new ArrayList<>();

				if(exploration.getSuperclass() != null &&  
						exploration.getSuperclass().getDeclaration() instanceof CtInterface){
					analyzeThoseIfces.add(exploration.getSuperclass());
					analyzeThoseIfces.addAll(exploration.getSuperclass().getSuperInterfaces());
				}else{
					analyzeThoseIfces.addAll(exploration.getSuperInterfaces());
				}

				// We first check if an interface is the responsible of a potential overriding
				for(Object ifce : analyzeThoseIfces){

					if(ifce instanceof CtTypeReference){
						CtTypeReference ctr = ((CtTypeReference) ifce);

						if(ctr.getDeclaration() == null)
							continue;

						if(ctr.getDeclaration() instanceof CtInterface){
							for(Object o : ((CtInterface)ctr.getDeclaration()).getAllMethods()){
								CtMethod exo = ((CtMethod)o);

								CtInterface intfc = ((CtInterface)ctr.getDeclaration());

								if(compareParams(params, extractParams(exo.getParameters()), formalParams2, intfc.getFormalTypeParameters()) && casted.getSimpleName().equals(exo.getSimpleName())){
									dst = getNodeForItemKey(exo);

									if(ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION)){

									}
									exploration = null;
								}
							}
						}else{
							for(Object o : ctr.getAllExecutables()){
								CtExecutableReference exo = ((CtExecutableReference)o);

								if(compareParams(params, extractParams(exo.getActualTypeArguments()), null, null) && casted.getSimpleName().equals(exo.getSimpleName())){
									dst = getNodeForItemKey(exo.getDeclaration());
									if(ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.INTERFACE_IMPLEMENTATION)){

									}
									exploration = null;
								}
							}
						}
					}
				}

				while(exploration != null && exploration.getSuperclass() != null){
					CtClass parentClass = (CtClass)(exploration.getSuperclass()).getDeclaration();

					if(parentClass != null){
						CtMethod method = parentClass.getMethod(casted.getSimpleName(), params);

						if(method != null && method.getBody() == null){
							dst = getNodeForItemKey(method);
							ProcessorCommunicator.addIfAllowed(dst, src, NodeTypes.METHOD, NodeTypes.METHOD, EdgeTypes.ABSTRACT_METHOD_IMPLEMENTATION);
							exploration = null;
						}else{
							//System.out.println("Leaving from: "+exploration.getQualifiedName());
							exploration = parentClass;
							//formalParams = exploration.getActualClass().getTypeParameters();
							formalParams2 = exploration.getFormalTypeParameters();
						}
					}else
						exploration = null;
				}
			}
		}
	}

	private static CtTypeReference[] extractParams(List paramsIn) {
		CtTypeReference[] params = new CtTypeReference[paramsIn.size()];
		int i = 0;

		for(Object ent : paramsIn){
			if(ent instanceof CtTypeReference){
				params[i++] = (CtTypeReference)ent;
			}else{
				CtParameterImpl entry = (CtParameterImpl) ent;
				params[i++] = entry.getType();
			}

		}
		return params;
	}

	private static boolean compareParams(CtTypeReference[] one, CtTypeReference[] two){
		return compareParams(one, two, null, null);
	}

	/***
	 * Uses Java introspection SDK TypeVariable because of a potential Spoon Bug.
	 * See com.vmusco.softminer.tests.processors.TestMethodReturnIfceWithGenericsProcessor for a resulting processor and
	 * classes in com.vmusco.softminer.tests.cases.testMethodReturnIfceWithGenericsMixed for a concrete case
	 * @param one
	 * @param two
	 * @param formalTypesForOne
	 * @param formalTypesForTwo
	 * @return
	 */
	private static boolean compareParams(CtTypeReference[] one, CtTypeReference[] two, List<CtTypeReference<?>> formalTypesForOne, List<CtTypeReference<?>> formalTypesForTwo){
		if(one.length != two.length)
			return false;

		for(int i=0; i<one.length; i++){
			if(!one[i].equals(two[i])){

				if(formalTypesForOne.size() != formalTypesForTwo.size())
					return false;

				boolean found = false;
				for(int j=0; j<formalTypesForOne.size(); j++){
					if(one[i].equals(formalTypesForOne.get(j)) && two[i].equals(formalTypesForTwo.get(j))){
						found = true;
						break;
					}
				}

				if(!found)
					return false;
			}
		}

		return true;
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