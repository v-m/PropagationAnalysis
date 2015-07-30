package com.vmusco.pminer.analyze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;

public class HistogramRawData extends MutantTestAnalyzer {
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
	public void fireIntersectionFound(ProcessStatistics ps, String mutationId, MutantIfos mi, String[] graphDetermined, UseGraph basin, long propatime){
		
		try {
			int bassinSize = basin.getBasinGraph().getNbNodes();

			if(firstWrite){
				firstWrite = false;
			}else{
				fos.write("\n".getBytes());
			}

			fos.write(Integer.toString(bassinSize).getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void fireExecutionEnded() {
		try {
			this.fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
