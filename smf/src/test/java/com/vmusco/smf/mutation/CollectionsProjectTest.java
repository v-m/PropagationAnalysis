package com.vmusco.smf.mutation;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.tmatesoft.svn.core.wc2.SvnUpdate;

import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.exceptions.BadStateException;
import com.vmusco.smf.testing.TestCasesProcessor;

/**
 * Tests for projects managing code with Apache Commons Collections - revision 1610049 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class CollectionsProjectTest {
	private static File temporaryFolder;
	private static File repoFolder = new File(temporaryFolder, "repo");
	private static String repoUrl = "http://svn.apache.org/repos/asf/commons/proper/collections/trunk";

	@Before
	public void cloneTestingRepo() throws IOException, SVNException{
		temporaryFolder = File.createTempFile("testRepo", null);
		System.out.println("Temporary folder is: "+temporaryFolder.getAbsolutePath());

		Assert.assertTrue(temporaryFolder.delete());
		Assert.assertTrue(temporaryFolder.mkdirs());
		temporaryFolder.deleteOnExit();
		
		repoFolder = new File(temporaryFolder, "repo");
		repoFolder.mkdirs();
		
		System.out.println("Cloning project for testing...");
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		try {
			final SvnTarget svnfold = SvnTarget.fromFile(repoFolder);
		    final SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSingleTarget(svnfold);
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(repoUrl)));
		    checkout.run();
		    
		    SvnUpdate update = svnOperationFactory.createUpdate();
		    update.setSingleTarget(svnfold);
		    update.setRevision(SVNRevision.create(1610049l));
		    update.run();
		} finally {
		    svnOperationFactory.dispose();
		}
		
	}

	@Test
	public void simplePipeExecution() throws IOException, BadStateException {
		File projFolder = new File(temporaryFolder, "project");
		
		ProcessStatistics ps = ProcessStatistics.rawCreateProject(ProcessStatistics.SOURCES_COPY, projFolder.getAbsolutePath());
		ps.createWorkingDir();
		Assert.assertTrue(new File(ps.getWorkingDir()).exists());
		ps.setProjectIn(repoFolder.getAbsolutePath());

		System.out.println("Exporting class path");
		ps.setCpLocalFolder(ProcessStatistics.CLASSPATH_PACK);
		ps.setSkipMvnClassDetermination(false);
		ps.exportClassPath();
		Assert.assertEquals(5, ps.getClasspath().length);
		Assert.assertEquals(5, new File(ps.resolveThis(ps.getCpLocalFolder())).list().length);
		
		Assert.assertTrue(ps.compileWithSpoon());

		ps.performFreshTesting(null);
		Assert.assertEquals(18, TestCasesProcessor.getNbFromAnnotations());
		Assert.assertEquals(162, TestCasesProcessor.getNbFromTestCases());
		Assert.assertEquals(TestCasesProcessor.getNbFromTestCases()+TestCasesProcessor.getNbFromAnnotations(), ps.getTestClasses().length);
		Assert.assertEquals(5262, ps.getTestCases().length);
	}

}
