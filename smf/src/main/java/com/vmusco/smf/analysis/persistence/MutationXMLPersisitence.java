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
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;


/**
 * This class is responsible of creating the index xml file for all mutants.
 * This class is *NOT* responsible of persisting mutation result !!!
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutationStatistics
 * @see MutantInfoXMLPersisitence
 */
public class MutationXMLPersisitence extends ExecutionPersistence<MutationStatistics<?>>{
	private File f;
	
	private static String MUTATIONS_1 = "mutation";
	private static String MUTATIONS_PARENT_2 = "parent";
	private static String MUTATION_NAME = "name";

	protected static String CLASS_TO_MUTATE_2 = "class-to-mutate";	// MUTATION
	protected static String CLASS_TO_MUTATE_3 = "class";				// MUTATION

	private static final String MUTATION_2 = "mutants";
	private static final String MUTATION_3 = "mutant";
	private static final String MUTATION_OPERATOR_3 = "operator-id";
	private static final String MUTANT_HASH_4 = "hash";

	private static final String MUTANT_ID_4 = "id";
	private static final String MUTANT_IN_4 = "in";
	private static final String MUTANT_VIABLE_4 = "viable";
	private static final String MUTANT_TO_4 = "to";
	private static final String MUTANT_FROM_4 = "from";
	private static final String MUTANT_SRCCOL_4 = "src-columns";
	private static final String MUTANT_SRCLINE_4 = "src-lines";
	private static final String MUTANT_SRC_4 = "src-pos";
	private static final String MUTANT_FILE_4 = "src-file";

	private static final String CONFIG_FILE = "config-file";

	private SAXBuilder sxb;
	private Document document;

	Element root;
	String targetPs, mutid, name;

	public MutationXMLPersisitence(File f) {
		this.f = f;
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
		mutid = root.getAttribute(MUTATION_OPERATOR_3).getValue();
		name = root.getAttributeValue(MUTATION_NAME);

		return true;
	}

	
	@Override
	public MutationStatistics<?> loadState()  throws PersistenceException {
		try {
			openFile();
		} catch (IOException e) {
			throw new PersistenceException(e);
		}
		
		ProcessStatistics ps = ProcessStatistics.loadState((new File(f.getParentFile(), targetPs)).getAbsolutePath());
		
		MutationStatistics<?> ms;
		try{
			//Class mutator = Class.forName(mutid);
			Class<MutationOperator<?>> mutator = MutatorsFactory.getOperatorClassFromId(mutid);
			ms = new MutationStatistics<>(ps, mutator, name);
		}catch(Exception e){
			throw new PersistenceException(e);
		}

		loadState(ms);
		return ms;
	}

