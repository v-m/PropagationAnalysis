package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.exceptions.PersistenceException;

/**
 * This class is responsible to persist to xml file the mutation project
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see ProcessStatistics
 */
public class ProcessXMLPersistence extends ExecutionPersistence<ProcessStatistics>{
	File f;
	private static String ROOT_ELEMENT_0 = "smf";

	// CONFIG START
	protected static String CONFIG_ELEMENT_1 = "config";
	protected static String SKIP_MVN_CLASS_2 = "skip-mvn-cp";			// CONFIG
	
	protected static String GLOBAL_ELEMENT_1 = "global";
	protected static String HANGTIMEOUT_2 = "testhang-timeout";
	protected static String PROJECT_IN_2 = "project-root";
	protected static String ORIGINAL_PROJECT_IN_2 = "originalproject-root";
	protected static String PROJECT_NAME_2 = "project-name";
	protected static String CURRENT_STATE_2 = "state";					// RUN
	protected static String CLASSPATH_2 = "classpath";					// RUN
	protected static String COPY_CP_2 = "local";					// RUN
	protected static String CLASSPATH_3 = "path";						// RUN

	protected static String SOURCE_ELEMENT_1 = "source";
	protected static String PROJECT_OUT_2 = "bytecode";				// SOURCE
	protected static String SRC_TO_COMPILE_2 = "include";				// SOURCE
	protected static String SRC_TO_COMPILE_3 = "path";				// SOURCE

	protected static String TESTS_ELEMENT_1 = "tests";
	protected static String TEST_OUT_2 = "bytecode";					// TESTS
	protected static String SRC_TO_TREAT_2 = "include";				// TESTS
	protected static String SRC_TO_TREAT_3 = "path";					// TESTS
	protected static String TESTS_RESSOURCES_2 = "ressources";		// TESTS
	protected static String TESTS_RESSOURCES_3 = "path";				// TESTS


	protected static String MUTATION_ELEMENT_1 = "mutation";
	protected static String MUTATION_BASEDIR = "basedir";
	protected static String MUTANTS_OUT_2 = "source";					// MUTATION
	protected static String MUTANTS_BYTECODE_2 = "bytecode";			// MUTATION
	protected static String MUTANTS_EXEC_2 = "executions";				// MUTATION
	// CONFIG END

	// TESTS START
	protected static String TESTS_1 = "tests";
	protected static String TESTSCLASSES_2 = "classes";				// TESTS
	protected static String TESTSCLASSES_3 = "class";					// TESTS
	protected static String TESTSCASES_2 = "cases";					// TESTS
	protected static String TESTSCASES_3 = "case";					// TESTS
	// TESTS END

	// TEST EXEC START
	protected static String TEST_EXEC_1 = "original-execution";

	protected static String FAILING_TC_3 = "failing";
	protected static String IGNORED_TC_3 = "ignored";
	protected static String HANGING_TC_3 = "hanging";
	protected static String ONE_TC_4 = "case";
	protected static String ONE_TS_4 = "suite";
	
	private static final String STACKTRACES_3 = "stacktraces";
	private static final String STACKTRACE_4 = "stacktrace";
	private static final String ONE_ST_5 = "trace";

	// TEST EXEC END

	protected static String TIME_ATTRIBUTE = "time";

	public ProcessXMLPersistence(File f) {
		this.f = f;
	}
	
