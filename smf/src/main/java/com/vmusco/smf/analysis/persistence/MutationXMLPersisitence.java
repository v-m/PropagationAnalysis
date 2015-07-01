package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;

/**
 * This class is responsible of creating the index.xml for all mutants but not the mutation result persistence !!!
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public class MutationXMLPersisitence extends ExecutionPersistence<MutationStatistics<?>>{

	private static String MUTATIONS_1 = "mutation";
	private static String MUTATIONS_PARENT_2 = "parent";
	private static String MUTATION_NAME = "name";

	protected static String CLASS_TO_MUTATE_2 = "class-to-mutate";	// MUTATION
	protected static String CLASS_TO_MUTATE_3 = "class";				// MUTATION

	private static String MUTATION_2 = "mutants";
	private static String MUTATION_3 = "mutant";
	private static final String MUTATION_CLASS_3 = "operator-class";
	private static final String MUTATION_OPERATOR_3 = "operator-id";
	private static final String MUTANT_HASH_4 = "hash";

	private static String MUTANT_ID_4 = "id";
	private static String MUTANT_IN_4 = "in";
	private static String MUTANT_VIABLE_4 = "viable";
	private static String MUTANT_TO_4 = "to";
	private static String MUTANT_FROM_4 = "from";

	private static String CONFIG_FILE = "config-file";

	private SAXBuilder sxb;
	private Document document;

	Element root;
	String targetPs, mutid, name;

	public MutationXMLPersisitence(File f) {
		super(f);
	}
	
	private boolean openFile() throws IOException{
		if(!f.exists()){
			return false;
		}

		sxb = new SAXBuilder();

		try {
			document = sxb.build(f);
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return false;
		}

		root = document.getRootElement();
		targetPs = root.getAttributeValue(MUTATIONS_PARENT_2);
		mutid = root.getAttribute(MUTATION_CLASS_3).getValue();
		name = root.getAttributeValue(MUTATION_NAME);

		return true;
	}

	
	@Override
	public MutationStatistics<?> loadState() throws Exception {
		openFile();
		
		ProcessStatistics ps = ProcessStatistics.loadState((new File(f.getParentFile(), targetPs)).getAbsolutePath());
		
		Class mutator = Class.forName(mutid);
		MutationStatistics<?> ms = new MutationStatistics<>(ps, mutator, name);

		loadState(ms);

		return ms;
	}

	@Override
	public void saveState(MutationStatistics<?> ms) throws IOException {
		File f = new File(ms.getConfigFileResolved());

		System.out.println(f);
		if(f.exists())
			f.delete();

		f.createNewFile();

		Element mutations = new Element(MUTATIONS_1);
		Document document = new Document(mutations);

		mutations.setAttribute(new Attribute(MUTATION_OPERATOR_3, ms.getMutationId()));
		mutations.setAttribute(new Attribute(MUTATION_CLASS_3, ms.getMutationClassName()));
		
		String rem = ms.getConfigFileResolved().substring(ms.ps.getWorkingDir().length());
		if(rem.charAt(0) == File.separatorChar)
			rem = rem.substring(1);
		
		int nbback = rem.split(File.separator).length - 1;
		
		String parent = "";
		
		for(int i=0; i<nbback; i++){
			parent += "../";
		}
		
		parent += ms.ps.persistFile;
		
		mutations.setAttribute(new Attribute(MUTATIONS_PARENT_2, parent));
		mutations.setAttribute(new Attribute(MUTATION_NAME, ms.mutationName));
		mutations.setAttribute(new Attribute(CONFIG_FILE, ms.configFile));

		if(ms.mutantsGenerationTime != null){
			mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(ms.mutantsGenerationTime)));
		}

		if(ms.classToMutate != null){
			Element tmp = new Element(CLASS_TO_MUTATE_2);
			mutations.addContent(tmp);
			ProcessXMLPersistence.populateXml(tmp, CLASS_TO_MUTATE_3, ms.classToMutate);
		}


		Element muts = new Element(MUTATION_2);
		mutations.addContent(muts);

		for(String mut : (String[]) ms.mutations.keySet().toArray(new String[0])){
			MutantIfos ifos = (MutantIfos) ms.mutations.get(mut);

			Element amutant = new Element(MUTATION_3);
			muts.addContent(amutant);

			amutant.setAttribute(new Attribute(MUTANT_ID_4, mut));
			amutant.setAttribute(new Attribute(MUTANT_VIABLE_4, ifos.viable?"true":"false"));
			if(ifos.hash != null)
				amutant.setAttribute(new Attribute(MUTANT_HASH_4, ifos.hash));

			setSensitiveAttribute(amutant, MUTANT_IN_4, ifos.mutationIn==null?"?":ifos.mutationIn);
			try{
				setSensitiveAttribute(amutant, MUTANT_FROM_4, ifos.mutationFrom);
				setSensitiveAttribute(amutant, MUTANT_TO_4, ifos.mutationTo);
			}catch(Exception ex){
				amutant.setAttribute(new Attribute(MUTANT_FROM_4, "!!! Uncodable !!!"));
				amutant.setAttribute(new Attribute(MUTANT_TO_4, "!!! Uncodable !!!"));
			}
		}

		FileOutputStream fos = new FileOutputStream(f);
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(document, fos);
		fos.close();
	}

	private void setSensitiveAttribute(Element amutant, String attributeName, String attributeValue) throws IllegalDataException{
		int pos; 
		
		for(int i : findSensitiveData(attributeValue)){
			while((pos = attributeValue.indexOf(i)) != -1){
				String tmp = attributeValue;
				attributeValue = tmp.substring(0, pos) + "/"+ i + tmp.substring(pos+1);
			}
		}
		
		amutant.setAttribute(attributeName, attributeValue);
	}
	
	private Integer[] findSensitiveData(String attributeValue){
		List<Integer> l = new ArrayList<>();
		
		for(int i=0; i<32; i++){
			if(i==9 || i==10 || i==13)
				continue;
			
			if(attributeValue.indexOf(i) != -1){
				l.add(i);
			}
		}
		
		return l.toArray(new Integer[0]);
	}

	@Override
	public void loadState(MutationStatistics<?> ms) throws Exception {
		openFile();
		
		ms.configFile = root.getAttribute(CONFIG_FILE).getValue();

		Attribute att = root.getAttribute(ProcessXMLPersistence.TIME_ATTRIBUTE);

		if(att != null){
			ms.mutantsGenerationTime = Long.parseLong(att.getValue());
		}

		Element tmp;

		if((tmp = root.getChild(CLASS_TO_MUTATE_2)) != null){
			List<Element> tmplist = tmp.getChildren(CLASS_TO_MUTATE_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ms.classToMutate = al.toArray(new String[0]);
		}

		if((tmp = root.getChild(MUTATION_2)) != null){
			List<Element> tmplist = tmp.getChildren(MUTATION_3);

			for(Element e: tmplist){
				MutantIfos ifos = new MutantIfos();
				ifos.mutationFrom = e.getAttribute(MUTANT_FROM_4).getValue();
				ifos.mutationTo = e.getAttribute(MUTANT_TO_4).getValue();
				ifos.mutationIn = e.getAttribute(MUTANT_IN_4).getValue();
				ifos.viable = e.getAttribute(MUTANT_VIABLE_4).getValue().equals("true");
				if(e.getAttribute(MUTANT_HASH_4) != null){
					ifos.hash = e.getAttribute(MUTANT_HASH_4).getValue();
				}
				String id = e.getAttribute(MUTANT_ID_4).getValue();
				ms.mutations.put(id, ifos);
			}
		}
	}

}
