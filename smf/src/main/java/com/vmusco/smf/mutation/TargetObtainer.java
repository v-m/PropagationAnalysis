package com.vmusco.smf.mutation;

import spoon.reflect.declaration.CtElement;

public interface TargetObtainer {
	CtElement DetermineTarget(CtElement e);
}
