package com.vmusco.softwearn.learn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;

public class MutationGraphKFold {
	private static final Logger logger = LogManager.getFormatterLogger(MutationGraphKFold.class.getSimpleName());
	
	private Set<TestListener> listeners = new HashSet<TestListener>();
	
	private int k;
	private MutationStatistics ms;
	private LearningGraph g;
	private ImpactLearner learner;
	List<MutantIfos[]> partitionDataset;
	
	/**
	 * Build an object for 10-fold testing with mutants and graph (@link {@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, ImpactLearner, boolean) 

	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateTenFold(MutationStatistics ms, LearningGraph g, int nbmutants, ImpactLearner learner, boolean ignoreOverlaping) throws PersistenceException{
		return instantiateKFold(ms, g, 10, nbmutants, learner, ignoreOverlaping);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph (@link {@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, ImpactLearner, boolean) 
	 * taking all mutants in the mutation object and taking into consideration overlaping mutants 

	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, ImpactLearner learner) throws PersistenceException {
		return instantiateKFold(ms, g, k, learner, false);
	}
	
	/**
	 * Build an object for k-fold testing with mutants and graph (@link {@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, ImpactLearner, boolean) 
	 * taking all mutants in the mutation object

	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, ImpactLearner learner, boolean ignoreOverlaping) throws PersistenceException {
		return instantiateKFold(ms, g, k, 0, learner, ignoreOverlaping);
	}
	
	/**
	 * Build an object for k-fold testing with mutants and graph (@link {@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, ImpactLearner, boolean) 
	 * taking into consideration overlaping mutants

	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, int nbmutants, ImpactLearner learner) throws PersistenceException {
		return instantiateKFold(ms, g, k, nbmutants, learner, false);
	}
	
	/**
	 * Build an object for k-fold testing with mutants and graph
	 * @param ms the mutation report object to take mutants from
	 * @param g the graph on which we want to learn informations
	 * @param k the number of folds to do
	 * @param nbmutants the number of mutants to consider
	 * @param learner a learning listener responsible of updating weights and getting end notifications
	 * @param ignoreOverlaping true to ignore the overlapping mutants once divided by k false to have a last fold which may be larger than others
	 * @throws PersistenceException 
	 * @throws Exception
	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, int nbmutants, ImpactLearner learner, boolean ignoreOverlaping) throws PersistenceException{
		MutationGraphKFold r = new MutationGraphKFold();
		
		r.g = g;
		r.ms = ms;
		r.k = k;
		r.learner = learner;
		
		List<MutantIfos> data = r.getData(nbmutants);
		r.partitionDataset = r.partitionDataset(data, ignoreOverlaping);
		r.learner.postPreparedSet(ms.getRelatedProcessStatisticsObject(), data.toArray(new MutantIfos[0]));
		
		return r;
	}
	
	public void setLearner(ImpactLearner learner) {
		this.learner = learner;
	}
	
	private List<MutantIfos> getData(int nbmut) throws PersistenceException{
		List<MutantIfos> l = new ArrayList<MutantIfos>();
		String[] allMutants = this.ms.listViableAndRunnedMutants(true);
		
		for(String m : allMutants)
			l.add(this.ms.getMutationStats(m));
		
		Collections.shuffle(l);
		
		int considerednbmut = nbmut;
		
		if(nbmut > allMutants.length){
			logger.info("Not enough mutants for the number requested (%d). Fixing to max (= %d)", nbmut, allMutants.length);
			considerednbmut = allMutants.length;
		}else if(nbmut == 0){
			considerednbmut = l.size();
		}
		
		logger.info("Picked %d mutants", considerednbmut);
		
		return l.subList(0, considerednbmut);
	}
	
	private List<MutantIfos[]> partitionDataset(List<MutantIfos> l, boolean ignoreOverlaping){
		List<MutantIfos[]> r = new ArrayList<MutantIfos[]>(); 
		
		if(l.size() < this.k){
			logger.error("Unable to log if the size of dataset (%d) is slower than k (%d) !", l.size(), this.k);
			return null;
		}
		
		int sizes = (int)Math.ceil(l.size()/this.k);
		logger.info("Size for fold: %d", sizes);
		
		for(int i = 0; i < this.k; i++){
			int nextsize = sizes*i+sizes;
			
			if(!ignoreOverlaping && i == this.k-1){
				nextsize = l.size();
			}
			
			logger.info("Slice %d-%d", sizes*i, nextsize);
			r.add((MutantIfos[]) l.subList(sizes*i, nextsize).toArray(new MutantIfos[0]));
		}
		
		return r;
	}
	
	public void kfold(final float testing_threshold) throws Exception{
		for(int i=0; i<partitionDataset.size(); i++){
			fold(partitionDataset, i, testing_threshold);
		}
		
		for(TestListener tl : listeners){
			tl.allFoldsEnded();
		}
	}
	
	private void fold(List<MutantIfos[]> partitions, int iteration, final float threshold){
		// Training
		for(int i=0; i<this.k; i++){
			if(this.k == iteration){
				continue;
			}
			
			logger.info("Training with %d subset", i);
			learn(partitions.get(i));
		}
		
		// Testing
		logger.info("Training with %d subset", iteration);
		test(partitions.get(iteration), threshold);
	}
	
	private void learn(MutantIfos[] mutants){
		for(MutantIfos mi : mutants){
			logger.info("Learning with %s", mi.getId());
			
			try {
				for(String aTest : ms.getRelatedProcessStatisticsObject().getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults())){
					learner.learn(g, mi.getMutationIn(), aTest+"()");
				}
			} catch (MutationNotRunException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void test(MutantIfos[] mutants, final float threshold){
		for(MutantIfos mi : mutants){
			logger.info("Testing with %s", mi.getId());
			
			String[] impactedTests = g.getImpactedTests(mi.getMutationIn(), threshold);
			
			for(TestListener tl : listeners){
				tl.testResult(mi, impactedTests);
			}
		}
		
		for(TestListener tl : listeners){
			tl.oneFoldEnded();
		}
	}
	
	public void addTestListener(TestListener aListener){
		listeners.add(aListener);
	}
	
	public void removeTestListener(TestListener aListener){
		listeners.remove(aListener);
	}
}
