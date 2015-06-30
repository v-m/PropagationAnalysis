package com.vmusco.smf.mutation.operators.pitest;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.MutationOperator;

import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;
import spoon.support.reflect.code.CtUnaryOperatorImpl;

public class InDecrementMutationOperator extends MutationOperator<CtUnaryOperatorImpl>{
	private boolean prepost;
	private boolean incdec;

	private final UnaryOperatorKind types[] = {
			UnaryOperatorKind.POSTDEC,
			UnaryOperatorKind.PREDEC,
			UnaryOperatorKind.POSTINC,
			UnaryOperatorKind.PREINC
	};
	
	public InDecrementMutationOperator() {
		super();
	}
	
	public InDecrementMutationOperator(boolean prepost, boolean incdec) {
		super();
		this.prepost = prepost;
		this.incdec = incdec;
	}

	@Override
	public void process(CtUnaryOperatorImpl element) {
		CtUnaryOperatorImpl ex = (CtUnaryOperatorImpl) element;

		if(element.getKind() == UnaryOperatorKind.POSTDEC || 
				element.getKind() == UnaryOperatorKind.PREDEC ||
				element.getKind() == UnaryOperatorKind.POSTINC ||
				element.getKind() == UnaryOperatorKind.PREINC){
			
			addElement(element);
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtUnaryOperatorImpl toMutate, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();

		if(toMutate instanceof CtUnaryOperator){
			CtUnaryOperator t = (CtUnaryOperator) toMutate;


			if(prepost && incdec){
				for(UnaryOperatorKind type : types){
					if(type == t.getKind())
						continue;

					CtUnaryOperator e = (CtUnaryOperator) factory.Core().clone(toMutate);
					e.setKind(type);
					e.setParent(toMutate.getParent());
					result.add(e);
				}
			}else{
				UnaryOperatorKind target;

				if(prepost){
					// Invert pre/post
					if(t.getKind() == UnaryOperatorKind.POSTDEC){
						target = UnaryOperatorKind.PREDEC;
					}else if(t.getKind() == UnaryOperatorKind.PREDEC){
						target = UnaryOperatorKind.POSTDEC;
					}else if(t.getKind() == UnaryOperatorKind.POSTINC){
						target = UnaryOperatorKind.PREINC;
					}else{
						target = UnaryOperatorKind.POSTINC;
					}
				}else if(incdec){
					// Invert inc/dec
					if(t.getKind() == UnaryOperatorKind.POSTDEC){
						target = UnaryOperatorKind.POSTINC;
					}else if(t.getKind() == UnaryOperatorKind.PREDEC){
						target = UnaryOperatorKind.PREINC;
					}else if(t.getKind() == UnaryOperatorKind.POSTINC){
						target = UnaryOperatorKind.POSTDEC;
					}else{
						target = UnaryOperatorKind.PREDEC;
					}
				}else{
					return result.toArray(new CtElement[0]);
				}

				CtUnaryOperator e = (CtUnaryOperator) factory.Core().clone(toMutate);
				e.setKind(target);
				e.setParent(toMutate.getParent());
				result.add(e);
			}
		}
		
		return result.toArray(new CtElement[0]);
	}

	@Override
	public String operatorId() {
		return "InDecrementMutationOperator";
	}
}
