package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.DESC_COLUMN;
import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.FDR_COLUMN;
import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.GENES_COLUMN;
import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.NAME_COLUMN;
import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.SUID_COLUMN;
import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.getStringTable;

import java.util.function.Predicate;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;

import com.google.inject.Inject;

public class StringDialogPage extends NetworkLoadDialogPage {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CyTableManager tableManager;
	
	private CyNetwork stringNetwork;
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		JPanel panel = super.createBodyPanel(callback);
		networkNamePanel.setAutomaticName("EnrichmentMap from STRING");
		// Assume StringDialogShowAction did its job of validating the current network.
		stringNetwork = applicationManager.getCurrentNetwork();
		return panel;
	}

	
	@Override
	public DataSetParameters getDataSetParameters() {
		String dataSetName = "STRING enrichment";
		
		Predicate<CyRow> filter = row -> {
			Long suid = row.get(SUID_COLUMN, Long.class);
			return stringNetwork.getSUID().equals(suid);
		};
		
		CyTable table = getStringTable(tableManager);
		
		TableParameters tableParams = new TableParameters(table, NAME_COLUMN, GENES_COLUMN, FDR_COLUMN, DESC_COLUMN, filter);
		return new DataSetParameters(dataSetName, tableParams);
	}
	
}
