package com.vmusco.smf.utils;

/**
 * Display information in terminals
 * Various tools are proposed (moving caret, colors, ...)
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class ConsoleTools {
	private static boolean on = true;
	
	public static void switchOff(){
		on = false;
	}
	public static void switchOn(){
		on = true;
	}
	
	private static char escCode = 0x1b;
	public static final int BOLD = 1;

	private static final int FG_BASE = 30;
	private static final int BG_BASE = 40;
	
	public static final int FG_RED = FG_BASE + 1;
	public static final int FG_GREEN = FG_BASE + 2;
	public static final int FG_YELLOW = FG_BASE + 3;
	public static final int FG_BLUE = FG_BASE + 4;
	public static final int FG_MAGENTA = FG_BASE + 5;
	public static final int FG_CYAN = FG_BASE + 6;
	public static final int FG_WHITE = FG_BASE + 7;

	public static final int BG_RED = BG_BASE + 1;
	public static final int BG_GREEN = BG_BASE + 2;
	public static final int BG_YELLOW = BG_BASE + 3;
	public static final int BG_BLUE = BG_BASE + 4;
	public static final int BG_MAGENTA = BG_BASE + 5;
	public static final int BG_CYAN = BG_BASE + 6;
	public static final int BG_WHITE = BG_BASE + 7;
	
	public static void memCursor(){
		write(String.format("%c[s",escCode));
	}
	
	public static void restoreCursor(){
		write(String.format("%c[u",escCode));
	}
	
	public static void moveCursor(int row, int col){
		write(String.format("%c[%d;%df",escCode,row,col));
	}

	public static void rewindLine(){
		rewindLine(1);
	}
	
	public static void rewindLine(int nb){
		write(String.format("%c[%dA",escCode, nb));
	}
	
	public static void restartPreviousLine(){
		rewindLine();
		restartLine();
	}
	
	public static void restartLine(){
		write(String.format("%c[0G%c[0K",escCode,escCode));
	}
	
	public static void reset(){
		write(formatReset());
	}
	
	public static String formatReset(){
		return formatGraphicRendition(0);
	}

	public static void enableGraphicRendition(int... nr){
		write(formatGraphicRendition(nr));
	}
	
	public static String formatGraphicRendition(int... nr){
		String out = "";
		
		for(int n : nr){
			out += (out.length()==0?"":";")+n;
		}
		
		return String.format("%c[%sm",escCode, out);
	}

	public static void write(String string) {
		if(!on)	return;
		System.out.print(string);
	}
	
	public static void write(String string, int... style) {
		enableGraphicRendition(style);
		write(string);
		reset();
	}
	
	public static void endLine(){
		endLine(1);
	}
	
	public static void endLine(int nb){
		for(int i = 0; i<nb; i++)
			write(formatEndLine());
	}
	
	public static String formatEndLine(){
		return "\n";
	}

	public static String format(String string, int... style) {
		String ret = "";
		ret += formatGraphicRendition(style);
		ret += format(string);
		ret += formatReset();
		
		return ret;
	}
	
	public static String format(String string) {
		return string;
	}

}
