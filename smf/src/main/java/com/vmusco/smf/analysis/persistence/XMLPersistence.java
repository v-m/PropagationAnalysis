package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.exceptions.PersistenceException;

public class XMLPersistence {
	
	public static void load(XMLPersistenceManager pm) throws PersistenceException{
		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			try {
				document = sxb.build(pm.getPersistenceInputStream());
			} catch (IOException e) {
				throw new PersistenceException(e);
			}
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return;
		}
		
		Element root = document.getRootElement();
		pm.load(root);
	}
	
	public static void save(XMLPersistenceManager pm) throws PersistenceException{
		Format format = Format.getPrettyFormat();
		format.setLineSeparator(LineSeparator.UNIX);
		XMLOutputter output = new XMLOutputter(format);
		Document document = new Document(pm.getSaveContent());
		Comment c = new Comment("\nThis is an execution file generated with SMF.\n");
		document.getContent().add(0, c);
		
		try {
			output.output(document, pm.getPersistenceOutputStream());
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}
	
}
