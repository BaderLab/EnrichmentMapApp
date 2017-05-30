package org.baderlab.csplugins.enrichmentmap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;

public class TestUtils {

	public static Map<String,CyNode> getNodes(CyNetwork network) {
		Map<String,CyNode> nodes = new HashMap<>();
	   	for(CyNode node : network.getNodeList()) {
	   		nodes.put(network.getRow(node).get(CyNetwork.NAME, String.class), node);
	   	}
	   	return nodes;
	}
	
	public static Map<String,CyEdge> getEdges(CyNetwork network) {
		Map<String,CyEdge> edges = new HashMap<>();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.put(network.getRow(edge).get(CyNetwork.NAME, String.class), edge);
	   	}
	   	return edges;
	}
	
	public static Map<String,CyEdge> getSignatureEdges(CyNetwork network, String prefix, String sigSetName) {
		Map<String,CyEdge> edges = new HashMap<>();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		CyRow row = network.getRow(edge);
	   		String sigName = Columns.EDGE_SIG_DATASET.get(row,prefix);
			if(sigName != null && sigName.equals(sigSetName)) {
	   			edges.put(row.get(CyNetwork.NAME, String.class), edge);
	   		}
	   	}
	   	return edges;
	}
	
	public static EdgeSimilarities getEdgeSimilarities(CyNetwork network) {
		EdgeSimilarities edges = new EdgeSimilarities();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.addEdge(network.getRow(edge).get(CyNetwork.NAME, String.class), edge);
	   	}
	   	return edges;
	}
	
	public static CyServiceRegistrar mockServiceRegistrar() {
		CyNetworkManager netManager = mock(CyNetworkManager.class);
		
		CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyNetworkManager.class)).thenReturn(netManager);
		
		return serviceRegistrar;
	}
}
