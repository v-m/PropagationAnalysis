package com.vmusco.smf.instrumentation;

import spoon.reflect.code.CtCFlowBreak;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtThrow;
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
	public static final String THROWKEY = ((char)2)+"=LMTINSTR=>";
	public static final String RETURNKEY = ((char)2)+"=LMRINSTR=>";

	@Override
	public void process(CtElement arg0) {
		if(arg0 instanceof CtExecutable<?>){
			CtExecutable<?> exec = (CtExecutable<?>) arg0;

			if(exec.getBody() != null){
				// At each method entry, we add a START
				CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
				snippet.setValue("java.lang.System.out.println(\""+STARTKEY+SpoonHelpers.resolveName((CtTypeMember) exec)+"\")");
				exec.getBody().insertBegin(snippet);
				snippet.setParent(exec);

				if(exec.getType().toString().equals("void")){
					// For void method, we add a END at the end of the method
					snippet = getFactory().Core().createCodeSnippetStatement();
					snippet.setValue("java.lang.System.out.println(\""+ENDKEY+SpoonHelpers.resolveName((CtTypeMember) exec)+"\")");
					exec.getBody().insertEnd(snippet);
					snippet.setParent(exec);
				}
			}
		}else if(arg0 instanceof CtReturn<?> || arg0 instanceof CtThrow){
			CtCFlowBreak exec = (CtCFlowBreak) arg0;

			CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
			CtTypeMember mt = exec.getParent(CtMethod.class);
			if(mt == null)
				mt = exec.getParent(CtConstructor.class);

			snippet.setValue("java.lang.System.out.println(\""+((arg0 instanceof CtThrow)?THROWKEY:RETURNKEY)+SpoonHelpers.resolveName(mt)+"\")");
			snippet.setParent(exec.getParent());

			exec.insertBefore(snippet);
			if(mt instanceof CtConstructor || ((CtMethod<?>)mt).getType().toString().equals("void")){
				CtIf rIf = getFactory().Core().createIf();
				CtLiteral<Boolean> ctl = getFactory().Core().createLiteral();
				ctl.setValue(true);
				rIf.setCondition(ctl);

				rIf.setThenStatement(getFactory().Core().clone(exec));
				rIf.setParent(exec.getParent());
				exec.replace(rIf);
			}
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
