package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtConstructorCall;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtFieldRead;
import spoon.reflect.code.CtFieldWrite;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtInterface;
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
import spoon.support.reflect.reference.SpoonClassNotFoundException;

import com.vmusco.smf.utils.SpoonHelpers;
import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

/**
 *
 * @author Vincenzo Musco - http://www.vmusco.com
 */
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class AbstractFeaturesProcessor extends AbstractProcessor<CtNamedElement>{
	public abstract void methodVisited(CtExecutable<?> execElement);
	public abstract void newReadFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess);
	public abstract void newWriteFieldAccess(CtExecutable<?> src, CtFieldAccess<?> anAccess);
	public abstract void newReflexionUsage(CtExecutable<?> src);
	public abstract void newMethodCall(CtExecutable<?> src, CtExecutable<?> aReferenceExecutable);
	public abstract void newIfceImplementation(CtExecutable<?> src, CtMethod<?> exo);
	public abstract void newInheritenceDeclaration(CtExecutable<?> src, CtMethod<?> exo);
	/**
	 * Declare an external method which is not overridden but depends on an instance of its parent
	 * @param exec the parent method
	 * @param currentClass the class which implicitly inherit the method
	 */
	public abstract void newImplicitlyInheritedFromParent(CtExecutable exec, CtClass currentClass);


	/**
	 * A default constructor is invoked by a declaration in a class
	 * @param src
	 * @param declaration
	 */
	public abstract void newDeclarationMethodCall(CtField<?> src, CtExecutable<?> declaration);

	/**
	 * Get the full name for an executable (method)
	 * @param anExecutable
	 * @return
	 */
	public static String getNodeForItemKey(CtExecutable anExecutable){
		return SpoonHelpers.resolveName((CtTypeMember)anExecutable);
	}





	@Override
	public void process(CtNamedElement element){
		if(ProcessorCommunicator.resolveInterfacesAndClasses && element instanceof CtClass){
			processClass((CtClass)element);
			return;
		}

		if(!preliminarTests(element))
			return;

		// Up to here, we have only CtExecutable elements
		CtExecutable execElement = (CtExecutable) element;

		// We add the method node
		if(ProcessorCommunicator.includeAllNodes){
			methodVisited(execElement);
		}

		// Bridge between a declared method and its abstract signature
		bridgeMethodAndAbstract(execElement);

		// Treating executable references
		List<CtExecutableReference> refs = Query.getReferences(element, new AbstractReferenceFilter<CtExecutableReference>(CtExecutableReference.class) {
			public boolean matches(CtExecutableReference reference) {
				return true;
			}
		});

		for(CtExecutableReference aReference : refs){
			try{
				treatExecutableReferences(aReference, execElement);
			}catch(Exception ex){
				exceptionOccured(ex);
			}
		}

		// Treating fields references
		if(ProcessorCommunicator.includesFields){
			AbstractFilter af = new AbstractFilter<CtFieldAccess>(CtFieldAccess.class) {
				public boolean matches(CtFieldAccess element) {
					return true;
				}
			};
			List<CtFieldAccess> refs2 = Query.getElements(element, af);

			for(CtFieldAccess anAccess : refs2){
				treatFieldReferences(anAccess, execElement);
			}
		}
	}

	private void processClass(CtClass element) {
		String currentClass = element.getSignature();

		Set<String> localMethods = new HashSet<>();
		List<CtExecutable> otherMethods = new ArrayList<>();

		//System.out.println(element.getClass().getSimpleName()+" ~~~ "+element.getSignature());
		Collection<CtExecutableReference<?>> allExecutables = null;
		try{
			allExecutables = element.getAllExecutables();
		}catch(SpoonClassNotFoundException e){
			if(element.getSuperclass().getDeclaration() == null){
				// The class directly depends on an exo-class
				// Nothing to do here as we don't care about exo-dependencies
			}else{
				// Case when a class inherit an endo class but the inherited class itself inherit from an exo class
				// eg. A extends B and B extends C where A and B are endo-classes, and C is an exo-class
				// TODO: Futher implementation here?
			}
			
			return;
		}

		for(CtExecutableReference exec: allExecutables){
			try{
				if(exec == null || 
						exec.getDeclaration() == null ||
						exec.getDeclaration() instanceof CtConstructor || 
						exec.getDeclaringType() == null || 
						exec.getDeclaringType().getDeclaration() == null)
					continue;
			}catch(NullPointerException ex){
				// Skip in case of null anywhere
				continue;
			}
			
			String declaringClass = exec.getDeclaringType().getDeclaration().getSignature();
			String signature = SpoonHelpers.notFullyQualifiedName((CtTypeMember)exec.getDeclaration());
			if(declaringClass.equals(currentClass)){
				localMethods.add(signature);
				//System.out.println("INNER: "+signature);
			}else{
				otherMethods.add(exec.getDeclaration());
			}
		}

		for(CtExecutable exec : otherMethods){
			String signature = SpoonHelpers.notFullyQualifiedName((CtTypeMember)exec);

			if(!localMethods.contains(signature)){
				newImplicitlyInheritedFromParent(exec, element);
			}
		}
	}
	private void exceptionOccured(Exception ex){
		System.err.println("Exception has occured");
		ex.printStackTrace();
	}

	private boolean preliminarTests(CtElement element) {
		if(element instanceof CtExecutable || (ProcessorCommunicator.includesFields && element instanceof CtField)){
			if(element instanceof CtField){
				CtField field = (CtField) element;

				// Note: if .getExecutable() is null, it is an exo dependency
				if(field.getDefaultExpression() != null){
					if(field.getDefaultExpression() instanceof CtInvocation){
						CtInvocation invok = (CtInvocation) field.getDefaultExpression();
						
						if(invok.getExecutable().getDeclaringType() != null && // Spoon bug requires this test for avoiding exception triggering
								invok.getExecutable() != null && 
								invok.getExecutable().getDeclaration() != null){
							newDeclarationMethodCall(field, invok.getExecutable().getDeclaration());
						}
					}else if(field.getDefaultExpression() instanceof CtConstructorCall){
						CtConstructorCall ccc = (CtConstructorCall) field.getDefaultExpression();
						if(ccc.getExecutable() != null && ccc.getExecutable().getDeclaration() != null){
							newDeclarationMethodCall(field, ccc.getExecutable().getDeclaration());
						}
					}
				}
				return false;
			}else{
				CtExecutable method = (CtExecutable) element;

				if(ProcessorCommunicator.removeOverridenMethods){
					if(method instanceof CtMethod){
						if(getAllOverriden(((CtMethod)method)).length > 0){
							return false;
						}
					}
				}
			}

			return true;
		}else{
			return false;
		}
	}

	private void treatFieldReferences(CtFieldAccess anAccess, CtExecutable src) {
		if(anAccess instanceof CtFieldRead){
			newReadFieldAccess(src, anAccess);
		}else if(anAccess instanceof CtFieldWrite){
			newWriteFieldAccess(src, anAccess);
		}else{
			return;
		}
	}


	private void treatExecutableReferences(CtExecutableReference aReference, CtExecutable src) {
		try{
			if(aReference.getDeclaration() == null){
				// This is an exodependency => not useful

				//But before, let's see if there is reflection in it...
				if(aReference.toString().startsWith("java.lang.reflect.") ||
						aReference.toString().startsWith("java.lang.Class.") ||
						aReference.toString().startsWith("java.lang.Class<?>.")){
					newReflexionUsage(src);
				}

				return;
			}
		}catch(Exception ex){
			// I think there is a bug in spoon here...
			// Let's skip this case
			return;
		}

		CtExecutable aReferenceExecutable = aReference.getDeclaration();

		if(ProcessorCommunicator.dropUselessCalls){
			if(aReferenceExecutable.getType().toString().equals("void")){
				return;
			}
			
			if(aReferenceExecutable.getParent().getParent() instanceof CtBlock){
				return;
			}
		}	
		
		if(ProcessorCommunicator.removeOverridenMethods){
			if(aReferenceExecutable instanceof CtMethod){
				if(getAllOverriden(((CtMethod)aReferenceExecutable)).length > 0){
					return;
				}
			}
		}

		newMethodCall(src, aReferenceExecutable);
	}

	/**
	 * Return the method in anElement which is extended by the one passed (aMethod) or null if none
	 * @param aMethod
	 * @param anElement
	 * @return
	 */
	private static CtMethod<?> isMethodComingFrom(CtMethod aMethod, CtTypeReference anElement){
		List<CtTypeReference<?>> resolveFrom = anElement.getDeclaration().getFormalTypeParameters();
		List<CtTypeReference<?>> resolveTo = anElement.getActualTypeArguments();
		if(resolveFrom.size() != resolveTo.size() && aMethod.getParent(CtClass.class) != null){
			resolveTo = (aMethod.getParent(CtClass.class)).getFormalTypeParameters();
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
						if(resolveTo.size() == 0)
							array[i] = null;
						else
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
						if(
								(array[i] == null && meth_params.get(i).getType().getActualClass() != null && !meth_params.get(i).getType().getActualClass().equals(Object.class))
								|| (array[i] != null && !meth_params.get(i).getType().equals(array[i]))
						){
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

	private static boolean isInClass(CtMethod aMethod){
		CtType<?> declaringType = ((CtTypeMember) aMethod).getDeclaringType();

		return declaringType instanceof CtClass;
	}

	private static boolean isInInterface(CtMethod aMethod){
		CtType<?> declaringType = ((CtTypeMember) aMethod).getDeclaringType();

		return declaringType instanceof CtInterface;
	}

	/**
	 * Return CtMethods overridden by the "casted" one
	 * @param casted
	 * @return
	 */
	private static CtMethod<?>[] getAllOverriden(CtMethod casted){
		List<CtMethod<?>> ret = new ArrayList<CtMethod<?>>();

		CtType<?> declaringType = ((CtTypeMember) casted).getDeclaringType();

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
							ret.add(exo);
							//newIfceImplementation(execElement, exo);
						}
					}

					analyzeThoseIfces.addAll(ifce.getSuperInterfaces());
				}

				while(analyzeThoseClasses.size() > 0){
					CtTypeReference parentClass = analyzeThoseClasses.remove(0);

					if(parentClass != null){
						CtMethod<?> exo = isMethodComingFrom(casted, parentClass);

						if(exo != null){
							ret.add(exo);
							//newAbstractImplementation(execElement, exo);
						}

						if(parentClass.getSuperclass() != null && parentClass.getSuperclass().getDeclaration() != null)
							analyzeThoseClasses.add(parentClass.getSuperclass());

						analyzeThoseIfces.addAll(parentClass.getSuperInterfaces());
					}
				}
			}
		}

		return ret.toArray(new CtMethod<?>[0]);
	}

	private void bridgeMethodAndAbstract(CtExecutable execElement) {
		if(ProcessorCommunicator.resolveInterfacesAndClasses && execElement instanceof CtMethod){
			CtMethod casted = (CtMethod) execElement;

			for(CtMethod<?> aMethod : getAllOverriden(casted)){
				if(isInClass(aMethod)){
					newInheritenceDeclaration(execElement, aMethod);
				}else if(isInInterface(aMethod)){
					newIfceImplementation(execElement, aMethod);
				}
			}
		}
	}
}