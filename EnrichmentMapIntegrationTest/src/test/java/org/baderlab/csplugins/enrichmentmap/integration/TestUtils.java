package org.baderlab.csplugins.enrichmentmap.integration;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
	
	public static File createTempFile(String path, String fileName) throws IOException {
		int dot = fileName.indexOf('.');
		String prefix = fileName.substring(0, dot);
		String suffix = fileName.substring(dot+1);
		File tempFile = File.createTempFile(prefix, suffix);
		InputStream in = TestUtils.class.getResourceAsStream(path + prefix + "." + suffix);
		assertNotNull(in);
		Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return tempFile;
	}
	
}
