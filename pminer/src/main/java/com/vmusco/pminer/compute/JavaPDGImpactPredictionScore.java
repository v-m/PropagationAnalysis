package com.vmusco.pminer.compute;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmusco.pminer.analyze.PRFStatistics;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.MutationNotRunException;
import com.vmusco.smf.exceptions.PersistenceException;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.GraphNodeVisitor;
import com.vmusco.softminer.graphs.Graph.GraphApi;

public abstract class JavaPDGImpactPredictionScore {

	public static PRFStatistics runOverMutants(String dbpath, MutationStatistics<?> ms, ImpactPredictionListener ipl) throws MutationNotRunException, PersistenceException, InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		PRFStatistics prf = new PRFStatistics();

		System.out.println("GETTING METHODS ENTRIES FROM DATABASE");
		Map<Long, JavaPDGTriplet> dbentries = getMethodsFromDerby(dbpath);
		System.out.println(" Has "+dbentries.size()+" methods");

		final Map<Long, String> tests = getMappingsForTests(ms, dbentries);
		Map<String, Long> bugs = getMappingsForBugInsertionPoints(ms, dbentries);

		System.out.println("\nBUILDING NUMERICAL GRAPH FROM DATABASE");
		Graph g = getNumericalCallGraphFromDerby(dbpath);
		System.out.println(" Has "+g.getNbNodes()+" nodes and "+g.getNbEdges()+" edges.");

		System.out.println("\nESTIMATING PROPAGATION FOR MUTATION");

		for(String mutant : ms.listViableAndRunnedMutants(true)){
			MutantIfos mi = ms.getMutationStats(mutant);
			
			ipl.fireOneMutantStarting(mutant, mi);
			
			final List<String> impactedAccordingToJavaPdg = new ArrayList<String>();
			g.visitTo(new GraphNodeVisitor() {

				@Override
				public void visitNode(String node) {
					if(tests.containsKey(Long.parseLong(node))){
						ipl.fireTestIntersection(tests.get(Long.parseLong(node))+" ("+node+")");
						impactedAccordingToJavaPdg.add(tests.get(Long.parseLong(node)));
					}
				}

				@Override
				public void visitEdge(String from, String to) { }

				@Override
				public String[] nextNodesToVisitFrom(String node) {
					return null;
				}

			}, Long.toString(bugs.get(mi.getMutationIn())));

			String[] ais = mi.getExecutedTestsResults().getMutantFailingAndHangingTestCases();
			String[] cis = impactedAccordingToJavaPdg.toArray(new String[0]);
			prf.cumulate(ais, cis);
			ipl.fireOneMutantResults(PRFStatistics.precision(ais, cis), 
					PRFStatistics.recall(ais, cis), 
					PRFStatistics.fscore(ais, cis));
		}

