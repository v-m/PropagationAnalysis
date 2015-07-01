package com.vmusco.smf.run;

import java.io.File;
import java.text.NumberFormat;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;

import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.MutationStatistics;
import com.vmusco.smf.analysis.ProcessStatistics;
import com.vmusco.smf.mutation.Mutation;
import com.vmusco.smf.mutation.MutationCreationListener;
import com.vmusco.smf.mutation.MutationOperator;
import com.vmusco.smf.mutation.MutatorsFactory;
import com.vmusco.smf.utils.ConsoleTools;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;

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
		opt = new Option("n", "nb-mutants", true, "number of desired viable mutants (default: max)");
		options.addOption(opt);
		opt = new Option("s", "stats", false, "display statistics about a mutation generation process");
		options.addOption(opt);
		opt = new Option("R", "reset", false, "drop all previously generated mutants if so (default: false)");
		options.addOption(opt);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);


		if(cmd.hasOption("operators")){
			Class<MutationOperator<?>>[] allClasses = MutatorsFactory.allAvailMutator();

			for(Class<MutationOperator<?>> c : allClasses){
				MutationOperator m = c.newInstance();
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
		Class<MutationOperator<?>> moc = null;

		ps = ProcessStatistics.rawLoad(cmd.getArgs()[0]);
		if(cmd.getArgs()[1].contains("\\.")){
			moc = MutatorsFactory.getOperatorClassFromFullName(cmd.getArgs()[1]);
		}else{
			moc = MutatorsFactory.getOperatorClassFromId(cmd.getArgs()[1]);
		}

		CreateMutation cm = new CreateMutation();
		cm.mop = ((MutationOperator)moc.newInstance()).operatorId();

		if(ps==null){
			ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED);
			ConsoleTools.write("unable to open ps file @ "+cmd.getArgs()[0]);
			ConsoleTools.endLine();

			System.exit(1);
		}

		if(moc == null){
			ConsoleTools.write("ERROR: ", ConsoleTools.FG_RED);
			ConsoleTools.write("unable to find mutator "+cmd.getArgs()[1]);
			ConsoleTools.endLine();

			System.exit(1);
		}

		MutationStatistics<?> ms = Mutation.createMutationElement(ps, moc);

		if(cmd.hasOption("mutate"))
			ms.classToMutate = cmd.getOptionValue("mutate").split(File.pathSeparator);

		if(cmd.getArgs().length > 2){
			ms.mutationName = cmd.getArgs()[2];
		}

		if(cmd.getArgs().length > 3){
			ms.classToMutate = cmd.getArgs()[3].split(File.pathSeparator);
		}


		System.out.println("Generating mutations, please wait...\n\n\n\n");

		if(cmd.hasOption("stats")){
			ms.loadMutants();
			ConsoleTools.write("# mutants: ", ConsoleTools.BOLD);
			ConsoleTools.write(Integer.toString(ms.mutations.size()));
			
			int nbviable = 0;
			for(String s : ms.mutations.keySet()){
				MutantIfos ifos = ms.mutations.get(s);
				nbviable += ifos.viable?1:0;
			}
			ConsoleTools.write("# viables: ", ConsoleTools.BOLD);
			ConsoleTools.write(Integer.toString(nbviable));
		}else{
			if(cmd.hasOption("name"))
				ms.mutationName = cmd.getOptionValue("name");
			
			if(cmd.hasOption("nb-mutants")){
				ms.loadOrCreateMutants(cmd.hasOption("reset"), cm, Integer.parseInt(cmd.getOptionValue("nb-mutants")));
			}else{
				ms.loadOrCreateMutants(cmd.hasOption("reset"), cm);
			}
		}
	}

	private int nbmutchekmax = 0;
	private int nbmutchek = 0;
	private int nbthischeck = 0;
	private int nbchecks = 0;
	private int nbviables = 0;
	private int nbunviables = 0;

	private CtElement mutprop;

	private CtElement element;

	private void displayStats(){
		long elasped = (System.currentTimeMillis() - this.startTime) / 1000;
		float percent = ((nbmutchek*1f) / (nbmutchekmax*1f)) *100;
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
		ConsoleTools.write(nbmutchek+"/"+nbmutchekmax+" ("+nf.format(percent)+"%)- ");
		ConsoleTools.write("#probes (local): ", ConsoleTools.BOLD);
		ConsoleTools.write(nbchecks+" ("+nbthischeck+") - ");
		ConsoleTools.write("#(un)viables: ", ConsoleTools.BOLD);
		ConsoleTools.write("("+nbunviables+") "+nbviables+" - elasped: "+elasped+" sec]");
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
	public void preparationDone(int nb_mutation_possibility) {
		this.nbmutchekmax = nb_mutation_possibility;
		System.out.println("Mutation viability checking done...");
	}

	@Override
	public void startingMutationCheck(int cpt, CtElement e) {
		nbmutchek++;
		nbthischeck=0;
		displayStats();
	}

	@Override
	public void newMutationProposal(int cpt, int cpt2, CtElement e, CtElement m) {
		nbthischeck++;
		nbchecks++;
		this.element = e;
		this.mutprop = m;
		displayStats();
	}

	@Override
	public void unviableMutant(int cpt, int cpt2, CtElement e, CtElement m) {
		nbunviables++;
		displayStats();
	}

	@Override
	public void viableMutant(int cpt, int cpt2, CtElement e, CtElement m) {
		nbviables++;
		displayStats();
	}

	@Override
	public void endingMutationCheck(int cpt, int validmutants, int droppedmutants, CtElement e) {
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
}
