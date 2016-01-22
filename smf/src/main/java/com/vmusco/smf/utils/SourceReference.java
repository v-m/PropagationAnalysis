package com.vmusco.smf.utils;

import com.vmusco.smf.exceptions.MalformedSourcePositionException;

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
	private int parentsearch;
	
	public SourceReference() {
	}
	
	/**
	 * Build a SourceReference based on a source position object. 
	 * If the position object is not correctly built, return an exception 
	 * (may occurs if the element point to an implicit element of the code, 
	 * ie. a super() call in a new object via hierarchy, the returned 
	 * position could be negative).
	 * @param sp
	 */
	public SourceReference(SourcePosition sp) throws MalformedSourcePositionException {
		if(sp.getColumn() < 0 ||
				sp.getEndColumn() < 0 ||
				sp.getLine() < 0 ||
				sp.getEndLine() < 0 ||
				sp.getSourceStart() < 0 ||
				sp.getSourceEnd() < 0)
			throw new MalformedSourcePositionException(sp); 
				
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

	public void setParentSearch(int parentsearch) {
		this.parentsearch = parentsearch;
	}

	/**
	 * Returns the number of getParents() required on the object to get a
	 * coherent SourcePosition. 
	 * @return
	 */
	public int getParentSearch() {
		return parentsearch;
	}
}
