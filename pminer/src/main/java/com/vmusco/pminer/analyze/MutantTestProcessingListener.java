package com.vmusco.pminer.analyze;

/***
 * This interface allows an object to listen to the computation of mutants
 * one a time
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public interface MutantTestProcessingListener <T> {
	public void aMutantHasBeenProceeded(T a);
}
