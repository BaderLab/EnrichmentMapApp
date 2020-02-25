package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.GenemaniaParameters;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.view.creation.CutoffPropertiesPanel;
import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogPage;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;

public class GenemaniaDialogPage implements CardDialogPage {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private CutoffPropertiesPanel cutoffPanel;
	
	private CardDialogCallback callback;
	private NamePanel networkNamePanel;
	private CyNetwork genemaniaNetwork;
	
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return "Create Enrichment Map";
	}
	
	
	private String getOrganismName() {
		return genemaniaNetwork.getRow(genemaniaNetwork).get("organism", String.class);
	}

	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		cutoffPanel.setGeneMania();

		// Assume GenemaniaDialogShowAction did its job of validating the current network.
		genemaniaNetwork = applicationManager.getCurrentNetwork();
		
		networkNamePanel = new NamePanel("Network Name");
		networkNamePanel.setAutomaticName(getOrganismName());
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(networkNamePanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		panel.add(cutoffPanel, GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		return panel;
	}

	@Override
	public void finish() {
		EMCreationParameters params = getCreationParameters();
		
		String dataSetName = getOrganismName();
		if(dataSetName == null)
			dataSetName = "Genemania";
		
		GenemaniaParameters genemaniaParams = new GenemaniaParameters(genemaniaNetwork);
		DataSetParameters dsParams = new DataSetParameters(dataSetName, genemaniaParams);
		List<DataSetParameters> dataSets = Collections.singletonList(dsParams);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator(MissingGenesetStrategy.IGNORE);
		
		dialogTaskManager.execute(tasks);
		
		callback.setFinishButtonEnabled(true);
		callback.close();
	}
	
	private EMCreationParameters getCreationParameters() {
		EMCreationParameters params = cutoffPanel.getCreationParameters();
		params.setNetworkName(networkNamePanel.getNameText());
		return params;
	}
	
	@Override
	public void extraButtonClicked(String actionCommand) {
		if(GenemaniaDialogParameters.RESET_BUTTON_ACTION.equals(actionCommand))
			reset();
	}
	
	private void reset() {
		int result = JOptionPane.showConfirmDialog(callback.getDialogFrame(), 
				"Clear inputs and restore default values?", "EnrichmentMap: Reset", JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION) {
			cutoffPanel.reset();
			callback.setFinishButtonEnabled(false);
		}
	}

}
