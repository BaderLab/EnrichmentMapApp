package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import static org.baderlab.csplugins.enrichmentmap.view.creation.genemania.StringDialogShowAction.getStringTable;

import java.util.function.Predicate;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
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
	@Inject private PropertyManager propertyManager;
	
	private CyNetwork stringNetwork;
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		JPanel panel = super.createBodyPanel(callback);
		networkNamePanel.setAutomaticName("EnrichmentMap from STRING");
		opened();
		return panel;
	}

	@Override
	public void opened() {
		stringNetwork = applicationManager.getCurrentNetwork();
	}
	
	@Override
	public DataSetParameters getDataSetParameters() {
		final String FDR_COLUMN   = propertyManager.getValue(PropertyManager.STRING_COLUMN_FDR);
		final String NAME_COLUMN  = propertyManager.getValue(PropertyManager.STRING_COLUMN_NAME);
		final String GENES_COLUMN = propertyManager.getValue(PropertyManager.STRING_COLUMN_GENES);
		final String SUID_COLUMN  = propertyManager.getValue(PropertyManager.STRING_COLUMN_SUID);
		final String DESC_COLUMN  = propertyManager.getValue(PropertyManager.STRING_COLUMN_DESC);
		
		String dataSetName = "STRING enrichment";
		
		Predicate<CyRow> filter = row -> {
			Long suid = row.get(SUID_COLUMN, Long.class);
			return stringNetwork.getSUID().equals(suid);
		};
		
		CyTable table = getStringTable(tableManager);
		
		TableParameters tableParams = 
				new TableParameters(table, NAME_COLUMN, GENES_COLUMN, null, FDR_COLUMN, DESC_COLUMN, filter);
		
		return new DataSetParameters(dataSetName, tableParams, null);
	}
	
}
