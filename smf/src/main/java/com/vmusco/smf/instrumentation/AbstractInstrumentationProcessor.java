package com.vmusco.smf.instrumentation;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

public abstract class AbstractInstrumentationProcessor extends AbstractProcessor<CtElement>{
	public abstract boolean isThisLineInstrumented(String line);
}
