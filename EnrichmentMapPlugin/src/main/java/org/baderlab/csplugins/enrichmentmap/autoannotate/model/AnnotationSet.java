package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableManager;
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
	
	public String name;
	public TreeMap<Integer, Cluster> clusterSet; // Having it be sorted is useful for the displayPanel
	public boolean drawn;
	public CyNetwork network;
	public CyNetworkView view;
	private CyTableManager tableManager;
	
	public AnnotationSet(String name, CyNetwork network, CyNetworkView view, String clusterColumnName, CyTableManager tableManager) {
		this.name = name;
		this.clusterSet = new TreeMap<Integer, Cluster>();
		this.network = network;
		this.view = view;
		this.tableManager = tableManager;
	}
	
	public void addCluster(Cluster cluster) {
		clusterSet.put(cluster.getClusterNumber(), cluster);
	}

	public void drawAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.drawAnnotations();
		}
	}
	
	public void eraseAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.erase();
		}
	}
	
	public void destroyAnnotations() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.destroy();
		}
	}
	
	public void updateCoordinates() {
		for (Cluster cluster : clusterSet.values()) {
			cluster.coordinates = new ArrayList<double[]>();
			for (CyNode node : cluster.nodes) {
				View<CyNode> nodeView = view.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				cluster.addCoordinates(coordinates);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void updateLabels() {
		for (Cluster cluster : this.clusterSet.values()) {
			// Only update if label hasn't been manually changed by the user
			if (!cluster.getLabelManuallyUpdated()) {
				cluster.setLabel("");
				int clusterNumber = cluster.getClusterNumber();
				Long clusterTableSUID = network.getDefaultNetworkTable().getRow(network.getSUID()).get(name, Long.class);
				CyRow clusterRow = tableManager.getTable(clusterTableSUID).getRow(clusterNumber);
				List<String> wordList = clusterRow.get("WC_Word", List.class);
				List<String> sizeList = clusterRow.get("WC_FontSize", List.class);
				List<String> clusterList = clusterRow.get("WC_Cluster", List.class);
				List<String> numberList = clusterRow.get("WC_Number", List.class);
				ArrayList<WordInfo> wordInfos = new ArrayList<WordInfo>();
				for (int i=0; i < wordList.size(); i++) {
					wordInfos.add(new WordInfo(wordList.get(i), 
											Integer.parseInt(sizeList.get(i)),
											Integer.parseInt(clusterList.get(i)),
											Integer.parseInt(numberList.get(i))));
				}
				String label = makeLabel(wordInfos);
				cluster.setLabel(label);
			}
		}
	}
	
	public String makeLabel(ArrayList<WordInfo> wordInfos) {
		Collections.sort(wordInfos);
		WordInfo biggestWord = wordInfos.get(0);
		String label = biggestWord.word;
		for (WordInfo word : wordInfos.subList(1, wordInfos.size())) {
			if (word.cluster == biggestWord.cluster) {
				word.size -= 1;
			}
		}
		Collections.sort(wordInfos);
		WordInfo secondBiggestWord = wordInfos.get(1);
		if (secondBiggestWord.size >= 0.3*biggestWord.size) {
			label += " " + secondBiggestWord.word;
		}
		try {
			WordInfo thirdBiggestWord = wordInfos.get(2);
			if (thirdBiggestWord.size > 0.8*secondBiggestWord.size) {
				label += " " + thirdBiggestWord.word;
			}
		} catch (Exception e) {
			return label;
		}
		try {
			WordInfo fourthBiggestWord = wordInfos.get(3);
			if (fourthBiggestWord.size > 0.9*secondBiggestWord.size) {
				label += " " + fourthBiggestWord.word;
			}
		} catch (Exception e) {
			return label;
		}
		return label;
	}
	
	@Override
	public String toString() {
		return name;
	}

}
