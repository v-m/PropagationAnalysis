package com.vmusco.pminer;

import com.vmusco.pminer.analyze.MutantTestAnalyzer;

/***
 * This interface allows an object to listen to the processing of mutants
 * one a time
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public interface MutantTestProcessingListener {
	public void aMutantHasBeenProceeded(MutantTestAnalyzer a);
}
