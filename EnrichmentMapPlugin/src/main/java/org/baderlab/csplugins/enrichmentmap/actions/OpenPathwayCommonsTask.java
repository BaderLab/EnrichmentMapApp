package org.baderlab.csplugins.enrichmentmap.actions;

import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

public class OpenPathwayCommonsTask extends AbstractTask {

	@Inject private OpenBrowser openBrowser;
	@Inject private PropertyManager propertyManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private @Named("cytoscape3.props") CyProperty<Properties> cy3props;
	
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
		
		System.out.println(cy3props);
		
		int port = Integer.parseInt(cy3props.getProperties().getProperty("rest.port"));
		
		System.out.println("port: " + port);
		
		String returnUri = UriBuilder
			.fromPath("/enrichmentmap/expressions/{0}/{1}")
			.host("localhost")
			.scheme("http")
			.port(port)
			.build(network.getSUID(), node.getSUID())
			.toString();
		
		String pcBaseUri = propertyManager.getValue(PropertyManager.PATHWAY_COMMONS_URL);
		
		String pcUri = UriBuilder
			.fromUri(pcBaseUri)
			.queryParam("uri", returnUri)
			.build()
			.toString();
		
		System.out.println("url: " + pcUri);
		
		openBrowser.openURL(pcUri);
	}

}
