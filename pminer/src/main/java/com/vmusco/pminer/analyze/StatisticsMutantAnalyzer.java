package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.Collections;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.softminer.utils.Tools;
import com.vmusco.softminer.utils.TypeWithInfo;

/***
 * This class is used to extract various statistics regarding the mutation/graph error propagation analysis
 * Considered metrics are: 
 * 	- precision/recall for each with their avg/median values
 * 	- number of error tests using mutation, using graph, basin size and avg/median values
 * @see StatisticsDisplayer
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 */
@Deprecated
public class StatisticsMutantAnalyzer extends MutantTestAnalyzer {
	
	// Numerical stats
	private ArrayList<Double> precmedian = new ArrayList<Double>();
	private ArrayList<Double> recamedian = new ArrayList<Double>();
	private ArrayList<Double> fscomedian = new ArrayList<Double>();
	private ArrayList<Double> exec_time = new ArrayList<Double>();

	private ArrayList<TypeWithInfo> sizeall = new ArrayList<TypeWithInfo>();
	private ArrayList<Double> mutonlyall = new ArrayList<Double>();
	private ArrayList<Double> graphonlyall = new ArrayList<Double>();
	private ArrayList<Double> basinall = new ArrayList<Double>();
	private ArrayList<Double> mutatall = new ArrayList<Double>();
	private ArrayList<Double> bothall = new ArrayList<Double>();

	private ArrayList<Double> mores = new ArrayList<Double>();
	private ArrayList<Double> lesses = new ArrayList<Double>();
	
	// Sets stats
	private ArrayList<String> considered = new ArrayList<String>();
	
	private ArrayList<String> perfect = new ArrayList<String>();
	private ArrayList<String> more = new ArrayList<String>();
	private ArrayList<String> less = new ArrayList<String>();
	private ArrayList<String> moreless = new ArrayList<String>();
	
	private ArrayList<UseGraphMutantStats> allMutatObjs = new ArrayList<UseGraphMutantStats>();

	public ArrayList<UseGraphMutantStats> getAllMutatObjs() {
		return allMutatObjs;
	}
	
	// Config
	private int nbc;
	private boolean needSorting = false;
	private MutantTestProcessingListener mta;
	private boolean forcingIntAnalysis = false;

	private UseGraphMutantStats mutstat;
	
	public UseGraphMutantStats getLastMutstat() {
		return this.mutstat;
	}

	/***
	 * Create a mutants statistic analyzer - this analyzer prioritize uninterrupted cases first...
	 * @param nbConsider limit the number of considered mutant - stop when too many are analyzed
	 * @param mta specify an invoker to warn after each mutant analysis...
	 */
	public StatisticsMutantAnalyzer(int nbConsider, MutantTestProcessingListener mta) {
		this.nbc = nbConsider;
		this.mta = mta;
	}

	@Override
	public void fireIntersectionFound(ProcessStatistics ps, String mutationId, MutantIfos mi, String[] graphDetermined, UseGraph basin, long propatime) throws MutationNotRunException{
		UseGraphMutantStats mutstat = new UseGraphMutantStats();

		mutstat.fillIn(ps, mutationId, mi, graphDetermined, basin, propatime);
		mutstat.calculate();
		
		updateStructure(mutstat);
	}
	
	public void updateStructure(UseGraphMutantStats ugms){
		considered.add(ugms.mutationId);
		
		// AVERAGES / MEDIANS
		sizeall.add(new TypeWithInfo<Double>(1d*ugms.graphsize, 1d*ugms.graphsizeonlytests));
		
		basinall.add(new Double(ugms.nb_graph));
		mutatall.add(new Double(ugms.nb_mutat));
		bothall.add(new Double(ugms.nb_boths));

		if(ugms.prediction_time >= 0){
			exec_time.add(ugms.prediction_time*1d);
		}
		
		if(ugms.precision >= 0)
			precmedian.add(ugms.precision);
		if(ugms.recall >= 0)
			recamedian.add(ugms.recall);
		if(ugms.fscore >= 0)
			fscomedian.add(ugms.fscore);

		// MUTANTS SETS
		if(ugms.nb_graph == ugms.nb_mutat && 
				ugms.nb_boths == ugms.nb_mutat && 
				ugms.nb_mores == 0 && ugms.nb_lesss == 0){
			perfect.add(ugms.mutationId);
		}else if(ugms.nb_mores > 0 && ugms.nb_lesss == 0){
			this.mores.add(new Double(ugms.nb_mores));
			more.add(ugms.mutationId);
			graphonlyall.add(new Double(ugms.nb_mores));
		}else if(ugms.nb_lesss > 0 && ugms.nb_mores == 0){
			less.add(ugms.mutationId);
			this.lesses.add(new Double(ugms.nb_lesss));
			mutonlyall.add(new Double(ugms.nb_lesss));
		}else{
			moreless.add(ugms.mutationId);
		}
		needSorting = true;
		
		allMutatObjs.add(ugms);
		
		this.mutstat = ugms;
		
		if(mta != null)
			mta.aMutantHasBeenProceeded(this);
	}

