package com.vmusco.pminer.analyze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 
 * @author Vincenzo Musco - http://www.vmusco.com
 */
public class HistogramRawData extends HistogramAnalyzer {
	private File f;
	private FileOutputStream fos;
	private boolean firstWrite = true;

	public HistogramRawData(File f) {
		try {
			this.f = new File(f.getAbsolutePath(), f.getName()+"_dist.raw");
			this.fos = new FileOutputStream(this.f);
			System.out.print(this.getClass().getCanonicalName()+" --- Raw distribution on: "+this.f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void executionEnded() {
		try {
			this.fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void fireIntersectionFound(String id, String in, int nblast) {
		try {
			if(firstWrite){
				firstWrite = false;
			}else{
				fos.write("\n".getBytes());
			}

			fos.write(Integer.toString(nblast).getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
