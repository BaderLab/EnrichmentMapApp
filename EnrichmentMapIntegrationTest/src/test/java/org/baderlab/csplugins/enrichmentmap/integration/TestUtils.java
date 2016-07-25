package org.baderlab.csplugins.enrichmentmap.integration;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class TestUtils {

	public static Map<String,CyNode> getNodes(CyNetwork network) {
		Map<String,CyNode> nodes = new HashMap<>();
	   	for(CyNode node : network.getNodeList()) {
	   		nodes.put(network.getRow(node).get("name", String.class), node);
	   	}
	   	return nodes;
	}
	
	public static Map<String,CyEdge> getEdges(CyNetwork network) {
		Map<String,CyEdge> edges = new HashMap<>();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.put(network.getRow(edge).get("name", String.class), edge);
	   	}
	   	return edges;
	}
	
	public static EdgeSimilarities getEdgeSimilarities(CyNetwork network) {
		EdgeSimilarities edges = new EdgeSimilarities();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.addEdge(network.getRow(edge).get("name", String.class), edge);
	   	}
	   	return edges;
	}
	
}
