package com.vmusco.smf.basics;

import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.run.CreateMutation;
import com.vmusco.smf.run.MutationRunTests;
import com.vmusco.smf.run.NewProject;
import com.vmusco.smf.run.ParallelMutationRunTests;

public class AllPipeTest {

	public static void main(String[] args) throws Exception {
		if(true){
			/*String[] s1 = new String[]{"/tmp/commons-io", "/home/vince/Experiments/datasets/commons-io", "-F", 
					"-r", "src/test/resources"};
			String[] s2 = new String[]{"/tmp/commons-io", "-R"};*/
			

			/*String[] s1 = new String[]{"/tmp/guava", "/home/vince/Experiments/datasets/guava", "-F", 
			"-s", "guava/src", 
			"-t", "guava-tests/test"};//:guava-testlib/src:guava-testlib/test"};
			String[] s2 = new String[]{"/tmp/guava", "-R"};*/
			

			String[] s1 = new String[]{"/tmp/sonar-core", "/home/vince/Experiments/datasets/sonarqube/sonar-core", "-F", 
					//"-s", "sonar-check-api/src/main/java:sonar-plugin-api/src/main/java:sonar-core/src/main/java",
					"-s", "src/main/java",
					//"-t", "sonar-plugin-api/src/test/java:sonar-core/src/test/java",
					"-t", "/src/test/java",
					//"-r", "sonar-core/src/test/resources"
					"-r", "src/main/resources:src/test/resources"};
			
			String[] s2 = new String[]{"/tmp/sonar-core", "-R"};



			
			NewProjectTest.printArgs("NewProject", s1);
			NewProjectTest.printArgs("NewProject", s2);
	
			NewProject.main(s1);
			NewProject.main(s2);
	
	
			//String[] s = new String[]{NewProjectTest.workspace+"spojo", "ABS"};
			//String[] s = new String[]{"/tmp/jgit", "ABS", "-m", "org.eclipse.jgit/src/org/eclipse/jgit/dircache/DirCacheCheckout.java"};
			
			//Class<MutationOperator<?>>[] allClasses = MutatorsFactory.allAvailMutator();
	
			/*for(Class<MutationOperator<?>> c : allClasses){
				s[1] = ((MutationOperator)c.newInstance()).operatorId();
				CreateMutation.main(s);
			}*/
			
			//CreateMutation.main(s);
			
		}else{
			/*String[] s = new String[]{"1", NewProjectTest.workspace+"spojo/mutations/main"};
			NewProjectTest.printArgs("ParallelMutationRunTests", s);
			ParallelMutationRunTests.main(s);*/
			
			String[] s = new String[]{"/tmp/toto2/mutations/main/AOR/mutations.xml", "-m", "mutant_44"};
			NewProjectTest.printArgs("MutationRunTests", s);
			MutationRunTests.main(s);
		}
	}

}
