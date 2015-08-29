package com.vmusco.smf.analysis;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.vmusco.smf.analysis.persistence.ExecutionPersistence;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.MutationXMLPersisitence;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.testing.TestingFunctions;
import com.vmusco.smf.utils.InterruptionManager;

/**
 * This class contains the mutations information for one project and one mutation operator
 * @param <T>
 * @see MutationXMLPersisitence
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see MutationOperator
 */
public class MutationStatistics<T extends MutationOperator<?>> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String DEFAULT_ID_NAME = "main";
	public static final String DEFAULT_CONFIGFILE = "mutations.xml";
	
	private ProcessStatistics ps = null;
	private String mutationOpId = null;
	private T mopobj = null;
	private String mutationName = null;		// name to specify mutations (if null, not considered)
	/**
	 * This is the list of class which should be mutated
	 * If this value is null, then all classes on the source are considered !
	 */
	private String[] classToMutate = new String[]{};
	private String mutator;			// The mutator to use for mutation
	private String configFile;
	private HashMap<String, MutantIfos> mutations = new HashMap<String, MutantIfos>();
	private Long mutantsGenerationTime = null;
	
	/**
	 * This method loads the last saved instance of the object
	 * Take care: the content of the execution is NOT loaded !
	 * Return null if an error has occured
	 * @throws PersistenceException 
	 */
	public static MutationStatistics<?> loadState(String persistFile) throws PersistenceException {
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
			if(mutations.get(s).isViable()){
				re.add(s);
			}
		}
		
		return re.toArray(new String[0]);
	}
	
		
	/**
	 * This method check which mutants has already been tested with test suites and
	 * load the result. It also return an array with all proceeded mutants
	 * @param nb the number to consider. zero for all
	 * @throws Exception 
	 */
	public String[] loadResultsForExecutedTestOnMutants(int nb) throws PersistenceException{
		ArrayList<String> re = new ArrayList<String>();
		File ff = new File(resolveName(ps.getMutantsTestResults()));
		
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
	
	public boolean checkIfExecutionExists(String mutid){
		File ff = new File(resolveName(ps.getMutantsTestResults()), mutid+".xml");
		return  ff.exists();
	}
	
	public MutationStatistics(ProcessStatistics ps, Class<T> mutator, String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.ps = ps;
		this.configFile = DEFAULT_CONFIGFILE;
		this.mutator = mutator.getCanonicalName();
		this.mutationName = name;
		resolveWithMutator();
	}
	
	@SuppressWarnings("unchecked")
	public void resolveWithMutator() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.mopobj = (T) Class.forName(this.mutator).newInstance();
		this.mutationOpId = this.mopobj.operatorId();
	}

	public MutationStatistics(ProcessStatistics ps, Class<T> mutator) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this(ps, mutator, DEFAULT_ID_NAME);
	}

	public ProcessStatistics getRelatedProcessStatisticsObject() {
		return ps;
	}
	
	public String getMutationId(){
		return mutationOpId;
	}
	
	public String getMutationClassName(){
		return mopobj.getClass().getName();
	}
	
	public T getMutationObject(){
		return mopobj;
	}
	
	public String getMutationName() {
		return mutationName;
	}
	public void setMutationName(String mutationName) {
		this.mutationName = mutationName;
	}
	
	public String[] getClassToMutate(boolean resolve) {
		if(resolve){
			String[] ret = new String[classToMutate.length];

			int i = 0;
			for(String it : classToMutate){
				ret[i++] = ps.getProjectIn(true) + File.separator + it;
			}

			return ret;
		}else{
			return classToMutate;
		}
	}
	
	public void setClassToMutate(String[] classToMutate) {
		this.classToMutate = classToMutate;
	}
	
	public String getMutator() {
		return mutator;
	}
	public void setMutator(String mutator) {
		this.mutator = mutator;
	}

	public String getConfigFile() {
		return configFile;
	}
	
	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}
	
	public String resolveName(String resolving){
		return ps.getWorkingDir() + File.separator + ps.getMutantsBasedir().replace("{id}", this.mutationName).replace("{op}", this.mutationOpId) + File.separator + resolving;
	}
	
	public String getSourceMutationResolved(){
		return resolveName(ps.getMutantsOut());
	}
	
	public String getBytecodeMutationResolved(){
		return resolveName(ps.getMutantsBytecodeOut());
	}
	
	public String getExecutionFileResolved(){
		return resolveName(ps.getMutantsTestResults());
	}

	public String getConfigFileResolved(){
		return resolveName(this.configFile);
	}

	public String getMutantFileResolved(String mutid){
		return getExecutionFileResolved() + File.separator + mutid + ".xml";
	}
	
	public void clearMutations(){
		mutations.clear();
	}
	
	public int getMutationsSize(){
		return mutations.size();
	}
	
	public Set<String> getAllMutationsId(){
		return (Set<String>) mutations.keySet();
	}
	
	/**
	 * Return an array with the number of not viable mutations, the number of treated ones and the number of remaining ones
	 * @return
	 * @see TestingFunctions#getUnfinishedCollection(MutationStatistics, boolean)
	 * @see TestingFunctions#getViableCollection(MutationStatistics)
	 */
	@Deprecated
	public int[] statsMutations(){
		int treated = 0;
		int not_viable = 0;
		
		for(String mid : getAllMutationsId()){
			MutantIfos mutationStats = getMutationStats(mid);

			if(!mutationStats.isViable())
				not_viable++;
			else{
				if(!mutationStats.isExecutionKnown()){
					mutationStats.setExecutedTests(checkIfExecutionExists(mid));
				}
				
				treated += mutationStats.isExecutedTests()?1:0;
			}
		}
		
		return new int[]{not_viable, treated, getMutationsSize()-treated-not_viable};
	}
	
	/**
	 * Load the mutation stats from file instead of returning directly from the structure
	 * Required because loading the state of MutationStatistics do not load the mutant states
	 * @param mutationId
	 * @return
	 * @throws Exception 
	 */
	public MutantIfos loadMutationStats(String mutationId) throws Exception{
		File ff = new File(resolveName(ps.getMutantsTestResults()));
		File f = new File(ff, mutationId+".xml");
		
		if(!f.exists())
			return null;
		
		MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(f);
		pers.loadState(mutations.get(mutationId));
		
		return mutations.get(mutationId);
	}
	
	/**
	 * Return the statistics for a mutation execution.
	 * If the MutationStatistics object has been loaded, the results for this exection are not loaded
	 * Use {@link MutationStatistics#loadMutationStats(String)} instead to load from file.
	 * @param mutationId
	 * @return
	 */
	public MutantIfos getMutationStats(String mutationId){
		//TODO: Include the loading here (with a boolean param) via loadMutationStats(String) and add a structure (or a vaiable in MutantIfos) to map what is legitim and what is not.
		return mutations.get(mutationId);
	}
	
	/**
	 * Determine if the mutant is defined in the mutation structure (!= files)
	 * @param mutid
	 * @return
	 */
	public boolean isMutantDefined(String mutid){
		return this.mutations.containsKey(mutid);
	}
	
	public void setMutationStats(String mutationId, MutantIfos informations){
		mutations.put(mutationId, informations);
	}
	
	public Long getMutantsGenerationTime() {
		return mutantsGenerationTime;
	}
	
	public void setMutantsGenerationTime(Long mutantsGenerationTime) {
		this.mutantsGenerationTime = mutantsGenerationTime;
	}

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
		
		if(InterruptionManager.isInterruptedDemanded()){
			InterruptionManager.notifyLastIterationFinished();
		}
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
