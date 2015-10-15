package com.vmusco.smf.instrumentation;

import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtTypeMember;

import com.vmusco.smf.utils.SpoonHelpers;

/**
 * Processor used to add a line at the entry of each function
 * NOTE: We cannot take into consideration the abstract methods and interface methods 
 * declaration points as they have a null body !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class EntryMethodInstrumentationProcessor extends AbstractInstrumentationProcessor{
	public static final String STARTKEY = ((char)2)+"=EMINSTR=>";
	public static final String ENDKEY = ((char)2)+"=LMINSTR=>";

	@Override
	public void process(CtElement arg0) {
		if(arg0 instanceof CtExecutable<?>){
			CtExecutable<?> exec = (CtExecutable<?>) arg0;
			System.out.println(exec.getSignature());
			if(exec.getBody() != null){
				CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
				snippet.setValue("java.lang.System.out.println(\""+STARTKEY+SpoonHelpers.resolveName((CtTypeMember) exec)+"\")");
				exec.getBody().insertBegin(snippet);

				if(exec instanceof CtConstructor<?> || (exec instanceof CtMethod<?> && ((CtMethod)exec).getType().getSimpleName().equals("void"))){
					snippet = getFactory().Core().createCodeSnippetStatement();
					snippet.setValue("java.lang.System.out.println(\""+ENDKEY+SpoonHelpers.resolveName((CtTypeMember) exec)+"\")");
					exec.getBody().insertEnd(snippet);
				}
			}
		}else if(arg0 instanceof CtReturn<?>){
			CtReturn<?> exec = (CtReturn<?>) arg0;

			CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
			CtMethod<?> mt = exec.getParent(CtMethod.class);
			snippet.setValue("java.lang.System.out.println(\""+ENDKEY+SpoonHelpers.resolveName(mt)+"\")");

			CtIf rIf = getFactory().Core().createIf();
			CtLiteral<Boolean> ctl = getFactory().Core().createLiteral();
			ctl.setValue(true);
			rIf.setCondition(ctl);

			rIf.setThenStatement(getFactory().Core().clone((CtReturn<?>)arg0));

			((CtReturn<?>) arg0).replace(snippet);
			snippet.insertAfter((CtReturn<?>) arg0);
		}
	}

	@Override
	public boolean isThisLineInstrumented(String line) {
		return line.startsWith(STARTKEY) || line.startsWith(ENDKEY);
	}

	public String getLineIfInstrumented(String line) {
		if(!isThisLineInstrumented(line))
			return null;
		else
			return line.substring(STARTKEY.length());
	}
}
