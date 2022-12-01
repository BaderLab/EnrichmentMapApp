package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults.ParseGSEAEnrichmentStrategy;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask.UnsortedRanksStrategy;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.task.TaskErrorStrategies;
import org.baderlab.csplugins.enrichmentmap.view.creation.CutoffPropertiesPanel;
import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogPage;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;

public abstract class NetworkLoadDialogPage implements CardDialogPage {
	
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	
	protected CardDialogCallback callback;
	protected NamePanel networkNamePanel;
	
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return "Create Enrichment Map";
	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		cutoffPanel.setGeneMania();
		
		networkNamePanel = new NamePanel("Network Name");
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(networkNamePanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		panel.add(cutoffPanel, GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		return panel;
	}

	
	protected EMCreationParameters getCreationParameters() {
		EMCreationParameters params = cutoffPanel.getCreationParameters();
		params.setNetworkName(networkNamePanel.getNameText());
		return params;
	}
	
	@Override
	public void extraButtonClicked(String actionCommand) {
		if("reset".equals(actionCommand))
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
	
	abstract DataSetParameters getDataSetParameters();
	
	@Override
	public void finish() {
		EMCreationParameters params = getCreationParameters();
		
		DataSetParameters dsParams = getDataSetParameters();
		List<DataSetParameters> dataSets = Collections.singletonList(dsParams);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		var strategies = new TaskErrorStrategies(MissingGenesetStrategy.IGNORE, ParseGSEAEnrichmentStrategy.FAIL_IMMEDIATELY, UnsortedRanksStrategy.LOG_WARNING);
		TaskIterator tasks = taskFactory.createTaskIterator(strategies);
		
		dialogTaskManager.execute(tasks);
		
		callback.setFinishButtonEnabled(true);
		callback.close();
	}

}
