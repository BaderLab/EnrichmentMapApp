package org.baderlab.csplugins.enrichmentmap.heatmap.task;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class HeatMapHierarchicalClusterQuestionTask extends AbstractTask implements ObservableTask {

	private static final String CLUSTER = "Cluster results anyways";
	private static final String NO_SORT = "Do not cluster the results";

	private int numConditions = 0;
	private int numConditions2 = 0;

	private HeatMapPanel heatmapPanel;
	private EnrichmentMap map;
	private EnrichmentMapParameters params;
	private HeatMapParameters hmParams;
	private CySwingApplication swingApplication;

	public HeatMapHierarchicalClusterQuestionTask(
			CySwingApplication swingApplication, int numConditions,
			int numConditions2, HeatMapPanel heatmapPanel, EnrichmentMap map) {
		this.swingApplication = swingApplication;
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.map = map;
		this.params = map.getParams();
		this.hmParams = this.params.getHmParams();
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		String clusterResponse = showInputDialog();
		if(clusterResponse == null)
			return;
		
		if(clusterResponse.equals(CLUSTER)) {
			HeatMapHierarchicalClusterTask clusterTask = new HeatMapHierarchicalClusterTask(numConditions, numConditions2, heatmapPanel, map);
			this.insertTasksAfterCurrentTask(clusterTask);
		} else {
			hmParams.setSort(HeatMapParameters.Sort.NONE);
		}
	}


	private String showInputDialog() {
		Object[] options = {NO_SORT, CLUSTER};
		String message = "<html>The combination of the selected gene sets contains more than 1000 genes.<BR><BR>"
				       + "Clustering may take a while. Would you like to cluster anyways?<BR></html>";
		
		String response = (String) JOptionPane.showInputDialog(
                swingApplication.getJFrame(),
                message,
                "EnrichmentMap: Clustering",
                JOptionPane.PLAIN_MESSAGE,
                getDialogIcon(),
                options,
                NO_SORT);
		
		return response;
	}
	
	private Icon getDialogIcon() {
        URL iconURL = getClass().getResource("enrichmentmap_logo.png");
        return iconURL == null ? null : new ImageIcon(iconURL);
	}
	
	public <R> R getResults(Class<? extends R> arg0) {
		return null;
	}

}
