package com.vmusco.smf.mutation;

import spoon.reflect.declaration.CtElement;

public interface MutationCreationListener {

	void preparationDone(int nb_mutation_possibility);

	void startingMutationCheck(int cpt, CtElement e);

	void newMutationProposal(int cpt, int cpt2, CtElement e, CtElement m);

	void unviableMutant(int cpt, int cpt2, CtElement e, CtElement m);

	void viableMutant(int cpt, int cpt2, CtElement e, CtElement m);

	void endingMutationCheck(int cpt, int validmutants, int droppedmutants,
			CtElement e);

	void mutationSummary(int validmutants, int droppedmutants, long time);
}
