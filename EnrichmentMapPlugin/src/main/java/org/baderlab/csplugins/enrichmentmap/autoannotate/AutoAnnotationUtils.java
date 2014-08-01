/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 12:50:09 PM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.WordInfo;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class AutoAnnotationUtils {
	
	private static int min_size = 35; // Minimum size of the cluster
	private static double padding = 1.6; // Amount the ellipses are stretched by
	private static double ellipseWidth = 5.0;
	
	public static void selectCluster(Cluster selectedCluster, CyNetwork network, boolean showHeatmap,
									 CommandExecutorTaskFactory executor, DialogTaskManager dialogTaskManager) {
		// Select the corresponding WordCloud
		if (!selectedCluster.isSelected()) {
			selectedCluster.setSelected(true);
			if (selectedCluster.isCollapsed()) {
				network.getRow(selectedCluster.getGroupNode()).set(CyNetwork.SELECTED, true);
			} else {
				for (CyNode node : selectedCluster.getNodes()) {
					network.getRow(node).set(CyNetwork.SELECTED, true);
				}
			}
			ArrayList<String> commands = new ArrayList<String>();
			String command = "wordcloud select cloudName=\"" + selectedCluster.getCloudName() + "\"";
			commands.add(command);
			Observer observer = new Observer();
			TaskIterator task = executor.createTaskIterator(commands, null);
			dialogTaskManager.execute(task, observer);
			while (! observer.isFinished()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (showHeatmap) {
				CytoPanel southPanel = AutoAnnotationManager.getInstance().getApplication().getCytoPanel(CytoPanelName.SOUTH);
				for (int panelIndex = 0; panelIndex < southPanel.getCytoPanelComponentCount(); panelIndex++) {
					try {
						// In some cases the panels don't implement CytoPanelComponent
						if (((CytoPanelComponent) southPanel.getComponentAt(panelIndex)).getTitle() == "Heat Map (nodes)") {
							southPanel.setSelectedIndex(panelIndex);
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
			
			selectedCluster.getEllipse().setSelected(true);
			selectedCluster.getTextAnnotation().setSelected(true);
		}
	}

	public static void deselectCluster(Cluster deselectedCluster, CyNetwork network) {
		if (deselectedCluster.isSelected()) {
			deselectedCluster.setSelected(false);
			// Deselect nodes in the cluster
			if (deselectedCluster.isCollapsed()) {
				network.getRow(deselectedCluster.getGroupNode()).set(CyNetwork.SELECTED, false);
			} else {
				for (CyNode node : deselectedCluster.getNodes()) {
					network.getRow(node).set(CyNetwork.SELECTED, false);
				}
			}
			// Reset the size/color of the annotations
			deselectedCluster.getEllipse().setSelected(false);
			deselectedCluster.getTextAnnotation().setSelected(false);
		}
	}

	public static void destroyCluster(Cluster clusterToDestroy, CommandExecutorTaskFactory executor, 
			DialogTaskManager dialogTaskManager) {
		// Get rid of the WordCloud
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + clusterToDestroy.getCloudName() + "\"";
		commands.add(command);
		Observer observer = new Observer();
		TaskIterator task = executor.createTaskIterator(commands, null);
		dialogTaskManager.execute(task, observer);
		while (!observer.isFinished()) {
			// Erase the cluster
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		clusterToDestroy.erase();
	}

	public static void drawCluster(Cluster cluster, CyNetworkView view, AnnotationFactory<ShapeAnnotation> shapeFactory, 
			AnnotationFactory<TextAnnotation> textFactory, AnnotationManager annotationManager) {

		double zoom = view.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR);

    	// Find the edges of the cluster
		double xmin = 100000000;
		double ymin = 100000000;
		double xmax = -100000000;
		double ymax = -100000000;
		for (double[] coordinates : cluster.getCoordinates()) {
			xmin = coordinates[0] < xmin ? coordinates[0] : xmin;
			xmax = coordinates[0] > xmax ? coordinates[0] : xmax;
			ymin = coordinates[1] < ymin ? coordinates[1] : ymin;
			ymax = coordinates[1] > ymax ? coordinates[1] : ymax;
		}
		double width = (xmax - xmin);
		width = width > min_size ? width : min_size;
		double height = (ymax - ymin);
		height = height > min_size ? height : min_size;
		
		// Parameters of the ellipse
		Integer xPos = (int) Math.round(xmin - width*(padding-1)/2);
		Integer yPos = (int) Math.round(ymin - height*(padding-1)/2);

		// Create and draw the ellipse
		HashMap<String, String> arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		ShapeAnnotation ellipse = shapeFactory.createAnnotation(ShapeAnnotation.class, view, arguments);
		ellipse.setShapeType("Ellipse");
		ellipse.setSize(width*padding*zoom, height*padding*zoom);
		ellipse.setBorderWidth(ellipseWidth);
		cluster.setEllipse(ellipse);
		annotationManager.addAnnotation(ellipse);

		// Parameters of the text label
		String labelText = cluster.getLabel();
		Integer fontSize = (int) Math.round(2.5*Math.pow(cluster.getSize(), 0.4));
		// To centre the annotation at the middle of the annotation
		xPos = (int) Math.round(xPos + width*padding/2 - 2.1*fontSize*labelText.length());
		yPos = (int) Math.round(yPos - 10.3*fontSize);
		
		// Create and draw the label
		arguments = new HashMap<String,String>();
		arguments.put("x", String.valueOf(xPos));
		arguments.put("y", String.valueOf(yPos));
		arguments.put("zoom", String.valueOf(zoom));
		arguments.put("canvas", "foreground");
		arguments.put("fontSize", String.valueOf((int) Math.round(fontSize*11*zoom)));
		TextAnnotation textAnnotation = textFactory.createAnnotation(TextAnnotation.class, view, arguments);
		textAnnotation.setText(labelText);
		cluster.setTextAnnotation(textAnnotation);
		annotationManager.addAnnotation(textAnnotation);
	}
	
	public static void updateClusterLabel(Cluster cluster, CyNetwork network, String annotationSetName, CyTable clusterSetTable) {
		// Only updates if the user hasn't changed the label manually
		int clusterNumber = cluster.getClusterNumber();
		// Look up the wordCloud info in the table
		CyRow clusterRow = clusterSetTable.getRow(clusterNumber);
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
		// Only update the labels if the wordCloud has changed
		if (wordInfos.size() != cluster.getWordInfos().size()) {
			// WordCloud table entry for this cluster has changed
			cluster.setWordInfos(wordInfos);
			cluster.setLabel(makeLabel(wordInfos));
		} else {
			for (int infoIndex = 0; infoIndex < cluster.getWordInfos().size(); infoIndex++) {
				if (!wordInfos.get(infoIndex).equals(cluster.getWordInfos().get(infoIndex))) {
					// WordCloud table entry for this cluster has changed
					cluster.setWordInfos(wordInfos);
					cluster.setLabel(makeLabel(wordInfos));
					return;
				}
			}
		}
	}
	
	public static String makeLabel(ArrayList<WordInfo> wordInfos) {
		// TODO more code reuse
		// WordInfos sort by size descending
		// Using a copy so as to not mess up the order for comparisons
		ArrayList<WordInfo> wordInfosCopy = new ArrayList<WordInfo>();
		for (WordInfo wordInfo : wordInfos) {
			wordInfosCopy.add(wordInfo.clone());
		}
		Collections.sort(wordInfosCopy);
		// Gets the biggest word
		WordInfo biggestWord = wordInfosCopy.get(0);
		String label = biggestWord.getWord();
		if (wordInfosCopy.size() > 1) {
			for (WordInfo word : wordInfosCopy.subList(1, wordInfosCopy.size())) {
				if (word.getCluster() == biggestWord.getCluster()) {
					word.setSize(word.getSize() - 1);
				}
			}
			Collections.sort(wordInfosCopy);
			WordInfo secondBiggestWord = wordInfosCopy.get(1);
			if (secondBiggestWord.getSize() >= 0.3*biggestWord.getSize()) {
				label += " " + secondBiggestWord.getWord();
			}
			for (WordInfo word : wordInfosCopy.subList(1, wordInfosCopy.size())) {
				if (!word.equals(secondBiggestWord) && word.getCluster() == secondBiggestWord.getCluster()) {
					word.setSize(word.getSize() - 1);
				}
			}
			Collections.sort(wordInfosCopy);
			try {
				WordInfo thirdBiggestWord = wordInfosCopy.get(2);
				if (thirdBiggestWord.getSize() > 0.8*secondBiggestWord.getSize()) {
					label += " " + thirdBiggestWord.getWord();
				}
			} catch (Exception e) {
				return label;
			}
			try {
				WordInfo fourthBiggestWord = wordInfosCopy.get(3);
				if (fourthBiggestWord.getSize() > 0.9*secondBiggestWord.getSize()) {
					label += " " + fourthBiggestWord.getWord();
				}
			} catch (Exception e) {
				return label;
			}
		}
		return label;
	}

	public static void registerClusterGroups(Cluster cluster, CyNetwork selectedNetwork,
											CyGroupManager groupManager) {
		CyGroup group = cluster.getGroup();
		if (group != null) {
			group.addGroupToNetwork(selectedNetwork);
		}
	}

	public static void unregisterClusterGroups(Cluster cluster,
			CyNetwork selectedNetwork, CyGroupManager groupManager) {
		CyGroup group = cluster.getGroup();
		if (group != null) {
			group.removeGroupFromNetwork(selectedNetwork);
		}
	}
}
