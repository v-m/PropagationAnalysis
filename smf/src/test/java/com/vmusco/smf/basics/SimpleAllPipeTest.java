package com.vmusco.smf.basics;

import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.run.CreateMutation;
import com.vmusco.smf.run.MutationRunTests;
import com.vmusco.smf.run.NewProject;

public class SimpleAllPipeTest {
	public static void main(String[] args) throws Exception {

		String[] s1 = new String[]{"/tmp/toto", "/home/vince/Experiments/bugimpact/workspace/test", "-F", 
				"-s", "src/main/java", 
				"-t", "src/test/java"};

		String[] s2 = new String[]{"/tmp/toto", "-R"};

		NewProjectTest.printArgs("NewProject", s1);
		NewProjectTest.printArgs("NewProject", s2);

		NewProject.main(s1);
		NewProject.main(s2);


		String[] s = new String[]{"/tmp/toto", "AOR"};
		Class<MutationOperator<?>>[] allClasses = MutatorsFactory.allAvailMutator();
		CreateMutation.main(s);

	/*		String[] s = new String[]{"/tmp/toto2/mutations/main/AOR/mutations.xml", "-m", "mutant_44"};
			NewProjectTest.printArgs("MutationRunTests", s);
			MutationRunTests.main(s);*/
	}

}
