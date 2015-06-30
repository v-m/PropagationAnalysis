package com.vmusco.softminer.sourceanalyzer.graphbuilding;

import java.io.File;

/**
 * This interface defines a method to locate a graph file if existing
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public interface PersistedGraphFinder {
	public File locate(String projectName, GraphBuildLogic genMethod);
}
