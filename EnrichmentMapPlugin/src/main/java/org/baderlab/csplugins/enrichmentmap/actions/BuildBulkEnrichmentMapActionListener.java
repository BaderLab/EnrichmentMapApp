package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.task.BuildBulkEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;

/**
 * Created by IntelliJ IDEA. User: User Date: 1/28/11 Time: 10:07 AM To change
 * this template use File | Settings | File Templates.
 */
public class BuildBulkEnrichmentMapActionListener implements ActionListener {

	private BulkEMCreationPanel inputPanel;

	/**
	 * @param panel Enrichment map input panel
	 */
	public BuildBulkEnrichmentMapActionListener(BulkEMCreationPanel panel) {
		this.inputPanel = panel;

	}

	/**
	 * Creates a new task, checks the info in the parameters for the minimum
	 * amount of information
	 *
	 * @param event
	 */
	public void actionPerformed(ActionEvent event) {

		//make sure that the minimum information is set in the current set of parameters
		EnrichmentMapParameters params = inputPanel.getParams();

		//set the bulk em flag
		params.setBulkEM(true);

		BuildBulkEnrichmentMapTask new_map = new BuildBulkEnrichmentMapTask(params);
		//boolean success = TaskManager.executeTask(new_map,config);

	}

}
