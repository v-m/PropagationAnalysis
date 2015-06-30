package com.vmusco.smf.projects;

import java.io.File;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.ProjectDefinition;

public class MyMiniProject extends ProjectDefinition{

	public void projectConfiguration(ProcessStatistics ps) {
		//ps.projectIn = "/home/vince/Experiments/bugimpact/workspace/test";

		ps.srcToCompile = new String[]{
				"src/main/java"
		};

		ps.srcTestsToTreat = new String[]{
				"src/test/java"
		};

		ps.skipMvnClassDetermination = false;
	}

	@Override
	public void mutationConfiguration(ProcessStatistics ps,
			MutationStatistics ms) {
		ms.classToMutate = new String[]{
				"src/main/java/test/test/MySum.java"
		};
	}
}
