package com.vmusco.pminer.analyze;

/***
 * This interface allows an object to listen to the computation of mutants
 * one a time
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public interface MutantTestProcessingListener <T> {
	public void aMutantHasBeenProceeded(T a);
}
