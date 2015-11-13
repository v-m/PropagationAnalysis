package com.vmusco.smf.instrumentation;

import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

import com.vmusco.smf.testing.TestingInstrumentedCodeHelper;

/**
 * This Processor is used to inject stack trace printing in a given software
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class StackTracePrintingInstrumentationProcessor extends AbstractInstrumentationProcessor{
	private static final Class instrumentationClass = TestingInstrumentedCodeHelper.class;

	@Override
	public void process(CtElement arg0) {
		if(!(arg0 instanceof CtExecutable<?>))
			return;

		CtExecutable<?> exec = (CtExecutable<?>) arg0;

		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		
		if(exec.getBody() != null){ 
			snippet.setValue(instrumentationClass.getCanonicalName()+".printStackTrace()");
			exec.getBody().insertBegin(snippet);
		}
	}

	@Override
	public boolean isThisLineInstrumented(String line) {
		return line.startsWith(TestingInstrumentedCodeHelper.STACKTRACELINE);
	}

	@Override
	public String getLineIfInstrumented(String line) {
		if(!isThisLineInstrumented(line))
			return null;
		else
			return line.substring(TestingInstrumentedCodeHelper.STACKTRACELINE.length());
	}

}
