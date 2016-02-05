package com.vmusco.smf.analysis.persistence;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	private static final String VALUE = "value";
	private static final String ID = "id";
	private static final String ENTRY = "entry";
	private static final String COMPRESSION_ENTRIES = "compression-entries";

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
	
	public static Element generateCompressionEntry(Map<String, Integer> compressor){
		Element e = new Element(COMPRESSION_ENTRIES);
		
		for(Entry<String, Integer> anEntry : compressor.entrySet()){
			Element e2 = new Element(ENTRY);
			e2.setAttribute(ID, Integer.toString(anEntry.getValue()));
			e2.setAttribute(VALUE, anEntry.getKey());
			e.addContent(e2);
		}
		
		return e;
	}
	
	public static boolean isDocumentUsingCompression(Element root){
		return root.getChild(COMPRESSION_ENTRIES) != null;
	}
	
	public static Map<Integer, String> readDecompressionEntries(Element root){
		Element compressionEntries = root.getChild(COMPRESSION_ENTRIES);
		Map<Integer, String> decompressor = new HashMap<Integer, String>();
		
		for(Element e : compressionEntries.getChildren(ENTRY)){
			Integer id = Integer.parseInt(e.getAttributeValue(ID));
			String key = e.getAttributeValue(VALUE);
			decompressor.put(id, key);
		}
		
		return decompressor;
	}
}
