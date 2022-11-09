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
		if(emNetworkSUID == null)
			throw new IllegalArgumentException("emNetworkSUID is null");
		if(!emManager.isEnrichmentMap(emNetworkSUID))
			throw new IllegalArgumentException("emNetworkSUID is not an EnrichmentMap network");
		if(associatedNetworkSUID == null)
			throw new IllegalArgumentException("associatedNetworkSUID is null");
		
		var network = netManager.getNetwork(associatedNetworkSUID);
		if(network == null)
			throw new IllegalArgumentException("associatedNetworkSUID is invalid");
		
		var map = emManager.getEnrichmentMap(emNetworkSUID);
		
		AssociatedApp assApp;
		try {
			assApp = AssociatedApp.valueOf(app.getSelectedValue());
		} catch(IllegalArgumentException | NullPointerException e) {
			throw new IllegalArgumentException("app is invalid");
		}
		
		map.addAssociatedNetworkID(network.getSUID());
		emManager.addAssociatedAppAttributes(network, map, assApp);
	}

}
