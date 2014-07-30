package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * @author arkadyark
 * <p>
 * Date   July 7, 2014<br>
 * Time   09:23:28 PM<br>
 */

public class AnnotationSet {
	
	// Name of the annotation set in the dropdown
	private String name;
	// Map of clusterNumbers to the comprising clusters
	private TreeMap<Integer, Cluster> clusterMap;
	// Name of the column that was used
	private String cloudNamePrefix;
	// Used to recreate the annotation set when merging clusters
	private String clusterColumnName;
	private String nameColumnName;
	private CyNetworkView view;
	
	// Constructor used when loading from a file
	public AnnotationSet() {
		this.clusterMap = new TreeMap<Integer, Cluster>();
	}
	
	public AnnotationSet(String name, CyNetworkView view, String clusterColumnName, String nameColumnName) {
		this.name = name;
		this.clusterMap = new TreeMap<Integer, Cluster>();
		this.cloudNamePrefix = name; // name may change later, this will stay the same to link to the cloud
		this.view = view;
		// Needed to recreate the annotation set on merges
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
	}
	
	public CyNetworkView getView() {
		return view;
	}

	public void setView(CyNetworkView view) {
		this.view = view;
	}

	public void addCluster(Cluster cluster) {
		clusterMap.put(cluster.getClusterNumber(), cluster);
	}

	public void updateCoordinates() {
		for (Cluster cluster : clusterMap.values()) {
			cluster.setCoordinates(new ArrayList<double[]>());
			for (CyNode node : cluster.getNodes()) {
				View<CyNode> nodeView = view.getNodeView(node);
				if (nodeView != null) {
					// nodeView can be null when group is collapsed
					double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					double[] coordinates = {x, y};
					cluster.addCoordinates(coordinates);
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TreeMap<Integer, Cluster> getClusterMap() {
		return clusterMap;
	}

	public void setClusterMap(TreeMap<Integer, Cluster> clusterSet) {
		this.clusterMap = clusterSet;
	}

	public String getCloudNamePrefix() {
		return cloudNamePrefix;
	}

	public void setCloudNamePrefix(String cloudNamePrefix) {
		this.cloudNamePrefix = cloudNamePrefix;
	}

	public String getClusterColumnName() {
		return clusterColumnName;
	}

	public void setClusterColumnName(String clusterColumnName) {
		this.clusterColumnName = clusterColumnName;
	}
	
	public String getNameColumnName() {
		return nameColumnName;
	}

	public void setNameColumnName(String nameColumnName) {
		this.nameColumnName = nameColumnName;
	}

	public String toSessionString() {
	    	// Each annotation set is stored in the format:
	    	/*
	    	 *  1 - Cloud Name Prefix (Primary identifier)
	    	 *  2 - Annotation Set Name
	    	 *  3 - Cluster Column Name
	    	 *  4 - Name Column Name
	    	 *  5... - Each cluster, stored in the format:
	    	 *  		1 - Cluster number
	    	 *  		2 - Cluster label
	    	 *  		3 - Selected (0/1)
	    	 *  		4 - labelManuallyUpdated
	    	 *  		5... - NodeSUID x y
	    	 *  		-1 - End of cluster
	    	 *  -1 - End of annotation set
	    	 */

		// Returns the string used when saving the session
		String sessionString = "";
		sessionString += cloudNamePrefix + "\n";
		sessionString += name + "\n";
		sessionString += clusterColumnName + "\n";
		sessionString += nameColumnName + "\n";
		for (Cluster cluster : clusterMap.values()) {
			sessionString += cluster.toSessionString();
		}
		sessionString += "End of annotation set\n";
		return sessionString;
	}

	public void load(ArrayList<String> text, CySession session) {
		setCloudNamePrefix(text.get(0));
		setName(text.get(1));
		setClusterColumnName(text.get(2));
		setNameColumnName(text.get(3));
		// Update the column in the network table with the new SUID of the table
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		for (CyTable table : autoAnnotationManager.getTableManager().getAllTables(true)) {
			if (table.getTitle().equals(cloudNamePrefix + " Table")) {
				view.getModel().getRow(view.getModel()).set(cloudNamePrefix, table.getSUID());
				break;
			}
		}
		
		int lineNumber = 4;
		ArrayList<String> clusterLines = new ArrayList<String>();
		while (lineNumber < text.size()) {
			String line = text.get(lineNumber);
			if (line.equals("End of cluster")) {
				Cluster cluster = new Cluster();
				cluster.setParent(this);
				cluster.load(clusterLines, session);
				clusterMap.put(cluster.getClusterNumber(), cluster);
				clusterLines = new ArrayList<String>();
			} else {
				// Add to the growing list of lines for the annotation set
				clusterLines.add(line);
			}
			lineNumber++;
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
}
