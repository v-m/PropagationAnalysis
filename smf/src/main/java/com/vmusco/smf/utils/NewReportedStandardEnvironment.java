package com.vmusco.smf.utils;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import spoon.support.StandardEnvironment;

/**
 * This is a class duplicate of the StandardEnvironment spoon class 
 * in order to remove the WARNINGS !
 * @author Vincenzo Musco - vincenzo.musco@inria.fr
 *
 */
public class NewReportedStandardEnvironment extends StandardEnvironment {

	public NewReportedStandardEnvironment() {
		Logger logger = Logger.getLogger(StandardEnvironment.class);
		
		logger.setLevel(Level.ERROR);
	}
}
