package com.vmusco.pminer.analyze;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.util.HashMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.vmusco.pminer.UseGraph;
import com.vmusco.smf.analysis.MutantIfos;
import com.vmusco.smf.analysis.ProcessStatistics;

public class HistogramForSizesOfSubgraphs extends MutantTestAnalyzer {
	private HashMap<Double, Double> histogramData = new HashMap<Double, Double>();
	private String project;
	private String folder;
	private int binSize;
	private double max=0;

	public HistogramForSizesOfSubgraphs(String folder, String project, int binSize) {
		this.project = project;
		this.folder = folder;
		this.binSize = binSize;
	}

	@Override
	public void fireIntersectionFound(ProcessStatistics ps, MutantIfos mi, UseGraph basin) {
		
		int bassinSize = basin.getBasinGraph().getNbNodes();
		double putInBin = Math.ceil(bassinSize/this.binSize);
		
		if(putInBin < 0)
			return;
		
		if(!histogramData.containsKey(putInBin)){
			histogramData.put(putInBin, 0d);
		}

		histogramData.put(putInBin, histogramData.get(putInBin) + 1);
		if(putInBin > max)
			max = putInBin;
	}

	@Override
	public void fireExecutionEnded() {
		XYSeries series = new XYSeries(project);

		/*System.out.println("===HISTOGRAM DATA END===");
		System.out.println("");
		System.out.println("Resumes:");*/

		//Integer[] ii = (Integer[]) histogramData.keySet().toArray(new Integer[histogramData.keySet().size()]);
		//Arrays.sort(ii);

		for(double k : histogramData.keySet()){
			//System.out.println(k+" => "+histogramData.get(k));
			series.add(k, histogramData.get(k));
		}
		
		final XYSeriesCollection dataset = new XYSeriesCollection(series);

		JFreeChart chart = ChartFactory.createXYBarChart(
				null,
				"#nodes", 
				false,
				"Occurences", 
				dataset,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domain.setRange(-0.5, max+0.5);
        final int bs = this.binSize;
        domain.setNumberFormatOverride(new DecimalFormat() {
		    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
		        return toAppendTo.append((int)(number * bs));
		    }
		});
        domain.setVerticalTickLabels(true);
        
	    
		try {
			ChartUtilities.saveChartAsPNG(new File(folder, project+"_distrib.png"), chart, 1024, 768);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
