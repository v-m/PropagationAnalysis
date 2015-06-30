package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ProcessXMLPersistence extends ExecutionPersistence<ProcessStatistics>{
	private static String ROOT_ELEMENT_0 = "smf";

	// CONFIG START
	protected static String CONFIG_ELEMENT_1 = "config";
	protected static String SKIP_MVN_CLASS_2 = "skip-mvn-cp";			// CONFIG
	
	protected static String GLOBAL_ELEMENT_1 = "global";
	protected static String PERSISTENCE_FILE_2 = "persistance-file";
	protected static String HANGTIMEOUT_2 = "testhang-timeout";
	protected static String PROJECT_IN_2 = "project-root";
	protected static String ORIGINAL_PROJECT_IN_2 = "originalproject-root";
	protected static String PROJECT_NAME_2 = "project-name";
	//protected static String WORKING_DIR_2 = "working-directory";		// RUN
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
	

	// TEST EXEC END

	protected static String TIME_ATTRIBUTE = "time";

	public ProcessXMLPersistence(File f) {
		super(f);
	}
	
	/**
	 * This method saves the instance of a ProcessStatistics object (XML)
	 * @throws IOException
	 */
	public void saveState(ProcessStatistics ps) throws IOException {

		if(!ps.isPersistanceEnabled()){
		}

		File ffinal;
		
		if(f.isDirectory()){
			ffinal = new File(f, ps.persistFile);
		}else{
			ps.persistFile = f.getName();
			ffinal = f;
		}

		if(ffinal.exists())
			ffinal.delete();

		f.createNewFile();

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

		if(ps.projectName != null){
			tmp = new Element(PROJECT_NAME_2);
			tmp.setText(ps.projectName);
			run.addContent(tmp);
		}

		if(ps.persistFile != null){
			tmp = new Element(PERSISTENCE_FILE_2);
			tmp.setText(ps.persistFile);
			run.addContent(tmp);
		}
		
		if(ps.testTimeOut > 0){
			tmp = new Element(HANGTIMEOUT_2);
			tmp.setText(Integer.toString(ps.testTimeOut));
			run.addContent(tmp);
		}

		if(ps.getProjectIn(false) != null){
			tmp = new Element(PROJECT_IN_2);
			tmp.setText(ps.getProjectIn(false));
			run.addContent(tmp);
		}

		if(ps.originalSrc != null){
			tmp = new Element(ORIGINAL_PROJECT_IN_2);
			tmp.setText(ps.originalSrc);
			run.addContent(tmp);
		}

		/*if(ps.workingDir != null){
			tmp = new Element(WORKING_DIR_2);
			tmp.setText(ps.workingDir);
			run.addContent(tmp);
		}*/

		tmp = new Element(CLASSPATH_2);
		run.addContent(tmp);
		if(ps.getOriginalClasspath() != null)
			populateXml(tmp, CLASSPATH_3, ps.getOriginalClasspath());

		if(ps.cpLocalFolder != null){
			Attribute a = new Attribute(COPY_CP_2, ps.cpLocalFolder);
			tmp.setAttribute(a);
		}
		
		if(ps.projectOut != null){
			tmp = new Element(PROJECT_OUT_2);
			tmp.setText(ps.projectOut);
			if(ps.buildProjectTime != null){
				tmp.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.buildProjectTime)));
			}
			source.addContent(tmp);
		}


		if(ps.testsOut != null){
			tmp = new Element(TEST_OUT_2);
			tmp.setText(ps.testsOut);
			if(ps.buildTestsTime != null){
				tmp.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.buildTestsTime)));
			}
			tests.addContent(tmp);
		}

		Attribute a = new Attribute(SKIP_MVN_CLASS_2, ps.skipMvnClassDetermination?"true":"false");
		config.setAttribute(a);

		tmp = new Element(CURRENT_STATE_2);
		tmp.setText(ProcessStatistics.fromStateToString(ps.currentState));
		run.addContent(tmp);

		/***** ARRAYS *****/
		if(ps.srcToCompile != null){
			tmp = new Element(SRC_TO_COMPILE_2);
			source.addContent(tmp);
			populateXml(tmp, SRC_TO_COMPILE_3, ps.srcToCompile);
		}

		if(ps.srcTestsToTreat != null){
			tmp = new Element(SRC_TO_TREAT_2);
			tests.addContent(tmp);
			populateXml(tmp, SRC_TO_TREAT_3, ps.srcTestsToTreat);
		}

		if(ps.testingRessources != null){
			tmp = new Element(TESTS_RESSOURCES_2);
			tests.addContent(tmp);
			populateXml(tmp, TESTS_RESSOURCES_3, ps.testingRessources);
		}

		a = new Attribute(MUTATION_BASEDIR, ps.mutantsBasedir);
		mutation.setAttribute(a);

		if(ps.mutantsOut != null){
			tmp = new Element(MUTANTS_OUT_2);
			tmp.setText(ps.mutantsOut);
			mutation.addContent(tmp);
		}
		if(ps.mutantsBytecodeOut != null){
			tmp = new Element(MUTANTS_BYTECODE_2);
			tmp.setText(ps.mutantsBytecodeOut);
			mutation.addContent(tmp);
		}
		if(ps.mutantsTestResults != null){
			tmp = new Element(MUTANTS_EXEC_2);
			tmp.setText(ps.mutantsTestResults);
			mutation.addContent(tmp);
		}


		if(ps.testClasses != null || ps.testCases != null){
			Element tt = new Element(TESTS_1);
			root.addContent(tt);

			if(ps.testClasses != null){
				Element testclasses = new Element(TESTSCLASSES_2);
				tt.addContent(testclasses);
				populateXml(testclasses, TESTSCLASSES_3, ps.testClasses);
			}

			if(ps.testCases != null){
				Element testcases = new Element(TESTSCASES_2);
				tt.addContent(testcases);
				populateXml(testcases, TESTSCASES_3, ps.testCases);
			}
		}


		if(ps.failingTestCases != null || ps.ignoredTestCases != null || ps.hangingTestCases != null){
			Element testexec = new Element(TEST_EXEC_1);
			root.addContent(testexec);

			if(ps.runTestsOriginalTime != null){
				testexec.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.runTestsOriginalTime)));
			}

			Element fail = new Element(FAILING_TC_3);;

			if(ps.errorOnTestSuite != null){
				populateXml(fail, ONE_TS_4, ps.errorOnTestSuite);
			}

			if(ps.failingTestCases != null){ 
				populateXml(fail, ONE_TC_4, ps.failingTestCases);
			}
			
			testexec.addContent(fail);

			if(ps.ignoredTestCases != null){
				Element e = new Element(IGNORED_TC_3);
				testexec.addContent(e);
				populateXml(e, ONE_TC_4, ps.ignoredTestCases);
			}

			if(ps.hangingTestCases != null){
				Element e = new Element(HANGING_TC_3);
				testexec.addContent(e);
				populateXml(e, ONE_TC_4, ps.hangingTestCases);
			}
		}

		//FileOutputStream fos = new FileOutputStream(f);
		XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
		output.output(document, new FileOutputStream(ffinal));
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


	public ProcessStatistics loadState() throws IOException {

		SAXBuilder sxb = new SAXBuilder();
		Document document;
		try {
			document = sxb.build(this.f);
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
		ps.persistFile = f.getName();
		
		ps.skipMvnClassDetermination = config.getAttribute(SKIP_MVN_CLASS_2).getValue().equals("true")?true:false;

		Element tmp;
		List<Element> tmplist;

		if((tmp = global.getChild(PERSISTENCE_FILE_2)) != null)
			ps.persistFile = tmp.getText();

		if((tmp = global.getChild(HANGTIMEOUT_2)) != null){
			ps.testTimeOut = Integer.parseInt(tmp.getText());
			ps.testTimeOut_auto = false;
		}
		
		if((tmp = global.getChild(ORIGINAL_PROJECT_IN_2)) != null)
			ps.originalSrc = tmp.getText();

		if((tmp = global.getChild(PROJECT_NAME_2)) != null)
			ps.projectName = tmp.getText();

		if((tmp = global.getChild(CURRENT_STATE_2)) != null)
			ps.currentState = ProcessStatistics.fromStringToState(tmp.getText());

		if((tmp = global.getChild(CLASSPATH_2)) != null){
			tmplist = tmp.getChildren(CLASSPATH_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.setOriginalClasspath(al.toArray(new String[0]));
			
			if(tmp.getAttribute(COPY_CP_2) != null){
				ps.cpLocalFolder = tmp.getAttribute(COPY_CP_2).getValue(); 
			}
		}
		
		Element source = config.getChild(SOURCE_ELEMENT_1);

		if((tmp = source.getChild(PROJECT_OUT_2)) != null){
			ps.projectOut = tmp.getText();
			if(tmp.getAttribute(TIME_ATTRIBUTE) != null){
				ps.buildProjectTime = Long.valueOf(tmp.getAttribute(TIME_ATTRIBUTE).getValue());
			}
		}

		if((tmp = source.getChild(SOURCE_ELEMENT_1)) != null)
			ps.projectOut = tmp.getText();

		if((tmp = source.getChild(SRC_TO_COMPILE_2)) != null){
			tmplist = tmp.getChildren(SRC_TO_COMPILE_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.srcToCompile = al.toArray(new String[0]);
		}



		Element tests = config.getChild(TESTS_ELEMENT_1);

		if((tmp = tests.getChild(TEST_OUT_2)) != null){
			ps.testsOut = tmp.getText();
			if(tmp.getAttribute(TIME_ATTRIBUTE) != null){
				ps.buildTestsTime = Long.valueOf(tmp.getAttribute(TIME_ATTRIBUTE).getValue());
			}
		}

		if((tmp = tests.getChild(SRC_TO_TREAT_2)) != null){
			tmplist = tmp.getChildren(SRC_TO_TREAT_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.srcTestsToTreat = al.toArray(new String[0]);
		}

		/*if((tmp = tests.getChild(TEST_CLASSPATH_2)) != null){
			tmplist = tmp.getChildren(TEST_CLASSPATH_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.tests_classpath = al.toArray(new String[0]);
		}*/

		if((tmp = tests.getChild(TESTS_RESSOURCES_2)) != null){
			tmplist = tmp.getChildren(TESTS_RESSOURCES_3);

			ArrayList<String> al = new ArrayList<String>();
			for(Element e: tmplist){
				al.add(e.getText());
			}
			ps.testingRessources = al.toArray(new String[0]);
		}



		Element mutation = config.getChild(MUTATION_ELEMENT_1);

		ps.mutantsBasedir = mutation.getAttribute(MUTATION_BASEDIR).getValue();
		
		if((tmp = mutation.getChild(MUTANTS_OUT_2)) != null)
			ps.mutantsOut = tmp.getText();

		if((tmp = mutation.getChild(MUTANTS_BYTECODE_2)) != null)
			ps.mutantsBytecodeOut = tmp.getText();
		
		if((tmp = mutation.getChild(MUTANTS_EXEC_2)) != null)
			ps.mutantsTestResults = tmp.getText();


		/************************************/

		tests = root.getChild(TESTS_1);
		if(tests != null){
			if((tmp = tests.getChild(TESTSCLASSES_2)) != null){
				tmplist = tmp.getChildren(TESTSCLASSES_3);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.testClasses = al.toArray(new String[0]);
			}

			if((tmp = tests.getChild(TESTSCASES_2)) != null){
				tmplist = tmp.getChildren(TESTSCASES_3);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.testCases = al.toArray(new String[0]);
			}
		}

		/************************************/

		Element original = root.getChild(TEST_EXEC_1);

		if(original != null){
			if(original.getAttribute(TIME_ATTRIBUTE) != null){
				ps.runTestsOriginalTime = Long.valueOf(original.getAttribute(TIME_ATTRIBUTE).getValue());
			}

			if((tmp = original.getChild(FAILING_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.failingTestCases = al.toArray(new String[0]);
				
				tmplist = tmp.getChildren(ONE_TS_4);

				al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.errorOnTestSuite = al.toArray(new String[0]);
			}

			if((tmp = original.getChild(IGNORED_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.ignoredTestCases = al.toArray(new String[0]);
			}

			if((tmp = original.getChild(HANGING_TC_3)) != null){
				tmplist = tmp.getChildren(ONE_TC_4);

				ArrayList<String> al = new ArrayList<String>();
				for(Element e: tmplist){
					al.add(e.getText());
				}
				ps.hangingTestCases = al.toArray(new String[0]);
			}
		}
		
		return ps;
	}

	@Override
	public void loadState(ProcessStatistics updateMe)
			throws Exception {
		throw new NotImplementedException();
	}
}
