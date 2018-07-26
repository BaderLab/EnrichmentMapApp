package org.baderlab.csplugins.enrichmentmap.actions;

import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.client.utils.URIBuilder;
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
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;

public class OpenPathwayCommonsTask extends AbstractTask {
	
	public static final String DEFAULT_BASE_URL = "http://apps.pathwaycommons.org/paint";

	@Inject private OpenBrowser openBrowser;
	@Inject private PropertyManager propertyManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private @Named("cytoscape3.props") CyProperty<Properties> cy3props;
	
	public static interface Factory {
		OpenPathwayCommonsTask create(CyNetwork network, CyNode node);
		OpenPathwayCommonsTask createForHeatMap(CyNetwork network);
	}
	
	private final CyNode node;
	private final CyNetwork network;
	
	@AssistedInject
	public OpenPathwayCommonsTask(@Assisted CyNetwork network, @Assisted CyNode node) {
		this.node = node;
		this.network = network;
	}
	
	@AssistedInject
	public OpenPathwayCommonsTask(@Assisted CyNetwork network) {
		this.node = null;
		this.network = network;
	}
	
	public String getPathwayCommonsURL() {
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		if(map == null)
			return null;
		
		int port = Integer.parseInt(cy3props.getProperties().getProperty("rest.port"));
		String pcBaseUri = propertyManager.getValue(PropertyManager.PATHWAY_COMMONS_URL);
		String nodeLabel = getNodeLabel(map);
		
		try {
			String returnPath;
			if(node == null)
				returnPath = "/enrichmentmap/expressions/heatmap";
			else
				returnPath = String.format("/enrichmentmap/expressions/%s/%s", network.getSUID(), node.getSUID());
			
			String returnUri = new URIBuilder()
				.setScheme("http")
				.setHost("localhost")
				.setPath(returnPath)
				.setPort(port)
				.build()
				.toString();
			
			String pcUri = new URIBuilder(pcBaseUri)
				.addParameter("uri", returnUri)
				.addParameter("q", nodeLabel)
				.build()
				.toString();
		
			return pcUri;
		} catch(URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private String getNodeLabel(EnrichmentMap map) {
		String prefix = map.getParams().getAttributePrefix();
		if(node == null)
			return "EnrichmentMap";
		else
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
