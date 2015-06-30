package com.vmusco.smf.mutation.operators.KingOffutt91;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vmusco.smf.mutation.MutationGateway;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.TargetObtainer;

import spoon.reflect.code.BinaryOperatorKind;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtBinaryOperator;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.code.CtInvocationImpl;
import spoon.support.reflect.code.CtVariableReadImpl;

public class UnaryOperatorInsertionMutator extends MutationOperator<CtBinaryOperator>{

	@Override
	public void process(CtBinaryOperator element) {
		if(element.getKind() == BinaryOperatorKind.PLUS ||
				element.getKind() == BinaryOperatorKind.MINUS ||
				element.getKind() == BinaryOperatorKind.MUL ||
				element.getKind() == BinaryOperatorKind.DIV ||
				element.getKind() == BinaryOperatorKind.MOD ||
				element.getKind() == BinaryOperatorKind.OR ||
				element.getKind() == BinaryOperatorKind.AND){
			MutationGateway.addElement(element);
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtBinaryOperator element, Factory factory) {
		List<CtElement> aList = new ArrayList<CtElement>();
		
		if(element.getKind() == BinaryOperatorKind.PLUS ||
				element.getKind() == BinaryOperatorKind.MINUS ||
				element.getKind() == BinaryOperatorKind.MUL ||
				element.getKind() == BinaryOperatorKind.DIV ||
				element.getKind() == BinaryOperatorKind.MOD){

			CtExpression<?> cpy = factory.Core().clone(element);
			CtExpression<?> expr = factory.Code().createBinaryOperator(cpy, factory.Code().createLiteral(-1), BinaryOperatorKind.MUL);
			cpy.setParent(expr);
			expr.setParent(element.getParent());
			
			cpy = factory.Core().clone(element);
			CtExpression<?> expr2 = factory.Code().createBinaryOperator(cpy, factory.Code().createLiteral(1), BinaryOperatorKind.PLUS);
			cpy.setParent(expr2);
			expr2.setParent(element.getParent());
			
			cpy = factory.Core().clone(element);
			CtExpression<?> expr3 = factory.Code().createBinaryOperator(cpy, factory.Code().createLiteral(1), BinaryOperatorKind.MINUS);
			cpy.setParent(expr3);
			expr3.setParent(element.getParent());
			
			aList.add(expr);
			aList.add(expr2);
			aList.add(expr3);
		}else if(element.getKind() == BinaryOperatorKind.OR ||
				element.getKind() == BinaryOperatorKind.AND){
			CtUnaryOperator<Object> uop = factory.Core().createUnaryOperator();

			CtExpression cpy = factory.Core().clone(element);
			cpy.setParent(uop);
			
			uop.setKind(UnaryOperatorKind.NOT);
			uop.setOperand(cpy);
			uop.setParent(element.getParent());
			
			aList.add(uop);
		}

		return aList.toArray(new CtElement[0]);
	}

	@Override
	public String operatorId() {
		return "UOI";
	}

}
