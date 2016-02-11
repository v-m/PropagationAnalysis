package com.vmusco.smf.mutation.operators.misc;

import java.util.ArrayList;
import java.util.List;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import com.vmusco.smf.mutation.SmfMutationOperator;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class NegateConstantsOperator extends SmfMutationOperator<CtLiteral<?>>{

	@Override
	public void process(CtLiteral<?> element) {
		if(!isCtElementCandidateAcceptable(element))
			return;

		if(element.getType() == null)
			return;

		String typename = element.getType().getSimpleName();

		if(typename.equals("int") || typename.equals("short") || typename.equals("byte") || typename.equals("float")){
			addElement(element);
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtLiteral<?> toMutate, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();

		CtElement mutationTo = determineMutation(toMutate, factory);

		if(mutationTo != null){
			mutationTo.setParent(toMutate.getParent());
			result.add(mutationTo);
		}

		return result.toArray(new CtElement[0]);
	}

	public CtElement determineMutation(CtLiteral<?> elem, Factory factory){
		String qname = elem.getType().getQualifiedName();
		if(qname.equals("int") ||
				qname.equals("short") ||
				qname.equals("byte") ){
			return handleIntMutation(elem, factory);
		}else if(qname.equals("float")){
			return handleFloatMutation(elem, factory);
		}else{
			return null;
		}
	}

	/**
	 * This function handle int, short and byte
	 * @param elem
	 * @param par
	 */
	private CtLiteral<?> handleIntMutation(CtLiteral elem, Factory factory){
		int analyzedValue = (int)elem.getValue();
		int mutateTo = -1 * analyzedValue;

		CtLiteral<?> createLiteral;

		if(elem.getType().getQualifiedName().equals("int")){
			createLiteral = factory.Code().createLiteral((int)mutateTo);
		}else if(elem.getType().getQualifiedName().equals("short")){
			createLiteral = factory.Code().createLiteral((short)mutateTo);
		}else if(elem.getType().getQualifiedName().equals("byte")){
			createLiteral = factory.Code().createLiteral((byte)mutateTo);
		}else{
			return null;
		}

		return createLiteral;
	}

	private CtLiteral<?> handleFloatMutation(CtLiteral elem, Factory factory){
		float analyzedValue = (float) elem.getValue();
		float mutateTo = -1 * analyzedValue;
		
		CtLiteral<?> createLiteral = factory.Code().createLiteral(mutateTo);
		return createLiteral;
	}
	
	@Override
	public String operatorId() {
		return "NegateConstantsOperator";
	}
}
