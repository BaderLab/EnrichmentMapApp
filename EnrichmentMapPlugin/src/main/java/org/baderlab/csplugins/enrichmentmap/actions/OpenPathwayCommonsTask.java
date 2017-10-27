package org.baderlab.csplugins.enrichmentmap.actions;

import javax.ws.rs.core.UriBuilder;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class OpenPathwayCommonsTask extends AbstractTask {

	@Inject private OpenBrowser openBrowser;
	@Inject private EnrichmentMapManager emManager;
	
	public static interface Factory {
		OpenPathwayCommonsTask create(CyNode node, CyNetwork network);
	}
	
	private final CyNode node;
	private final CyNetwork network;
	
	@Inject
	public OpenPathwayCommonsTask(@Assisted CyNode node, @Assisted CyNetwork network) {
		this.node = node;
		this.network = network;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map == null)
			return;
		
		String returnUri = UriBuilder
			.fromPath("localhost/enrichmentmap/expressions/{0}/{1}")
			.scheme("http")
			.port(1234)
			.build(network.getSUID(), node.getSUID())
			.toString();
		
		String pcUri = UriBuilder
			.fromPath("localhost:3000/paint")
			.scheme("http")
			.queryParam("uri", returnUri)
			.build()
			.toString();
		
		System.out.println("url: " + pcUri);
		
		openBrowser.openURL(pcUri);
	}

}