	@Override
	public boolean forceStop() {
		return nbc > 0 && considered.size() >= nbc;
	}

	@Override
	public void fireExecutionEnded() {

	}

	
	
	
	
	
	
	
	
	
	
	
	
	






	// GETTER FOR LAST
	////////////////////

	public String getLastMutationId() {
		return this.mutstat.mutationId;
	}

	public String getLastMutationInsertionPoint(){
		return this.mutstat.mutationInsertionPoint;
	}

	public int getLastGraphSize() {
		return this.mutstat.graphsize;
	}


	public int getLastNumberOfCasesDeterminedByGraphs() {
		return this.mutstat.nb_graph;
	}


	public int getLastNumberOfCasesDeterminedByMutation() {
		return this.mutstat.nb_mutat;
	}


	public int getLastNumberOfCasesDeterminedByBoth() {
		return this.mutstat.nb_boths;
	}


	public int getLastNumberOfCasesDeterminedOnlyByGraphs() {
		return this.mutstat.nb_mores;
	}


	public int getLastNumberOfCasesDeterminedOnlyByMutation() {
		return this.mutstat.nb_lesss;
	}


	public double getLastPrecision() {
		return this.mutstat.precision;
	}


	public double getLastRecall() {
		return this.mutstat.recall;
	}


	public double getLastFScore() {
		return this.mutstat.fscore;
	}


	// GETTER FOR SET SIZES
	/////////////////////////
	public int getNbPerfect(){
		return perfect.size();
	}

	public int getNbMore(){
		return more.size();
	}

	public int getNbLess(){
		return less.size();
	}

	public int getNbMoreLess(){
		return moreless.size();
	}

	public int getNbTotal(){
		return considered.size();
	}

	public double getPartPerfect(){
		return ((getNbPerfect()*1.0) / getNbTotal()); 
	}

	public double getPartMore(){
		return ((getNbMore()*1.0) / getNbTotal()); 
	}

	public double getPartLess(){
		return ((getNbLess()*1.0) / getNbTotal()); 
	}

	public double getPartMoreLess(){
		return ((getNbMoreLess()*1.0) / getNbTotal()); 
	}

	// GETTER FOR AVERAGES
	///////////////////////

	private void sort(){
		if(needSorting){
			Collections.sort(sizeall);
			Collections.sort(basinall);
			Collections.sort(mutatall);
			Collections.sort(bothall);
			Collections.sort(mutonlyall);
			Collections.sort(graphonlyall);

			Collections.sort(precmedian);
			Collections.sort(recamedian);
			Collections.sort(fscomedian);
			Collections.sort(mores);
			Collections.sort(lesses);
			Collections.sort(exec_time);

			needSorting = false;
		}
	}

	public double getAvgPrecision(){
		sort();
		return Tools.average(Tools.toDoubleArray(precmedian));
	}

	public double getMedianPrecision(){
		sort();
		return Tools.median(Tools.toDoubleArray(precmedian));
	}
	
	public double getAvgExecTime(){
		sort();
		return Tools.average(Tools.toDoubleArray(exec_time));
	}

	public double getMedianExecTime(){
		sort();
		return Tools.median(Tools.toDoubleArray(exec_time));
	}
	
	
	public double getAvgMoreSizes(){
		sort();
		return Tools.average(Tools.toDoubleArray(mores));
	}

