package com.vmusco.pminer.analyze;

import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;


public abstract class MutantTestAnalyzer {

	public static <T> Set<T> intersection(Set<T> list1, Set<T> list2) {
        Set<T> list = new HashSet<T>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
	}
	
	/***
	 * This method is called once before startng the execution (i.e. to display headers ?)
	 * Default behavior: None
	 */
	public void fireExecutionStarting(){}
	
	/***
	 * This method is called once the full test execution is ended (i.e. to display avg results ?)
	 * Default behavior: None
	 */
	public void fireExecutionEnded(){}

	public abstract void fireIntersectionFound(ProcessStatistics ps, 
			String mutationId,
			MutantIfos mi,
			String[] graphDetermined, 
			UseGraph basin, long propatime);
	
	/***
	 * This method can be invoked to order the runner to interrupt its execution and discard remaining set
	 * @return
	 */
	public boolean forceStop(){
		return false;
	}
}
