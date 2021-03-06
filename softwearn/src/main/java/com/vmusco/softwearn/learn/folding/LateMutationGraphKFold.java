package com.vmusco.softwearn.learn.folding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vmusco.pminer.analyze.MutationStatisticsCollecter;
import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException.TYPE;
import com.vmusco.pminer.impact.ConsequencesExplorer;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softwearn.exceptions.CoherencyException;
import com.vmusco.softwearn.learn.LearningKGraph;
import com.vmusco.softwearn.learn.learner.late.LateImpactLearner;

public class LateMutationGraphKFold extends MutationGraphExplorer{
	private static final Logger logger = LogManager.getFormatterLogger(LateMutationGraphKFold.class.getSimpleName());

	private int k;
	private MutationStatistics ms;
	private LateImpactLearner learner;
	private ConsequencesExplorer tester;
	List<MutantIfos[]> partitionDataset;

	private Random random = null;

	// For persistence restoration only
	public LateMutationGraphKFold() {
		super(null);
	}
	
	public LateMutationGraphKFold(LearningKGraph g) {
		super(g.graph());
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link LateMutationGraphKFold#instantiateKFold(MutationStatistics, LearningKGraph, int, int, LateImpactLearner, ConsequencesExplorer, boolean, Random)})
	 * @throws PersistenceException 	 
	 * @throws CoherencyException */
	public static LateMutationGraphKFold instantiateTenFold(MutationStatistics ms, LearningKGraph g, int nbmutants, LateImpactLearner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException, CoherencyException{
		return instantiateKFold(ms, g, 10, nbmutants, learner, tester, ignoreOverlaping, null);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link LateMutationGraphKFold#instantiateKFold(MutationStatistics, LearningKGraph, int, int, LateImpactLearner, ConsequencesExplorer, boolean, Random)})
	 * taking all mutants in the mutation object and taking into consideration overlaping mutants 
	 * @throws PersistenceException 	 
	 * @throws CoherencyException */
	public static LateMutationGraphKFold instantiateKFold(MutationStatistics ms, LearningKGraph g, int k, LateImpactLearner learner, ConsequencesExplorer tester) throws PersistenceException, CoherencyException {
		return instantiateKFold(ms, g, k, learner, tester, false);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link LateMutationGraphKFold#instantiateKFold(MutationStatistics, LearningKGraph, int, int, LateImpactLearner, ConsequencesExplorer, boolean, Random)})
	 * taking all mutants in the mutation object
	 * @throws PersistenceException 	 
	 * @throws CoherencyException */
	public static LateMutationGraphKFold instantiateKFold(MutationStatistics ms, LearningKGraph g, int k, LateImpactLearner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException, CoherencyException {
		return instantiateKFold(ms, g, k, 0, learner, tester, ignoreOverlaping, null);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph ({@link LateMutationGraphKFold#instantiateKFold(MutationStatistics, LearningKGraph, int, int, LateImpactLearner, ConsequencesExplorer, boolean, Random)})
	 * taking into consideration overlaping mutants
	 * @throws PersistenceException 	 
	 * @throws CoherencyException */
	public static LateMutationGraphKFold instantiateKFold(MutationStatistics ms, LearningKGraph g, int k, int nbmutants, LateImpactLearner learner, ConsequencesExplorer tester) throws PersistenceException, CoherencyException {
		return instantiateKFold(ms, g, k, nbmutants, learner, tester, false, null);
	}
	
	public static LateMutationGraphKFold instantiateKFold(MutationStatistics ms2, LearningKGraph g, int k2, int nbmut, LateImpactLearner il, ConsequencesExplorer t, Random random) throws PersistenceException, CoherencyException {
		return instantiateKFold(ms2, g, k2, nbmut, il, t, false, random);
	}

	/**
	 * Build an object for k-fold testing with mutants and graph
	 * @param ms the mutation report object to take mutants from
	 * @param g the graph on which we want to learn informations
	 * @param k the number of folds to do
	 * @param nbmutants the number of mutants to consider
	 * @param learner a learning listener responsible of updating weights and getting end notifications
	 * @param ignoreOverlaping true to ignore the overlapping mutants once divided by k false to have a last fold which may be larger than others
	 * @param random a seed to shuffle (null to total random generation)
	 * @throws PersistenceException 
	 * @throws CoherencyException 
	 * @throws Exception
	 */
	public static LateMutationGraphKFold instantiateKFold(MutationStatistics ms, LearningKGraph g, int k, int nbmutants, LateImpactLearner learner, ConsequencesExplorer tester, boolean ignoreOverlaping, Random random) throws PersistenceException, CoherencyException{
		LateMutationGraphKFold r = new LateMutationGraphKFold(g);
		if(random != null)
			r.random  = random;
		
		r.init(ms, k, nbmutants, learner, tester, ignoreOverlaping);
		return r;
	}

	protected void init(MutationStatistics ms, int k, int nbmutants, LateImpactLearner learner, ConsequencesExplorer tester, boolean ignoreOverlaping) throws PersistenceException, CoherencyException {
		this.ms = ms;
		this.k = k;
		this.setLearner(learner);
		this.setTester(tester);

		List<MutantIfos> data = this.getData(nbmutants);
		this.partitionDataset = this.partitionDataset(data, ignoreOverlaping);

		if(this.partitionDataset == null){
			throw new CoherencyException(String.format("The number of available mutants (%d) is lower than the desired k value (%d)", data.size(), this.k));
		}
		
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

	public void setLearner(LateImpactLearner learner) {
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

		// Prior sorting to ensure to apply similarly Random object
		Collections.sort(l, new Comparator<MutantIfos>() {
			@Override
			public int compare(MutantIfos o1, MutantIfos o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		if(this.random != null)
			Collections.shuffle(l, random);
		else
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

			logger.debug("Slice %d-%d", sizes*i, nextsize);
			r.add((MutantIfos[]) l.subList(sizes*i, nextsize).toArray(new MutantIfos[0]));
		}

		return r;
	}

	public void learnKFold() throws MutationNotRunException {
		LearningKGraph lg = (LearningKGraph)g;
		lg.switchToLearningPhase();

		for(int i=0; i<partitionDataset.size(); i++){
			learn(partitionDataset.get(i), i);
		}

		learner.learningRoundFinished(lg);
	}

	public void testKFold(final float testing_threshold){
		LearningKGraph lg = (LearningKGraph)g;
		lg.setThreshold(testing_threshold);

		for(int i=0; i<partitionDataset.size(); i++){
			test(i);
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
		LearningKGraph lg = (LearningKGraph)g;
		learner.learn(lg, point, tests, k);
	}

	public MutantIfos[] getTestingSubset(int iteration){
		return partitionDataset.get(iteration);
	}

	public void test(int iteration){
		LearningKGraph lkg = (LearningKGraph)getGraph();
		
		lkg.setK(iteration);
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
				logger.debug("A special entry point is thrown ("+(e.getType().name())+")...");
				
				for(MutationStatisticsCollecter msc : listeners){
					if(e.getType() == TYPE.ISOLATED)
						msc.isolatedFound(mi.getId(), mi.getMutationIn());
					else if(e.getType() == TYPE.NOT_FOUND)
						msc.unboundedFound(mi.getId(), mi.getMutationIn());
				}
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

	public LateImpactLearner getLearner(){
		return learner;
	}

	public void learningRoundFinished(){
		learner.learningRoundFinished((LearningKGraph)g);
	}






	/********
	 * PERSISTENCE TOOLS
	 */

	// For persistence purposes
	public List<MutantIfos[]> getPartitionDataset(){
		return partitionDataset;
	}

	public void setPartitionDataset(List<MutantIfos[]> dataset){
		partitionDataset = dataset;
	}

	public int getTotalNbOfMutants(){
		int total = 0;
		
		for(MutantIfos[] mi : partitionDataset){
			total += mi.length;
		}
		
		return total;
	}
	
	public ConsequencesExplorer getTester() {
		return tester;
	}
	
	public void setMutationStatisticsObject(MutationStatistics ms){
		this.ms = ms;
	}
}