	public double getMedianMoreSizes(){
		sort();
		return Tools.median(Tools.toDoubleArray(mores));
	}
	
	
	
	public double getAvgLessSizes(){
		sort();
		return Tools.average(Tools.toDoubleArray(lesses));
	}

	public double getMedianLessSizes(){
		sort();
		return Tools.median(Tools.toDoubleArray(lesses));
	}

	public double getAvgRecall(){
		sort();
		return Tools.average(Tools.toDoubleArray(recamedian));
	}

	public double getMedianRecall(){
		sort();
		return Tools.median(Tools.toDoubleArray(recamedian));
	}

	public double getAvgFScore(){
		sort();
		return Tools.average(Tools.toDoubleArray(fscomedian));
	}

	public double getMedianFScore(){
		sort();
		return Tools.median(Tools.toDoubleArray(fscomedian));
	}

	public double getAvgCasesFoundByGraph(){
		sort();
		return Tools.average(Tools.toDoubleArray(basinall));
	}

	public double getMedianCasesFoundByGraph(){
		sort();
		return Tools.median(Tools.toDoubleArray(basinall));
	}

	public double getAvgCasesFoundByMutation(){
		sort();
		return Tools.average(Tools.toDoubleArray(mutatall));
	}

	public double getMedianCasesFoundByMutation(){
		sort();
		return Tools.median(Tools.toDoubleArray(mutatall));
	}

	public double getAvgCasesFoundByBoth(){
		sort();
		return Tools.average(Tools.toDoubleArray(bothall));
	}

	public double getMedianCasesFoundByBoth(){
		sort();
		return Tools.median(Tools.toDoubleArray(bothall));
	}

	public double getAvgCasesFoundOnlyByGraph(){
		sort();
		return Tools.average(Tools.toDoubleArray(graphonlyall));
	}

	public double getMedianCasesFoundOnlyByGraph(){
		sort();
		return Tools.median(Tools.toDoubleArray(graphonlyall));
	}

	public double getAvgCasesFoundOnlyByMutation(){
		sort();
		return Tools.average(Tools.toDoubleArray(mutonlyall));
	}

	public double getMedianCasesFoundOnlyByMutation(){
		sort();
		return Tools.median(Tools.toDoubleArray(mutonlyall));
	}

	public double getAvgGraphSize(){
		sort();
		
		ArrayList<Double> calc = new ArrayList<Double>();
		for(TypeWithInfo<Double> it: sizeall){
			calc.add(it.getValue());
		}
		
		return Tools.average(Tools.toDoubleArray(calc));
	}

	public double getMedianGraphSize(){
		sort();
		TypeWithInfo<Double>[] medianWithInfo = Tools.medianWithInfo(sizeall);
		
		if(medianWithInfo.length == 2){
			return (medianWithInfo[0].getValue() + medianWithInfo[1].getValue()) / 2;
		}else{
			return medianWithInfo[0].getValue();
		}
	}
	
	public double getNbTestsInMedianGraphSize(){
		sort();
		TypeWithInfo<Double>[] medianWithInfo = Tools.medianWithInfo(sizeall);
		
		if(medianWithInfo.length == 2){
			return (medianWithInfo[0].getInfo() + medianWithInfo[1].getInfo()) / 2;
		}else{
			return medianWithInfo[0].getInfo();
		}
	}

	public double getMinGraphSize(){
		sort();
		return (sizeall.size()>0)?((TypeWithInfo<Double>)sizeall.get(0)).getValue():-1;
	}
	
	public double getNbTestsInMinGraphSize(){
		sort();
		return (sizeall.size()>0)?((TypeWithInfo<Double>)sizeall.get(0)).getInfo():-1;
	}
	
	public double getMaxGraphSize(){
		sort();
		return (sizeall.size()>0)?((TypeWithInfo<Double>)sizeall.get(sizeall.size() - 1)).getValue():-1;
	}
	
	public double getNbTestsInMaxGraphSize(){
		sort();
		return (sizeall.size()>0)?((TypeWithInfo<Double>)sizeall.get(sizeall.size() - 1)).getInfo():-1;
	}
}
