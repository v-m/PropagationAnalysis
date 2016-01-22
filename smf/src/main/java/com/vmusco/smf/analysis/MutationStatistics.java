package com.vmusco.smf.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import spoon.reflect.cu.SourcePosition;
import spoon.reflect.declaration.CtElement;

import com.vmusco.smf.analysis.persistence.MutantInfoXMLPersisitence;
import com.vmusco.smf.analysis.persistence.MutationXmlPersistenceManager;
import com.vmusco.smf.analysis.persistence.XMLPersistence;
import com.vmusco.smf.exceptions.BadObjectTypeException;
import com.vmusco.smf.exceptions.MalformedSourcePositionException;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.utils.InterruptionManager;
import com.vmusco.smf.utils.SetTools;
import com.vmusco.smf.utils.SourceReference;

/**
 * This class contains the mutations information for one project and one mutation operator
 * @param <T>
 * @see MutationXMLPersisitence
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see SmfMutationOperator
 */
public class MutationStatistics implements Serializable {
	private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getFormatterLogger(MutationStatistics.class.getSimpleName());
	
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_ID_NAME = "main";
	public static final String DEFAULT_CONFIGFILE = "mutations.xml";

	private ProcessStatistics ps = null;
	private String mutationName = null;		// name to specify mutations (if null, not considered)
	/**
	 * This is the list of class which should be mutated
	 * If this value is null, then all classes on the source are considered !
	 */
	private String[] classToMutate = new String[]{};
	private HashMap<String, MutantIfos> mutations = new HashMap<String, MutantIfos>();
	private Long mutantsGenerationTime = null;

	private MutationOperator mutop = null;

