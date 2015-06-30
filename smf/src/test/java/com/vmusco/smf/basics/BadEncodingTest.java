package com.vmusco.smf.basics;

import com.vmusco.smf.run.CreateMutation;
import com.vmusco.smf.run.NewProject;

public class BadEncodingTest {
	public static void main(String[] args) throws Exception {
		String[] s1 = new String[]{"/tmp/hitup", "/home/vince/Experiments/bugimpact/workspace/Tests", "-s", "src", "-c", "/home/vince/Experiments/bugimpact/workspace/smf/lib/spoon.jar:/home/vince/Experiments/bugimpact/workspace/smf/target/smf-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "-F", "--no-tests"}; //, "-r", "hadoop-common-project/hadoop-common/src/test/resources/", "-F"};
		String[] s2 = new String[]{"/tmp/hitup", "-R"};
		String[] s3 = new String[]{"-m", "src/test/ASimpleClass.java", "/tmp/hitup", "LCR"};
		
		NewProject.main(s1);
		NewProject.main(s2);
		CreateMutation.main(s3);
	}
}
