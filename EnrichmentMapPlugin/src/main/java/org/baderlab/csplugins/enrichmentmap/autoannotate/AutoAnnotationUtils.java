/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 12:50:09 PM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.WordInfo;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.WordInfoNumberComparator;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.DrawClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.VisualizeClusterAnnotationTaskFactory;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyEdge;
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
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class AutoAnnotationUtils {
		
	
	public static void destroyCluster(Cluster clusterToDestroy, CommandExecutorTaskFactory executor, 
			SynchronousTaskManager<?> syncTaskManager) {
		destroyCloud(clusterToDestroy, executor, syncTaskManager);
		// Erase the annotations
		clusterToDestroy.erase();
		// Remove the cluster from the annotation set
		clusterToDestroy.getParent().getClusterMap().remove(clusterToDestroy.getClusterNumber());
	}
	
	public static void destroyCloud(Cluster clusterToDestroy, CommandExecutorTaskFactory executor, 
			SynchronousTaskManager<?> syncTaskManager) {
		// Delete the WordCloud through the command line
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + clusterToDestroy.getCloudName() + "\"";
		commands.add(command);
		TaskIterator task = executor.createTaskIterator(commands, null);
		syncTaskManager.execute(task);
	}

	
	public static void updateFontSizes() {
 		// Set font size to fontSize
 		for (CyNetworkView view : 
 			AutoAnnotationManager.getInstance().getNetworkViewToAutoAnnotationParameters().keySet()) {
 			AutoAnnotationParameters params = AutoAnnotationManager.getInstance().getNetworkViewToAutoAnnotationParameters().get(view);
 			for (AnnotationSet annotationSet : params.getAnnotationSets().values()) {
 				TaskIterator currentTasks = new TaskIterator();
 				for (Cluster cluster : annotationSet.getClusterMap().values()) {
					// Redraw annotation to update font size
 					cluster.eraseText();
 					
 					DrawClusterLabelTask drawlabel = new DrawClusterLabelTask(cluster);
 					currentTasks.append(drawlabel);
 					
 					
					if (!annotationSet.isSelected() || !annotationSet.isShowLabel()) {
						cluster.eraseText();
					}
 				}
 				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
 			}
 		}
	}
	
	public static void updateNodeCentralities(Cluster cluster) {
		CyNetwork network = cluster.getParent().getView().getModel();
		// Use similarity coefficient if possible
		String edgeAttribute;
		try {
			edgeAttribute = EnrichmentMapManager.getInstance().getCyNetworkList().get(network.getSUID()).getParams().getAttributePrefix() + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT;
		} catch (NullPointerException e) {
			edgeAttribute = "--None--";
		}
		Set<CyNode> nodeSet = cluster.getNodes();
		HashMap<CyNode, Double> nodeCentralities = new HashMap<CyNode, Double>();
		for (CyNode node : nodeSet) {
			double clusterWeightedDegreeSum = 0;
			for (CyEdge edge : network.getAdjacentEdgeIterable(node, CyEdge.Type.ANY)) {
				if (edge.getSource() != node && nodeSet.contains(edge.getSource()) ||
					edge.getTarget() != node && nodeSet.contains(edge.getTarget())) {
					try {
						clusterWeightedDegreeSum += network.getRow(edge).get(edgeAttribute, Double.class);
					} catch (Exception e) {
						clusterWeightedDegreeSum++;
					}
				}
			}
			nodeCentralities.put(node, clusterWeightedDegreeSum);
		}
		cluster.setNodesToCentralities(nodeCentralities);
	}
}
