package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 * @author arkadyark
 * <p>
 * Date   July 7, 2014<br>
 * Time   09:23:28 PM<br>
 */

public class AnnotationSet {
	
	// Default values for visual properties
	public static final boolean DEFAULT_CONSTANT_FONT_SIZE = false;
	public static final boolean DEFAULT_SHOW_ELLIPSES = true;
	public static final int DEFAULT_FONT_SIZE = 12;
	public static final int DEFAULT_ELLIPSE_WIDTH = 3;
	public static final int DEFAULT_ELLIPSE_OPACITY = 20;
	public static final String DEFAULT_SHAPE_TYPE = "ELLIPSE";
	public static final boolean DEFAULT_SHOW_LABEL = true;
	// Default values for label generation
	public static final int DEFAULT_MAX_WORDS = 4;
	public static final List<Integer> DEFAULT_WORDSIZE_THRESHOLDS = 
			Collections.unmodifiableList(Arrays.asList(30, 80, 90, 90, 90, 90));
	public static final double[] DEFAULT_LABEL_POSITION = {0.5, 0};
	public static final int DEFAULT_SAME_CLUSTER_BONUS = 8;
	public static final int DEFAULT_CENTRALITY_BONUS = 4;

	// Map of clusterNumbers to the comprising clusters
	private TreeMap<Integer, Cluster> clusterMap;
	// Name of the column that was used
	private String name;
	// Used to recreate the annotation set when merging clusters
	private String clusterColumnName;
	private String nameColumnName;
	// View that the cluster set belongs to
	private CyNetworkView view;
	// Whether or not groups are created for this annotation set
	private boolean useGroups;
	// Whether or not this annotation set is currently selected (showing)
	private boolean selected = false;
	// Visual properties
	boolean constantFontSize = DEFAULT_CONSTANT_FONT_SIZE;
	boolean showEllipses = DEFAULT_SHOW_ELLIPSES;
	int fontSize = DEFAULT_FONT_SIZE;
	int ellipseWidth = DEFAULT_ELLIPSE_WIDTH;
	int ellipseOpacity = DEFAULT_ELLIPSE_OPACITY;
	String shapeType = DEFAULT_SHAPE_TYPE;
	boolean showLabel = DEFAULT_SHOW_LABEL;
	// Label options
	private int maxWords = DEFAULT_MAX_WORDS;
	private List<Integer> wordSizeThresholds = DEFAULT_WORDSIZE_THRESHOLDS;
	private double[] labelPosition = DEFAULT_LABEL_POSITION;
	private int sameClusterBonus = DEFAULT_SAME_CLUSTER_BONUS;
	private int centralityBonus = DEFAULT_CENTRALITY_BONUS;
	
	// Constructor used when loading from a file
	public AnnotationSet() {
		this.clusterMap = new TreeMap<Integer, Cluster>();
	}
	
