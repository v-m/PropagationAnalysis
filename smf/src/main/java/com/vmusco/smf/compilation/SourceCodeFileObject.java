package com.vmusco.smf.compilation;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class SourceCodeFileObject extends SimpleJavaFileObject {

	private String sourceContent;
	
	public SourceCodeFileObject(String simpleClassName, String sourceContent) {
		super(URI.create((simpleClassName + Kind.SOURCE.extension)), Kind.SOURCE);
		this.sourceContent = sourceContent;
	}
	
	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return sourceContent;
	}
	

}
