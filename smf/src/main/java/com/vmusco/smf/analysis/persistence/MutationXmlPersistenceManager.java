package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.utils.SourceReference;

public class MutationXmlPersistenceManager extends XMLPersistenceManager<MutationStatistics>{
	private static String MUTATIONS_1 = "mutation";
	private static String MUTATIONS_PARENT_2 = "parent";
	private static String MUTATION_NAME = "name";

	protected static String CLASS_TO_MUTATE_2 = "class-to-mutate";	// MUTATION
	protected static String METHOD_TO_MUTATE_2 = "method-to-mutate";	// MUTATION
	
	protected static String CLASS_TO_MUTATE_3 = "class";				// MUTATION
	protected static String METHOD_TO_MUTATE_3 = "signature";				// MUTATION

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
	private static final String MUTANT_SRC_PARENT_4 = "src-parent";
	private static final String MUTANT_FILE_4 = "src-file";
	

	public MutationXmlPersistenceManager(File f) throws PersistenceException {
		super(prepareObjectIfPossible(f));
	}

	public MutationXmlPersistenceManager(MutationStatistics ms) throws PersistenceException {
		super(getObjectIfPresent(ms));
		
	}
	
	private static MutationStatistics prepareObjectIfPossible(File f) throws PersistenceException {
		MutationStatistics ms;
		
		if(!f.exists()){
			throw new PersistenceException(String.format("File %s not found !", f.getAbsolutePath()));
		}

		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			document = sxb.build(f);
			Element root = document.getRootElement();
			String targetPs = root.getAttributeValue(MUTATIONS_PARENT_2);
			String mutid = null;
			if(root.getAttribute(MUTATION_OPERATOR_3) != null)
				mutid = root.getAttribute(MUTATION_OPERATOR_3).getValue();
			
			String name = root.getAttributeValue(MUTATION_NAME);

			ProcessStatistics ps = ProcessStatistics.loadState((new File(f.getParentFile(), targetPs)).getAbsolutePath());
			
			MutationOperator mutator = MutatorsFactory.getOperatorClassFromId(mutid);
			ms = new MutationStatistics(ps, mutator, name);
		} catch (JDOMException | IOException e) {
			throw new PersistenceException(e);
		}
		
		return ms;
	}
	
	private static MutationStatistics getObjectIfPresent(MutationStatistics ms) throws PersistenceException {
		if(ms.getRelatedProcessStatisticsObject() == null)
			throw new PersistenceException("To persist using a MutationStatistics objet, it should be fully defined (ps link missing !)");
		
		return ms;
	}

	@Override
	public File getPersistenceFile() {
		return new File(getLinkedObject().getConfigFileResolved());
	}

	@Override
	public void load(Element root) {
		MutationStatistics ms = getLinkedObject();
		Attribute att = root.getAttribute(ProjectXmlPersistenceManager.TIME_ATTRIBUTE);

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
		
		if((tmp = root.getChild(METHOD_TO_MUTATE_2)) != null){
			List<Element> tmplist = tmp.getChildren(METHOD_TO_MUTATE_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ms.setMethodSignaturesToMutate(al.toArray(new String[0]));
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
				
				ifos.setSourceReference(new SourceReference());
				
				if(e.getAttribute(MUTANT_SRCCOL_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRCCOL_4).getValue().split("-");
					ifos.getSourceReference().setColumnStart(Integer.parseInt(vals[0]));
					ifos.getSourceReference().setColumnEnd(Integer.parseInt(vals[1]));
				}
				
				if(e.getAttribute(MUTANT_SRCLINE_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRCLINE_4).getValue().split("-");
					ifos.getSourceReference().setLineStart(Integer.parseInt(vals[0]));
					ifos.getSourceReference().setLineEnd(Integer.parseInt(vals[1]));
				}
				
				if(e.getAttribute(MUTANT_SRC_4) != null){
					String[] vals = e.getAttribute(MUTANT_SRC_4).getValue().split("-");
					ifos.getSourceReference().setSourceStart(Integer.parseInt(vals[0]));
					ifos.getSourceReference().setSourceEnd(Integer.parseInt(vals[1]));
				}
				
				if(e.getAttribute(MUTANT_SRC_PARENT_4) != null){
					int val = Integer.valueOf(e.getAttribute(MUTANT_SRC_PARENT_4).getValue());
					ifos.getSourceReference().setParentSearch(val);
				}

				if(e.getAttribute(MUTANT_FILE_4) != null){
					ifos.getSourceReference().setFile(e.getAttribute(MUTANT_FILE_4).getValue());
				}
				
				String id = e.getAttribute(MUTANT_ID_4).getValue();
				ms.setMutationStats(id, ifos);
			}
		}
	}

	@Override
	public Element getSaveContent() {
		MutationStatistics ms = getLinkedObject();
		
		Element mutations = new Element(MUTATIONS_1);

		mutations.setAttribute(new Attribute(MUTATION_OPERATOR_3, ms.getMutationOperator().operatorId()));
		
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

		if(ms.getMutantsGenerationTime() != null){
			mutations.setAttribute(new Attribute(ProjectXmlPersistenceManager.TIME_ATTRIBUTE, Long.toString(ms.getMutantsGenerationTime())));
		}

		if(ms.getClassToMutate(false) != null){
			Element tmp = new Element(CLASS_TO_MUTATE_2);
			mutations.addContent(tmp);
			ProjectXmlPersistenceManager.populateXml(tmp, CLASS_TO_MUTATE_3, ms.getClassToMutate(false));
		}
		
		if(ms.getMethodSignaturesToMutate() != null){
			Element tmp = new Element(METHOD_TO_MUTATE_2);
			mutations.addContent(tmp);
			ProjectXmlPersistenceManager.populateXml(tmp, METHOD_TO_MUTATE_3, ms.getMethodSignaturesToMutate());
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
			
			if(ifos.getSourceReference() != null){
				amutant.setAttribute(new Attribute(MUTANT_SRCCOL_4, ifos.getSourceReference().getColumnRange()));
				amutant.setAttribute(new Attribute(MUTANT_SRCLINE_4, ifos.getSourceReference().getLineRange()));
				amutant.setAttribute(new Attribute(MUTANT_SRC_4, ifos.getSourceReference().getSourceRange()));
				amutant.setAttribute(new Attribute(MUTANT_FILE_4, ifos.getSourceReference().getFile()));
				if(ifos.getSourceReference().getParentSearch() > 0){
					amutant.setAttribute(new Attribute(MUTANT_SRC_PARENT_4, Integer.toString(ifos.getSourceReference().getParentSearch())));
				}
			}
			
			setSensitiveAttribute(amutant, MUTANT_IN_4, ifos.getMutationIn()==null?"?":ifos.getMutationIn());
			try{
				setSensitiveAttribute(amutant, MUTANT_FROM_4, ifos.getMutationFrom());
			}catch(Exception ex){
				amutant.setAttribute(new Attribute(MUTANT_FROM_4, "?"));
			}

			try{
				setSensitiveAttribute(amutant, MUTANT_TO_4, ifos.getMutationTo());
			}catch(Exception ex){
				amutant.setAttribute(new Attribute(MUTANT_TO_4, "?"));
			}
		}
		
		return mutations;
		
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

}
