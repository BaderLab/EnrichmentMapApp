package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNode;
import org.cytoscape.group.CyGroup;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

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
	private HashMap<CyNode, double[]> nodesToCoordinates;
	private int size = 0;
	private String label;
	private TextAnnotation textAnnotation;
	private ShapeAnnotation ellipse;
	private AnnotationSet parent;
	private boolean selected = false;
	private ArrayList<WordInfo> wordInfos;
	private HashMap<CyNode, Double> nodesToRadii;
	private HashMap<CyNode, Double> nodesToCentralities;
	private String mostCentralNodeLabel;
	private boolean coordinatesChanged = true;
	private double[] bounds = new double[4];
	
	private Set<CyNode> nodes;
	
	// Used when initializing from a session file
	public Cluster() {
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodesToCoordinates = new HashMap<CyNode, double[]>();
		this.nodesToRadii = new HashMap<CyNode, Double>();
		this.nodesToCentralities = new HashMap<CyNode, Double>();
		this.nodes = new HashSet<CyNode> ();
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
		this.wordInfos = new ArrayList<WordInfo>();
		this.nodesToCoordinates = new HashMap<CyNode, double[]>();
		this.nodesToRadii = new HashMap<CyNode, Double>();
		this.nodesToCentralities = new HashMap<CyNode, Double>();
		this.nodes = new HashSet<CyNode> ();
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
	
	public double[] getBounds() {
		//Only updates the bounds of cluster where the coordinates have changed.
		if (coordinatesChanged) {
			// Find the edges of the cluster
			double xmin = 100000000;
			double ymin = 100000000;
			double xmax = -100000000;
			double ymax = -100000000;
			for (double[] coordinates : nodesToCoordinates.values()) {
				xmin = coordinates[0] < xmin ? coordinates[0] : xmin;
				xmax = coordinates[0] > xmax ? coordinates[0] : xmax;
				ymin = coordinates[1] < ymin ? coordinates[1] : ymin;
				ymax = coordinates[1] > ymax ? coordinates[1] : ymax;
			}
			bounds[0] = xmin;
			bounds[1] = xmax;
			bounds[2] = ymin;
			bounds[3] = ymax;
			
			coordinatesChanged = false;
		}
		return bounds;
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
	
	public HashMap<CyNode, double[]> getNodesToCoordinates() {
		return nodesToCoordinates;
	}
	
	public HashMap<CyNode, Double> getNodesToRadii() {
		return nodesToRadii;
	}
	
	public void addNodeCoordinates(CyNode node, double[] coordinates) {
		if (!nodesToCoordinates.containsKey(node)) {
			size++;
			if (group != null && group.getGroupNode() != node) {
				ArrayList<CyNode> nodeToAdd = new ArrayList<CyNode>();
				nodeToAdd.add(node);
				group.addNodes(nodeToAdd);
			}
		
		nodes.add(node);
		nodesToCoordinates.put(node, coordinates);
		}
		else if(coordinatesHaveChanged(nodesToCoordinates.get(node),coordinates)){
			nodesToCoordinates.put(node, coordinates);
			coordinatesChanged = true; 
		}
	}
	
	public void updateCoordinates() {
			
			boolean hasNodeViews = false;
			for (CyNode node : this.getNodes()) {
				View<CyNode> nodeView = parent.getView().getNodeView(node);
				// nodeView can be null when group is collapsed
				if (nodeView != null) {
					hasNodeViews = true;
					//the new coordinates
					double[] coordinates = {nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
											nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)};
					double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
					
					double[] default_coordinates = {0.0,0.0};
					//check to make sure the node has coordinates.  - it is possilbe that it was just expanded from the group
					//and not coordinates are registered
					double[] previous_coordinates = (nodesToCoordinates.containsKey(node)) ? nodesToCoordinates.get(node) : default_coordinates;
					
					if (coordinatesHaveChanged(coordinates, previous_coordinates)) {
						// Coordinates have changed, redrawing necessary
						this.setCoordinatesChanged(true);
						this.addNodeCoordinates(node, coordinates);
						this.addNodeRadius(node, nodeRadius);
					}
						
				} 
				else{
					if(nodesToCoordinates.containsKey(node)) nodesToCoordinates.remove(node);
					if(nodesToRadii.containsKey(node)) nodesToRadii.remove(node);
				}
			}//end of for CyNode
				if(!hasNodeViews) {
					View<CyNode> nodeView = parent.getView().getNodeView(this.getGroupNode());
					if (nodeView != null) {
						hasNodeViews = true;
						// nodeView can be null when group is collapsed
						double[] coordinates = {nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
												nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)};
						double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
					// Coordinates have changed, redrawing necessary
					this.setCoordinatesChanged(true);
					// Draw the annotation as if all nodes were where the groupNode is
					this.addNodeCoordinates(this.getGroupNode(), coordinates);
					this.addNodeRadius(this.getGroupNode(), nodeRadius);
				}
			}
		}
	
	private boolean coordinatesHaveChanged(double[] oldCoordinates, double[] newCoordinates) {
		return (oldCoordinates == null || 
				(Math.abs(oldCoordinates[0] - newCoordinates[0]) > 0.01 &&
				Math.abs(oldCoordinates[1] - newCoordinates[1]) > 0.01));
	}
	
	public void addNodeRadius(CyNode node, double radius) {
		nodesToRadii.put(node, radius);
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
		if(nodes != null)
			return nodes.size();
		return 0;
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
		if(textAnnotation !=null)
			textAnnotation.removeAnnotation();
	}
	
	public void eraseEllipse() {
		if(ellipse!= null)
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

	public void setNodesToCentralities(HashMap<CyNode, Double> nodesToCentralities) {
		this.nodesToCentralities = nodesToCentralities;
	}
	
	public HashMap<CyNode, Double> getNodesToCentralities() {
		return nodesToCentralities;
	}
	
	public String getMostCentralNodeLabel() {
		if (mostCentralNodeLabel == null) {
			CyNode maxNode = nodesToCoordinates.keySet().iterator().next();
			double maxCentrality = -1;
			for (CyNode node : nodesToCentralities.keySet()) {
				double centrality = nodesToCentralities.get(node);
				if (centrality > maxCentrality) {
					maxNode = node;
					maxCentrality = centrality;
				}
			}
			CyColumn current_column = parent.getView().getModel().getDefaultNodeTable().getColumn(parent.getNameColumnName());
			if(current_column.getType() == String.class)
				mostCentralNodeLabel = parent.getView().getModel().getRow(maxNode).get(parent.getNameColumnName(), String.class);
			else if(current_column.getType() == List.class && current_column.getListElementType() == String.class){
				List<String> nodelabelList = parent.getView().getModel().getRow(maxNode).get(parent.getNameColumnName(), List.class);
				if(nodelabelList != null){
					for(Object list_value : nodelabelList)
						mostCentralNodeLabel = mostCentralNodeLabel + list_value.toString();
				}
			}
		}
		return mostCentralNodeLabel;
	}
	
	public void removeGroup() {
		group = null;
	}
	
	@Override
	public int compareTo(Cluster cluster2) {
		return label.compareTo(cluster2.getLabel());
	}
	
	public Set<CyNode> getNodes() {
		return nodes;
	}
	
	public void removeNode(CyNode nodeToRemove) {
		if (nodesToCoordinates.containsKey(nodeToRemove) || nodes.contains(nodeToRemove) || nodesToRadii.containsKey(nodeToRemove)) {
			if (nodes.contains(nodeToRemove) )
					nodes.remove(nodeToRemove);
			if (nodesToCoordinates.containsKey(nodeToRemove)) 
					nodesToCoordinates.remove(nodeToRemove);
			if(nodesToRadii.containsKey(nodeToRemove))
				nodesToRadii.remove(nodeToRemove);
			//if there is a group defined remove the node from the group
			if (group != null) {
				//group remove expects a list of nodes so put the solitary node into a list.
				ArrayList<CyNode> node = new ArrayList<CyNode>();
				node.add(nodeToRemove);
				group.removeNodes(new ArrayList<CyNode>(node));
			}
			size--;
		}
	}
	
	public void swallow(Cluster cluster2) {
		// Add all of the nodes and coordinates from the second cluster
		HashMap<CyNode, double[]> cluster2NodesToCoordinates = cluster2.getNodesToCoordinates();
		HashMap<CyNode, Double> cluster2NodesToRadii = cluster2.getNodesToRadii();
		for (CyNode node : cluster2NodesToCoordinates.keySet()) {
			addNodeCoordinates(node, cluster2NodesToCoordinates.get(node));
			addNodeRadius(node, cluster2NodesToRadii.get(node));
		}
		for (Entry<CyNode, double[]> entry : cluster2.getNodesToCoordinates().entrySet()) {
			addNodeCoordinates(entry.getKey(), entry.getValue());
		}
		
	}

	public String makeLabel(ArrayList<WordInfo> wordInfos, String mostCentralNodeLabel,
			double sameClusterBonus, double centralityBonus, List<Integer> wordSizeThresholds,
			int maxWords) {
		// Work with a copy so as to not mess up the order for comparisons
		ArrayList<WordInfo> wordInfosCopy = new ArrayList<WordInfo>();
		for (WordInfo wordInfo : wordInfos) {
			wordInfosCopy.add(wordInfo.clone());
		}
		// Empty WordClouds are given an empty label
		if (wordInfosCopy.size() == 0) return "";
		// Sorts by size descending
		Collections.sort(wordInfosCopy);
		// Gets the biggest word in the cloud
		WordInfo biggestWord = wordInfosCopy.get(0);
		ArrayList<WordInfo> label = new ArrayList<WordInfo>();
		label.add(biggestWord);
		int numWords = 1;
		WordInfo nextWord = biggestWord;
		wordInfosCopy.remove(0);
		for (WordInfo word : wordInfosCopy) {
			if (mostCentralNodeLabel.toLowerCase().contains(word.getWord())) {
				word.setSize(word.getSize() + centralityBonus);
			}
		}
		while (numWords < maxWords && wordInfosCopy.size() > 0) {
			for (WordInfo word : wordInfosCopy) {
				if (word.getCluster() == nextWord.getCluster()) {
					word.setSize(word.getSize() + sameClusterBonus);						
				}
			}
			Collections.sort(wordInfosCopy); // Sizes have changed, re-sort
			double wordSizeThreshold = nextWord.getSize()*wordSizeThresholds.get(numWords - 1)/100.0;
			nextWord = wordInfosCopy.get(0);
			wordInfosCopy.remove(0);
			if (nextWord.getSize() > wordSizeThreshold) {
				label.add(nextWord);
				numWords++;
			} else {
				break;
			}
		}
		// Sort first by size, then by WordCloud 'number', tries to preserve original word order
		Collections.sort(label, WordInfoNumberComparator.getInstance());
		// Create a string label from the word infos
		return join(label, " ");
	}
	
	// Connects strings together, separated by separator
		private String join(List<WordInfo> stringList, String separator) {
			String joined = "";
			for (int index = 0; index < stringList.size(); index++) {
				if (index == 0) {
					joined += stringList.get(index).getWord();
				} else {
					joined += separator + stringList.get(index).getWord();
				}
			}
			return joined;
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
		if(ellipse != null){
			Map<String, String> ellipseArgs = ellipse.getArgMap();
			for (String property : ellipseArgs.keySet()) {
				sessionString += property + "\t" + ellipseArgs.get(property) + "\n";
			}
		}
			// Text
		if(textAnnotation != null){
			sessionString += "Text Annotations\n";
			Map<String, String> textArgs = textAnnotation.getArgMap();
			for (String property : textArgs.keySet()) {
				sessionString += property + "\t" + textArgs.get(property) + "\n";
			}
			sessionString += "End of annotations\n";
		}
		//expand the groups
		if (group != null) {
			if (group.isCollapsed(parent.getView().getModel())) {
				group.expand(parent.getView().getModel());
			}
		}
		//update the coordinates based on the expansion
		this.updateCoordinates();	
		
		// Write each node and its coordinates
		for (CyNode node : nodesToCoordinates.keySet()) {
			long nodeID = node.getSUID();
			double[] coordinates = nodesToCoordinates.get(node);
			double nodeX = coordinates[0];
			double nodeY = coordinates[1];
			double nodeRadius = nodesToRadii.get(node);
			double nodeCentrality = nodesToCentralities.get(node);
			sessionString += nodeID + "\t" + nodeX + "\t" + nodeY + "\t" + nodeRadius + "\t" + nodeCentrality + "\n";
		}
		sessionString += "End of nodes\n";
		sessionString += "End of cluster\n";
		
		// Destroy group (causes problems with session loading)
		//if (group != null) {
		//	if (group.isCollapsed(parent.getView().getModel())) {
		//		group.expand(parent.getView().getModel());
		//	}
		//	group.removeGroupFromNetwork(parent.getView().getModel());
		//	AutoAnnotationManager.getInstance().getGroupManager().destroyGroup(group);
		//}
		
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
		ellipse = AutoAnnotationManager.getInstance().getShapeFactory().createAnnotation(ShapeAnnotation.class,
				parent.getView(), ellipseMap);
		lineNumber++;
		line = text.get(lineNumber);
		
		Map<String, String> textMap = new HashMap<String, String>();
		while (!line.equals("End of annotations")) {
			String[] splitLine = line.split("\t");
			textMap.put(splitLine[0], splitLine[1]);
			lineNumber++;
			line = text.get(lineNumber);
		}
		textAnnotation = AutoAnnotationManager.getInstance().getTextFactory().createAnnotation(TextAnnotation.class,
				parent.getView(), textMap);
		lineNumber++;
		line = text.get(lineNumber);
		
		// Reload nodes
		while (!line.equals("End of nodes")) {
			String[] splitLine = line.split("\t");
			CyNode node = session.getObject(Long.valueOf(splitLine[0]), CyNode.class);
			double[] nodeCoordinates = {Double.valueOf(splitLine[1]), Double.valueOf(splitLine[2])}; 
			addNodeCoordinates(node, nodeCoordinates);
			double nodeRadius = Double.valueOf(splitLine[3]);
			addNodeRadius(node, nodeRadius);
			double nodeCentrality = Double.valueOf(splitLine[4]);
			nodesToCentralities.put(node, nodeCentrality);
			lineNumber++;
			line = text.get(lineNumber);
		}
	}
	
	@Override
	public String toString() {
		return label;
	}

	public boolean coordinatesChanged() {
		return coordinatesChanged ;
	}

	public void setCoordinatesChanged(boolean b) {
		coordinatesChanged = b;
	}
}