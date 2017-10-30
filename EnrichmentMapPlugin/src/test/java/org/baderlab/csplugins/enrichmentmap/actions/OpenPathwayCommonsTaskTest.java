package org.baderlab.csplugins.enrichmentmap.actions;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.task.BaseNetworkTest;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.OpenBrowser;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.name.Named;


@RunWith(JukitoRunner.class)
public class OpenPathwayCommonsTaskTest extends BaseNetworkTest {

	private EnrichmentMap map;
	
	@Before
	public void setUp(CyProperty<Properties> emProps, @Named("cytoscape3.props") CyProperty<Properties> cy3props) {
	    map = createBasicNetwork();
	    when(emProps.getProperties()).thenReturn(new Properties());
	    Properties p = new Properties();
	    p.setProperty("rest.port", "1234");
	    when(cy3props.getProperties()).thenReturn(p);
	}
	
	@After
	public void tearDown(EnrichmentMapManager emManager) {
		emManager.reset();
	}
	
	@Test
	public void testPathwayCommonsTask(
			CyNetworkManager networkManager, 
			OpenBrowser openBrowser, 
			PropertyManager propertyManager, 
			OpenPathwayCommonsTask.Factory pathwayCommonsTaskFactory
	) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		CyNode node = TestUtils.getNodes(network).get("TOP1_PLUS100");
		propertyManager.setValue(PropertyManager.PATHWAY_COMMONS_URL, "http://pathway.commons/paint");
		
		@SuppressWarnings("deprecation")
		String returnUri = URLEncoder.encode("http://localhost:1234/enrichmentmap/expressions/" + network.getSUID() + "/" + node.getSUID());
		String expectedUri = "http://pathway.commons/paint?uri=" + returnUri;
		
		OpenPathwayCommonsTask task = pathwayCommonsTaskFactory.create(node, network);
		task.run(null);
		verify(openBrowser).openURL(expectedUri);
	}
	
}
