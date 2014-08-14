package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.group.CyGroup;
import org.cytoscape.session.CySession;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 18, 2014<br>
 * Time   12:47 PM<br>
 * <p>
 * Class to store the relevant sets of data corresponding to each cluster
 */

public class Cluster implements Comparable<Cluster> {
	
	private int clusterNumber;
	private String cloudName;
	private CyGroup group;
	private ArrayList<CyNode> nodeList;
	private ArrayList<double[]> coordinates;
	private int size;
	private String label;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	private AnnotationSet parent;
	private boolean selected;
	private ArrayList<WordInfo> wordInfos;
	
	// Used when initializing from a session file
	public Cluster() {
		this.coordinates = new ArrayList<double[]>();
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodeList = new ArrayList<CyNode>();
		size = 0;
	}
	
	// Used when creating clusters in the task
	public Cluster(int clusterNumber, AnnotationSet parent, CyGroup group) {
		this(clusterNumber, parent);
		this.group = group;
	}
	
	public Cluster(int clusterNumber, AnnotationSet parent) {
		this.clusterNumber = clusterNumber;
		this.parent = parent;
		this.cloudName = parent.getName() + " Cloud " + clusterNumber;
		this.coordinates = new ArrayList<double[]>();
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodeList = new ArrayList<CyNode>();
		size = 0;
		selected = false;
	}
	
	public int getClusterNumber() {
		return clusterNumber;
	}
	
	public CyGroup getGroup() {
		return group;
	}
	
	public AnnotationSet getParent() {
		return parent;
	}
	
	public void setParent(AnnotationSet annotationSet) {
		parent = annotationSet;
	}
	
	public boolean isCollapsed() {
		if (group != null) {
			return group.isCollapsed(parent.getView().getModel());
		} return false;
	}
	
	public CyNode getGroupNode() {
		if (group != null) {
			return group.getGroupNode();
		}
		return null;
	}
	
	public ArrayList<double[]> getCoordinates() {
		return this.coordinates;
	}
	
	public void setCoordinates(ArrayList<double[]> coordinates) {
		this.coordinates = coordinates;
	}
	
	public List<CyNode> getNodes() {
		return nodeList;
	}
	
	public void addNode(CyNode node) {
		if (group != null && group.getGroupNode() != node) {
			ArrayList<CyNode> nodeToAdd = new ArrayList<CyNode>();
			nodeToAdd.add(node);
			group.addNodes(nodeToAdd);
		}
		nodeList.add(node);
		size++;
	}
	
