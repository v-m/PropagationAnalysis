package com.vmusco.softminer.sourceanalyzer.graphbuilding;

/***
 * This interface gives the minimal implementation to obtain a class path on a specific context
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public interface ClassPathObtainer {
	public String[] obtainClassPath() throws Exception; 
}
