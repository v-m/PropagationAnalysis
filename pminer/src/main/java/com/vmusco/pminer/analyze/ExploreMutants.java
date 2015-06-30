package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.vmusco.pminer.MutantTestProcessingListener;
import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.softminer.graphs.Graph;

public class ExploreMutants {
	private Graph usegraph;
	private MutationStatistics<?> ms;
	private ArrayList<MutantTestAnalyzer> analyzeListeners = new ArrayList<MutantTestAnalyzer>();

	public ExploreMutants(MutationStatistics<?> ms, Graph usegraph) {
		this.ms = ms;
		this.usegraph = usegraph;
	}

	public void addMutantTestAnalyzeListener(MutantTestAnalyzer mta){
		this.analyzeListeners.add(mta);
	}

	public void removeMutantTestAnalyzeListener(MutantTestAnalyzer mta){
		this.analyzeListeners.remove(mta);
	}

	public void start(String[] allMutations/*, boolean reverseReadVariable*/) throws Exception {
		ProcessStatistics ps = ms.ps;
		//HashMap<String, UseGraph> cache = new HashMap<String, UseGraph>();
		//HashMap<String, Long> durations = new HashMap<String, Long>();

		// LOADING PHASE FINISHED !

		fireExecutionStarting();

		int cpt = 0;
		for(String mutation : allMutations){											// For each mutant...
			if(++cpt %50 == 0)
				System.out.println("\t"+(cpt)+" "+mutation);
			
			boolean forceStop = false;
			MutantIfos ifos = (MutantIfos) ms.mutations.get(mutation);

			// relevant IS list of tests impacted by the introduced bug (determined using mutation)
			String[] relevantArray = purifyFailAndHangResultSetForMutant(ps, ifos);

			if(relevantArray == null){
				continue;
			}

			UseGraph propaGraph = new UseGraph(usegraph);
			long duration = usegraph.visitDirectedByGraphNodeVisitor(propaGraph, ifos.mutationIn);

			// retrieved IS list of tests impacted by the introduced bug (determined use graph)
			String[] retrievedArray = getRetrievedTests(propaGraph, ps.testCases);

			forceStop = fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, duration);

			if(forceStop)
				break;
		}
		
		fireExecutionEnded();

	}

	public boolean fireIntersectionFound(ProcessStatistics ps, String mutation, MutantIfos ifos, String[] retrievedArray, UseGraph propaGraph, long propatime){
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireIntersectionFound(ps, mutation, ifos, retrievedArray, propaGraph, propatime);

			if(aListerner.forceStop()){
				return true;
			}
		}
		
		return false;
	}
	
	public void fireExecutionStarting(){
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionStarting();
		}
	}

	private void fireExecutionEnded() {
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			aListerner.fireExecutionEnded();
		}
	}
	

	/***
	 * This method return a list test impacted by the a bug inserted in this.mutations.get(mutationKey)
	 * @param bba
	 * @param mutationKey
	 * @return
	 */
	public static String[] getRetrievedTests(UseGraph basin, String[] tests){
		String[] bugs = basin.getBasinNodes();

		Set<String> retrieved = new HashSet<String>();

		for(String bug : bugs){																// We explore each bug determined using the basins technique
			for(String test : tests){														// We determine whether bug basin determined node is a test
				if(bug.indexOf(test) != -1)													// If so...
					retrieved.add(test);
				else if(bug.indexOf("test") != -1){
					// Nothing to do ? Those cases are reported as test but not returned as so by smf !
				}
			}
		}

		return retrieved.toArray(new String[retrieved.size()]);
	}

	public static String[] purifyResultSetForMutant(String[] mutantSet, String[] globalSet){
		Set<String> al = new HashSet<String>();
		Set<String> al2 = new HashSet<String>();

		for(String s : mutantSet){
			al.add(s);
		}

		for(String s : globalSet){
			al2.add(s);
		}

		al.removeAll(al2);

		return al.toArray(new String[0]); 
	}

	private static String[] includeTestSuiteGlobalFailingCases(ProcessStatistics ps, String[] testsuites, String[] include){
		Set<String> cases = new HashSet<String>();
		
		if(include != null){
			for(String s : include){
				cases.add(s);
			}
		}
		
		for(String ts : testsuites){
			for(String s : ps.testCases){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}
		
		return cases.toArray(new String[0]);
	}
	
	public static String[] purifyFailingResultSetForMutant(ProcessStatistics ps, MutantIfos mi){
		String[] mutset = includeTestSuiteGlobalFailingCases(ps, mi.mutantErrorOnTestSuite, mi.mutantFailingTestCases);
		String[] glbset = includeTestSuiteGlobalFailingCases(ps, ps.errorOnTestSuite, ps.failingTestCases);

		return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyIgnoredResultSetForMutant(ProcessStatistics ps, MutantIfos mi){
		String[] mutset = mi.mutantIgnoredTestCases;
		String[] glbset = ps.ignoredTestCases;
		
		return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyHangingResultSetForMutant(ProcessStatistics ps, MutantIfos mi){
		String[] mutset = mi.mutantHangingTestCases;
		String[] glbset = ps.hangingTestCases;
		
		return purifyResultSetForMutant(mutset, glbset);
	}

	public static String[] purifyFailAndHangResultSetForMutant(ProcessStatistics ps, MutantIfos mi){
		Set<String> cases = new HashSet<String>();
		
		for(String ts : mi.mutantErrorOnTestSuite){
			for(String s : ps.testCases){
				if(s.startsWith(ts)){
					cases.add(s);
				}
			}
		}

		for(String s:mi.mutantHangingTestCases){
			cases.add(s);
		}

		for(String s:mi.mutantFailingTestCases){
			cases.add(s);
		}
		
		for(String s : mi.mutantErrorOnTestSuite){
			for(String ss : ps.testCases){
				if(ss.startsWith(s)){
					cases.add(ss);
				}
			}
		}

		return purifyResultSetForMutant(cases.toArray(new String[0]), ps.getUnmutatedFailAndHang());
	}
}
