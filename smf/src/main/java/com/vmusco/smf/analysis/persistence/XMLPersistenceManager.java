package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Element;

import com.vmusco.smf.exceptions.PersistenceException;

public abstract class XMLPersistenceManager<T> {
	private T obj;
	
	public XMLPersistenceManager(T obj) {
		this.obj = obj;
	}
	
	public OutputStream getPersistenceOutputStream() throws PersistenceException {
		try {
			return new FileOutputStream(getPersistenceFile());
		} catch (FileNotFoundException e) {
			throw new PersistenceException(e);
		}
	}
	
	public InputStream getPersistenceInputStream() throws PersistenceException {
		try {
			return new FileInputStream(getPersistenceFile());
		} catch (FileNotFoundException e) {
			throw new PersistenceException(e);
		}
	}
	
	public abstract File getPersistenceFile();
	public abstract void load(Element root);
	public abstract Element getSaveContent() throws PersistenceException;
	
	public T getLinkedObject(){
		return obj;
	}
}
