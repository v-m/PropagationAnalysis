package com.vmusco.smf.analysis.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;

public class ProjectXmlPersistenceManager extends XMLPersistenceManager<ProcessStatistics> {
	private static String ROOT_ELEMENT_0 = "smf";
	protected static String TIME_ATTRIBUTE = "time";

	// CONFIG START
	protected static String CONFIG_ELEMENT_1 = "config";
	protected static String SKIP_MVN_CLASS_2 = "skip-mvn-cp";			// CONFIG
	
	protected static String GLOBAL_ELEMENT_1 = "global";
	protected static String HANGTIMEOUT_2 = "testhang-timeout";
	protected static String PROJECT_IN_2 = "project-root";
	protected static String ORIGINAL_PROJECT_IN_2 = "originalproject-root";
	protected static String PROJECT_NAME_2 = "project-name";
	protected static String COMPLIANCE_LEVEL = "compliance-level";
	protected static String ALT_JRE = "alternative-jre";
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
	
	public ProjectXmlPersistenceManager(ProcessStatistics obj) {
		super(obj);
	}
	
	@Override
	public File getPersistenceFile(){
		return new File(getLinkedObject().getConfigFilePath());
	}

	@Override
	public void load(Element root) {
		ProcessStatistics ps = getLinkedObject();
		Element config = root.getChild(CONFIG_ELEMENT_1);
		Element global = config.getChild(GLOBAL_ELEMENT_1);
		
		
		String datasetRepository = null;
		
		if(global.getChild(PROJECT_IN_2) != null){
			datasetRepository = global.getChild(PROJECT_IN_2).getText();
			ps.setProjectIn(datasetRepository);
		}
		
		if(global.getChild(COMPLIANCE_LEVEL) != null){
			ps.setComplianceLevel(Integer.parseInt(global.getChild(COMPLIANCE_LEVEL).getText()));
		}
		
		if(global.getChild(ALT_JRE) != null){
			ps.setAlternativeJre(global.getChild(ALT_JRE).getText());
		}
		
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
			if(original.getAttribute(TIME_ATTRIBUTE) != null){
				ps.setRunTestsOriginalTime(Long.valueOf(original.getAttribute(TIME_ATTRIBUTE).getValue()));
			}

			TestsExecutionIfos tei = TestInformationPersistence.readFrom(original, true);
			tei.setCalledNodeInformation(TestInformationPersistence.readCalledNodes(original));
			
			if(timeout >= 0){
				tei.setTestTimeOut(timeout);
			}
			
			ps.setTestExecutionResult(tei);
		}
	}

	@Override
	public Element getSaveContent() {
		ProcessStatistics ps = getLinkedObject();
		
		Element root = new Element(ROOT_ELEMENT_0);

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
		
		
		tmp = new Element(COMPLIANCE_LEVEL);
		tmp.setText(Integer.toString(ps.getComplianceLevel()));
		run.addContent(tmp);
		
		if(ps.getAlternativeJre() != null){
			tmp = new Element(ALT_JRE);
			tmp.setText(ps.getAlternativeJre());
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


		if(ps.getTestExecutionResult() != null){
			Element testexec = new Element(TEST_EXEC_1);
			root.addContent(testexec);

			if(ps.getRunTestsOriginalTime() != null){
				testexec.setAttribute(new Attribute(TIME_ATTRIBUTE, Long.toString(ps.getRunTestsOriginalTime())));
			}

			TestInformationPersistence.insertInto(testexec, ps.getTestExecutionResult());

			/*if(ps.getStackTraces() != null){
				Element e = new Element(STACKTRACES_3);
				testexec.addContent(e);
				
				for(String[] st : ps.getStackTraces()){
					Element ee = new Element(STACKTRACE_4);
					populateXml(ee, ONE_ST_5, st);
				}
			}*/
		}
		
		return root;
	}

	protected static void populateXml(Element parent, String elementsName, String[] data){
		populateXml(parent, elementsName, data, null);
	}
	
	protected static void populateXml(Element parent, String elementsName, String[] data, HashMap<String, Integer> compressor){
		if(data == null)
			return;

		for(String s: data){
			Element e = new Element(elementsName);
			if(compressor == null)
				e.setText(s);
			else
				e.setText(Integer.toString(TestInformationPersistence.keyForCompression(s, compressor)));
			
			parent.addContent(e);
		}
	}

}
