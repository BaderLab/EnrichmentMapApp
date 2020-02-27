package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.GenemaniaParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;

import com.google.inject.Inject;

public class GenemaniaDialogPage extends NetworkLoadDialogPage {

	@Inject private CyApplicationManager applicationManager;
	@Inject private PropertyManager propertyManager;
	
	private CyNetwork genemaniaNetwork;
	
	
	private String getOrganismName() {
		final String ORGANISM_COLUMN = propertyManager.getValue(PropertyManager.GENEMANIA_COLUMN_ORGANISM);
		return genemaniaNetwork.getRow(genemaniaNetwork).get(ORGANISM_COLUMN, String.class);
	}

	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		JPanel panel = super.createBodyPanel(callback);
		opened();
		return panel;
	}
	
	@Override
	public void opened() {
		genemaniaNetwork = applicationManager.getCurrentNetwork();
		networkNamePanel.setAutomaticName(getOrganismName());
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
