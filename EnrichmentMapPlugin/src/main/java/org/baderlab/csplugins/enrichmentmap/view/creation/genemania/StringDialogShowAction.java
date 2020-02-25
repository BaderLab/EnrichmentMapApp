package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
	
	public static final String NAME_COLUMN  = "term name";
	public static final String FDR_COLUMN   = "FDR value";
	public static final String GENES_COLUMN = "genes";
	public static final String DESC_COLUMN  = "description";
	public static final String SUID_COLUMN  = "network.SUID";
	
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyTableManager tableManager;
	@Inject private Provider<JFrame> jframeProvider;
	
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
				"The current network does not contain STRING enrichment data.", 
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
	
	// enrichmentmap build-table table="STRING Enrichment: All" pValueColumn="FDR value" nameColumn="term name" genesColumn="genes"
	private boolean hasRequiredData(CyNetwork network) {
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
