package com.vmusco.smf.mutation.operators.misc;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.SmfMutationOperator;

import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class InDecrementMutationOperator extends SmfMutationOperator<CtUnaryOperator<?>>{
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
	public void process(CtUnaryOperator<?> element) {
		if(!isCtElementCandidateAcceptable(element))
			return;

		if(element.getKind() == UnaryOperatorKind.POSTDEC || 
				element.getKind() == UnaryOperatorKind.PREDEC ||
				element.getKind() == UnaryOperatorKind.POSTINC ||
				element.getKind() == UnaryOperatorKind.PREINC){
			
			addElement(element);
		}
	}

	@Override
	public CtElement[] getMutatedEntries(CtUnaryOperator<?> toMutate, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();

		if(toMutate instanceof CtUnaryOperator){
			
			if(prepost && incdec){
				for(UnaryOperatorKind type : types){
					if(type == toMutate.getKind())
						continue;

					CtUnaryOperator<?> e = (CtUnaryOperator<?>) factory.Core().clone(toMutate);
					e.setKind(type);
					e.setParent(toMutate.getParent());
					result.add(e);
				}
			}else{
				UnaryOperatorKind target;

				if(prepost){
					// Invert pre/post
					if(toMutate.getKind() == UnaryOperatorKind.POSTDEC){
						target = UnaryOperatorKind.PREDEC;
					}else if(toMutate.getKind() == UnaryOperatorKind.PREDEC){
						target = UnaryOperatorKind.POSTDEC;
					}else if(toMutate.getKind() == UnaryOperatorKind.POSTINC){
						target = UnaryOperatorKind.PREINC;
					}else{
						target = UnaryOperatorKind.POSTINC;
					}
				}else if(incdec){
					// Invert inc/dec
					if(toMutate.getKind() == UnaryOperatorKind.POSTDEC){
						target = UnaryOperatorKind.POSTINC;
					}else if(toMutate.getKind() == UnaryOperatorKind.PREDEC){
						target = UnaryOperatorKind.PREINC;
					}else if(toMutate.getKind() == UnaryOperatorKind.POSTINC){
						target = UnaryOperatorKind.POSTDEC;
					}else{
						target = UnaryOperatorKind.PREDEC;
					}
				}else{
					return result.toArray(new CtElement[0]);
				}

				CtUnaryOperator<?> e = (CtUnaryOperator<?>) factory.Core().clone(toMutate);
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
