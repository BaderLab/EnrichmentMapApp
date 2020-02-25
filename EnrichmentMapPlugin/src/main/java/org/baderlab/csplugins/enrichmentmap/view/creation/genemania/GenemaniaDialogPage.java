package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.GenemaniaParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;

public class GenemaniaDialogPage extends NetworkLoadDialogPage {

	@Inject private CyApplicationManager applicationManager;
	
	private CyNetwork genemaniaNetwork;
	
	
	private String getOrganismName() {
		return genemaniaNetwork.getRow(genemaniaNetwork).get("organism", String.class);
	}

	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		JPanel panel = super.createBodyPanel(callback);
		networkNamePanel.setAutomaticName(getOrganismName());
		// Assume GenemaniaDialogShowAction did its job of validating the current network.
		genemaniaNetwork = applicationManager.getCurrentNetwork();
		return panel;
	}

	@Override
	public DataSetParameters getDataSetParameters() {
		String dataSetName = getOrganismName();
		if(dataSetName == null)
			dataSetName = "Genemania";
		
		GenemaniaParameters params = new GenemaniaParameters(genemaniaNetwork);
		return new DataSetParameters(dataSetName, params);
	}
	
}