		return prf;
	}

	/**
	 * Check if parameters in params1 and params2 are identicals
	 * @param params1
	 * @param params2
	 * @return
	 */
	public static boolean sameParameters(String[] params1, String[] params2) {
		if(params1.length != params2.length)
			return false;

		for(int i=0; i<params1.length; i++){
			if(!params1[i].equals(params2[i]))
				return false;
		}

		return true;
	}

	/**
	 * Establish a connection with the JavaPDG Derby database
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private static Connection dbconnect(String path) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
		String driver = "org.apache.derby.jdbc.EmbeddedDriver";
		Class.forName(driver).newInstance();
		String protocol = "jdbc:derby:";
		Connection conn = DriverManager.getConnection(protocol + path+";create=false");
		conn.setSchema("JAVAPDG");

		return conn;
	}

	private static Map<Long, JavaPDGTriplet> getMethodsFromDerby(String dbpath) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Map<Long, JavaPDGTriplet> dbentries = new HashMap<>();
		Connection conn = dbconnect(dbpath);

		Statement createStatement = conn.createStatement();
		createStatement.execute("SELECT METHODID, METHODNAME, METHODSIGNATURE, METHODPACKAGENAME FROM METHOD");
		ResultSet resultSet = createStatement.getResultSet();

		while(resultSet.next()){
			JavaPDGTriplet q = new JavaPDGTriplet();

			q.method = resultSet.getString("METHODNAME");
			q.signature = resultSet.getString("METHODSIGNATURE");
			q.packg = resultSet.getString("METHODPACKAGENAME");
			dbentries.put(resultSet.getLong("METHODID"), q);
		}

		return dbentries;
	}

	private static Graph getNumericalCallGraphFromDerby(String dbpath) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Graph g = Graph.getNewGraph(GraphApi.GRAPH_STREAM);

		Connection conn = dbconnect(dbpath);
		Statement createStatement = conn.createStatement();
		createStatement.execute("SELECT SRC, TAR FROM CG");
		ResultSet resultSet = createStatement.getResultSet();

		while(resultSet.next()){
			long src = resultSet.getLong("SRC");
			long dst = resultSet.getLong("TAR");
			g.addDirectedEdgeAndNodeIfNeeded(Long.toString(src), Long.toString(dst));
		}

		return g;
	}

	private static Map<Long, String> getMappingsForTests(MutationStatistics<?> ms, Map<Long, JavaPDGTriplet> dbentries) throws PersistenceException{
		Map<Long, String> equiv = new HashMap<>();
		ProcessStatistics ps = ms.getRelatedProcessStatisticsObject();

		//System.out.println("Mapping tests");
		//System.out.println("*************");

		for(String t : ps.getTestCases()){
			if(!t.endsWith(")"))
				t = t+"()";

			if(!equiv.containsKey(t)){
				Long searchAdaptedMethod = searchAdaptedMethod(dbentries, t);

				if(searchAdaptedMethod != null){
					JavaPDGTriplet triplet = dbentries.get(searchAdaptedMethod);
					//System.out.println(searchAdaptedMethod+" <= "+t);
					equiv.put(searchAdaptedMethod, t);

				}else{
					System.err.println("Unable to find candidate for "+t);
				}
			}
		}

		return equiv;
	}

	private static Map<String, Long> getMappingsForBugInsertionPoints(MutationStatistics<?> ms, Map<Long, JavaPDGTriplet> dbentries) throws PersistenceException{
		Map<String, Long> equiv = new HashMap<>();

		//System.out.println("Mapping mutation entry points");
		//System.out.println("*****************************");

		for(String m : ms.listViableAndRunnedMutants(true)){
			MutantIfos mi = ms.getMutationStats(m);

			if(!equiv.containsKey(mi.getMutationIn())){
				Long searchAdaptedMethod = searchAdaptedMethod(dbentries, mi.getMutationIn());

				if(searchAdaptedMethod != null){
					JavaPDGTriplet triplet = dbentries.get(searchAdaptedMethod);
					//System.out.println(searchAdaptedMethod+" <= "+mi.getMutationIn());
					equiv.put(mi.getMutationIn(), searchAdaptedMethod);

				}else{
					System.err.println("Unable to find candidate for "+mi.getMutationIn());
				}
			}
		}

		return equiv;
	}


	/**
	 * Try to find a possible matching between JVM signature and ours...
	 * If find ONE signature, return its ID, else return null
	 * @param dbentries
	 * @param method
	 * @return
	 */
	private static Long searchAdaptedMethod(Map<Long, JavaPDGTriplet> dbentries, String method){
		List<Long> candidated = new ArrayList<>();
		String pkg = method.substring(0, method.lastIndexOf('('));
		String mth = pkg.substring(pkg.lastIndexOf(".") + 1);

		pkg = pkg.substring(0, pkg.lastIndexOf("."));
		String params = method.substring(method.lastIndexOf('(') + 1, method.lastIndexOf(')') );

		for(Long l : dbentries.keySet()){
			JavaPDGTriplet q = dbentries.get(l);

			String[] object_tmp = params.split(",");

			for(int i = 0; i < object_tmp.length; i++){
				if(object_tmp[i].length() == 1)
					object_tmp[i] = "java.lang.Object";
			}

			if(q.getPackageName().equals(pkg) && 
					q.getMethodName().equals(mth) && 
					sameParameters(q.getParameters(), object_tmp)){
				candidated.add(l);
			}
		}

		if(candidated.size()==1)
			return candidated.get(0);

		return null;
	}
}
