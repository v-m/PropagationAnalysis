package com.vmusco.pminer.analyze;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantExecutionIfos;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.utils.MutationsSetTools;
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

	public void start(String[] allMutations) throws Exception {
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();

		// LOADING PHASE FINISHED !

		fireExecutionStarting();

		int cpt = 0;
		for(String mutation : allMutations){											// For each mutant...
			if(++cpt %50 == 0)
				System.out.println("\t"+(cpt)+" "+mutation);

			boolean forceStop = false;
			MutantIfos ifos = ms.getMutationStats(mutation);

			// relevant IS list of tests impacted by the introduced bug (determined using mutation)
			String[] relevantArray = ifos.getExecutedTestsResults().getCoherentMutantFailAndHangTestCases(ps);

			if(relevantArray == null){
				continue;
			}

			UseGraph propaGraph = new UseGraph(usegraph);
			long duration = usegraph.visitDirectedByGraphNodeVisitor(propaGraph, ifos.getMutationIn());

			forceStop = fireIntersectionFound(ps, ifos, propaGraph, duration);

			if(forceStop)
				break;
		}

		fireExecutionEnded();

	}

	public boolean fireIntersectionFound(ProcessStatistics ps, MutantIfos ifos, UseGraph propaGraph, long duration) throws MutationNotRunException {
		for(MutantTestAnalyzer aListerner : this.analyzeListeners){
			if(aListerner instanceof MutationStatisticsCollecter){
				((MutationStatisticsCollecter) aListerner).declareNewTime(duration);
			}
			
			aListerner.fireIntersectionFound(ps, ifos, propaGraph);

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
				if(ProcessStatistics.areTestsEquivalents(bug, test)){						// If so...
					retrieved.add(test);
					//}else if(bug.indexOf("test") != -1){
					// Nothing to do ? Those cases are reported as test but not returned as so by smf !
				}
			}
		}

		return retrieved.toArray(new String[retrieved.size()]);
	}
}
