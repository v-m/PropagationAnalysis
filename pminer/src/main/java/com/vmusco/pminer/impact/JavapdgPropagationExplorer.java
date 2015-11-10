package com.vmusco.pminer.impact;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmusco.pminer.exceptions.SpecialEntryPointException;
import com.vmusco.pminer.exceptions.SpecialEntryPointException.TYPE;
import com.vmusco.softminer.graphs.Graph;
import com.vmusco.softminer.graphs.Graph.GraphApi;

/**
 * Using a JavaPDG database
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class JavapdgPropagationExplorer extends ConsequencesExplorer {
	private Map<Long, JavaPDGTriplet> dbentries;
	private Map<Long, String> testsMapped = null;
	private	Map<String, Long> bugs;

	public JavapdgPropagationExplorer(String dbpath) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		super(Graph.getNewGraph(GraphApi.GRAPH_STREAM));

		// GETTING METHODS ENTRIES FROM DATABASE
		dbentries = getMethodsFromDerby(dbpath);
		bugs = new HashMap<String, Long>();

		//BUILDING NUMERICAL GRAPH FROM DATABASE
		populateWithNumericalCallGraphFromDerby(dbpath);
		//System.out.println(" Has "+base.getNbNodes()+" nodes and "+base.getNbEdges()+" edges.");
	}
	
	public JavapdgPropagationExplorer(String archivepath, String dbname) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		this("jar:("+archivepath+")"+dbname);
	}

	public void visit(String id) throws SpecialEntryPointException {
		visit(new String[]{ id });
	}
	
	/**
	 * Takes into consideration only the fist node for propagation estimation
	 */
	@Override
	public void visit(String[] ids) throws SpecialEntryPointException {
		String id = ids[0];
		Long node;

		if(!bugs.containsKey(id)){
			// Search the correspondence
			node = searchAdaptedMethod(id);
			bugs.put(id, node);
		}else{
			node = bugs.get(id);
		}

		if(node == null){
			throw new SpecialEntryPointException(TYPE.NOT_FOUND);
		}

		base.visitTo(populateNew(), Long.toString(node));
	}

	/**
	 * This function returns the numerals nodes only !!!
	 * TODO: Propose an alternative writing type?
	 */
	@Override
	public String[] getLastConsequenceNodes() {
		return getLastConcequenceGraph().getNodesNames();
	}

	@Override
	public String[] getLastConsequenceNodesIn(String[] tests) {
		if(this.testsMapped == null){
			getMappingsForTests(tests);
		}
		
		ArrayList<String> ret = new ArrayList<String>();

		for(String node : getLastConcequenceGraph().getNodesNames()){
			if(testsMapped.containsKey(Long.parseLong(node))){
				ret.add(testsMapped.get(Long.parseLong(node)));
			}
		}

		return (String[])ret.toArray(new String[0]);
	}

	private void getMappingsForTests(String[] tests) {
		testsMapped = new HashMap<>();

		for(String t : tests){
			if(!t.endsWith(")"))
				t = t+"()";

			if(!testsMapped.containsKey(t)){
				Long searchAdaptedMethod = searchAdaptedMethod(t);

				if(searchAdaptedMethod != null){
					//JavaPDGTriplet triplet = dbentries.get(searchAdaptedMethod);
					testsMapped.put(searchAdaptedMethod, t);
					//System.out.println("Find candidate for "+t+" :)");
				}else{
					//System.err.println("Unable to find candidate for "+t);
				}
			}
		}
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
	 * Try to find a possible matching between JVM signature and ours...
	 * If find ONE signature, return its ID, else return null
	 * @param dbentries
	 * @param method
	 * @return
	 */
	private Long searchAdaptedMethod(String method){
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

			if(q.getPackageName().equals(pkg)){
				if(q.getMethodName().equals(mth)){ 
					if(sameParameters(q.getParameters(), object_tmp)){
						candidated.add(l);
					}
				}
			}
		}

		if(candidated.size()==1)
			return candidated.get(0);

		return null;
	}

	private void populateWithNumericalCallGraphFromDerby(String dbpath) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Connection conn = dbconnect(dbpath);
		Statement createStatement = conn.createStatement();
		createStatement.execute("SELECT SRC, TAR FROM CG");
		ResultSet resultSet = createStatement.getResultSet();

		while(resultSet.next()){
			long src = resultSet.getLong("SRC");
			long dst = resultSet.getLong("TAR");
			base.addDirectedEdgeAndNodeIfNeeded(Long.toString(src), Long.toString(dst));
		}
	}


	private Map<Long, JavaPDGTriplet> getMethodsFromDerby(String dbpath) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		Map<Long, JavaPDGTriplet> dbentries = new HashMap<>();
		Connection conn = dbconnect(dbpath);

		Statement createStatement = conn.createStatement();
		createStatement.execute("SELECT METHODID, METHODNAME, METHODSIGNATURE, METHODPACKAGENAME FROM METHOD");
		ResultSet resultSet = createStatement.getResultSet();

		while(resultSet.next()){
			JavaPDGTriplet q = new JavaPDGTriplet();

			q.setMethod(resultSet.getString("METHODNAME"));
			q.setSignature(resultSet.getString("METHODSIGNATURE"));
			q.setPackage(resultSet.getString("METHODPACKAGENAME"));

			dbentries.put(resultSet.getLong("METHODID"), q);
		}

		return dbentries;
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

}
