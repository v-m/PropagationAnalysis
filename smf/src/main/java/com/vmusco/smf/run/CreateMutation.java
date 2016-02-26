package com.vmusco.smf.run;

import java.io.File;
import java.text.NumberFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.mutation.SmfMutationOperator;
import com.vmusco.smf.utils.ConsoleTools;
import com.vmusco.smf.utils.SafeInterruption;

/**
 * This entry point is used to create a new mutation project.
 * The old functionality to get statistics about a project has been moved to
 * {@link ProjectTools}
 * @author Vincenzo Musco - http://www.vmusco.com
 * @see ProjectTools
 */
public class CreateMutation implements MutationCreationListener{

	private static final Class<?> thisclass = CreateMutation.class;

	private long startTime;
	private String mop;

	private CreateMutation(){
		this.startTime = System.currentTimeMillis(); 
	}

	public static void main(String[] args) throws Exception {
		String useAsSepString = "use "+File.pathSeparator+" as separator";

		Options options = new Options();

		Option opt;
		opt = new Option("h", "help", false, "display this message");
		options.addOption(opt);
		opt = new Option("o", "operators", false, "display the list of available operators");
		options.addOption(opt);
		opt = new Option("n", "name", true, "a name for the mutation");
		options.addOption(opt);
		opt = new Option("m", "mutate", true, "classes to mutate ("+useAsSepString+" - default: all classes)");
		options.addOption(opt);
		opt = new Option("n", "nb-mutants", true, "number of desired viable mutants (already generated are not taken into consideration) (default: max)");
		options.addOption(opt);
		opt = new Option("s", "safe-persist", true, "specify the number of mutant to generate before do an intermediate safety persistance, 0 for no intermediate safe persistance (default: 0)");
		options.addOption(opt);
		opt = new Option("P", "paranoid", false, "save after each generated mutant (overwrite -s behavior, similar as -s 1)");
		options.addOption(opt);
		opt = new Option("R", "reset", false, "drop all previously generated mutants if so (default: false)");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);


		if(cmd.hasOption("operators")){
			Class<SmfMutationOperator<?>>[] allClasses = MutatorsFactory.allAvailMutator();

			for(Class<SmfMutationOperator<?>> c : allClasses){
				SmfMutationOperator<?> m = c.newInstance();
				System.out.println("-> "+m.operatorId()+" ("+c.getCanonicalName()+"): "+m.shortDescription());
			}

			System.exit(0);
		}

		if(cmd.getArgs().length < 2 || cmd.hasOption("help")){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(thisclass.getCanonicalName()+" [options] <configFile> <mutatorOp>", options);
			System.exit(0);
		}


