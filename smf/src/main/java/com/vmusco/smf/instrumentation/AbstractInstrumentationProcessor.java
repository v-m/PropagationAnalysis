package com.vmusco.smf.instrumentation;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtElement;

/**
 * An instrumentation abstract processor
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class AbstractInstrumentationProcessor extends AbstractProcessor<CtElement>{
	public abstract boolean isThisLineInstrumented(String line);
	public abstract String getLineIfInstrumented(String line);
}
