package com.vmusco.smf.mutation;

import com.vmusco.smf.mutation.operators.AbstractBinaryOperator;

import spoon.reflect.declaration.CtElement;
import spoon.reflect.factory.Factory;

/**
 * This interface define a manual mutation operation to add in general abstract processing steps
 * such as in class {@link AbstractBinaryOperator}
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
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
