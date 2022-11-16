package org.baderlab.csplugins.enrichmentmap.commands;

import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

public class AssociateNetworkCommandTask extends AbstractTask {
	
	@Tunable
	public Long emNetworkSUID;
	
	@Tunable
	public Long associatedNetworkSUID;
	
	@Tunable(description = "One of GENEMANIA, STRING, AUTOANNOTATE")
	public ListSingleSelection<String> app;

	
	@Inject private CyNetworkManager netManager;
	@Inject private EnrichmentMapManager emManager;
	
	public AssociateNetworkCommandTask() {
		app = new ListSingleSelection<>(
				AssociatedApp.GENEMANIA.name(), 
				AssociatedApp.STRING.name(), 
				AssociatedApp.AUTOANNOTATE.name()
			);
	}
	
	@Override
	public void run(TaskMonitor tm) {
		// AutoAnnotate may call this command on non-EM networks, so we need to fail gracefully, ie don't throw an Exception.
		if(emNetworkSUID == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "emNetworkSUID is null");
			return;
		}
		if(associatedNetworkSUID == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "associatedNetworkSUID is null");
			return;
		}
		if(!emManager.isEnrichmentMap(emNetworkSUID)) {
			tm.showMessage(TaskMonitor.Level.WARN, "emNetworkSUID is not an EnrichmentMap network");
			return;
		}
		var network = netManager.getNetwork(associatedNetworkSUID);
		if(network == null) {
			tm.showMessage(TaskMonitor.Level.ERROR, "associatedNetworkSUID is invalid");
			return;
		}
		
		AssociatedApp assApp;
		try {
			assApp = AssociatedApp.valueOf(app.getSelectedValue());
		} catch(IllegalArgumentException | NullPointerException e) {
			tm.showMessage(TaskMonitor.Level.ERROR, "app is invalid");
			return;
		}
		
		var map = emManager.getEnrichmentMap(emNetworkSUID);
		map.addAssociatedNetworkID(network.getSUID());
		emManager.addAssociatedAppAttributes(network, map, assApp);
	}

}
