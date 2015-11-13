package com.vmusco.smf.mutation.operators.misc;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.SmfMutationOperator;

import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ConstantMutationOperator extends SmfMutationOperator<CtLiteral<?>>{

	@Override
	public void process(CtLiteral<?> element) {
		if(element.getType() == null)
			return;

		String typename = element.getType().getSimpleName();

		if(typename.contains("String") || element.toString().equals("null")){
			return;
		}

		addElement(element);
	}

	@Override
	public CtElement[] getMutatedEntries(CtLiteral<?> element, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();

		if(element == null || !(element instanceof CtLiteral))
			return emptySet();
		
		CtLiteral<?> mutationTo = determineMutation(element, factory);

		if(mutationTo != null){
			mutationTo.setParent(element.getParent());
			result.add(mutationTo);
		}

		return result.toArray(new CtElement[0]);
	}
	
	public CtLiteral<?> determineMutation(CtLiteral<?> elem, Factory factory){
		String qname = elem.getType().getQualifiedName();
		if(qname.equals("long")){
			return handleLongMutation(elem, factory);
		}else if(qname.equals("int") ||
				qname.equals("short") ||
				qname.equals("byte") ){
			return handleIntMutation(elem, factory);
		}else if(qname.equals("boolean")){
			return handleBooleanMutation(elem, factory);
		}else if(qname.equals("double") ||
				qname.equals("float")){
			return handleDoubleMutation(elem, factory);
		}else{
			return null;
		}
	}

	private CtLiteral<?> handleBooleanMutation(CtLiteral elem, Factory factory){
		boolean analyzedValue = (boolean) elem.getValue();
		boolean mutateTo = !analyzedValue;

		CtLiteral<?> createLiteral = factory.Code().createLiteral(mutateTo);
		return createLiteral;
	}

	/**
	 * This function handle int, short and byte
	 * @param elem
	 * @param par
	 */
	private CtLiteral<?> handleIntMutation(CtLiteral elem, Factory factory){
		int analyzedValue = (int)elem.getValue();

		int mutateTo;

		if(analyzedValue == 1)
			mutateTo = 0;
		else if(analyzedValue == -1)
			mutateTo = 1;
		else if(analyzedValue == 5)
			mutateTo = -1;
		else
			mutateTo = analyzedValue + 1;


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
	
	/**
	 * This function handle long 
	 * @param elem
	 * @param par
	 */
	private CtLiteral<?> handleLongMutation(CtLiteral elem, Factory factory){
		long analyzedValue = (long) elem.getValue();
		long mutateTo = 0;
		
		if(analyzedValue != 1)
			mutateTo = analyzedValue + 1;

		CtLiteral<?> createLiteral = factory.Code().createLiteral(mutateTo);
		return createLiteral;
	}

	/**
	 * This function handle double and floats
	 * @param elem
	 * @param factory
	 * @return
	 */
	
	private CtLiteral<?> handleDoubleMutation(CtLiteral elem, Factory factory){
		double analyzedValue = 0.0d;

		if(elem.getValue() instanceof Double)
			analyzedValue = (double) elem.getValue();
		else
			analyzedValue = new Double((float) elem.getValue()).doubleValue();

		double mutateTo = 0;
		
		if(!Double.isNaN(analyzedValue)){
			if( (analyzedValue > 1 && elem.getValue() instanceof Double)
					|| (analyzedValue > 2 && elem.getValue() instanceof Float)){
				mutateTo = 1;
			}
		}

		CtLiteral<?> createLiteral;

		if(elem.getType().getQualifiedName().equals("double")){
			createLiteral = factory.Code().createLiteral((double)mutateTo);
		}else if(elem.getType().getQualifiedName().equals("float")){
			createLiteral = factory.Code().createLiteral((float)mutateTo);
		}else{
			return null;
		}

		return createLiteral;
	}

	@Override
	public String operatorId() {
		return "ConstantMutationOperator";
	}
}
