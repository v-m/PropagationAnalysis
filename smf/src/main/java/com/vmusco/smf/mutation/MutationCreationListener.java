package com.vmusco.smf.mutation;

import spoon.reflect.declaration.CtElement;

public interface MutationCreationListener {
	void preparationDone(int nb_mutation_possibility, int nb_viables_wanted);
	void startingMutationCheck(CtElement e);
	void newMutationProposal(CtElement e, CtElement m);
	void unviableMutant(CtElement e, CtElement m);
	void viableMutant(CtElement e, CtElement m);
	void alreadyProcessedMutant(CtElement e, CtElement m);
	void endingMutationCheck(int validmutants, int droppedmutants, CtElement e);
	void mutationSummary(int validmutants, int droppedmutants, long time);
}
