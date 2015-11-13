package com.vmusco.smf.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * This importer class is directly taken from the author website (http://www.feu.de/ps/prjs/EzUnit/eval/ISSTA13/programs/javareader.java)
 * and adapted to translate informations to smf data structures. 
 */
public class SteimannDatasetMatrixReader {
	// classes
	public static class Result {
		public String 			projectName = null;
		public LinkedList<Test> tests;
		public LinkedList<UUT> 	uuts;
	}

	public static class Test {
		public String 						name;			// name of the test
		public String						result;			// test result (PASSED, FAILED, ERROR)

		// UUTs called by the test
		public LinkedList<UUTEntry>			calledUUTs = new LinkedList<UUTEntry>();

		public String						exception;		// exception that caused the test to fail

		// stack trace in case of an exception (stores UUT indices)
		// due to the nature of the stack trace given by a profiled JUnit, overloaded
		// methods can't be resolved, so all of them have to be stored as possible
		// member of the stack trace;
		// the stack trace might be empty if the exception has been thrown directly
		// within the test method
		public Stack<LinkedList<Integer>> 	stackTrace = new Stack<LinkedList<Integer>>();

		public boolean						passed = true;

		@Override
		public boolean equals( Object test ) {
			if ( test instanceof Test )
				return name.equals( ( (Test)test ).name );
			else
				return false;
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

	public static class UUT {
		public String 			name;			// name of the UUT

		public boolean 			faulty;			// indicates if the UUT contained an injected fault
		public String 			faultClass;		// class of the injected fault (STATEMENT, OPERATOR, ASSIGNMENT, ...)
		public int 				faultPosition;	// position of the injected fault
		public String 			faultType;		// description of the injected fault

		public HashSet<Test>	callingTests = new HashSet<Test>();	// tests calling this UUT
	}

	public static class UUTEntry {
		public UUT uut;				// called UUT
		public int count;			// call count of UUT 		

		public UUTEntry( UUT uut, int count ) {
			this.uut = uut;
			this.count = count;
		}
	}

	// enums
	public static enum ReadMode {
		NONE,
		META,
		TESTS,
		UUTS,
		MATRIX,
	}

	public static Result getTestsAndUUTsFromFile( String filePath ) throws IOException {
		FileReader fin = new FileReader( filePath );
		BufferedReader in = new BufferedReader( fin );

		LinkedList<Test> tests = new LinkedList<Test>();
		LinkedList<UUT> uuts = new LinkedList<UUT>();
		String projectName = null;

		String line;
		String[] info;
		ReadMode mode = ReadMode.NONE;
		int index = 0;
		Test test;
		UUT uut;

		while ( ( line = in.readLine() ) != null ) {
			// skip empty lines
			if ( line.isEmpty() )
				continue;

			// change the mode, if necessary
			if ( line.startsWith( "#metadata" ) )
				mode = ReadMode.META;
			else if ( line.startsWith( "#tests" ) )
				mode = ReadMode.TESTS;
			else if ( line.startsWith( "#uuts" ) )
				mode = ReadMode.UUTS;
			else if ( line.startsWith( "#matrix" ) )
				mode = ReadMode.MATRIX;
			else {
				// process data according to the current mode
				switch ( mode ) {
				case NONE:
					break; // do nothing

				case META:
					// search for the project
					if ( line.startsWith( "Project" ) ) 
						projectName = line.split( ";" )[1];
					break;

				case TESTS:
					// read the test
					info = line.split( " " );

					test = new Test();
					test.name = info[0];

					// get the test result
					test.result = info[1];
					test.passed = ( test.result.equals( "PASSED" ) );
					test.calledUUTs.clear();
					test.stackTrace.clear();
					test.exception = null;

					// get the exception
					if ( info.length > 2 ) {

						// for the jexel project, a mysterious "class" string appears before the exception, 
						// so if this is the case, shift the info to the left
						if ( info[2].equals( "class" ) ) {
							String[] newInfo = new String[info.length - 1];

							newInfo[0] = info[0];
							newInfo[1] = info[1];

							for ( int i = 2; i < info.length - 1; ++i )
								newInfo[i] = info[i+1];

							info = newInfo;
						}

						test.exception = info[2];

						// get the stack trace
						if ( info.length > 3 ) {
							for ( int i = 3; i < info.length; ++i ) {
								LinkedList<Integer> mutIndices = new LinkedList<Integer>();

								// might be either a direct method reference or
								// an ambiguous set of overloaded methods
								if ( !info[i].contains( "(" ) ) {
									try {
										mutIndices.push( Integer.parseInt( info[i] ) );
									} catch ( Exception ex ) {}
									test.stackTrace.push( mutIndices );
								}
								else {
									String[] indices = info[i].substring( 1, info[i].indexOf( ')' ) ).split( "," );

									for ( String mutIndex : indices )
										mutIndices.push( Integer.parseInt( mutIndex ) );

									test.stackTrace.push( mutIndices );
								}
							}
						}
					}

					tests.add( test );
					break;

				case UUTS:
					// read the UUT
					info = line.split( "\\|" );

					uut = new UUT();
					uut.name = info[0];

					uut.faultClass = null;
					uut.faultPosition = 0;
					uut.faultType = null;
					uut.faulty = false;

					if ( info.length > 1 ) {
						uut.faulty = true;
						uut.faultClass = info[1];
						uut.faultPosition = Integer.parseInt( info[2].trim() );
						uut.faultType = info[3];
					}

					uuts.add( uut );
					break;

				case MATRIX:
					// read the matrix line
					StringTokenizer tn = new StringTokenizer( line );
					test = tests.get( index++ );

					while ( tn.hasMoreTokens() ) {
						uut = uuts.get( Integer.parseInt( tn.nextToken() ) );
						int count = Integer.parseInt( tn.nextToken() );

						test.calledUUTs.add( new UUTEntry( uut, count ) );
						uut.callingTests.add( test );
					}
					break;
				}
			}
		}

		in.close();

		Result result = new Result();
		result.projectName = projectName;
		result.tests = tests;
		result.uuts = uuts;

		return result;
	}
}
