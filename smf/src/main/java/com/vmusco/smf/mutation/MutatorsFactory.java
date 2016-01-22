package com.vmusco.smf.mutation;

import com.vmusco.smf.mutation.operators.ExternalMutationOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.AbsoluteValueInsertionMutator;
import com.vmusco.smf.mutation.operators.KingOffutt91.ArithmeticMutatorOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.LogicalConnectorReplacementOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.RelationalBinaryOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.UnaryOperatorInsertionMutator;

/**
 * This class allows to get mutation operators based on code op defined using {@link SmfMutationOperator#operatorId()} and 
 * by full canonical name. For this class to work, the {@link MutatorsFactory#mutationClasses} must be updated for each new
 * opeartor
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public abstract class MutatorsFactory {
	private MutatorsFactory(){}

	@SuppressWarnings("unchecked")
	private static Class<SmfMutationOperator<?>>[] mutationClasses = new Class[]{
		AbsoluteValueInsertionMutator.class,
		ArithmeticMutatorOperator.class,
		RelationalBinaryOperator.class,
		LogicalConnectorReplacementOperator.class,
		UnaryOperatorInsertionMutator.class
	};

	public static Class<SmfMutationOperator<?>>[] allAvailMutator(){
		return MutatorsFactory.mutationClasses;
	}

	public static MutationOperator getOperatorClassFromId(String codeop) {
		if(codeop == null)
			return null;
		
		for(Class<SmfMutationOperator<?>> op : allAvailMutator()){
			SmfMutationOperator<?> mo;
			try {
				mo = op.newInstance();
				
				if(mo.operatorId().equals(codeop)){
					return mo;
				}
			} catch (InstantiationException | IllegalAccessException e) {
				// Should not occurs !
				e.printStackTrace();
			}
		}
		
		return new ExternalMutationOperator(codeop);
	}
	
	public static SmfMutationOperator<?> getOperatorClassFromFullName(String opname) throws InstantiationException, IllegalAccessException{
		for(Class<SmfMutationOperator<?>> op : allAvailMutator()){
			if(op.getCanonicalName().equals(opname)){
				return op.newInstance();
			}
		}
		
		return null;
	}

	
}
