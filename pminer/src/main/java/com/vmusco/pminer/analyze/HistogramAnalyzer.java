package com.vmusco.pminer.analyze;

import com.vmusco.smf.analysis.MutantIfos;

public abstract class HistogramAnalyzer extends MutantTestAnalyzer {
	private int propsize;

	
	public void setPropagationSizeForNextProcessing(int size){
		this.propsize = size;
	}
	

	public int getPropagationSizeForNextProcessing(){
		return this.propsize;
	}
	

	@Override
	public void unboundedFound(String id, String in) {
		
	}

	@Override
	public void isolatedFound(String id, String in) {
		
	}
	
	public abstract void fireIntersectionFound(String id, String in, int nblast);
	
	/**
	 * Use {@link HistogramAnalyzer#fireIntersectionFound(String, String, int)} instead, or invoke {@link HistogramAnalyzer#setPropagationSizeForNextProcessing(int)} before.
	 * ais and cis are not used !
	 */
	@Override
	public void intersectionFound(String id, String in, String[] ais, String[] cis) {
		fireIntersectionFound(id, in, propsize);
	}
}
