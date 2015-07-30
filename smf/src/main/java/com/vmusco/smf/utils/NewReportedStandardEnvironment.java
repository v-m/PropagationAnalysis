package com.vmusco.smf.utils;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import spoon.support.StandardEnvironment;

/**
 * This is a trick duplicate class of the StandardEnvironment spoon class 
 * in order to remove the WARNINGS !
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class NewReportedStandardEnvironment extends StandardEnvironment {
	private static final long serialVersionUID = 1L;

	public NewReportedStandardEnvironment() {
		Logger logger = Logger.getLogger(StandardEnvironment.class);
		
		logger.setLevel(Level.ERROR);
	}
}