	/**
	 * This method saves the instance of a ProcessStatistics object (XML)
	 * @throws IOException
	 */
	@Override
	public void saveState(ProcessStatistics ps) throws PersistenceException {
		File ffinal;
		
		if(f.isDirectory()){
			ffinal = new File(f, ps.getPersistFile(false));
		}else{
			ffinal = f;
		}

		if(ffinal.exists())
			ffinal.delete();

		try {
			f.createNewFile();
		} catch (IOException e1) {
			throw new PersistenceException(e1);
		}

		Element root = new Element(ROOT_ELEMENT_0);
		Document document = new Document(root);
		Comment c = new Comment("\nThis is an execution file generated with SMF.\n");
		document.getContent().add(0, c);

		Element config = new Element(CONFIG_ELEMENT_1);
		root.addContent(config);

		Element run = new Element(GLOBAL_ELEMENT_1);
		config.addContent(run);

		Element source = new Element(SOURCE_ELEMENT_1);
		config.addContent(source);

		Element tests = new Element(TESTS_ELEMENT_1);
		config.addContent(tests);

		Element mutation = new Element(MUTATION_ELEMENT_1);
		config.addContent(mutation);

		Element tmp;

		if(ps.getProjectName() != null){
			tmp = new Element(PROJECT_NAME_2);
			tmp.setText(ps.getProjectName());
			run.addContent(tmp);
		}

		if(ps.getTestTimeOut() > 0){
			tmp = new Element(HANGTIMEOUT_2);
			tmp.setText(Integer.toString(ps.getTestTimeOut()));
			run.addContent(tmp);
		}

		if(ps.getProjectIn(false) != null){
			tmp = new Element(PROJECT_IN_2);
			tmp.setText(ps.getProjectIn(false));
			run.addContent(tmp);
		}

		if(ps.getOriginalSrc() != null){
			tmp = new Element(ORIGINAL_PROJECT_IN_2);
			tmp.setText(ps.getOriginalSrc());
			run.addContent(tmp);
		}

		tmp = new Element(CLASSPATH_2);
		run.addContent(tmp);
		if(ps.getOriginalClasspath() != null)
			populateXml(tmp, CLASSPATH_3, ps.getOriginalClasspath());

		if(ps.getCpLocalFolder() != null){
			Attribute a = new Attribute(COPY_CP_2, ps.getCpLocalFolder());
			tmp.setAttribute(a);
		}
		
		if(ps.getProjectOut(false) != null){
			tmp = new Element(PROJECT_OUT_2);
			tmp.setText(ps.getProjectOut(false));
			if(ps.getBuildProjectTime() != null){
				tmp.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.getBuildProjectTime())));
			}
			source.addContent(tmp);
		}


		if(ps.getTestsOut(false) != null){
			tmp = new Element(TEST_OUT_2);
			tmp.setText(ps.getTestsOut(false));
			if(ps.getBuildTestsTime() != null){
				tmp.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.getBuildTestsTime())));
			}
			tests.addContent(tmp);
		}

		Attribute a = new Attribute(SKIP_MVN_CLASS_2, ps.isSkipMvnClassDetermination()?"true":"false");
		config.setAttribute(a);

		tmp = new Element(CURRENT_STATE_2);
		tmp.setText(ProcessStatistics.fromStateToString(ps.getCurrentState()));
		run.addContent(tmp);

		/***** ARRAYS *****/
		if(ps.getSrcToCompile(false) != null){
			tmp = new Element(SRC_TO_COMPILE_2);
			source.addContent(tmp);
			populateXml(tmp, SRC_TO_COMPILE_3, ps.getSrcToCompile(false));
		}

		if(ps.getSrcTestsToTreat(false) != null){
			tmp = new Element(SRC_TO_TREAT_2);
			tests.addContent(tmp);
			populateXml(tmp, SRC_TO_TREAT_3, ps.getSrcTestsToTreat(false));
		}

		if(ps.getTestingRessources(false) != null){
			tmp = new Element(TESTS_RESSOURCES_2);
			tests.addContent(tmp);
			populateXml(tmp, TESTS_RESSOURCES_3, ps.getTestingRessources(false));
		}

		a = new Attribute(MUTATION_BASEDIR, ps.getMutantsBasedir());
		mutation.setAttribute(a);

		if(ps.getMutantsOut() != null){
			tmp = new Element(MUTANTS_OUT_2);
			tmp.setText(ps.getMutantsOut());
			mutation.addContent(tmp);
		}
		if(ps.getMutantsBytecodeOut() != null){
			tmp = new Element(MUTANTS_BYTECODE_2);
			tmp.setText(ps.getMutantsBytecodeOut());
			mutation.addContent(tmp);
		}
		if(ps.getMutantsTestResults() != null){
			tmp = new Element(MUTANTS_EXEC_2);
			tmp.setText(ps.getMutantsTestResults());
			mutation.addContent(tmp);
		}


		if(ps.getTestClasses() != null || ps.getTestCases() != null){
			Element tt = new Element(TESTS_1);
			root.addContent(tt);

			if(ps.getTestClasses() != null){
				Element testclasses = new Element(TESTSCLASSES_2);
				tt.addContent(testclasses);
				populateXml(testclasses, TESTSCLASSES_3, ps.getTestClasses());
			}

			if(ps.getTestCases() != null){
				Element testcases = new Element(TESTSCASES_2);
				tt.addContent(testcases);
				populateXml(testcases, TESTSCASES_3, ps.getTestCases());
			}
		}


		if(ps.getFailingTestCases() != null || ps.getIgnoredTestCases() != null || ps.getHangingTestCases() != null){
			Element testexec = new Element(TEST_EXEC_1);
			root.addContent(testexec);

			if(ps.getRunTestsOriginalTime() != null){
				testexec.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.getRunTestsOriginalTime())));
			}

			Element fail = new Element(FAILING_TC_3);;

			if(ps.getErrorOnTestSuite() != null){
				populateXml(fail, ONE_TS_4, ps.getErrorOnTestSuite());
			}

			if(ps.getFailingTestCases() != null){ 
				populateXml(fail, ONE_TC_4, ps.getFailingTestCases());
			}
			
			testexec.addContent(fail);

			if(ps.getIgnoredTestCases() != null){
				Element e = new Element(IGNORED_TC_3);
				testexec.addContent(e);
				populateXml(e, ONE_TC_4, ps.getIgnoredTestCases());
			}

			if(ps.getHangingTestCases() != null){
				Element e = new Element(HANGING_TC_3);
				testexec.addContent(e);
				populateXml(e, ONE_TC_4, ps.getHangingTestCases());
			}
			
			if(ps.getStackTraces() != null){
				Element e = new Element(STACKTRACES_3);
				testexec.addContent(e);
				
				for(String[] st : ps.getStackTraces()){
					Element ee = new Element(STACKTRACE_4);
					populateXml(ee, ONE_ST_5, st);
				}
			}
		}

		Format format = Format.getPrettyFormat();
		format.setLineSeparator(LineSeparator.UNIX);
		XMLOutputter output = new XMLOutputter(format);
		
		try {
			output.output(document, new FileOutputStream(ffinal));
		} catch (Exception e) {
			throw new PersistenceException(e);
		}
	}

	protected static void populateXml(Element parent, String elementsName, String[] data){
		if(data == null)
			return;

		for(String s: data){
			Element e = new Element(elementsName);
			e.setText(s);
			parent.addContent(e);
		}
	}

	@Override
	public ProcessStatistics loadState() throws PersistenceException {

		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			try {
				document = sxb.build(this.f);
			} catch (IOException e) {
				throw new PersistenceException(e);
			}
		} catch (JDOMException e1) {
			e1.printStackTrace();
			return null;
		}

		Element root = document.getRootElement();

		/************************************/

		Element config = root.getChild(CONFIG_ELEMENT_1);
		Element global = config.getChild(GLOBAL_ELEMENT_1);
		
		String datasetRepository = global.getChild(PROJECT_IN_2).getText();
		//String workingDir = global.getChild(WORKING_DIR_2).getText();
		ProcessStatistics ps = ProcessStatistics.rawCreateProject(datasetRepository, this.f.getParentFile().getAbsolutePath());
		ps.setSkipMvnClassDetermination(config.getAttribute(SKIP_MVN_CLASS_2).getValue().equals("true")?true:false);

		Element tmp;
		List<Element> tmplist;
		
		if((tmp = global.getChild(ORIGINAL_PROJECT_IN_2)) != null)
			ps.setOriginalSrc(tmp.getText());

		if((tmp = global.getChild(PROJECT_NAME_2)) != null)
			ps.setProjectName(tmp.getText());

		if((tmp = global.getChild(CURRENT_STATE_2)) != null)
			ps.setCurrentState(ProcessStatistics.fromStringToState(tmp.getText()));

		if((tmp = global.getChild(CLASSPATH_2)) != null){
			tmplist = tmp.getChildren(CLASSPATH_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.setOriginalClasspath(al.toArray(new String[0]));
			
			if(tmp.getAttribute(COPY_CP_2) != null){
				ps.setCpLocalFolder(tmp.getAttribute(COPY_CP_2).getValue()); 
			}
		}
		
		Element source = config.getChild(SOURCE_ELEMENT_1);

		if((tmp = source.getChild(PROJECT_OUT_2)) != null){
			ps.setProjectOut(tmp.getText());
			if(tmp.getAttribute(TIME_ATTRIBUTE) != null){
				ps.setBuildProjectTime(Long.valueOf(tmp.getAttribute(TIME_ATTRIBUTE).getValue()));
			}
		}

		if((tmp = source.getChild(SOURCE_ELEMENT_1)) != null)
			ps.setProjectOut(tmp.getText());

		if((tmp = source.getChild(SRC_TO_COMPILE_2)) != null){
			tmplist = tmp.getChildren(SRC_TO_COMPILE_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.setSrcToCompile(al.toArray(new String[0]));
		}



		Element tests = config.getChild(TESTS_ELEMENT_1);

		if((tmp = tests.getChild(TEST_OUT_2)) != null){
			ps.setTestsOut(tmp.getText());
			if(tmp.getAttribute(TIME_ATTRIBUTE) != null){
				ps.setBuildTestsTime(Long.valueOf(tmp.getAttribute(TIME_ATTRIBUTE).getValue()));
			}
		}

		if((tmp = tests.getChild(SRC_TO_TREAT_2)) != null){
			tmplist = tmp.getChildren(SRC_TO_TREAT_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.setSrcTestsToTreat(al.toArray(new String[0]));
		}

		if((tmp = tests.getChild(TESTS_RESSOURCES_2)) != null){
			tmplist = tmp.getChildren(TESTS_RESSOURCES_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.setTestingRessources(al.toArray(new String[0]));
		}



		Element mutation = config.getChild(MUTATION_ELEMENT_1);

		ps.setMutantsBasedir(mutation.getAttribute(MUTATION_BASEDIR).getValue());
		
		if((tmp = mutation.getChild(MUTANTS_OUT_2)) != null)
			ps.setMutantsOut(tmp.getText());

		if((tmp = mutation.getChild(MUTANTS_BYTECODE_2)) != null)
			ps.setMutantsBytecodeOut(tmp.getText());
		
		if((tmp = mutation.getChild(MUTANTS_EXEC_2)) != null)
			ps.setMutantsTestResults(tmp.getText());


		/************************************/

		tests = root.getChild(TESTS_1);
		if(tests != null){
			if((tmp = tests.getChild(TESTSCLASSES_2)) != null){
				tmplist = tmp.getChildren(TESTSCLASSES_3);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.setTestClasses(al.toArray(new String[0]));
			}

			if((tmp = tests.getChild(TESTSCASES_2)) != null){
				tmplist = tmp.getChildren(TESTSCASES_3);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.setTestCases(al.toArray(new String[0]));
			}
		}

		/************************************/

		int timeout = -1;
		if((tmp = global.getChild(HANGTIMEOUT_2)) != null){
			timeout = Integer.parseInt(tmp.getText());
		}
		
		Element original = root.getChild(TEST_EXEC_1);

		if(original != null){
			TestsExecutionIfos tei = new TestsExecutionIfos();
			if(original.getAttribute(TIME_ATTRIBUTE) != null){
				ps.setRunTestsOriginalTime(Long.valueOf(original.getAttribute(TIME_ATTRIBUTE).getValue()));
			}

			if((tmp = original.getChild(FAILING_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				tei.setFailingTestCases(al.toArray(new String[0]));
				
				tmplist = tmp.getChildren(ONE_TS_4);

				al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				tei.setErrorOnTestSuite(al.toArray(new String[0]));
			}

			if((tmp = original.getChild(IGNORED_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				tei.setIgnoredTestCases(al.toArray(new String[0]));
			}

			if((tmp = original.getChild(HANGING_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				tei.setHangingTestCases(al.toArray(new String[0]));
			}
			
			if(timeout >= 0){
				tei.setTestTimeOut(timeout);
			}
			
			ps.setTestExecutionResult(tei);
		}
		
		return ps;
	}

	@Override
	public void loadState(ProcessStatistics updateMe) throws PersistenceException {
		throw new PersistenceException(new Exception("Not implemented yet"));
	}
}
