package com.vmusco.smf.mutation;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.operators.KingOffutt91.AbsoluteValueInsertionMutator;
import com.vmusco.smf.mutation.operators.KingOffutt91.ArithmeticMutatorOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.LogicalConnectorReplacementOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.RelationalBinaryOperator;
import com.vmusco.smf.mutation.operators.KingOffutt91.UnaryOperatorInsertionMutator;

public abstract class MutatorsFactory {
	private MutatorsFactory(){}

	private static Class<MutationOperator<?>>[] mutationClasses = new Class[]{
		AbsoluteValueInsertionMutator.class,
		ArithmeticMutatorOperator.class,
		RelationalBinaryOperator.class,
		LogicalConnectorReplacementOperator.class,
		UnaryOperatorInsertionMutator.class
	};

	public static Class<MutationOperator<?>>[] allAvailMutator(){
		return MutatorsFactory.mutationClasses;
	}

	public static Class<MutationOperator<?>> getOperatorClassFromId(String codeop) throws InstantiationException, IllegalAccessException{
		for(Class<MutationOperator<?>> op : allAvailMutator()){
			MutationOperator<?> mo = op.newInstance();
			
			if(mo.operatorId().equals(codeop)){
				return op;
			}
		}
		
		return null;
	}
	
	public static Class<MutationOperator<?>> getOperatorClassFromFullName(String opname) throws InstantiationException, IllegalAccessException{
		for(Class<MutationOperator<?>> op : allAvailMutator()){
			if(op.getCanonicalName().equals(opname)){
				return op;
			}
		}
		
		return null;
	}

	
}