		ProcessStatistics ps = null;
		SmfMutationOperator<?> moc = null;

		ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);
		if(cmd.getArgs()[1].contains("\\.")){
			moc = MutatorsFactory.getOperatorClassFromFullName(cmd.getArgs()[1]);
		}else{
			moc = (SmfMutationOperator<?>)MutatorsFactory.getOperatorClassFromId(cmd.getArgs()[1]);
		}

		if(moc == null){
			ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED);
			ConsoleTools.write("unable to find mutator "+cmd.getArgs()[1]);
			ConsoleTools.endLine();

			System.exit(1);
		}
		
		CreateMutation cm = new CreateMutation();
		cm.mop = moc.operatorId();

		if(ps==null){
			ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED);
			ConsoleTools.write("unable to open ps file @ "+cmd.getArgs()[0]);
			ConsoleTools.endLine();

			System.exit(1);
		}

		final MutationStatistics ms = Mutation.createMutationElement(ps, moc);

		if(cmd.hasOption("mutate"))
			ms.setClassToMutate(cmd.getOptionValue("mutate").split(File.pathSeparator));

		if(cmd.getArgs().length > 2){
			ms.setMutationName(cmd.getArgs()[2]);
		}

		if(cmd.getArgs().length > 3){
			ms.setClassToMutate(cmd.getArgs()[3].split(File.pathSeparator));
		}

		int safepersist = 0;
		int nbmut = -1;
		
		if(cmd.hasOption("safe-persist"))
			safepersist = Integer.parseInt(cmd.getOptionValue("safe-persist"));
		
		if(cmd.hasOption("paranoid")){
			ConsoleTools.write("Running in paranoid mode. For preformances reasons, avoid it if not required.\n");
			safepersist = 1;
		}
		
		if(cmd.hasOption("nb-mutants"))
			nbmut = Integer.parseInt(cmd.getOptionValue("nb-mutants"));
		
		System.out.println("Generating mutations, please wait...\n\n\n\n");

		SafeInterruption id = new SafeInterruption();
		
		Runtime.getRuntime().addShutdownHook(id);
		
		if(cmd.hasOption("name"))
			ms.setMutationName(cmd.getOptionValue("name"));
		
		ms.loadOrCreateMutants(cmd.hasOption("reset"), cm, nbmut, safepersist, id);

		id.disableBlocker();
	}

	private int nbchecks = 0;
	private int nbviables = 0;
	private int nbunviables = 0;
	private int alreadyproc = 0;
	private int nbtotalelem = 0;
	private int wantedviables = 0;

	private CtElement mutprop;
	private CtElement element;


	private void displayStats(){
		long elasped = (System.currentTimeMillis() - this.startTime) / 1000;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		if(element != null){
			ConsoleTools.restartPreviousLine();
			ConsoleTools.restartPreviousLine();
		}
		if(mutprop != null){
			ConsoleTools.restartPreviousLine();
		}
		ConsoleTools.restartPreviousLine();
		ConsoleTools.write("Mutation ("+this.mop+") viability checking... [");

		ConsoleTools.write("#element: ", ConsoleTools.BOLD);
		ConsoleTools.write("#probes: ", ConsoleTools.BOLD);
		ConsoleTools.write(nbchecks+"/"+nbtotalelem+" - ");
		ConsoleTools.write("#(already)(un)viables (expected): ", ConsoleTools.BOLD);
		ConsoleTools.write("("+alreadyproc+")("+nbunviables+") "+nbviables+"(/"+wantedviables+") - elasped: "+elasped+" sec]");
		ConsoleTools.endLine();
		
		int NBL = 200;
		
		try{
			if(element != null){
				CtElement parent = element.getParent(CtClass.class);
				ConsoleTools.write(" Parent: ", ConsoleTools.BOLD);
				if(parent != null){
					ConsoleTools.write(parent.getSignature());
				}else{
					ConsoleTools.write("undeterm.");
				}
				ConsoleTools.endLine();
				
				ConsoleTools.write(" Current element: ", ConsoleTools.BOLD);
				String show = element.toString();
				ConsoleTools.write(show.substring(0, show.length()>NBL?NBL:show.length()));
				if(show.length()>NBL)
					ConsoleTools.write("...");
				ConsoleTools.endLine();
			}
		}catch(Exception e){
			ConsoleTools.write("Exception catched here for display. Safety code to avoid instability...");
			ConsoleTools.endLine();
		}
		
		try{
			if(mutprop != null){
				ConsoleTools.write(" Proposal: ", ConsoleTools.BOLD);
				String show = mutprop.toString();
				ConsoleTools.write(show.substring(0, show.length()>NBL?NBL:show.length()));
				if(show.length()>NBL)
					ConsoleTools.write("...");
				ConsoleTools.endLine();
			}
		}catch(Exception e){
			ConsoleTools.write("Exception catched here for display. Safety code to avoid instability...");
			ConsoleTools.endLine();
		}
	}

	@Override
	public void preparationDone(int nb_mutation_possibility, int nb_viables_wanted) {
		this.nbtotalelem = nb_mutation_possibility;
		this.wantedviables = nb_viables_wanted;
		System.out.println("Mutation viability checking done...");
	}

	@Override
	public void startingMutationCheck(CtElement e) {
		displayStats();
	}

	@Override
	public void newMutationProposal(CtElement e, CtElement m) {
		nbchecks++;
		this.element = e;
		this.mutprop = m;
		displayStats();
	}

	@Override
	public void unviableMutant(CtElement e, CtElement m) {
		nbunviables++;
		displayStats();
	}

	@Override
	public void viableMutant(CtElement e, CtElement m) {
		nbviables++;
		displayStats();
	}

	@Override
	public void endingMutationCheck(int validmutants, int droppedmutants, CtElement e) {
	}

	@Override
	public void mutationSummary(int validmutants, int droppedmutants, long time) {
		long elasped = (System.currentTimeMillis() - this.startTime) / 1000;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);
		ConsoleTools.write("Process ended: ", ConsoleTools.BOLD, ConsoleTools.FG_BLUE);
		int total = validmutants + droppedmutants;
		float percent = (validmutants*1f / total)*100;
		ConsoleTools.write(validmutants+" viable mutants has been found on the "+total+" generated mutants ("+nf.format(percent)+"%) in "+elasped+" sec.");
		ConsoleTools.endLine();
		ConsoleTools.endLine();
	}

	@Override
	public void alreadyProcessedMutant(CtElement e, CtElement m) {
		alreadyproc ++;
		displayStats();
	}
}
