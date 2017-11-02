package org.baderlab.csplugins.enrichmentmap.actions;

import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
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
	
	public String getPathwayCommonsURL() {
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map == null)
			return null;
		
		int port = Integer.parseInt(cy3props.getProperties().getProperty("rest.port"));
		
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
			.queryParam("q", getNodeLabel(map))
			.build()
			.toString();
		
		return pcUri;
	}
	
	private String getNodeLabel(EnrichmentMap map) {
		String prefix = map.getParams().getAttributePrefix();
		return Columns.NODE_GS_DESCR.get(network.getRow(node), prefix);
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		String pcUri = getPathwayCommonsURL();
		if(pcUri != null) {
			boolean success = openBrowser.openURL(pcUri);
			if(!success) {
				System.out.println("Could not open URL: " + pcUri);
			}
		}
	}

}
