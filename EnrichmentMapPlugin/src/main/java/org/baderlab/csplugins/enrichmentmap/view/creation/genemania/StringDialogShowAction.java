package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogShowAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class StringDialogShowAction extends CardDialogShowAction {

	private static final String TABLE_NAME = "STRING Enrichment: All";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyTableManager tableManager;
	@Inject private Provider<JFrame> jframeProvider;
	@Inject private PropertyManager propertyManager;
	
	public StringDialogShowAction() {
		super(StringDialogParameters.class, "Create from STRING...");
	}
	
	public void showDialog() {
		CyNetwork network = applicationManager.getCurrentNetwork();
		if(network == null) {
			showNoNetworkError();
			return;
		}
		if(!hasRequiredData(network)) {
			showNotStringNetworkError();
			return;
		}
		super.showDialog();
	}

	private void showNoNetworkError() {
		JOptionPane.showMessageDialog(jframeProvider.get(), 
				"Please select a network first.", 
				"EnrichmentMap: Create from STRING", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void showNotStringNetworkError() {
		JOptionPane.showMessageDialog(jframeProvider.get(), 
				"The current network does not contain STRING enrichment data.\n"
				+ "Please run \"Functional enrichment\" on the STRING network first.", 
				"EnrichmentMap: Create from STRING", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	public static CyTable getStringTable(CyTableManager tableManager) {
		for(CyTable t: tableManager.getAllTables(true)) {
			if(t.getTitle().equalsIgnoreCase(TABLE_NAME)) {
				return t;
			}
		}
		return null;
	}
	
	private boolean hasRequiredData(CyNetwork network) {
		final String FDR_COLUMN   = propertyManager.getValue(PropertyManager.STRING_COLUMN_FDR);
		final String NAME_COLUMN  = propertyManager.getValue(PropertyManager.STRING_COLUMN_NAME);
		final String GENES_COLUMN = propertyManager.getValue(PropertyManager.STRING_COLUMN_GENES);
		final String SUID_COLUMN  = propertyManager.getValue(PropertyManager.STRING_COLUMN_SUID);
		
		CyTable table = getStringTable(tableManager);
		if(table == null)
			return false;
		if(table.getColumn(FDR_COLUMN) == null)
			return false;
		if(table.getColumn(NAME_COLUMN) == null)
			return false;
		if(table.getColumn(GENES_COLUMN) == null)
			return false;
		if(table.getColumn(SUID_COLUMN) == null)
			return false;
		int count = table.countMatchingRows("network.SUID", network.getSUID());
		if(count <= 0)
			return false;
		return true;
	}

}
