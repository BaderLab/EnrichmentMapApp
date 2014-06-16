package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 12, 2014<br>
 * Time   05:32 PM<br>
 */

public final class AutoAnnotator {
    
    private CySwingApplication application;
    private OpenBrowser browser;
	private CyNetwork network;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkView networkView;
	private HashMap<Integer, ArrayList<CyNode>> clustersToNodes;
	private HashMap<Integer, double[]> clustersToCoordinates;

	public AutoAnnotator(CySwingApplication application, OpenBrowser browser, 
			CyNetworkManager networkManager, CyNetworkViewManager networkViewManager) {
		// get all of the nodes and their corresponding clusters
    	this.application = application;
    	this.browser = browser;
    	try {
    		this.network = getEMNetwork(networkManager.getNetworkSet().iterator());
    	} catch (Exception e) {
    		// TODO - this should make some pop-up window
    		System.out.println("Load the Enrichment Map first!");
    	}
    	this.networkViewManager = networkViewManager;
    	try {
    		this.networkView = getEMNetworkView();
    	} catch (Exception e) {
    		// TODO - this should make some pop-up window
    		System.out.println("Could not find network view!");
    	}
    	List<CyNode> nodes = this.network.getNodeList();
    	for (CyNode node : nodes) {
    		this.network.getRow(node).get("name", String.class);
    	}
		this.clustersToNodes = mapClustersToNodes();
		this.clustersToCoordinates = mapClustersToCoordinates();
		this.application = application;
    }
	
	private CyNetwork getEMNetwork(Iterator<CyNetwork> allNetworks) throws Exception {
		// TODO - change this to prompt the user for the name of the EnrichmentMap network (maybe only if it can't be found)
		// TODO - Make the exception more meaningful
		while (allNetworks.hasNext()) {
			CyNetwork network = allNetworks.next();
			if (network.toString().contains("Enrichment Map")) {
				return network;
			}
		}
		throw new Exception();
	}
	
	private CyNetworkView getEMNetworkView() throws Exception {
    	if (this.networkViewManager.viewExists(this.network)) {
    		return (CyNetworkView) this.networkViewManager.getNetworkViews(this.network).toArray()[0];
    	}
    	else {
    		throw new Exception();
    	}
	}
	
	private HashMap<Integer, ArrayList<CyNode>> mapClustersToNodes() {
		HashMap<Integer, ArrayList<CyNode>> clustersToNodes = new HashMap<Integer, ArrayList<CyNode>>();
		List<CyNode> nodes = network.getNodeList();
		for (CyNode node : nodes) {
			// this should work for all algorithms, and prompt the user if none are available
			Integer clusterNumber = this.network.getRow(node).get("__mclCluster", Integer.class);
			// empty values (no cluster) are given null
			if (clusterNumber != null) {
				// Populate the HashMap
				if (!clustersToNodes.containsKey(clusterNumber)) {
					clustersToNodes.put(clusterNumber, new ArrayList<CyNode>());
				}
				clustersToNodes.get(clusterNumber).add(node);
			}
		}
		return clustersToNodes;
	}
	
	private HashMap<Integer, double[]> mapClustersToCoordinates() {
		HashMap<Integer, double[]> clustersToCoordinates = new HashMap<Integer, double[]>();
		for (Integer clusterNumber : this.clustersToNodes.keySet()) {
			for (CyNode node : this.clustersToNodes.get(clusterNumber)) {
				View<CyNode> nodeView = this.networkView.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				clustersToCoordinates.put(clusterNumber, coordinates);
			}
		}
		return clustersToCoordinates;
	}
}