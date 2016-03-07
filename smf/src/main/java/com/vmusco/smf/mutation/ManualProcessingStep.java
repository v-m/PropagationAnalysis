package com.vmusco.smf.mutation;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

/**
 * This interface define a manual mutation operation to add in general abstract processing steps
 * such as in class {@link AbstractBinaryOperator}
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public interface ManualProcessingStep {
	/**
	 * Mutate the element
	 * @param anElement the element to mutate
	 * @param f the factory item to assist the mutation
	 * @return the result of the mutation
	 */
	CtElement mutate(CtElement anElement, Factory f);
}
