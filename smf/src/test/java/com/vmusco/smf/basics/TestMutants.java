package com.vmusco.smf.basics;

import com.vmusco.smf.run.CreateMutation;
import com.vmusco.smf.run.MutationRunTests;

public class TestMutants {

	public static void main(String[] args) throws Exception {
		String[] s = new String[]{
				"/home/vince/Experiments/bugimpact/mutants/commons-codec/mutations/main/ABS/mutations.xml",
				"-m", "mutant_1675"
		};
		
		MutationRunTests.main(s);
	}
}
