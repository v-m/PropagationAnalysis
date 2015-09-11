package com.vmusco.smf.mutation.operators.KingOffutt91;

import java.util.HashMap;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtConditional;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.CtVariableRead;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtTypedElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import com.vmusco.smf.mutation.MutationGateway;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.TargetObtainer;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class AbsoluteValueInsertionMutator extends MutationOperator<CtElement>{

	@Override
	public void process(CtElement element) {
		if(element instanceof CtInvocation<?>){
			CtInvocation<?> srcinv = (CtInvocation<?>) element;

			if(!addAbsolute(srcinv) && !isJavaAbsolute(srcinv.getSignature()))
				return;
			
			if(srcinv.getParent() instanceof CtStatement)
				return;
			
			MutationGateway.addElement(element);
		}else if(element instanceof CtLiteral<?> || element instanceof CtVariableRead<?> || element instanceof CtVariableAccess<?>){
			CtTypedElement<?> anElem = (CtTypedElement<?>) element;

			CtElement findPar = anElem;
			while(findPar != null){
				findPar = findPar.getParent(CtInvocation.class);
				
				if(findPar!=null){
					if(isJavaAbsolute(findPar.getSignature())){
						return;
					}
				}
			}
			
			if(!isAcceptableType(anElem.getType()))
				return;

			MutationGateway.addElement(anElem);
		}
	}

	@Override
	public HashMap<CtElement, TargetObtainer> getMutatedEntriesWithTarget(CtElement element, Factory factory) {
		HashMap<CtElement, TargetObtainer> ret = new HashMap<CtElement, TargetObtainer>();
		
		if(element instanceof CtInvocation && isJavaAbsolute(element.getSignature())){
			CtInvocation<?> srcinv = (CtInvocation<?>) element;
			
			CtElement newElement = (CtElement) factory.Core().clone(srcinv.getArguments().get(0));
			newElement.setParent(srcinv.getParent());
			ret.put(newElement, MutationOperator.createParentTarget(1));
			
			CtConditional<?> inv = createConditionalAbs(factory, (CtExpression<?>) srcinv.getArguments().get(0), true);
			inv.setParent(srcinv.getParent());
			ret.put(inv, MutationOperator.createParentTarget(1));
		}else{
			//CtTypedElement<?> anElem = (CtTypedElement<?>) element;
			CtElement clonedElement = factory.Core().clone(element);

			if(element instanceof CtLiteral){
				CtLiteral<?> lit = factory.Code().createLiteral(-1);
				CtBinaryOperator<Object> createBinaryOperator = factory.Code().createBinaryOperator(lit, (CtLiteral<?>)clonedElement, BinaryOperatorKind.MUL);
				clonedElement.setParent(createBinaryOperator);
				lit.setParent(createBinaryOperator);
				ret.put(createBinaryOperator, MutationOperator.createSelfTarget());
			}else{
				CtConditional<?> inv = createConditionalAbs(factory, clonedElement, false);
				clonedElement.setParent(inv);
				ret.put(inv, MutationOperator.createSelfTarget());
				
				inv = createConditionalAbs(factory, element, true);
				ret.put(inv, MutationOperator.createSelfTarget());
			}
		}

		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CtConditional createConditionalAbs(Factory f, CtElement e, boolean invabs){
		CtElement cloned_e = f.Core().clone(e);

		CtConditional<?> cond = f.Core().createConditional();

		CtExpression<Boolean> condition = f.Code().createBinaryOperator((CtExpression<?>) cloned_e, f.Code().createLiteral(0), BinaryOperatorKind.GE);
		CtExpression negate = f.Code().createBinaryOperator((CtExpression<?>) cloned_e, f.Code().createLiteral(-1), BinaryOperatorKind.MUL);

		cond.setCondition(condition);
		if(invabs){
			cond.setThenExpression(negate);
			cond.setElseExpression((CtExpression) cloned_e);
		}else{
			cond.setThenExpression((CtExpression) cloned_e);
			cond.setElseExpression(negate);
		}
		
		condition.setParent(cond);
		negate.setParent(cond);
		cloned_e.setParent(cond);

		return cond;
	}

	/*private CtInvocation<?> createInvocation(Factory f, CtElement element) {
		CtExpression<?> anExpr = (CtExpression<?>) element;

		Method t = null; 
		try {
			t = java.lang.Math.class.getMethod("abs", new Class[] {anExpr.getType().getActualClass()});
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		CtExecutableReference<?> met = f.Method().createReference(t);
		return f.Code().createInvocation(null, met, f.Core().clone(anExpr));
	}*/

	@Override
	public CtElement[] getMutatedEntries(CtElement element, Factory factory) {
		return null;
	}

	@Override
	public String operatorId() {
		return "ABS";
	}

	private boolean addAbsolute(CtElement element){
		if(element instanceof CtInvocation<?>){
			CtInvocation<?> inv = (CtInvocation<?>) element;
			String sign = inv.getSignature();
			if(!isAcceptableType(inv.getType()))
				return false;

			if(isJavaAbsolute(sign)){
				// This is already a absolute value
				return false;
			}
		}

		return true;
	}
	
	private boolean isAcceptableType(CtTypeReference<?> element){
		if(element == null)
			return false;
		String el = element.toString();

		return (el.equals("int") ||
				el.equals("java.lang.Integer") ||
				el.equals("long") ||
				el.equals("java.lang.Long") ||
				el.equals("float") ||
				el.equals("java.lang.Float") ||
				el.equals("double") ||
				el.equals("java.lang.Double"));
	}

	public boolean isJavaAbsolute(String sign){
		String cleaner = "";
		
		for(int i = 0; i<sign.length(); i++){
			char c = sign.charAt(i);
			
			if(Character.isLetter(c) || c == '#' || c == '.')
				cleaner += c;
		}
		
		return cleaner.startsWith("java.lang.Math#abs");
	}
}
