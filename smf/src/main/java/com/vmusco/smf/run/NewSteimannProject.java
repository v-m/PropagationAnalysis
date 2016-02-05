package com.vmusco.smf.run;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.analysis.TestsExecutionIfos;
import com.vmusco.smf.analysis.persistence.MutationXmlPersistenceManager;
import com.vmusco.smf.analysis.persistence.ProjectXmlPersistenceManager;
import com.vmusco.smf.analysis.persistence.XMLPersistence;
import com.vmusco.smf.mutation.operators.ExternalMutationOperator;
import com.vmusco.smf.utils.BytecodeTools;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.SourceReference;
import com.vmusco.smf.utils.SteimannDatasetMatrixReader;
import com.vmusco.smf.utils.SteimannDatasetMatrixReader.Result;
import com.vmusco.smf.utils.SteimannDatasetMatrixReader.Test;
import com.vmusco.smf.utils.SteimannDatasetMatrixReader.UUT;
import com.vmusco.smf.utils.SteimannDatasetMatrixReader.UUTEntry;

/**
 * See http://www.feu.de/ps/prjs/EzUnit/eval/ISSTA13/ for dataset
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class NewSteimannProject {

	public static void main(String[] args) throws Exception {
		Options options = new Options();

		Option opt;
		opt = new Option("F", "force", false, "Overwrite the working directory if it already exists !");
		options.addOption(opt);
		opt = new Option("h", "help", false, "print this message");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);

		if( cmd.getArgs().length < 3 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();

			String head = "Import a project from Steimann et al. dataset (ISSTA'13 paper) as a a new SMF project entitled <name> in <working-dir>. This project cannot be mutated but can be used as entry for some tools."
					+"<def-file> is the definition file and <mut-folder> is the folder containing the mutants to take into consideration";

			String foot = "";

			formatter.printHelp("[options] <working-dir> <def-file> <mut-folder> [<name>]", head, options, foot);
			System.exit(0);
		}
		
		File f = new File(cmd.getArgs()[0]);
		
		if(f.exists()){
			if(!cmd.hasOption("force")){
				ConsoleTools.write("Unable to create this project as it already exists. To overwrite, use --force option.\n");
				System.exit(1);
			}else{
				ConsoleTools.write("Overwritting working directory...\n");
				FileUtils.deleteQuietly(f);
			}
		}

		f.mkdir();

		ProcessStatistics ps = createProjectFromReference(cmd.getArgs()[1], f.getAbsolutePath());
		if(cmd.getArgs().length > 3)
			ps.setProjectName(cmd.getArgs()[3]);
		
		ps.createWorkingDir();

		File[] muts = new File(cmd.getArgs()[2]).listFiles();

		MutationStatistics ms = importMutants(ps, muts);
		ms.createExecutionFolderIfNeeded();

		XMLPersistence.save(new ProjectXmlPersistenceManager(ps));
		XMLPersistence.save(new MutationXmlPersistenceManager(ms));
		ms.saveAllMutationStats();
	}
	
	public static MutationStatistics importMutants(ProcessStatistics ps, File[] muts) throws IOException {
		MutationStatistics ms = new MutationStatistics(ps, new ExternalMutationOperator("STEIMANN"));
		
		for(File mut : muts){
			Result testsAndUUTsFromFile = SteimannDatasetMatrixReader.getTestsAndUUTsFromFile(mut.getAbsolutePath());
			
			MutantIfos mi = new MutantIfos();
			TestsExecutionIfos tei = new TestsExecutionIfos();
			mi.setExecutedTestsResults(tei);
			mi.setId(mut.getName().replaceAll(".txt", "").split("_")[1]);
			ms.setMutationStats(mi.getId(), mi);

			// Searching for the mutated element...
			UUT mutelement = null;
			for(UUT u : testsAndUUTsFromFile.uuts){
				if(u.faulty){
					if(mutelement != null){
						System.err.println("Several changed points --- Error !");
						System.exit(1);
					}
					mutelement = u;
				}
			}
			
			if(mutelement == null){
				System.out.println("No mutation point for "+mut.getName()+", skipping file");
				continue;
			}
			
			mi.setMutationIn(BytecodeTools.signatureConverter(mutelement.name));
			mi.setViable(true);
			mi.setMutationTo(mutelement.faultType);
			mi.setMutationFrom("-");
			
			// Simple position storing only the relative position in file buffer. Line/Col are set to zero
			SourceReference sr = new SourceReference();
			sr.setSourceRange(mutelement.faultPosition, mutelement.faultPosition);
			sr.setLineRange(0, 0);
			sr.setColumnRange(0, 0);
			sr.setSourceRange(mutelement.faultPosition, mutelement.faultPosition);
			sr.setFile("");
			mi.setSourceReference(sr);
			
			Set<String> myFailingTests = new HashSet<>();
			
			for(Test t : testsAndUUTsFromFile.tests){
				String test = BytecodeTools.signatureConverter(t.name);
				//String testClass = test.substring(0, test.lastIndexOf('.') - 1);

				if(!t.passed){
					myFailingTests.add(test);
				}

				Set<String> calls = new HashSet<String>();

				for(UUTEntry u : t.calledUUTs){
					calls.add(BytecodeTools.signatureConverter(u.uut.name));
				}

				tei.addCalledNodeInformation(test, calls.toArray(new String[calls.size()]));
				tei.setFailingTestCases(myFailingTests.toArray(new String[myFailingTests.size()]));
			}
		}
		
		return ms;
	}

	public static ProcessStatistics createProjectFromReference(String referenceFile, String workingDir) throws IOException{
		Result testsAndUUTsFromFile = SteimannDatasetMatrixReader.getTestsAndUUTsFromFile(referenceFile);

		ProcessStatistics ps = new ProcessStatistics(workingDir);
		TestsExecutionIfos cleanTestExecution = new TestsExecutionIfos();
		ps.setTestExecutionResult(cleanTestExecution);

		Set<String> myTestClasses = new HashSet<>();
		Set<String> myTests = new HashSet<>();
		Set<String> myFailingTests = new HashSet<>();

		for(Test t : testsAndUUTsFromFile.tests){
			String test = BytecodeTools.signatureConverter(t.name);
			String testClass = test.substring(0, test.lastIndexOf('.') - 1);

			myTestClasses.add(testClass);
			myTests.add(test);

			if(!t.passed){
				myFailingTests.add(testClass);
			}

			Set<String> calls = new HashSet<String>();

			for(UUTEntry u : t.calledUUTs){
				calls.add(BytecodeTools.signatureConverter(u.uut.name));
			}

			cleanTestExecution.addCalledNodeInformation(test, calls.toArray(new String[calls.size()]));
		}


		System.out.println(String.format("Found %d test classes, %d tests with %d failing tests.", myTestClasses.size(), myTests.size(), myFailingTests.size()));
		ps.setTestCases(myTests.toArray(new String[myTests.size()]));
		ps.setTestClasses(myTestClasses.toArray(new String[myTestClasses.size()]));
		cleanTestExecution.setFailingTestCases(myFailingTests.toArray(new String[myFailingTests.size()]));
		System.out.println("========================");

		for(UUT u : testsAndUUTsFromFile.uuts){
			// Nothing to do here yet
		}

		return ps;
	}

}
