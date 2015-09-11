package com.vmusco.softminer.sourceanalyzer.processors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.vmusco.softminer.sourceanalyzer.ProcessorCommunicator;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeParameterReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.Query;
import spoon.reflect.visitor.filter.AbstractReferenceFilter;

/**
*
* @author Vincenzo Musco - http://www.vmusco.com
*/
public class ClassesProcessor extends AbstractProcessor<CtType<?>>{

	
	public String[] processEntry(CtType<?> element, CtTypeReference<?> aReference){
		if(aReference.getDeclaration() == element){
			return null;
		}
		
		return new String[]{element.getQualifiedName(),aReference.getQualifiedName()};
	}
	
	public final void process(CtType<?> element) {
		List<CtTypeReference<?>> refs =	Query.getReferences(element, new AbstractReferenceFilter<CtTypeReference<?>>(CtTypeReference.class) {
			public boolean matches(CtTypeReference<?> reference) {
				return true;
			}
		});

		Set<CtTypeReference<?>> unduplicate = new HashSet<CtTypeReference<?>>(refs);
		HashMap<String, Set<String>> edges = new HashMap<String, Set<String>>();
		//Set<String> undupStr = ;
		
		for(CtTypeReference<?> aReference : unduplicate){
			// We start by skipping the parameterables types parameters...
			if(aReference instanceof CtTypeParameterReference)
				continue;
			
			String[] nextAdd = this.processEntry(element, aReference);
			if(nextAdd != null){
				if(!edges.containsKey(nextAdd[0]))
					edges.put(nextAdd[0], new HashSet<String>());
			
				edges.get(nextAdd[0]).add(nextAdd[1]);
			}
		}
		
		Iterator<String> iterator = edges.keySet().iterator();
		while(iterator.hasNext()){
			String src = iterator.next();
			Set<String> set = edges.get(src);
			
			for(String dst : set.toArray(new String[set.size()])){
				if(ProcessorCommunicator.outputgraph != null)
					ProcessorCommunicator.outputgraph.addDirectedEdgeAndNodeIfNeeded(src, dst, true, false);
				System.out.println(src+" -> "+dst);
			}
		}
		
		
		
	}
}