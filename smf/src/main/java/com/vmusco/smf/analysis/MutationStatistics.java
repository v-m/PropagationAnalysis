package com.vmusco.smf.analysis;

import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import com.vmusco.smf.analysis.persistence.ExecutionPersistence;
import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.MutationXMLPersisitence;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.utils.InterruptionManager;
import com.vmusco.smf.utils.MutationsSetTools;

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
	//private String configFile;
	private HashMap<String, MutantIfos> mutations = new HashMap<String, MutantIfos>();
	private Long mutantsGenerationTime = null;

	/**
	 * This method loads the last saved instance of the object
	 * The content of the test execution is NOT loaded. To load them, use {@link MutationStatistics#loadState(String, boolean)} instead or invoke
	 * {@link MutationStatistics#listViableAndRunnedMutants(boolean) with true argument.} to load them later.
	 * Return null if an error has occured
	 * @param persistFile the file to load
	 * @param loadTestExecutionsResults true if the test executions results should be loaded (when existing)
	 * @throws PersistenceException 
	 */
	public static MutationStatistics<?> loadState(String persistFile) throws PersistenceException {
		return loadState(persistFile, false);
	}

	/**
	 * This method loads the last saved instance of the object
	 * Take care: the content of the execution is NOT loaded !
	 * @param persistFile the file to load
	 * @param loadTestExecutionsResults true if the test executions results should be loaded (when existing)
	 * @throws PersistenceException 
	 */
	public static MutationStatistics<?> loadState(String persistFile, boolean loadTestExecutionsResults) throws PersistenceException {
		File finalf = new File(persistFile);
		if(finalf.isDirectory()){
			finalf = new File(finalf, MutationStatistics.DEFAULT_CONFIGFILE);
		}

		ExecutionPersistence<MutationStatistics<?>> persist = new MutationXMLPersisitence(finalf);
		return persist.loadState();
	}

	/**
	 * List all mutants
	 * @return
	 */
	public String[] listMutants(){
		return (String[]) mutations.keySet().toArray(new String[0]);
	}

	/**
	 * List viables mutants
	 * @return
	 */
	public String[] listViableMutants(){
		ArrayList<String> re = new ArrayList<String>();

		for(String s : mutations.keySet().toArray(new String[0])){
			if(mutations.get(s).isViable()){
				re.add(s);
			}
		}

		return re.toArray(new String[0]);
	}

	/**
	 * Similar as {@link MutationStatistics#listViableAndRunnedMutants(boolean, boolean)} without force reload.
	 */
	public String[] listViableAndRunnedMutants(boolean load) throws PersistenceException{
		return listViableAndRunnedMutants(load, false);
	}

	/**
	 * List all viables mutants which has been tested (and eventually load them and force reload)
	 * @param load true if the structure must be loaded at the same time
	 * @param forceReload if true, reload even if already loaded, else not
	 * @return
	 * @throws PersistenceException 
	 */
	public String[] listViableAndRunnedMutants(boolean load, boolean forceReload) throws PersistenceException{
		ArrayList<String> re = new ArrayList<String>();

		for(String s : listViableMutants()){
			if(load){
				boolean mustBeLoaded = false;

				if(forceReload){
					mustBeLoaded = true;
				}else{
					try{
						getMutationStats(s).getExecutedTestsResults();
						re.add(s);
					}catch(MutationNotRunException e1){
						mustBeLoaded = true;	
					}
				}
				if(mustBeLoaded){
					try {
						loadMutationStats(s);
						re.add(s);
					} catch (MutationNotRunException e) { }
				}
			}else{
				if(isMutantExecutionPersisted(s))
					re.add(s);
			}
		}

		return re.toArray(new String[0]);
	}

	/**
	 * List all viables but killed mutants
	 * @param load true if the structure must be loaded at the same time
	 * @return
	 * @throws PersistenceException 
	 */
	public String[] listViableButKilledMutants() throws PersistenceException{
		try {
			return removeAliveMutants(listViableAndRunnedMutants(true));
		} catch (MutationNotRunException e) {
			// Should never occurs...
			e.printStackTrace();
			return null;
		}
	}

	public String[] removeAliveMutants(String[] mutants) throws PersistenceException, MutationNotRunException{
		ArrayList<String> re = new ArrayList<String>();

		for(String s : mutants){
			if(isMutantKilled(s)){
				re.add(s);
			}
		}

		return re.toArray(new String[0]);
	}

	public MutationStatistics(ProcessStatistics ps, Class<T> mutator, String name) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.ps = ps;
		//this.configFile = DEFAULT_CONFIGFILE;
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

	/*public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}*/

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
		return resolveName(this.DEFAULT_CONFIGFILE);
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

	private File getMutantExecutionFile(String mutationId){
		return new File(getMutantFileResolved(mutationId));
	}

	/**
	 * Determine if a file for mutant execution test has been generated (whatever content)
	 * @param mutationId
	 * @return
	 */
	public boolean isMutantExecutionPersisted(String mutationId){
		return getMutantExecutionFile(mutationId).exists();
	}

	/**
	 * Load the mutation stats from file instead of returning directly from the structure (except if already loaded)
	 * Required because loading the state of MutationStatistics do not load the mutant states
	 * @param mutationId
	 * @return
	 * @throws PersistenceException 
	 * @throws MutationNotRunException 
	 */
	public MutantIfos loadMutationStats(String mutationId) throws PersistenceException, MutationNotRunException {
		if(!mutations.get(mutationId).isExecutionKnown()){
			if(!isMutantExecutionPersisted(mutationId))
				throw new MutationNotRunException();

			MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(getMutantExecutionFile(mutationId));
			pers.loadState(mutations.get(mutationId));
		}
		return mutations.get(mutationId);
	}

	/**
	 * Return the statistics for a mutation execution.
	 * If the MutationStatistics object has been loaded, the results for this execution are not loaded
	 * Use {@link MutationStatistics#loadMutationStats(String)} instead to load from file.
	 * @param mutationId
	 * @return
	 */
	public MutantIfos getMutationStats(String mutationId){
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
		informations.setId(mutationId);
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
		loadOrCreateMutants(reset, mcl, 0);
	}

	public void loadOrCreateMutants(boolean reset, MutationCreationListener mcl, int safepersist) throws Exception {
		loadOrCreateMutants(reset, mcl, -1, safepersist);
	}

	public void loadOrCreateMutants(boolean reset, MutationCreationListener mcl, int nb, int safepersist) throws PersistenceException, URISyntaxException {
		File f = new File(getConfigFileResolved());

		if(!reset && f.exists()){
			loadMutants();
		}

		Mutation.createMutants(ps, this, mcl, reset, nb, safepersist);

		saveMutants();

		if(InterruptionManager.isInterruptedDemanded()){
			InterruptionManager.notifyLastIterationFinished();
		}
	}


	public void saveMutants() throws PersistenceException {
		File f = new File(getConfigFileResolved());
		MutationXMLPersisitence per = new MutationXMLPersisitence(f);
		per.saveState(this);
	}

	public void loadMutants() throws PersistenceException {
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

	public boolean isMutantAlive(String mutid) throws MutationNotRunException, PersistenceException {
		MutantIfos mutationStats = loadMutationStats(mutid);

		return MutationsSetTools.areSetsSimilars(ps.getFailingTestCases(), mutationStats.getExecutedTestsResults().getRawMutantFailingTestCases()) &&
				MutationsSetTools.areSetsSimilars(ps.getHangingTestCases(), mutationStats.getExecutedTestsResults().getRawMutantHangingTestCases());
	}

	public boolean isMutantKilled(String mutid) throws MutationNotRunException, PersistenceException {
		return !isMutantAlive(mutid);
	}
}