	@Override
	public void saveState(MutationStatistics<?> ms)  throws PersistenceException {
		File f = new File(ms.getConfigFileResolved());

		System.out.println(f);
		if(f.exists())
			f.delete();

		try {
			f.createNewFile();
		} catch (IOException e) {
			throw new PersistenceException(e);
		}

		Element mutations = new Element(MUTATIONS_1);
		Document document = new Document(mutations);

		mutations.setAttribute(new Attribute(MUTATION_OPERATOR_3, ms.getMutationId()));
		//mutations.setAttribute(new Attribute(MUTATION_CLASS_3, ms.getMutationClassName()));
		
		String rem = ms.getConfigFileResolved().substring(ms.getRelatedProcessStatisticsObject().getWorkingDir().length());
		if(rem.charAt(0) == File.separatorChar)
			rem = rem.substring(1);
		
		int nbback = rem.split(File.separator).length - 1;
		
		String parent = "";
		
		for(int i=0; i<nbback; i++){
			parent += "../";
		}
		
		parent += ms.getRelatedProcessStatisticsObject().getPersistFile(false);
		
		mutations.setAttribute(new Attribute(MUTATIONS_PARENT_2, parent));
		mutations.setAttribute(new Attribute(MUTATION_NAME, ms.getMutationName()));
		mutations.setAttribute(new Attribute(CONFIG_FILE, ms.getConfigFile()));

		if(ms.getMutantsGenerationTime() != null){
			mutations.setAttribute(new Attribute(ProcessXMLPersistence.TIME_ATTRIBUTE, Long.toString(ms.getMutantsGenerationTime())));
		}

		if(ms.getClassToMutate(false) != null){
			Element tmp = new Element(CLASS_TO_MUTATE_2);
			mutations.addContent(tmp);
			ProcessXMLPersistence.populateXml(tmp, CLASS_TO_MUTATE_3, ms.getClassToMutate(false));
		}


		Element muts = new Element(MUTATION_2);
		mutations.addContent(muts);

		for(String mut : ms.listMutants()){
			MutantIfos ifos = (MutantIfos) ms.getMutationStats(mut);

			Element amutant = new Element(MUTATION_3);
			muts.addContent(amutant);

			amutant.setAttribute(new Attribute(MUTANT_ID_4, mut));
			amutant.setAttribute(new Attribute(MUTANT_VIABLE_4, ifos.isViable()?"true":"false"));
			
			if(ifos.getHash() != null)
				amutant.setAttribute(new Attribute(MUTANT_HASH_4, ifos.getHash()));
			
			if(ifos.getStartColumn() != -1 && ifos.getEndColumn() != -1){
				amutant.setAttribute(new Attribute(MUTANT_SRCCOL_4, ifos.getStartColumn()+"-"+ifos.getEndColumn()));
			}
			
			if(ifos.getStartLine() != -1 && ifos.getEndLine() != -1){
				amutant.setAttribute(new Attribute(MUTANT_SRCLINE_4, ifos.getStartLine()+"-"+ifos.getEndLine()));
			}

			if(ifos.getStartSource() != -1 && ifos.getEndSource() != -1){
				amutant.setAttribute(new Attribute(MUTANT_SRC_4, ifos.getStartSource()+"-"+ifos.getEndSource()));
			}

			if(ifos.getSourceFile() != null){
				amutant.setAttribute(new Attribute(MUTANT_FILE_4, ifos.getSourceFile()));
			}

			setSensitiveAttribute(amutant, MUTANT_IN_4, ifos.getMutationIn()==null?"?":ifos.getMutationIn());
			try{
				setSensitiveAttribute(amutant, MUTANT_FROM_4, ifos.getMutationFrom());
				setSensitiveAttribute(amutant, MUTANT_TO_4, ifos.getMutationTo());
			}catch(Exception ex){
				amutant.setAttribute(new Attribute(MUTANT_FROM_4, "!!! Uncodable !!!"));
				amutant.setAttribute(new Attribute(MUTANT_TO_4, "!!! Uncodable !!!"));
			}
		}

		try{
			FileOutputStream fos = new FileOutputStream(f);
			XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
			output.output(document, fos);
			fos.close();
		}catch(Exception e){
			throw new PersistenceException(e);
		}
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
	public void loadState(MutationStatistics<?> ms) throws PersistenceException {
		try {
			openFile();
		} catch (IOException e1) {
			throw new PersistenceException(e1);
		}
		
		ms.setConfigFile(root.getAttribute(CONFIG_FILE).getValue());

		Attribute att = root.getAttribute(ProcessXMLPersistence.TIME_ATTRIBUTE);

		if(att != null){
			ms.setMutantsGenerationTime(Long.parseLong(att.getValue()));
		}

		Element tmp;

		if((tmp = root.getChild(CLASS_TO_MUTATE_2)) != null){
			List<Element> tmplist = tmp.getChildren(CLASS_TO_MUTATE_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ms.setClassToMutate(al.toArray(new String[0]));
		}

		if((tmp = root.getChild(MUTATION_2)) != null){
			List<Element> tmplist = tmp.getChildren(MUTATION_3);

			for(Element e: tmplist){
				MutantIfos ifos = new MutantIfos();
				ifos.setMutationFrom(e.getAttribute(MUTANT_FROM_4).getValue());
				ifos.setMutationTo(e.getAttribute(MUTANT_TO_4).getValue());
				ifos.setMutationIn(e.getAttribute(MUTANT_IN_4).getValue());
				ifos.setViable(e.getAttribute(MUTANT_VIABLE_4).getValue().equals("true"));

				if(e.getAttribute(MUTANT_HASH_4) != null){
					ifos.setHash(e.getAttribute(MUTANT_HASH_4).getValue());
				}
				
				if(e.getAttribute(MUTANT_SRCCOL_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRCCOL_4).getValue().split("-");
					ifos.setStartColumn(Integer.parseInt(vals[0]));
					ifos.setEndColumn(Integer.parseInt(vals[1]));
				}
				
				if(e.getAttribute(MUTANT_SRCLINE_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRCLINE_4).getValue().split("-");
					ifos.setStartLine(Integer.parseInt(vals[0]));
					ifos.setEndLine(Integer.parseInt(vals[1]));
				}
				
				if(e.getAttribute(MUTANT_SRC_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRC_4).getValue().split("-");
					ifos.setStartSource(Integer.parseInt(vals[0]));
					ifos.setEndSource(Integer.parseInt(vals[1]));
				}

				if(e.getAttribute(MUTANT_FILE_4) != null){
					ifos.setSourceFile(e.getAttribute(MUTANT_FILE_4).getValue());
				}
				
				String id = e.getAttribute(MUTANT_ID_4).getValue();
				ms.setMutationStats(id, ifos);
			}
		}
	}

}
