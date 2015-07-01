package com.vmusco.smf.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.vmusco.smf.analysis.persistence.ExecutionPersistence;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.MutationXMLPersisitence;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutationOperator;

public class MutationStatistics<T extends MutationOperator<?>> implements Serializable {
	public static final String DEFAULT_ID_NAME = "main";
	public static final String DEFAULT_CONFIGFILE = "mutations.xml";
	
	/**
	 * This method loads the last saved instance of the object
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static MutationStatistics<?> loadState(String persistFile) throws Exception {
		File finalf = new File(persistFile);
		if(finalf.isDirectory()){
			finalf = new File(finalf, MutationStatistics.DEFAULT_CONFIGFILE);
		}
		
		ExecutionPersistence<MutationStatistics<?>> persist = new MutationXMLPersisitence(finalf);
		return persist.loadState();
	}
	
	/**
	 * List viables mutants
	 * @return
	 */
	public String[] listMutants(){
		ArrayList<String> re = new ArrayList<String>();
		
		for(String s : mutations.keySet().toArray(new String[0])){
			if(mutations.get(s).viable){
				re.add(s);
			}
		}
		
		return re.toArray(new String[0]);
	}
	
		
	/**
	 * This method check which mutants has already been tested with test suites and
	 * load the result. It also return an array with all proceeded mutants
	 * @throws Exception 
	 */
	public String[] loadResultsForExecutedTestOnMutants(int nb) throws Exception{
		ArrayList<String> re = new ArrayList<String>();
		File ff = new File(resolveName(ps.mutantsTestResults));
		
		List<String> l = new ArrayList<>();
		for(String f : ff.list()){
			l.add(f);
		}
		
		Collections.sort(l);
		
		while(nb>0 && l.size() > nb){
			l.remove(l.size() - 1);
		}
		
		for(String fs : l){
			File f = new File(ff, fs);
			if(f.length() > 0) {
				String name = f.getName();
				if(name.endsWith(".xml")){
					name = name.substring(0, name.length()-4);
				}
				
				MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(f);
				pers.loadState(mutations.get(name));
				re.add(name);
			}
		}
		
		return re.toArray(new String[0]);
	}
	
	public MutationStatistics(ProcessStatistics ps, Class<T> mutator, String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.ps = ps;
		this.configFile = DEFAULT_CONFIGFILE;
		this.mutator = mutator.getCanonicalName();
		this.mutationName = name;
		resolveWithMutator();
	}
	
	public void resolveWithMutator() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.mopobj = (T) Class.forName(this.mutator).newInstance();
		this.mutationOpId = this.mopobj.operatorId();
	}

	public MutationStatistics(ProcessStatistics ps, Class<T> mutator) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(ps, mutator, DEFAULT_ID_NAME);
	}
	
	public ProcessStatistics ps = null;
	
	private String mutationOpId = null;
	private T mopobj = null;
	
	public String getMutationId(){
		return mutationOpId;
	}
	
	public String getMutationClassName(){
		return mopobj.getClass().getName();
	}
	
	public T getMutationObject(){
		return mopobj;
	}
	
	/**
	 * This is a name to specify mutations (if null, not considered)
	 */
	public String mutationName = null;
	
	/**
	 * This is the list of class which should be mutated
	 * If this value is null, then all classes on the source are considered !
	 */
	public String[] classToMutate = new String[]{};
	
	/**
	 * The mutator to use for mutation
	 */
	public String mutator;
	public String configFile;

	
	public String resolveName(String resolving){
		return ps.getWorkingDir() + File.separator + ps.mutantsBasedir.replace("{id}", this.mutationName).replace("{op}", this.mutationOpId) + File.separator + resolving;
	}
	
	public String getSourceMutationResolved(){
		return resolveName(ps.mutantsOut);
	}
	
	public String getBytecodeMutationResolved(){
		return resolveName(ps.mutantsBytecodeOut);
	}
	
	public String getExecutionFileResolved(){
		return resolveName(ps.mutantsTestResults);
	}

	public String getConfigFileResolved(){
		return resolveName(this.configFile);
	}

	public String getMutantFileResolved(String mutid){
		return getExecutionFileResolved() + File.separator + mutid + ".xml";
	}

	public HashMap<String, MutantIfos> mutations = new HashMap<String, MutantIfos>();
	public Long mutantsGenerationTime = null;

	public void loadOrCreateMutants(boolean reset) throws Exception {
		loadOrCreateMutants(reset, null);
	}
	
	public void loadOrCreateMutants(boolean reset, MutationCreationListener mcl) throws Exception {
		loadOrCreateMutants(reset, mcl, -1);
	}
	
	public void loadOrCreateMutants(boolean reset, MutationCreationListener mcl, int nb) throws Exception {
		File f = new File(getConfigFileResolved());

		if(!reset && f.exists()){
			loadMutants();
		}
		
		if(nb == -1){
			Mutation.createMutants(ps, this, mcl, reset);
		}else{
			Mutation.createMutants(ps, this, mcl, reset, nb);
		}
		
		MutationXMLPersisitence per = new MutationXMLPersisitence(f);
		per.saveState(this);
	}
	
	public void loadMutants() throws Exception{
		File f = new File(getConfigFileResolved());
		
		MutationXMLPersisitence per = new MutationXMLPersisitence(f);
		per.loadState(this);
	}
	
	public void createExecutionFolderIfNeeded(){
		File f = new File(this.getExecutionFileResolved());
		
		if(!f.exists()){
			f.mkdirs();
		}
	}
}
