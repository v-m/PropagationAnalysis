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
	public void fireUnboundedFound(MutantIfos mi) {
		
	}

	@Override
	public void fireIsolatedFound(MutantIfos mi) {
		
	}
	
	public abstract void fireIntersectionFound(MutantIfos mi, int nblast);
	
	/**
	 * Use {@link HistogramAnalyzer#fireIntersectionFound(MutantIfos, int)} instead, or invoke {@link HistogramAnalyzer#setPropagationSizeForNextProcessing(int)} before.
	 * ais and cis are not used !
	 */
	@Override
	public void fireIntersectionFound(MutantIfos mi, String[] ais, String[] cis) {
		fireIntersectionFound(mi, propsize);
	}
}