	/**
	 * This method loads the last saved instance of the object
	 * The content of the test execution is NOT loaded. To load them, invoke {@link MutationStatistics#listViableAndRunnedMutants(boolean) with true argument.} to load them later.
	 * Return null if an error has occured
	 * @param persistFile the file to load
	 * @throws PersistenceException 
	 */
	public static MutationStatistics loadState(String persistFile) throws PersistenceException {
		logger.trace("Loading ms state %s", persistFile);
		File finalf = new File(persistFile);
		if(finalf.isDirectory()){
			finalf = new File(finalf, MutationStatistics.DEFAULT_CONFIGFILE);
		}

		MutationXmlPersistenceManager mgr = new MutationXmlPersistenceManager(new File(persistFile));
		XMLPersistence.load(mgr);
		
		return mgr.getLinkedObject();
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
	 * Similar as {@link MutationStatistics#listViableAndRunnedMutants(boolean, boolean)} without force reload and without deep loading
	 */
	public String[] listViableAndRunnedMutants(boolean load) throws PersistenceException{
		return listViableAndRunnedMutants(load, false, false);
	}

	/**
	 * List all viables mutants which has been tested (and eventually load them and force reload)
	 * @param load true if the structure must be loaded at the same time
	 * @param forceReload if true, reload even if already loaded, else not
	 * @return
	 * @throws PersistenceException 
	 */
	public String[] listViableAndRunnedMutants(boolean load, boolean forceReload, boolean deepLoading) throws PersistenceException{
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
						loadMutationStats(s, deepLoading);
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

	public MutationStatistics(ProcessStatistics ps, MutationOperator mutator, String name) {
		this.ps = ps;
		this.mutationName = name;
		this.mutop = mutator;
		
	}

	public MutationStatistics(ProcessStatistics ps, MutationOperator mutator) {
		this(ps, mutator, DEFAULT_ID_NAME);
	}

	public ProcessStatistics getRelatedProcessStatisticsObject() {
		return ps;
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

	public String resolveName(String resolving){
		return ps.getWorkingDir() + File.separator + ps.getMutantsBasedir().replace("{id}", this.mutationName).replace("{op}", this.mutop.operatorId()) + File.separator + resolving;
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
	public MutantIfos loadMutationStats(String mutationId, boolean deepLoading) throws PersistenceException, MutationNotRunException {
		logger.trace("Loading mutation state %s", mutationId);
		if(!mutations.get(mutationId).isExecutionKnown()){
			if(!isMutantExecutionPersisted(mutationId))
				throw new MutationNotRunException(mutationId);

			
			MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(getMutationStats(mutationId), getMutantExecutionFile(mutationId), deepLoading);
			XMLPersistence.load(pers);
		}
		return mutations.get(mutationId);
	}

	public void saveMutationStats(String mutationId) throws FileNotFoundException, PersistenceException{
		File ff = new File(getMutantFileResolved(mutationId));
		ff.getParentFile().mkdirs();
		MutantInfoXMLPersisitence pers = new MutantInfoXMLPersisitence(getMutationStats(mutationId), ff);
		pers.setFileLock(new FileOutputStream(ff));
		XMLPersistence.save(pers);
	}
	
	public void saveAllMutationStats() throws FileNotFoundException, PersistenceException{
		for(String mutationId : listMutants()){
			saveMutationStats(mutationId);
		}
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
		loadOrCreateMutants(reset, mcl, -1, safepersist, false);
	}

	public void loadOrCreateMutants(boolean reset, MutationCreationListener mcl, int nb, int safepersist, boolean stackTraceInstrumentation) throws PersistenceException, URISyntaxException, BadObjectTypeException {
		File f = new File(getConfigFileResolved());

		if(!reset && f.exists()){
			loadMutants();
		}

		Mutation.createMutants(ps, this, mcl, reset, nb, safepersist, stackTraceInstrumentation);

		saveMutants();

		if(InterruptionManager.isInterruptedDemanded()){
			InterruptionManager.notifyLastIterationFinished();
		}
	}


	public void saveMutants() throws PersistenceException {
		MutationXmlPersistenceManager mgr = new MutationXmlPersistenceManager(this);
		XMLPersistence.save(mgr);
	}

	public void loadMutants() throws PersistenceException {
		MutationXmlPersistenceManager mgr = new MutationXmlPersistenceManager(this);
		XMLPersistence.load(mgr);
	}

	public void createExecutionFolderIfNeeded(){
		File f = new File(this.getExecutionFileResolved());

		if(!f.exists()){
			f.mkdirs();
		}
	}

	public boolean isMutantAlive(String mutid) throws MutationNotRunException, PersistenceException {
		MutantIfos mutationStats = loadMutationStats(mutid, false);

		return SetTools.areSetsSimilars(ps.getFailingTestCases(), mutationStats.getExecutedTestsResults().getRawFailingTestCases()) &&
				SetTools.areSetsSimilars(ps.getHangingTestCases(), mutationStats.getExecutedTestsResults().getRawHangingTestCases());
	}

	public boolean isMutantKilled(String mutid) throws MutationNotRunException, PersistenceException {
		return !isMutantAlive(mutid);
	}

	/**
	 * Create a SourceReference from a SourcePosition with resolving the file source path
	 * If the position for the element in source file is malformed see {@link SourceReference#SourceReference(SourcePosition)},
	 * try to resolve with parent until finding a good match. If none found, return null... 
	 * @param toReplace
	 * @return
	 * @throws MalformedSourcePositionException 
	 */
	public static SourceReference generateSourceReferenceForMutation(CtElement toReplace) {
		SourceReference sr = null;
		CtElement search = toReplace;
		
		boolean again = true;
		int parentsearch = 0;
		
		while(again && search != null){
			try {
				sr = new SourceReference(search.getPosition());
				again = false;
			} catch (MalformedSourcePositionException e) {
				search = search.getParent();
				again = true;
				parentsearch++;
			}
		}
		
		if(search == null){
			return null;
		}else{
			sr.setParentSearch(parentsearch);
		}

		return sr;
	}
	
	public SourceReference shortenPathForGeneratedSourceReference(SourceReference sr) {
		if(sr.getFile().startsWith(ps.resolveThis(ps.getOriginalSrc()))){
			sr.setFile(sr.getFile().substring(ps.resolveThis(ps.getOriginalSrc()).length()));
		}
		
		return sr;
	}
	
	public String[] getRunningClassPath(String forMutant) throws IOException{
		String[] rcp = getRelatedProcessStatisticsObject().getRunningClassPath();
		String[] ret = new String[rcp.length + 1];
		
		ret[0] = getBytecodeMutationResolved() + File.separator + forMutant;
		
		int i = 1;
		for(String it : rcp){
			ret[i++] = it;
		}
		
		return ret;
	}
	
	public MutationOperator getMutationOperator() {
		return this.mutop;
	}
	
	/******
	 * PS method bridges
	 */
	
	/**
	 * Invoke {@link ProcessStatistics#getCoherentMutantFailAndHangTestCases(TestsExecutionIfos)}.
	 */
	public String[] getCoherentMutantFailAndHangTestCases(TestsExecutionIfos tei) throws MutationNotRunException {
		return getRelatedProcessStatisticsObject().getCoherentMutantFailAndHangTestCases(tei);
	}

	/**
	 * Invoke {@link ProcessStatistics#getTestCases()}.
	 */
	public String[] getTestCases() {
		return getRelatedProcessStatisticsObject().getTestCases();
	}
	
	/**
	 * Invoke {@link ProcessStatistics#getTestClasses()}.
	 */
	public String[] getTestClasses() {
		return getRelatedProcessStatisticsObject().getTestClasses();
	}

	/**
	 * Invoke {@link ProcessStatistics#getTestExecutionResult()}.
	 */
	public TestsExecutionIfos getTestExecutionResult(){
		return getRelatedProcessStatisticsObject().getTestExecutionResult();
	}
	
	/**
	 * This iterator is aware of memory consumption !
	 * @return
	 */
	public Iterator<MutantIfos> iterator(){
		final List<String> list;
		
		try {
			list = Arrays.asList(listViableAndRunnedMutants(false));
		} catch (PersistenceException e) {
			// Not thrown as parameter is false (no loading)
			e.printStackTrace();
			return null;
		}
		
		return new Iterator<MutantIfos>() {
			int pos = 0;
			
			@Override
			public boolean hasNext() {
				return pos < list.size();
			}

			@Override
			public MutantIfos next() {
				if(!hasNext())
					return null;
				
				String m = list.get(pos++);
				
				MutantIfos mi_notgc = getMutationStats(m);
				try {
					mi_notgc.loadExecution(MutationStatistics.this, true);
				} catch (Exception e) {
					return null;
				}
				MutantIfos mi = new MutantIfos(mi_notgc); 
				mi_notgc.unloadExecution(false);
				
				return mi;
			}
		};
	}

	/**
	 * This method can be used as a trick to reduce memory consumption by garbage collecting executions once the returned object is lost.
	 * The returned MutantIfos is a fully loaded object as the internal one is still a light one
	 * @param m
	 * @return
	 * @throws MutationNotRunException
	 * @throws PersistenceException
	 */
	public MutantIfos getExternalDeepLoaded(String m) throws MutationNotRunException, PersistenceException {
		
		MutantIfos mi_notgc = getMutationStats(m);
		mi_notgc.unloadExecution(false);
		mi_notgc.loadExecution(this, true);
		MutantIfos mi = new MutantIfos(mi_notgc);
		mi_notgc.unloadExecution(true);
		
		return mi;
	}
	
}
