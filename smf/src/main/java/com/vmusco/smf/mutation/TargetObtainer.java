package com.vmusco.smf.mutation;

import spoon.reflect.declaration.CtElement;

/**
 * Defines a method to get a target in the mutation process
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public interface TargetObtainer {
	CtElement determineTarget(CtElement e);
}
