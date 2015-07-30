package com.vmusco.smf.run;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vmusco.smf.testing.TestsExecutionListener;
import com.vmusco.smf.utils.ConsoleTools;

/**
 * This is an abstract implementation of a test runner entry point
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class GlobalTestRunning implements TestsExecutionListener{

	private File file;
	private FileOutputStream fos;

	protected int nbtestsuite = 0;
	protected int nbtestsuitedone = 0;
	protected int nbtotaltc = 0;
	protected int nbfail = 0;
	protected int nbunrunnable = 0;
	protected int nbignored = 0;
	protected int nbhang = 0;
	protected int nbnotpermitted = 0;
	protected String state;
	protected String execname;

	public GlobalTestRunning() {
		this.fos = null;
	}

	public GlobalTestRunning(String file) {
		this.file = new File(file);
	}

	protected void printMutantStat(){
		ConsoleTools.restartPreviousLine();
		ConsoleTools.write(execname+": ", ConsoleTools.BOLD);
		ConsoleTools.write(state);
		ConsoleTools.write(" [testsuite: "+nbtestsuitedone+"/"+nbtestsuite+", processed: "+nbtotaltc+", ");
		ConsoleTools.write("fails: "+nbfail, ConsoleTools.BOLD, nbfail==0?ConsoleTools.FG_GREEN:ConsoleTools.FG_RED);
		ConsoleTools.write(" (");
		ConsoleTools.write("suites: "+nbunrunnable, nbunrunnable==0?0:ConsoleTools.FG_YELLOW);
		ConsoleTools.write("), ignore: "+nbignored+", hang: "+nbhang+", discared: "+nbnotpermitted+"].");
		ConsoleTools.endLine();
	}

	@Override
	public void testSuiteExecutionStart(int nbtest, int nbmax, String cmd) {
		this.state = "Testing...";
		nbtestsuite = nbmax;
		nbtestsuitedone = nbtest;
		printMutantStat();

		if(this.fos != null){
			try {
				this.fos.write('\n');
				this.fos.write(cmd.getBytes());
				this.fos.write('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void testCaseException(int nbtest, String readLine, String[] executedCommand) {
		this.state = "An exception has be thrown by the process";
		printMutantStat();
		ConsoleTools.write("\n\tError message: "+readLine+"\n");
		ConsoleTools.endLine();
		ConsoleTools.write("\n\tRerun with: ");
		ConsoleTools.endLine();
		for(String cmd : executedCommand){
			ConsoleTools.write(cmd+" ");
		}
		ConsoleTools.endLine();
		ConsoleTools.write("\nContinuing other executions...");
		ConsoleTools.endLine();
		//System.exit(1);
	}

	@Override
	public void testCaseExecutionFinished(int cpt, String[] all, String[] fail, String[] ignored, String[] hang) {
		this.state = "Finished test suite "+cpt;
		printMutantStat();
	}

	@Override
	public void testCaseNewFail(int cpt, String line) {
		this.nbfail++;
		printMutantStat();

		if(this.fos != null){
			try {
				this.fos.write('\t');
				this.fos.write(line.getBytes());
				this.fos.write('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void testCaseNotPermitted(int cpt, String line) {
		this.nbnotpermitted++;
		printMutantStat();
	}

	@Override
	public void testCaseNewIgnored(int cpt, String line) {
		this.nbignored++;
		printMutantStat();
	}

	@Override
	public void testCaseEntered(int cpt, String line) {
		this.nbtotaltc++;
		printMutantStat();
	}

	@Override
	public void testCaseUndeterminedTest(int cpt, String line) {
	}

	@Override
	public void testCaseOtherCase(int cpt, String line) {
	}

	@Override
	public void testCaseNewLoop(int cpt, String line) {
		this.nbhang++;
		printMutantStat();
	}

	@Override
	public void testSuiteUnrunnable(int cpt, String aTest, String line) {
		nbunrunnable++;
		printMutantStat();
	}
	
	protected void resetAndopenStream() throws FileNotFoundException{
		if(file.exists())
			file.delete();

		this.fos = new FileOutputStream(file);
	}

	protected void closeStream() throws IOException {
		if(this.fos != null)
			fos.close();
	}

	public void testCaseFailureInfos(int cpt, String line) {
		if(this.fos != null){
			try {
				this.fos.write('\t');
				this.fos.write('\t');
				this.fos.write(line.getBytes());
				this.fos.write('\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void currentTimeout(int timeout) {
		ConsoleTools.write("Current timeout is: "+timeout);
		ConsoleTools.endLine();
		ConsoleTools.endLine();
	}
	
	@Override
	public void newTimeout(int timeout) {
		System.out.println("Dynamic timeout tweaking... New timeout is "+timeout+" secs. \n\n\n");
	}
}