	public void addCoordinates(double[] coordinates) {
		this.coordinates.add(coordinates);
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public void setGroup(CyGroup group) {
		this.group = group;
	}

	public int getSize() {
		return size;
	}
	
	public ShapeAnnotation getEllipse() {
		return ellipse;
	}
	
	public void setEllipse(ShapeAnnotation ellipse) {
		this.ellipse = ellipse;
	}
	
	public TextAnnotation getTextAnnotation() {
		return textAnnotation;
	}
	
	public void setTextAnnotation(TextAnnotation textAnnotation) { 
		this.textAnnotation = textAnnotation;
	}
	
	public String getCloudName() {
		return cloudName;
	}

	public void erase() {
		eraseText();
		eraseEllipse();
	}
	
	public void eraseText() {
		textAnnotation.removeAnnotation();
	}
	
	public void eraseEllipse() {
		ellipse.removeAnnotation();
	}
	
	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public ArrayList<WordInfo> getWordInfos() {
		return wordInfos;
	}

	public void setWordInfos(ArrayList<WordInfo> wordInfos) {
		this.wordInfos = wordInfos;
	}

	public void removeGroup() {
		group = null;
	}
	
	@Override
	public int compareTo(Cluster cluster2) {
		return label.compareTo(cluster2.getLabel());
	}
	
	public void swallow(Cluster cluster2) {
		// Add all of the nodes from the second cluster
		for (CyNode node2 : cluster2.getNodes()) {
			addNode(node2);
		}
		// Add the coordinates from the second cluster
		for (double[] coordinates2 : cluster2.getCoordinates()) {
			addCoordinates(coordinates2);
		}
		// Remove the second cluster
		cluster2.getParent().getClusterMap().remove(cluster2.getClusterNumber());
	}
	
	public String toSessionString() {
		/* Each cluster is stored in the format:
	    	 *  		1 - Cluster number
	    	 *  		2 - Cluster label
	    	 *  		3 - Selected (0/1)
	    	 *  		4 - Use groups or not
	    	 *  		5... - Nodes
	    	 *  		6... - Node coordinates
	    	 *  		-1 - End of cluster
	    	 */
		
		String sessionString = "";
		// Write parameters of the cluster
		sessionString += clusterNumber + "\n";
		sessionString += label + "\n";
		sessionString += selected + "\n";
		// Write parameters of the annotations to recreate them after
			// Ellipse
		Map<String, String> ellipseArgs = ellipse.getArgMap();
		for (String property : ellipseArgs.keySet()) {
			sessionString += property + "\t" + ellipseArgs.get(property) + "\n";
		}
			// Text
		sessionString += "Text Annotations\n";
		Map<String, String> textArgs = textAnnotation.getArgMap();
		for (String property : textArgs.keySet()) {
			sessionString += property + "\t" + textArgs.get(property) + "\n";
		}
		sessionString += "End of annotations\n";
		
		// Write each node
		for (int nodeIndex=0 ; nodeIndex < getSize() ; nodeIndex++) {
			long nodeID = getNodes().get(nodeIndex).getSUID();
			sessionString += nodeID + "\n";
		}
		sessionString += "End of nodes\n";
		// Write coordinates (separately, in case a node has been collapsed)
		for (int coordinateIndex=0 ; coordinateIndex < getCoordinates().size() ; coordinateIndex++) {
			double nodeX = coordinates.get(coordinateIndex)[0];
			double nodeY = coordinates.get(coordinateIndex)[1];
			sessionString += nodeX + "\t" + nodeY + "\n";
		}
		sessionString += "End of coordinates\n";
		sessionString += "End of cluster\n";
		
		// Destroy group (causes problems with session loading)
		if (group != null) {
			if (group.isCollapsed(parent.getView().getModel())) {
				group.expand(parent.getView().getModel());
			}
			group.removeGroupFromNetwork(parent.getView().getModel());
			AutoAnnotationManager.getInstance().getGroupManager().destroyGroup(group);
		}
		
		return sessionString;
	}
	
	public void load(ArrayList<String> text, CySession session) {
		clusterNumber = Integer.valueOf(text.get(0));
		cloudName = parent.getName() + " Cloud " + clusterNumber;
		label = text.get(1);
		selected = Boolean.valueOf(text.get(2));
		
		// Load the parameters of the annotations
		int lineNumber = 3;
		String line = text.get(lineNumber);
		Map<String, String> ellipseMap = new HashMap<String, String>();
		while (!line.equals("Text Annotations")) {
			String[] splitLine = line.split("\t");
			ellipseMap.put(splitLine[0], splitLine[1]);
			lineNumber++;
			line = text.get(lineNumber);
		}
		lineNumber++;
		line = text.get(lineNumber);
		Map<String, String> textMap = new HashMap<String, String>();
		while (!line.equals("End of annotations")) {
			String[] splitLine = line.split("\t");
			textMap.put(splitLine[0], splitLine[1]);
			lineNumber++;
			line = text.get(lineNumber);
		}
		lineNumber++;
		line = text.get(lineNumber);
		
		// Reload nodes
		while (!line.equals("End of nodes")) {
			addNode(session.getObject(Long.valueOf(line), CyNode.class));
			lineNumber++;
			line = text.get(lineNumber);
		}
		// Skip the end line
		lineNumber++;
		line = text.get(lineNumber);
		// Reload coordinates
		while (!line.equals("End of coordinates")) {
			String[] splitLine = line.split("\t");
			double[] nodeCoordinates = {Double.valueOf(splitLine[0]), Double.valueOf(splitLine[1])}; 
			addCoordinates(nodeCoordinates);
			lineNumber++;
			line = text.get(lineNumber);
		}
	}
	
	@Override
	public String toString() {
		return label;
	}
}