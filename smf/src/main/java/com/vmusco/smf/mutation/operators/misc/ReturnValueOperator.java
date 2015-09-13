package com.vmusco.smf.mutation.operators.misc;

import java.util.ArrayList;
import java.util.List;

import com.vmusco.smf.mutation.MutationOperator;

import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFieldAccess;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.code.CtNewClass;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtArrayTypeReference;
import spoon.reflect.reference.CtTypeReference;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ReturnValueOperator extends MutationOperator<CtReturn<?>> {

	@Override
	public void process(CtReturn<?> element) {
		if(element.getReturnedExpression() == null)
			return;

		addElement(element);
	}

	@Override
	public CtElement[] getMutatedEntries(CtReturn<?> toMutate, Factory factory) {
		List<CtElement> result = new ArrayList<CtElement>();

		CtElement par = toMutate;

		while(par != null && !(par instanceof CtMethod)){
			par = par.getParent();
		}

		if(par != null){
			CtElement mutationTo = determineMutation((CtExpression<?>)toMutate, (CtMethod<?>)par, factory);

			if(mutationTo != null){
				mutationTo.setParent(toMutate.getParent());
				result.add(mutationTo);
			}
		}

		return result.toArray(new CtElement[0]);
	}

	public CtElement determineMutation(CtExpression<?> elem, CtMethod<?> par, Factory factory){

		if(!(par.getType() instanceof CtArrayTypeReference<?>) && par.getType().isPrimitive()){
			String qname = par.getType().getQualifiedName();
			if(qname.equals("long") ||
					qname.equals("int") ||
					qname.equals("short") ||
					qname.equals("byte") ){
				return handleLongMutation(elem, par, factory);
			}else if(qname.equals("boolean")){
				return handleBooleanMutation(elem, par, factory);
			}else if(qname.equals("double") ||
					qname.equals("float")){
				return handleDoubleMutation(elem, par, factory);
			}else{
				//System.out.println("Missing primitive type: "+qname);
				return null;
			}
		}else{
			return handleObjectMutation(elem,par, factory);
		}
	}

	private CtElement handleObjectMutation(CtExpression<?> elem, CtMethod<?> par, Factory factory) {
		CtTypeReference<?> ret = (CtTypeReference<?>) elem.getType();

		if(elem instanceof CtVariableAccess){
			return null;
		}

		CtExpression<?> retExpr;
		if(!ret.getSimpleName().equals(CtTypeReference.NULL_TYPE_NAME)){
			retExpr = factory.Code().createLiteral(null);
		}else{
			//			CtThrow throwStmt = factory.Core().createThrow();
			//			CtExpression<Exception> exp = factory.Code()
			//					.createCodeSnippetExpression("java.lang.RuntimeException");
			//			throwStmt.setThrownExpression(exp);
			//			return throwStmt;
			retExpr = factory.Code().createCodeSnippetExpression("((Object)null).toString()");
		}
		return retExpr;
	}

	private CtExpression<?> handleBooleanMutation(CtExpression elem, CtMethod par, Factory factory){
		boolean analyzedValue = false;

		if(elem instanceof CtLiteral){
			CtLiteral tt = (CtLiteral)elem;
			analyzedValue = (boolean) tt.getValue();
		}else if(elem instanceof CtNewClass){
			CtNewClass tt = (CtNewClass)elem;

			if(tt.getArguments().size() == 1){
				CtElement param = (CtElement) tt.getArguments().get(0);

				if(param instanceof CtLiteral){
					CtLiteral<?> lit = ((CtLiteral)param);
					if(lit.getValue() instanceof Boolean)
						analyzedValue = (boolean) ((CtLiteral)param).getValue();
					else if(lit.getValue() instanceof String){
						try{
							analyzedValue = (boolean) Boolean.parseBoolean((String) lit.getValue());
						}catch(NumberFormatException ex){
							//System.err.println("An exception has occured");
							return null;
						}
					}else{
						//System.out.println(lit.getValue());
						return null;
					}
				}else{
					//System.out.println(param.getClass());
					return null;
				}
			}

		}else if(elem instanceof CtVariableAccess){
			//CtVariableAccess<?> tt = (CtVariableAccess<?>)elem.getReturnedExpression();
			// Whatever the value is in the variable, consider <> 0 (require a dynamic inspection for going further)
			return null;
		}else{
			//System.err.println("??? "+elem.getReturnedExpression().getClass().getSimpleName());
			return null;

		}

		boolean mutateTo = !analyzedValue;

		CtLiteral<?> createLiteral = factory.Code().createLiteral(mutateTo);
		return createLiteral;

		//System.out.println("Mutate "+analyzedValue+" => "+ mutateTo);
	}

	/**
	 * This function handle long, int, short and byte as they are all numerical values
	 * @param elem
	 * @param par
	 */
	private CtExpression<?> handleLongMutation(CtExpression elem, CtMethod par, Factory factory){
		boolean isLong = par.getType().getQualifiedName().equals("long");
		long analyzedValue = isLong?0:1;

		if(elem instanceof CtLiteral){
			CtLiteral tt = (CtLiteral)elem;
			if(tt.getValue() instanceof Long){
				analyzedValue = (long) tt.getValue();
			}else{
				analyzedValue = new Long((int)tt.getValue()).longValue();
			}
		}else if(elem instanceof CtNewClass){
			CtNewClass tt = (CtNewClass)elem;

			if(tt.getArguments().size() == 1){
				CtElement param = (CtElement) tt.getArguments().get(0);

				if(param instanceof CtLiteral){
					CtLiteral<?> lit = ((CtLiteral)param);
					if(lit.getValue() instanceof Long)
						analyzedValue = (long) ((CtLiteral)param).getValue();
					else if(lit.getValue() instanceof Integer || 
							lit.getValue() instanceof Short ||
							lit.getValue() instanceof Byte)
						analyzedValue = (long) new Long((int)((CtLiteral)param).getValue()).longValue();
					else if(lit.getValue() instanceof String){
						try{
							analyzedValue = (long) Long.parseLong((String) lit.getValue());
						}catch(NumberFormatException ex){
							//System.err.println("An exception has occured");
							return null;
						}
					}else{
						//System.out.println(lit.getValue());
						return null;
					}
				}else{
					//System.out.println(param.getClass());
					return null;
				}
			}

		}else if(elem instanceof CtVariableAccess){
			//CtVariableAccess<?> tt = (CtVariableAccess<?>)elem.getReturnedExpression();
			// Whatever the value is in the variable, consider <> 0 (require a dynamic inspection for going further)
			return null;
		}else{
			//System.err.println("??? "+elem.getReturnedExpression().getClass().getSimpleName());
			return null;
		}


		long mutateTo;

		if(!isLong){
			mutateTo = new Long((analyzedValue==0)?1:0).longValue();
		}else{
			mutateTo = analyzedValue + 1;
		}


		CtLiteral<?> createLiteral;

		if(par.getType().getQualifiedName().equals("long")){
			createLiteral = factory.Code().createLiteral((long)mutateTo);
		}else if(par.getType().getQualifiedName().equals("int")){
			createLiteral = factory.Code().createLiteral((int)mutateTo);
		}else if(par.getType().getQualifiedName().equals("short")){
			createLiteral = factory.Code().createLiteral((short)mutateTo);
		}else if(par.getType().getQualifiedName().equals("byte")){
			createLiteral = factory.Code().createLiteral((byte)mutateTo);
		}else{
			return null;
		}

		return createLiteral;

		//System.out.println("Mutate "+analyzedValue+" => "+ mutateTo);
	}

	private CtExpression<?> handleDoubleMutation(CtExpression elem, CtMethod<?> par, Factory factory){
		double analyzedValue = 0.0d;

		if(elem instanceof CtLiteral){
			CtLiteral tt = (CtLiteral)elem;
			if(tt.getValue() instanceof Double)
				analyzedValue = (double) tt.getValue();
			else
				analyzedValue = new Double((float) tt.getValue()).doubleValue();
		}else if(elem instanceof CtNewClass){
			CtNewClass<?> tt = (CtNewClass)elem;

			if(tt.getArguments().size() == 1){
				CtElement param = (CtElement) tt.getArguments().get(0);

				if(param instanceof CtLiteral){
					CtLiteral<?> lit = ((CtLiteral)param);
					if(lit.getValue() instanceof Double
							|| lit.getValue() instanceof Float)
						analyzedValue = (double) ((CtLiteral)param).getValue();
					else if(lit.getValue() instanceof String){
						try{
							analyzedValue = (long) Double.parseDouble((String) lit.getValue());
						}catch(NumberFormatException ex){
							//System.out.println("An exception has occured");
							return null;
						}
					}else{
						//ystem.out.println("---"+lit.getType());
						return null;
					}
				}else if(param instanceof CtFieldAccess){
					CtFieldAccess field = (CtFieldAccess) param;
					if(field.getVariable().getQualifiedName().endsWith("#NaN")){
						analyzedValue = Double.NaN;
					}else{
						//System.out.println("----"+param.getClass());
						return null;
					}
				}else{
					//System.out.println("--"+param.getClass());
					return null;
				}
			}
		}else if(elem instanceof CtFieldAccess){
			CtFieldAccess field = (CtFieldAccess) elem;
			if(field.getVariable().getQualifiedName().endsWith("#NaN")){
				analyzedValue = Double.NaN;
			}else{
				//System.out.println("!!!!");
				return null;
			}
		}else if(elem instanceof CtVariableAccess){
			//CtVariableAccess<?> tt = (CtVariableAccess<?>)elem.getReturnedExpression();
			// Whatever the value is in the variable, consider <> 0 (require a dynamic inspection for going further)
			return null;
		}else{
			//System.err.println("??? "+elem.getReturnedExpression().getClass().getSimpleName());
			return null;
		}

		double mutateTo = Double.isNaN(analyzedValue)?0:-(analyzedValue + 1.0);

		CtLiteral<?> createLiteral;

		if(par.getType().getQualifiedName().equals("double")){
			createLiteral = factory.Code().createLiteral((double)mutateTo);
		}else if(par.getType().getQualifiedName().equals("long")){
			createLiteral = factory.Code().createLiteral((long)mutateTo);
		}else{
			return null;
		}

		return createLiteral;
	}
	
	@Override
	public String operatorId() {
		return "ReturnValueOperator";
	}
}
