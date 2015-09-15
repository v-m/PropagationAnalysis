package com.vmusco.smf.utils;

import spoon.reflect.cu.SourcePosition;

/**
 * This class copies content of {@link SourcePosition} to persisit and restore
 * @author Vincenzo Musco - http://www.vmusco.com
 *
 */
public class SourceReference {
	private String file;
	private int column_start, column_end;
	private int line_start, line_end;
	private int source_start, source_end;
	
	public SourceReference() {
	}
	
	public SourceReference(SourcePosition sp) {
		setFile(sp.getFile().getAbsolutePath());
		setColumnRange(sp.getColumn(), sp.getEndColumn());
		setLineRange(sp.getLine(), sp.getEndLine());
		setSourceRange(sp.getSourceStart(), sp.getSourceEnd());
	}
	
	public int getColumnEnd() {
		return column_end;
	}

	public int getColumnStart() {
		return column_start;
	}

	public int getLineStart() {
		return line_start;
	}

	public int getLineEnd() {
		return line_end;
	}

	public int getSourceStart() {
		return source_start;
	}

	public int getSourceEnd() {
		return source_end;
	}

	public String getFile() {
		return file;
	}
	
	public void setColumnEnd(int n) {
		column_end = n;
	}

	public void setColumnStart(int n) {
		column_start = n;
	}

	public void setLineStart(int n) {
		line_start = n;
	}

	public void setLineEnd(int n) {
		line_end = n;
	}

	public void setSourceStart(int n) {
		source_start = n;
	}

	public void setSourceEnd(int n) {
		source_end = n;
	}
	
	public void setSourceRange(int n, int m){
		setSourceStart(n);
		setSourceEnd(m);
	}

	public void setLineRange(int n, int m){
		setLineStart(n);
		setLineEnd(m);
	}

	public void setColumnRange(int n, int m){
		setColumnStart(n);
		setColumnEnd(m);
	}

	public void setFile(String f) {
		file = f;
	}
	
	
	
	public String getSourceRange(){
		return String.format("%d-%d", getSourceStart(), getSourceEnd());
	}

	public String getLineRange(){
		return String.format("%d-%d", getLineStart(), getLineEnd());
	}
	
	public String getColumnRange(){
		return String.format("%d-%d", getColumnStart(), getColumnEnd());
	}

}
