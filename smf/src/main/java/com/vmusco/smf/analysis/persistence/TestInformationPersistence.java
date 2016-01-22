package com.vmusco.smf.analysis.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.exceptions.BadStateException;

public final class TestInformationPersistence {
	
	protected static String FAILING_TC_3 = "failing";
	protected static String IGNORED_TC_3 = "ignored";
	protected static String HANGING_TC_3 = "hanging";
	protected static String ONE_TC_4 = "case";
	protected static String ONE_TS_4 = "suite";
	
	protected static String CALLS = "callings";
	protected static String A_TEST = "test";
	protected static String A_TEST_NAME = "name";
	protected static String A_CALLED_NODE = "node";
	protected static String A_CALLED_NODE_CPT = "count";
	
	

	/*private static final String STACKTRACES_3 = "stacktraces";
	private static final String STACKTRACE_4 = "stacktrace";
	private static final String ONE_ST_5 = "trace";*/

	
	private TestInformationPersistence() {
		// No instance can be created... Tool class only
	}
	
	public static int keyForCompression(String id, HashMap<String, Integer> compressor){
		Integer ret = compressor.get(id);
		
		if(ret == null){
			ret = compressor.size();
			compressor.put(id, ret);
		}
		
		return ret;
	}
	
	public static void insertInto(Element root, TestsExecutionIfos tei){
		insertInto(root, tei, null);
	}
	
	public static void insertInto(Element root, TestsExecutionIfos tei, HashMap<String, Integer> compressor){
		root.setAttribute(new Attribute(ProjectXmlPersistenceManager.TIME_ATTRIBUTE, Long.toString(tei.getRunTestsTime())));

		Element e = new Element(FAILING_TC_3);
		root.addContent(e);
		ProjectXmlPersistenceManager.populateXml(e, ONE_TS_4, tei.getRawErrorOnTestSuite(), compressor);
		ProjectXmlPersistenceManager.populateXml(e, ONE_TC_4, tei.getRawFailingTestCases(), compressor);

		e = new Element(IGNORED_TC_3);
		root.addContent(e);
		ProjectXmlPersistenceManager.populateXml(e, ONE_TC_4, tei.getRawIgnoredTestCases(), compressor);

		e = new Element(HANGING_TC_3);
		root.addContent(e);
		ProjectXmlPersistenceManager.populateXml(e, ONE_TC_4, tei.getRawHangingTestCases(), compressor);
		
		
		try {
			Element calls = new Element(CALLS);
			
			for(String t : tei.getTestsWithCalledInformations()){
				String[] calledNodes = tei.getCalledNodes(t);
				e = new Element(A_TEST);
				if(compressor == null)
					e.setAttribute(A_TEST_NAME, t);
				else
					e.setAttribute(A_TEST_NAME, Integer.toString(keyForCompression(t, compressor)));
				
				ProjectXmlPersistenceManager.populateXml(e, A_CALLED_NODE, calledNodes, compressor);
				
				calls.addContent(e);
			}
			root.addContent(calls);
		} catch (BadStateException e1) {
			// No informations about calls... Just skip this
		}
		
		
		/*if(tei.getStacktraces() != null){
			e = new Element(STS);
	
			for(String[] st : tei.getStacktraces()){
				Element ee = new Element(ST);
	
				for(String t : st){
					Element eee = new Element(T);
					eee.setText(t);
					ee.addContent(eee);
				}
				
				e.addContent(ee);
			}
	
			root.addContent(e);
		}*/
	}
	
	/**
	 * Read content and fill a new TestsExecutionIfos according to the XML file.
	 * @param root
	 * @param deepLoading load heavy intensive informations (eg. called nodes, stacktraces, etc.) or not
	 * @return
	 */
	public static TestsExecutionIfos readFrom(Element root, boolean deepLoading){
		TestsExecutionIfos tei = new TestsExecutionIfos();

		tei.setRunTestsTime(Long.valueOf(root.getAttributeValue(ProjectXmlPersistenceManager.TIME_ATTRIBUTE)));

		Map<Integer, String> decompressor = null;
		// Check if compression is used...
		if(XMLPersistence.isDocumentUsingCompression(root)){
			//System.out.println("Compression is used for file...");
			decompressor = XMLPersistence.readDecompressionEntries(root);
		}
		
		Element tmp;

		if((tmp = root.getChild(FAILING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ONE_TC_4);

			List<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				if(decompressor != null){
					al.add(decompressor.get(Integer.parseInt(e.getText())));
				}else{
					al.add(e.getText());
				}
			}
			tei.setFailingTestCases(al.toArray(new String[0]));

			tmplist = tmp.getChildren(ONE_TS_4);
			al = new ArrayList<String>();
			for(Element e: tmplist){
				if(decompressor != null){
					al.add(decompressor.get(Integer.parseInt(e.getText())));
				}else{
					al.add(e.getText());
				}
			}
			tei.setErrorOnTestSuite(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(IGNORED_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ONE_TC_4);

			List<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				if(decompressor != null){
					al.add(decompressor.get(Integer.parseInt(e.getText())));
				}else{
					al.add(e.getText());
				}
			}
			tei.setIgnoredTestCases(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(HANGING_TC_3)) != null){
			List<Element> tmplist = tmp.getChildren(ONE_TC_4);

			List<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				if(decompressor != null){
					al.add(decompressor.get(Integer.parseInt(e.getText())));
				}else{
					al.add(e.getText());
				}
			}
			tei.setHangingTestCases(al.toArray(new String[0]));
		}
		
		if(deepLoading)
			deepRead(root, tei);
		
		return tei;
	}
	
	public static void deepRead(Element root, TestsExecutionIfos tei){
		tei.setCalledNodeInformation(readCalledNodes(root));
		
		/*if((tmp = root.getChild(STS)) != null){
			List<String[]> st = new ArrayList<String[]>();
			
			List<Element> tmplist = tmp.getChildren(ST);
			for(Element e: tmplist){
				List<String> stt = new ArrayList<String>();
				List<Element> tmplist2 = e.getChildren(T);
				
				for(Element ee: tmplist2){
					stt.add(ee.getText());
				}
				
				st.add(stt.toArray(new String[0]));
			}
			
			tei.setStacktraces(st.toArray(new String[0][0]));
		}*/
	}
	
	public static Map<String, String[]> readCalledNodes(Element root){
		Element tmp;
		Map<String, String[]> ret = new HashMap<String, String[]>(); 
		
		Map<Integer, String> decompressor = null;
		// Check if compression is used...
		if(XMLPersistence.isDocumentUsingCompression(root)){
			//System.out.println("Compression is used for file...");
			decompressor = XMLPersistence.readDecompressionEntries(root);
		}
		
		if((tmp = root.getChild(CALLS)) != null){
			List<Element> tmplist = tmp.getChildren(A_TEST);
			
			for(Element child: tmplist){
				String name = child.getAttributeValue(A_TEST_NAME);
				if(decompressor != null)
					name = decompressor.get(Integer.parseInt(name));
				List<String> al = new ArrayList<String>();
				
				for(Element e : child.getChildren(A_CALLED_NODE)){
					if(decompressor != null){
						al.add(decompressor.get(Integer.parseInt(e.getText())));
					}else{
						al.add(e.getText());
					}
				}
				
				ret.put(name, al.toArray(new String[al.size()]));
			}
		}
		
		return ret;
	}
}
