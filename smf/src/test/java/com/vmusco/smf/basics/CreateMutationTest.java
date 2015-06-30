package com.vmusco.smf.basics;

import java.io.File;

import org.junit.Test;

import com.vmusco.smf.mutation.TestContants;
import com.vmusco.smf.run.CreateMutation;
import com.vmusco.smf.run.NewProject;

public class CreateMutationTest {
	
	
	public static void main(String[] args) throws Exception {
		String[] s = new String[]{
				//"/tmp/toto/codec.smf.run.xml", "AOR"
				//"/tmp/test/toto/run.smf.xml", "UOI"
				//"-m", "src/main/java/org/apache/commons/lang3/StringUtils.java", "/tmp/hitup/", "ABS"
				//"-m", "src/test/ASimpleClass.java", "/tmp/hitup", "LCR"
				"/home/vince/Experiments/bugimpact/mutants/workspace/commons-codec", "AOR", "-n", "bis", "-m", "src/main/java/org/apache/commons/codec/binary/Base64.java"
				
		};
		
		CreateMutation.main(s);
	}
}
