package com.vmusco.softwearn.learn.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softwearn.learn.LearningGraph;
import com.vmusco.softwearn.learn.learner.Learner;
import org.apache.logging.log4j.core.appender.SyslogAppender;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class MutationGraphKFold extends MutationGraphExplorer{
	private static final Logger logger = LogManager.getFormatterLogger(MutationGraphKFold.class.getSimpleName());

	private int k;
	private MutationStatistics ms;
	private Learner learner;
	private ConsequencesExplorer tester;
	List<MutantIfos[]> partitionDataset;

	public MutationGraphKFold(LearningGraph g) {
		super(g.graph());
	}

	/**
	 * Build an object for 10-fold testing with mutants and graph ({@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, Learner, ConsequencesExplorer, boolean)})
	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateTenFold(MutationStatistics ms, LearningGraph g, int nbmutants, Learner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException{
		return instantiateKFold(ms, g, 10, nbmutants, learner, tester, ignoreOverlaping);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, Learner, ConsequencesExplorer, boolean)})
	 * taking all mutants in the mutation object and taking into consideration overlaping mutants 
	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, Learner learner, ConsequencesExplorer tester) throws PersistenceException {
		return instantiateKFold(ms, g, k, learner, tester, false);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, Learner, ConsequencesExplorer, boolean)})
	 * taking all mutants in the mutation object
	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, Learner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException {
		return instantiateKFold(ms, g, k, 0, learner, tester, ignoreOverlaping);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link MutationGraphKFold#instantiateKFold(MutationStatistics, LearningGraph, int, int, Learner, ConsequencesExplorer, boolean)})
	 * taking into consideration overlaping mutants
	 * @throws PersistenceException 	 */
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, int nbmutants, Learner learner, ConsequencesExplorer tester) throws PersistenceException {
		return instantiateKFold(ms, g, k, nbmutants, learner, tester, false);
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
	public static MutationGraphKFold instantiateKFold(MutationStatistics ms, LearningGraph g, int k, int nbmutants, Learner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException{
		MutationGraphKFold r = new MutationGraphKFold(g);

		r.init(ms, k, nbmutants, learner, tester, ignoreOverlaping);
		return r;
	}

	protected void init(MutationStatistics ms, int k, int nbmutants, Learner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException {
		this.ms = ms;
		this.k = k;
		this.setLearner(learner);
		this.setTester(tester);

		List<MutantIfos> data = this.getData(nbmutants);
		this.partitionDataset = this.partitionDataset(data, ignoreOverlaping);
		//this.learner.postPreparedSet(ms, data.toArray(new MutantIfos[0]));
		
		for(MutantIfos mi : data){
			try {
				ArrayList<String> tests = new ArrayList<>();
				for(String i : ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults())){
					tests.add(i.endsWith("()")?i:i+"()");
				}
				
				this.learner.postDeclareAnImpact(mi.getMutationIn(), tests.toArray(new String[tests.size()]));
			} catch (MutationNotRunException e) {
				e.printStackTrace();
			}
		}
	}

	public void setLearner(Learner learner) {
		this.learner = learner;
	}

	public void setTester(ConsequencesExplorer tester) {
		this.tester = tester;
	}

	private List<MutantIfos> getData(int nbmut) throws PersistenceException{
		List<MutantIfos> l = new ArrayList<MutantIfos>();
		String[] allMutants = this.ms.listViableAndRunnedMutants(true);

		for(String m : allMutants)
			l.add(this.ms.getMutationStats(m));

		Collections.shuffle(l);
		/*
		 * Can be sorted for testing purposes...
		 * Collections.sort(l, new Comparator<MutantIfos>() {
			@Override
			public int compare(MutantIfos o1, MutantIfos o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});*/

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

			logger.debug("Slice %d-%d", sizes*i, nextsize);
			r.add((MutantIfos[]) l.subList(sizes*i, nextsize).toArray(new MutantIfos[0]));
		}

		return r;
	}

	public void kfold(final float testing_threshold) throws Exception{
		for(int i=0; i<partitionDataset.size(); i++){
			fold(i, testing_threshold);
		}
	}

	public void fold(int iteration, final float threshold) throws MutationNotRunException{
		logger.info("Training with %d subset", iteration);
		LearningGraph lg = (LearningGraph)g;

		lg.resetLearnedInformations();

		// Training
		learn(iteration);

		lg.setThreshold(threshold);

		// Testing
		test(iteration);
	}

	public void learn(int iteration) throws MutationNotRunException{
		logger.debug("Fold %d", iteration);

		for(int i=0; i<this.k; i++){
			if(i == iteration){
				continue;
			}

			logger.debug("Training with subset %d", i);
			learn(partitionDataset.get(i), i);
		}
	}

	private void learn(MutantIfos[] mutants, int k) throws MutationNotRunException{
		for(MutantIfos mi : mutants){
			learner.setChangeId(mi.getId());
			logger.trace("Learning with %s", mi.getId());
			String point = mi.getMutationIn();
			String[] tests = ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());

			learn(point, tests, k);
		}
	}

	/**
	 * 
	 * @param point
	 * @param tests
	 * @param k is the k-th fold (k is the nr of subset which is tested (ie. not learned) which means this update will only occurs for all others except k).
	 */
	public void learn(String point, String[] tests, int k) {
		logger.trace("K = "+k);
		LearningGraph lg = (LearningGraph)g;
		learner.learn(lg, point, tests, k);
	}

	public MutantIfos[] getTestingSubset(int iteration){
		return partitionDataset.get(iteration);
	}

	public void test(int iteration){
		test(getTestingSubset(iteration), iteration);
	}

	@Override
	public void test(MutantIfos[] mutants, int k){
		for(MutationStatisticsCollecter msc : listeners){
			msc.executionStarting();
		}

		for(MutantIfos mi : mutants){
			logger.info("Testing with %s", mi.getId());

			String[] ais;
			try {
				long time = System.currentTimeMillis();
				tester.visit(ms, mi);
				time = System.currentTimeMillis() - time;
				String[] cis = tester.getLastConsequenceNodes();
				ais = ms.getCoherentMutantFailAndHangTestCases(mi.getExecutedTestsResults());

				for(MutationStatisticsCollecter msc : listeners){
					msc.intersectionFound(mi.getId(), mi.getMutationIn(), ais, cis);
					msc.declareNewTime(time);
				}
			} catch (MutationNotRunException e) {
				e.printStackTrace();
				System.exit(1);
			} catch (SpecialEntryPointException e) {
				logger.debug("A special entry point is thrown...");
				//e.printStackTrace();
			}
		}

		for(MutationStatisticsCollecter msc : listeners){
			msc.executionEnded();
		}
	}

	public int getInputDatasetSize(){
		int ret = 0;

		for(MutantIfos[] mia : partitionDataset){
			ret += mia.length;
		}

		return ret;
	}

	public int getK() {
		return k;
	}

	protected MutationStatistics getMutationStatistics(){
		return ms;
	}

	protected Learner getLearner(){
		return learner;
	}

	public void learningRoundFinished(){
		learner.learningRoundFinished((LearningGraph)g);
	}
}
