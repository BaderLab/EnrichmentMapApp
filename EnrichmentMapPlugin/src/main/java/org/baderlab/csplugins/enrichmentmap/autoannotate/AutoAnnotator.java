package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.awt.Paint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
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
	private HashMap<Integer, ArrayList<double[]>> clustersToCoordinates;
	private AnnotationManager annotationManager;
	private CyServiceRegistrar registrar;

	public AutoAnnotator(CySwingApplication application, OpenBrowser browser, 
			CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
			AnnotationManager annotationManager, CyServiceRegistrar registrar) {
		// get all of the nodes and their corresponding clusters
    	this.application = application;
    	this.browser = browser;
    	
    	this.registrar = registrar;
    	
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
    	
    	this.annotationManager = annotationManager;
    	List<CyNode> nodes = this.network.getNodeList();
    	for (CyNode node : nodes) {
    		this.network.getRow(node).get("name", String.class);
    	}
		this.clustersToNodes = mapClustersToNodes();
		this.clustersToCoordinates = mapClustersToCoordinates();
		drawClusters();
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
	
	private HashMap<Integer, ArrayList<double[]>> mapClustersToCoordinates() {
		HashMap<Integer, ArrayList<double[]>> clustersToCoordinates = new HashMap<Integer, ArrayList<double[]>>();
		for (Integer clusterNumber : this.clustersToNodes.keySet()) {
			ArrayList<double[]> coordinatesList = new ArrayList<double[]>();
			for (CyNode node : this.clustersToNodes.get(clusterNumber)) {
				View<CyNode> nodeView = this.networkView.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				coordinatesList.add(coordinates);				
			}
			clustersToCoordinates.put(clusterNumber,coordinatesList);
		}
		return clustersToCoordinates;
	}
	
	private void drawClusters() {
    	AnnotationFactory<ShapeAnnotation> shapeFactory = (AnnotationFactory<ShapeAnnotation>) registrar.getService(AnnotationFactory.class, "(type=ShapeAnnotation.class)");    	
    	double padding = 1.7;
    	double min_size = 10.0;
    	for (int clusterNumber : this.clustersToCoordinates.keySet()) {
    		double xmin = 1000000;
			double ymin = 1000000;
    		double xmax = -1000000;
    		double ymax = -1000000;
    		for (double[] coordinates : clustersToCoordinates.get(clusterNumber)) {
    			xmin = coordinates[0] < xmin ? coordinates[0] : xmin;
    			xmax = coordinates[0] > xmax ? coordinates[0] : xmax;
    			ymin = coordinates[1] < ymin ? coordinates[1] : ymin;
    			ymax = coordinates[1] > ymax ? coordinates[1] : ymax;
    		}
    		
    		double zoom = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);
    		
    		// This magic number 10 floating around here isn't good style (nor are the other ones after this)
    		double width = (xmax - xmin)*zoom;
    		width = width > min_size ? width : min_size;
    		double height = (ymax - ymin)*zoom;
    		height = height > min_size ? height : min_size;
    		
    		HashMap<String, String> arguments = new HashMap<String,String>();
    		arguments.put("x", String.valueOf(xmin - 20*padding)); // put your values for the annotation position
    		arguments.put("y", String.valueOf(ymin - 20*padding)); // put your values for the annotation position
    		arguments.put("zoom", String.valueOf(zoom));
    		arguments.put("canvas", "foreground");
    		ShapeAnnotation ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, this.networkView, arguments);
    		ellipse.setShapeType("Ellipse");
    		ellipse.setSize(width*padding, height*padding);
    		this.annotationManager.addAnnotation(ellipse);
    	}
	}
}