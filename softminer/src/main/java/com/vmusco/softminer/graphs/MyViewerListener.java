package com.vmusco.softminer.graphs;

import java.util.HashMap;

import org.graphstream.ui.swingViewer.Viewer;
import org.graphstream.ui.swingViewer.ViewerListener;

public class MyViewerListener implements ViewerListener{
	private boolean alive = true;
	
	private GraphStream sg;
	private Viewer v;
	private double[] posmem;
	
	private String lastCycle = null;
	private int nbCycles;
	
	public MyViewerListener(GraphStream sg, Viewer viewer) {
		this.sg = sg;
		this.v = viewer;
	}
	
	@Override
	public void viewClosed(String viewName) {
		alive = false;
	}

	@Override
	public void buttonPushed(String id) {
		v.disableAutoLayout();
		posmem = getCoordOfNode(id);
	}

	private double[] getCoordOfNode(String id){
		Object[] coords = (Object[])sg.getGraph().getNode(id).getAttribute("xyz");
		return new double[]{(double)coords[0], (double)coords[1]};
	}
	
	@Override
	public void buttonReleased(String id) {
		HashMap<String, String> na = sg.getNodeAttribute(id);
		double[] posnow = getCoordOfNode(id);
		
		if(Math.abs(posnow[0] - posmem[0]) < 0.01 && Math.abs(posnow[1] - posmem[1]) < 0.01){
		
			if(sg.getNodeLabel(id) == null){
				sg.setNodeLabel(id, id);
				na.put("text-background-mode", "plain");
				na.put("text-background-color", "grey");
				na.put("text-padding", "5px");
				na.put("text-alignment", "under");
			}else{
				sg.setNodeLabel(id, null);
				na.remove("text-background-mode");
				na.remove("text-background-color");
				na.remove("text-padding");
				na.remove("text-alignment");
				
				if(lastCycle != null && lastCycle.equals(id)){
					if(nbCycles >= 3){
						sg.getGraph().removeNode(id);
						nbCycles = 0;
						lastCycle = null;
					}else{
						nbCycles++;
					}
				}else{
					lastCycle = id;
					nbCycles = 1;
				}
			}
			
			sg.setNodeAttribute(id, na);
		
		}
		
	}
	
	public boolean isAlive() {
		return alive;
	}

}
