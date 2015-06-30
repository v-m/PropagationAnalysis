package com.vmusco.smf.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class LogToFile {
	private PrintStream retainer_out = null;
	private PrintStream retainer_err = null;
	
	public void redirectTo(File f) throws IOException{
		retainer_out = System.out;
		retainer_err = System.err;
		
		f.createNewFile();
		PrintStream psfile = new PrintStream(new FileOutputStream(f));
		System.setErr(psfile);
		System.setOut(psfile);
	}
	
	public void restablish(){
		System.setOut(retainer_out);
		System.setErr(retainer_err);
		retainer_out = null;
		retainer_err = null;
	}
}
