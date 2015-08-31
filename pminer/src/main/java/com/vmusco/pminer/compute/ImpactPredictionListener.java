package com.vmusco.pminer.compute;

import com.vmusco.smf.analysis.MutantIfos;

public interface ImpactPredictionListener{
	public void fireTestIntersection(String string);
	public void fireOneMutantResults(double p, double r, double f);
	public void fireOneMutantStarting(String mutant, MutantIfos mi);
}