	// Constructor used when created from an annotation task
	public AnnotationSet(String cloudNamePrefix, CyNetworkView view, String clusterColumnName, String nameColumnName) {
		this();
		this.name = cloudNamePrefix;
		this.view = view;
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

	public void removeCluster(Cluster cluster) {
		clusterMap.remove(cluster);
	}
	
	public int getNextClusterNumber() {
		int clusterNumber = 1;
		while (clusterMap.containsKey(clusterNumber)) {
			clusterNumber++;
		}
		return clusterNumber;
	}
	
	// Get the coordinates of the nodes in each cluster
	// Returns whether or not there has been a change
	public void updateCoordinates() {
		for (Cluster cluster : clusterMap.values()) {
			HashMap<CyNode, double[]> nodesToCoordinates = cluster.getNodesToCoordinates();
			HashMap<CyNode, Double> nodesToRadii = cluster.getNodesToRadii();
			boolean hasNodeViews = false;
			for (CyNode node : cluster.getNodes()) {
				View<CyNode> nodeView = view.getNodeView(node);
				if (nodeView != null) {
					hasNodeViews = true;
					// nodeView can be null when group is collapsed
					double[] coordinates = {nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
											nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)};
					double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
					if (coordinatesHaveChanged(coordinates, nodesToCoordinates.get(node))) {
						// Coordinates have changed, redrawing necessary
						cluster.setCoordinatesChanged(true);
						cluster.addNodeCoordinates(node, coordinates);
						cluster.addNodeRadius(node, nodeRadius);
					}
						
				} 
				else{
					if(nodesToCoordinates.containsKey(node)) nodesToCoordinates.remove(node);
					if(nodesToRadii.containsKey(node)) nodesToRadii.remove(node);
				}
			}//end of for CyNode
				if(!hasNodeViews) {
					View<CyNode> nodeView = view.getNodeView(cluster.getGroupNode());
					if (nodeView != null) {
						hasNodeViews = true;
						// nodeView can be null when group is collapsed
						double[] coordinates = {nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
												nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)};
						double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
					// Coordinates have changed, redrawing necessary
					cluster.setCoordinatesChanged(true);
					// Draw the annotation as if all nodes were where the groupNode is
					cluster.addNodeCoordinates(cluster.getGroupNode(), coordinates);
					cluster.addNodeRadius(cluster.getGroupNode(), nodeRadius);
				}
			}
		}
	}

	private boolean coordinatesHaveChanged(double[] oldCoordinates, double[] newCoordinates) {
		return (oldCoordinates == null || 
				(Math.abs(oldCoordinates[0] - newCoordinates[0]) > 0.01 &&
				Math.abs(oldCoordinates[1] - newCoordinates[1]) > 0.01));
	}
	
	public TreeMap<Integer, Cluster> getClusterMap() {
		return clusterMap;
	}

	public void setClusterMap(TreeMap<Integer, Cluster> clusterSet) {
		this.clusterMap = clusterSet;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean usingGroups() {
		return useGroups;
	}

	public void setUseGroups(boolean useGroups) {
		this.useGroups = useGroups;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isConstantFontSize() {
		return constantFontSize;
	}

	public void setConstantFontSize(boolean constantFontSize) {
		this.constantFontSize = constantFontSize;
	}

	public boolean isShowEllipses() {
		return showEllipses;
	}

	public void setShowEllipses(boolean showEllipses) {
		this.showEllipses = showEllipses;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getEllipseWidth() {
		return ellipseWidth;
	}

	public void setEllipseWidth(int ellipseWidth) {
		this.ellipseWidth = ellipseWidth;
        for (Cluster cluster : clusterMap.values()) {
        	if (cluster.isSelected()) {
        		cluster.getEllipse().setBorderWidth(ellipseWidth*2);
        	} else {
        		cluster.getEllipse().setBorderWidth(ellipseWidth);
        	}
        	cluster.getEllipse().update();
        }

	}

	public int getEllipseOpacity() {
		return ellipseOpacity;
	}

	public void setEllipseOpacity(int ellipseOpacity) {
		this.ellipseOpacity = ellipseOpacity;
        for (Cluster cluster : clusterMap.values()) {
        	cluster.getEllipse().setFillOpacity(ellipseOpacity);
        	cluster.getEllipse().update();
        }
	}

	public String getShapeType() {
		return shapeType;
	}

	public void setShapeType(String shapeType) {
		this.shapeType = shapeType;
	}

	public boolean isShowLabel() {
		return showLabel;
	}

	public void setShowLabel(boolean showLabel) {
		this.showLabel = showLabel;
	}

	public int getMaxWords() {
		return maxWords;
	}

	public void setMaxWords(int maxWords) {
		this.maxWords = maxWords;
	}

	public List<Integer> getWordSizeThresholds() {
		return wordSizeThresholds;
	}

	public void setWordSizeThresholds(List<Integer> wordSizeThresholds) {
		this.wordSizeThresholds = wordSizeThresholds;
	}

	public double[] getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(double[] labelPosition) {
		this.labelPosition = labelPosition;
	}

	public int getSameClusterBonus() {
		return sameClusterBonus;
	}

	public void setSameClusterBonus(int sameClusterBonus) {
		this.sameClusterBonus = sameClusterBonus;
	}

	public int getCentralityBonus() {
		return centralityBonus;
	}

	public void setCentralityBonus(int centralityBonus) {
		this.centralityBonus = centralityBonus;
	}
	
	public void setLabelOptions(LabelOptions labelOptions) {
		if (labelOptions != null) {
			setMaxWords(labelOptions.getMaxWords());
			setWordSizeThresholds(labelOptions.getWordSizeThresholds());
			setLabelPosition(labelOptions.getLabelPosition());
			setSameClusterBonus(labelOptions.getSameClusterBonus());
			setCentralityBonus(labelOptions.getCentralityBonus());
			for (Cluster cluster : clusterMap.values()) {
				cluster.setLabel(AutoAnnotationUtils.makeLabel(cluster.getWordInfos(), 
						cluster.getMostCentralNodeLabel(),
						sameClusterBonus, centralityBonus,
						wordSizeThresholds, maxWords));
				cluster.getTextAnnotation().setText(cluster.getLabel());
			}
		}
	}
	
	public String toSessionString() {
	    	// Each annotation set is stored in the format:
	    	/*
	    	 *  1 - Annotation set name (Primary identifier)
	    	 *  2 - Cluster Column Name
	    	 *  3 - Name Column Name
	    	 *  4 - Whether or not this annotation set uses groups
	    	 *  5... - Each cluster, stored in the format:
	    	 *  		1 - Cluster name
	    	 *  		2 - Cluster label
	    	 *  		3 - Selected (0/1)
	    	 *  		4 - labelManuallyUpdated
	    	 *  		5... - NodeSUID x y
	    	 *  		-1 - End of cluster
	    	 *  -1 - End of annotation set
	    	 */

		// Returns the string used when saving the session
		String sessionString = "";
		sessionString += name + "\n";
		sessionString += clusterColumnName + "\n";
		sessionString += nameColumnName + "\n";
		sessionString += useGroups + "\n";
		// Display Options
		sessionString += constantFontSize + "\n";
		sessionString += showEllipses + "\n";
		sessionString += fontSize + "\n";
		sessionString += ellipseWidth + "\n";
		sessionString += ellipseOpacity + "\n";
		sessionString += shapeType + "\n";
		sessionString += showLabel + "\n";
		// Label Options
		sessionString += maxWords + "\n";
		sessionString += sameClusterBonus + "\n";
		sessionString += centralityBonus + "\n";
		sessionString += labelPosition[0] + "\t" + labelPosition[1] + "\n";
		for (int wordSizeThreshold : wordSizeThresholds) {
			sessionString += wordSizeThreshold + "\t";
		}
		sessionString += "\n";
		for (Cluster cluster : clusterMap.values()) {
			sessionString += cluster.toSessionString();
		}
		sessionString += "End of annotation set\n";
		return sessionString;
	}

	public void load(ArrayList<String> text, CySession session) {
		setName(text.get(0));
		setClusterColumnName(text.get(1));
		setNameColumnName(text.get(2));
		setUseGroups(Boolean.valueOf(text.get(3)));
		setConstantFontSize(Boolean.valueOf(text.get(4)));
		setShowEllipses(Boolean.valueOf(text.get(5)));
		setFontSize(Integer.valueOf(text.get(6)));
		setEllipseWidth(Integer.valueOf(text.get(7)));
		setEllipseOpacity(Integer.valueOf(text.get(8)));
		setShapeType(text.get(9));
		setShowLabel(Boolean.valueOf(text.get(10)));
		setMaxWords(Integer.valueOf(text.get(11)));
		setSameClusterBonus(Integer.valueOf(text.get(12)));
		setCentralityBonus(Integer.valueOf(text.get(13)));
		String[] labelPositionString = text.get(14).split("\t");
		double[] labelPosition = {Double.valueOf(labelPositionString[0]), Double.valueOf(labelPositionString[1])};
		setLabelPosition(labelPosition);
		String[] wordSizeThresholdString = text.get(15).split("\t");
		wordSizeThresholds = new ArrayList<Integer>();
		for (String wordSizeThreshold : wordSizeThresholdString) {
			wordSizeThresholds.add(Integer.valueOf(wordSizeThreshold));
		}
		// Update the column in the network table with the new SUID of the table
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		for (CyTable table : autoAnnotationManager.getTableManager().getAllTables(true)) {
			if (table.getTitle().equals(name)) {
				view.getModel().getRow(view.getModel()).set(name, table.getSUID());
				break;
			}
		}
		
		int lineNumber = 16;
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
