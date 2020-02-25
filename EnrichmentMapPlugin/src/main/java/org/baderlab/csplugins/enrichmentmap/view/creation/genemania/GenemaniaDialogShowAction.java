package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogShowAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class GenemaniaDialogShowAction extends CardDialogShowAction {

	@Inject private CyApplicationManager applicationManager;
	@Inject private Provider<JFrame> jframeProvider;
	
	public GenemaniaDialogShowAction() {
		super(GenemaniaDialogParameters.class, "Create from Genemania...");
	}
	
	public void showDialog() {
		CyNetwork network = applicationManager.getCurrentNetwork();
		if(network == null) {
			showNoNetworkError();
			return;
		}
		if(!hasRequiredData(network)) {
			showNotGenemaniaNetworkError();
			return;
		}
		
//		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
//		boolean confirm = showConfirmDialog(networkName);
//		if(confirm) {
//			createEMNetworkFrom(network);
//		}
		super.showDialog();
	}

//	private boolean showConfirmDialog(String networkName) {
//		int r = JOptionPane.showConfirmDialog(jframeProvider.get(), 
//				"Create an EnrichmentMap network from the Genemania network '" + networkName + "'?", 
//				"EnrichmentMap: Create from Genemania", 
//				JOptionPane.OK_CANCEL_OPTION);
//		return r == JOptionPane.OK_OPTION;
//	}
	
	private void showNoNetworkError() {
		JOptionPane.showMessageDialog(jframeProvider.get(), 
				"Please select a network first.", 
				"EnrichmentMap: Create from Genemania", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	private void showNotGenemaniaNetworkError() {
		JOptionPane.showMessageDialog(jframeProvider.get(), 
				"The current network does not contain Genemania annotation data.", 
				"EnrichmentMap: Create from Genemania", 
				JOptionPane.ERROR_MESSAGE);
	}
	
//	private void createEMNetworkFrom(CyNetwork genemaniaNetwork) {
//		
//		
//	}
	
	private boolean hasRequiredData(CyNetwork network) {
		CyTable table = network.getDefaultNetworkTable();
		
		CyColumn column = table.getColumn("annotations");
		if(column == null)
			return false;
		
		CyRow row = table.getRow(network.getSUID());
		if(row == null)
			return false;
		
		String jsonString = row.get("annotations", String.class);
		if(jsonString == null)
			return false;
		
		try {
			Gson gson = new GsonBuilder().create();
			Type listType = new TypeToken<ArrayList<GenemaniaAnnotation>>(){}.getType();
			List<GenemaniaAnnotation> fromJson = gson.fromJson(jsonString, listType);
			if(fromJson == null || fromJson.isEmpty()) {
				return false;
			} else {
				System.out.println("found it: " + fromJson.get(0));
			}
		} catch(JsonParseException e) {
			return false;
		}
		
		// survived the gauntlet
		return true;
	}
}
