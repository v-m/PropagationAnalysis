package com.vmusco.smf.instrumentation;

import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtTypeMember;

import com.vmusco.smf.mutation.Mutation;

/**
 * Processor used to add a line at the entry of each function
 * NOTE: We cannot take into consideration the abstract methods and interface methods 
 * declaration points as they have a null body !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class EntryMethodInstrumentationProcessor extends AbstractInstrumentationProcessor{
	public static final String STARTKEY = "="+((char)2)+"EMINSTR=>";

	@Override
	public void process(CtElement arg0) {
		if(!(arg0 instanceof CtExecutable<?>))
			return;
		
		CtExecutable<?> exec = (CtExecutable<?>) arg0;
		
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		snippet.setValue("System.out.println(\""+STARTKEY+Mutation.resolveName((CtTypeMember) exec)+"\")");
		
		if(exec.getBody() != null){
			exec.getBody().insertBegin(snippet);
			//System.out.println(arg0);
		}
	}
	
	@Override
	public boolean isThisLineInstrumented(String line) {
		return line.startsWith(STARTKEY);
	}
}
