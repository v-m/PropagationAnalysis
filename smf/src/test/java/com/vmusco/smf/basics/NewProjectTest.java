package com.vmusco.smf.basics;

import java.io.File;

import org.junit.Test;

import com.vmusco.smf.mutation.TestContants;
import com.vmusco.smf.run.NewProject;

public class NewProjectTest {

	protected final static String workspace = "/home/vince/Experiments/bugimpact/mutants/workspace/";
	protected final static String dataset = "/home/vince/Experiments/datasets/";


	public static void main(String[] args) throws Exception {
		String[] s1, s2;

		
		// LANG
		s1 = new String[]{workspace+"commons-lang", dataset+"commons-lang", "-F"};
		s2 = new String[]{workspace+"commons-lang"};

		// CODEC
		s1 = new String[]{workspace+"commons-codec", dataset+"commons-codec", "-F",
				"-r", "target/classes"};
		s2 = new String[]{workspace+"commons-codec"};

		// IO
		s1 = new String[]{workspace+"commons-io", dataset+"commons-io", "-F", 
				"-r", "src/test/resources"};
		s2 = new String[]{workspace+"commons-io"};

		// COLLECTIONS
		s1 = new String[]{workspace+"commons-collections4", dataset+"commons-collections4", "-F"};
		s2 = new String[]{workspace+"commons-collections4"};

		//JODA-TIME
		s1 = new String[]{workspace+"joda-time", dataset+"joda-time", "-F", 
				"-r", "src/test/resources:target/classes"};
		s2 = new String[]{workspace+"joda-time", "-R" };

		//JGIT CORE
		s1 = new String[]{workspace+"jgit", "/home/vince/Experimentdatasets/jgit/", "-F", 
				"-s", "org.eclipse.jgit/src", 
				"-t", "org.eclipse.jgit.test/src:org.eclipse.jgit.test/tst", 
				"-r", "org.eclipse.jgit.test/tst-rsrc/"};
		s2 = new String[]{workspace+"jgit", "-R"};

		//GSON
		s1 = new String[]{workspace+"gson", dataset+"gson/gson/", "-F"};
		s2 = new String[]{workspace+"gson", "-R"};

		//TODO: HADOOP
		/*s1 = new String[]{workspace+"hadoop", dataset+"hadoop", "-F", 
				"-s", "hadoop-common-project/hadoop-common/src/main/java", 
				"-t", "hadoop-common-project/hadoop-common/src/test/java/", 
				"-r", "hadoop-common-project/hadoop-common/src/test/resources/"};
		s2 = new String[]{workspace+"hadoop", "-R"};*/

		//GUAVA
		/*s1 = new String[]{workspace+"guava", dataset+"guava", "-F", 
				"-s", "guava/src", 
				"-t", "guava-tests/test"};//:guava-testlib/src:guava-testlib/test"};
		s2 = new String[]{workspace+"guava", "-R"};*/

		//SHINDIG
		/*s1 = new String[]{workspace+"shindig", dataset+"shindig", "-F", 
				"-s", "java/common/src/main/java/", 
				"-t", "java/common/src/test/java/",
				"-r", "java/common/src/main/resources:java/common/src/test/resources"}
		s2 = new String[]{workspace+"shindig", "-R"};*/

		//SPOJO-CORE
		s1 = new String[]{workspace+"spojo", dataset+"Spojo", "-F", 
				"-s", "spojo-core/src/main/java/", 
				"-t", "spojo-core/src/test/java/"};
		s2 = new String[]{workspace+"spojo", "-R"};

		//TODO: JBEHAVE-CORE
		/*s1 = new String[]{workspace+"jbehave-core", dataset+"jbehave-core/jbehave-core", "-F", 
				"-s", "src/main/java", 
				"-t", "src/test/java",
				"-r", "src/test/resources/test+dir:src/main/resources:src/test/resources"};
		
		s2 = new String[]{workspace+"jbehave-core", "-T"};*/

		// SONARQUBE

		s1 = new String[]{workspace+"sonar-core", dataset+"sonarqube/sonar-core", "-F", 
				//"-s", "sonar-check-api/src/main/java:sonar-plugin-api/src/main/java:sonar-core/src/main/java",
				"-s", "src/main/java",
				//"-t", "sonar-plugin-api/src/test/java:sonar-core/src/test/java",
				"-t", "/src/test/java",
				//"-r", "sonar-core/src/test/resources"
				"-r", "src/main/resources:src/test/resources"};
		
		/*s1 = new String[]{workspace+"sonar-core", dataset+"sonarqube", "-F", 
				"-s", "sonar-check-api/src/main/java:sonar-plugin-api/src/main/java:sonar-core/src/main/java", 
				"-t", "sonar-plugin-api/src/test/java:sonar-core/src/test/java",
				"-r", "sonar-core/src/test/resources"};//:guava-testlib/src:guava-testlib/test"};
		
		s2 = new String[]{workspace+"sonar-core", "-R"};*/
		
		printArgs("NewProject", s1);
		printArgs("NewProject", s2);

		NewProject.main(s1);
		NewProject.main(s2);
	}

	public static void printArgs(String cmd, String[] s) {
		System.out.print("java -cp target/smf-0.0.1-SNAPSHOT-jar-with-dependencies.jar:lib/spoon.jar com.vmusco.smf.run."+cmd+" ");

		for(String ss : s){
			System.out.print(ss+" ");
		}
		System.out.println();
	}
}
