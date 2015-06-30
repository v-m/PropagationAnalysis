package com.vmusco.softminer.sourceanalyzer.processors;

public abstract class SpecificTags {
	private SpecificTags(){
		
	}

	/***
	 * This tag specify a variable read access
	 * TARGET = edge
	 */
	public static final String __READ_OPERATION = "read_op";

	/***
	 * This tag specify a variable write access
	 * TARGET = edge
	 */
	public static final String __WRITE_OPERATION = "write_op";

	/***
	 * This tag specify a variable write access
	 * TARGET = node (or edge if exo dependencies)
	 */
	public static final String __USES_REFLEXION = "uses_reflexion";

	/***
	 * This tag specify an edge as an abstract method implementation
	 * TARGET = edge
	 */
	public static final String __IS_ABSTRACT_METHOD_IMPLEMENTATION = "abstract_method_implementation";

	/***
	 * This tag specify an edge as an interface method implementation
	 * TARGET = edge
	 */
	public static final String __IS_INTERFACE_IMPLEMENTATION = "interface_implementation";
	
}
