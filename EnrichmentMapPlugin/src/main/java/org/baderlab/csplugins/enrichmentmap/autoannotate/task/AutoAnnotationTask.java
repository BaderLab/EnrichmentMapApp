/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id: BuildEnrichmentMapTask.java 383 2009-10-08 20:06:35Z risserlin $
// $LastChangedDate: 2009-10-08 16:06:35 -0400 (Thu, 08 Oct 2009) $
// $LastChangedRevision: 383 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/BuildEnrichmentMapTask.java $

package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotationTask extends AbstractTask {
	
	private CyNetwork network;
	private CyNetworkView view;
	private String clusterColumnName;
	private String nameColumnName;
	private String algorithm;
	private boolean layout;
	private CyLayoutAlgorithmManager layoutManager;
	private boolean groups;
	private String annotationSetName;
	private CyTableManager tableManager;
	private AutoAnnotationPanel annotationPanel;
	private DialogTaskManager dialogTaskManager;
	private SynchronousTaskManager<?> syncTaskManager;
	private CommandExecutorTaskFactory executor;
	private CytoPanel westPanel;

	public AutoAnnotationTask (CyNetworkView selectedView, 
			String clusterColumnName, String nameColumnName, 
			String algorithm, boolean layout, boolean groups, 
			String annotationSetName){
		
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		this.annotationPanel = autoAnnotationManager.getAnnotationPanel();
		this.view = selectedView;
		this.network = view.getModel();
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.algorithm = algorithm;
		this.layout = layout;
		this.layoutManager = autoAnnotationManager.getLayoutManager();
		this.groups = groups;
		this.annotationSetName = annotationSetName;
		this.dialogTaskManager = autoAnnotationManager.getDialogTaskManager();
		this.syncTaskManager = autoAnnotationManager.getSyncTaskManager();
		this.tableManager = autoAnnotationManager.getTableManager();
		this.executor = autoAnnotationManager.getCommandExecutor();
		this.westPanel = autoAnnotationManager.getWestPanel();
	};

	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		
		annotationPanel.setAnnotating(true);
		
		taskMonitor.setTitle("Annotating Enrichment Map");

		if (algorithm != null) {
			taskMonitor.setProgress(0.1);
			taskMonitor.setStatusMessage("Clustering nodes...");
			runClusterMaker();
		}
		
		taskMonitor.setProgress(0.3);
		taskMonitor.setStatusMessage("Creating clusters...");
		if (cancelled) return;
		
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(true);
    	AnnotationSet annotationSet = makeClusters(network, view, annotationSetName);
    	
    	if (layout && network.getDefaultNodeTable().getColumn(clusterColumnName).getType() != List.class) {
    		// Can't group layout with fuzzy clusters
    		taskMonitor.setProgress(0.4);
    		taskMonitor.setStatusMessage("Laying out nodes...");
    		layoutNodes(annotationSet);
    	}
    	
    	taskMonitor.setProgress(0.5);
    	taskMonitor.setStatusMessage("Running WordCloud...");
    	if (cancelled) return;
    	
    	runWordCloud(annotationSet, network);
    	
    	taskMonitor.setProgress(0.7);
    	taskMonitor.setStatusMessage("Annotating Clusters...");
    	if (cancelled) return;
    	
		Long clusterTableSUID = network.getDefaultNetworkTable().getRow(network.getSUID()).get(annotationSetName, Long.class);
    	CyTable clusterSetTable = tableManager.getTable(clusterTableSUID);
    	// Generate the labels for the clusters
    	for (Cluster cluster : annotationSet.getClusterMap().values()) {
    		AutoAnnotationUtils.updateClusterLabel(cluster, clusterSetTable);
    	}
    	
    	//Add groups if groups was selected
    	if (annotationSet.usingGroups()) createGroups(annotationSet);
    	
    	// Add these clusters to the table on the annotationPanel
    	annotationPanel.addClusters(annotationSet);
    	annotationPanel.updateSelectedView(view);
		westPanel.setSelectedIndex(westPanel.indexOfComponent(annotationPanel));
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(false);
		
		// Let the panel know annotating is finished
		annotationPanel.setAnnotating(false);
		
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Done!");
	}
	
	private void createGroups(AnnotationSet annotationSet){
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		CyGroupManager groupManager = autoAnnotationManager.getGroupManager();
		CyGroupFactory groupFactory =autoAnnotationManager.getGroupFactory();		
		
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
						
				//Create a Node with the Annotation Label to represent the group
				CyNode groupNode = this.network.addNode();
				this.network.getRow(groupNode).set(CyNetwork.NAME, cluster.getLabel());
				autoAnnotationManager.flushPayloadEvents();
				
				CyGroup group = groupFactory.createGroup(this.network, groupNode,new ArrayList<CyNode>(cluster.getNodes()),null, true);							
				cluster.setGroup(group);
			}
	}
	
	private void layoutNodes(AnnotationSet clusters) {
		CyLayoutAlgorithm attributeCircle = layoutManager.getLayout("attributes-layout");
		TaskIterator iterator = attributeCircle.createTaskIterator(view, attributeCircle.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, clusterColumnName);
		syncTaskManager.execute(iterator);
		CyLayoutAlgorithm force_directed = layoutManager.getLayout("force-directed");
		for (Cluster cluster : clusters.getClusterMap().values()) {
			Set<View<CyNode>> nodeViewSet = new HashSet<View<CyNode>>();
			for (CyNode node : cluster.getNodes()) {
				nodeViewSet.add(view.getNodeView(node));
			}
			// Only apply layout to nodes of size greater than 4
			if (nodeViewSet.size() > 4) {
				iterator = force_directed.createTaskIterator(view, force_directed.createLayoutContext(), nodeViewSet, null);
				syncTaskManager.execute(iterator);
			}
		}
	}

	private void runClusterMaker() {
		// Delete potential existing columns - sometimes clusterMaker doesn't do this
		if (network.getDefaultNodeTable().getColumn(clusterColumnName) != null) {
			network.getDefaultNodeTable().deleteColumn(clusterColumnName);
		}
		
		// Cluster based on similarity coefficient if possible
		String edgeAttribute;
		try {
			edgeAttribute = EnrichmentMapManager.getInstance().getCyNetworkList().get(view.getModel().getSUID()).getParams().getAttributePrefix() + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT;
		} catch (NullPointerException e) {
			edgeAttribute = "--None--";
		}
		
		for (View<CyNode> nodeView : view.getNodeViews()) {
			if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) {
				network.getRow(nodeView.getModel()).set(CyNetwork.SELECTED, true);
			}
		}
		
		// Executes the task inside of clusterMaker
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(getCommand(algorithm, edgeAttribute, network.toString()));
		Observer observer = new Observer();
		TaskIterator taskIterator = executor.createTaskIterator(commands, null);
		dialogTaskManager.execute(taskIterator, observer);
		while (!observer.isFinished()) {
			// Prevents task from continuing to execute until clusterMaker has finished
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private AnnotationSet makeClusters(CyNetwork network, CyNetworkView networkView, String name) {
		AnnotationSet annotationSet = new AnnotationSet(name, networkView, clusterColumnName, nameColumnName);
		
		List<CyNode> nodes = network.getNodeList();
		Class<?> columnType = network.getDefaultNodeTable().getColumn(clusterColumnName).getType();
		for (CyNode node : nodes) {
			TreeMap<Integer, Cluster> clusterMap = annotationSet.getClusterMap();
			// Get coordinates from the nodeView
			View<CyNode> nodeView = networkView.getNodeView(node);
			double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double[] coordinates = {x, y};
			double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);

			if (columnType == Integer.class) { // Discrete clustering
				Integer clusterNumber;
				clusterNumber = network.getRow(node).get(clusterColumnName, Integer.class);
				if (clusterNumber != null) { // empty values (no cluster) are given null, ignore these
					addNodeToCluster(clusterNumber, node, coordinates, nodeRadius, clusterMap, annotationSet);
				}
			} else if (columnType == List.class) { // Fuzzy clustering
				List<Integer> clusterNumbers = new ArrayList<Integer>();
				clusterNumbers = network.getRow(node).get(clusterColumnName, List.class);
				// Iterate over each cluster for the node, and add the node to each cluster
				for (int i = 0; i < clusterNumbers.size(); i++) {
					int clusterNumber = clusterNumbers.get(i);
					addNodeToCluster(clusterNumber, node, coordinates, nodeRadius, clusterMap, annotationSet);
				}
			} // No other possible columnTypes (since the dropdown only contains these types
		}
		annotationSet.setUseGroups(groups);
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			AutoAnnotationUtils.updateNodeCentralities(cluster);
		}
		return annotationSet;
	}
	
	// Adds the node and its coordinates to cluster number specified by clusterNumber
	private void addNodeToCluster(Integer clusterNumber, CyNode node, double[] coordinates,
			double nodeRadius, TreeMap<Integer, Cluster> clusterMap, AnnotationSet annotationSet) {
		Cluster cluster;
		if (!clusterMap.keySet().contains(clusterNumber)) {
			// Cluster doesn't exist, create it
			cluster = new Cluster(clusterNumber, annotationSet);
			annotationSet.addCluster(cluster);
		} else {
			// Cluster exists, look it up
			cluster = clusterMap.get(clusterNumber);
		}
		cluster.addNodeCoordinates(node, coordinates);
		cluster.addNodeRadius(node, nodeRadius);
	}
	
	private void runWordCloud(AnnotationSet annotationSet, CyNetwork network) {
		TreeMap<Integer, Cluster> clusterMap = annotationSet.getClusterMap();
		//ArrayList<String> commands = new ArrayList<String>();
		for (int clusterNumber : clusterMap.keySet()) {
			
			ArrayList<String> commands = new ArrayList<String>();
			
			Cluster cluster = clusterMap.get(clusterNumber);

			Set<CyNode> current_nodes = cluster.getNodes();
			String names = "";
			//TODO command will have issue with ccommas in the node names
			for(CyNode node : current_nodes){
				names = names +  "SUID:" + network.getRow(node).get(CyNetwork.SUID,  Long.class) + ",";
			}
			String command = "wordcloud create wordColumnName=\"" + nameColumnName + "\"" + 
			" cloudName=\"" + annotationSetName + " Cloud " +  clusterNumber + "\""
			+ " cloudGroupTableName=\"" + annotationSetName + "\"" + " nodelist=\"" + names + "\"";

			commands.add(command);
		//}
		//run all the commands (to create all the wordclouds) and wait til it finishes executing before continuing.
		Observer observer = new Observer();
		TaskIterator taskIterator = executor.createTaskIterator(commands, null);
		dialogTaskManager.execute(taskIterator, observer);
		// Prevents task from continuing to execute until wordCloud has finished
			while (!observer.isFinished()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
		}
		}
	}

	private void setClusterSelected(Cluster cluster, CyNetwork network, boolean b) {
		// Clear any previously selected nodes
		for (CyNode node : network.getNodeList()) {
			network.getRow(node).set(CyNetwork.SELECTED, false);
		}
		for (CyNode node : cluster.getNodesToCoordinates().keySet()) {
			network.getRow(node).set(CyNetwork.SELECTED, b);
		}
	}
		
	private String getCommand(String algorithm, String edgeAttribute, String networkName) {
		String command = "";
		if (algorithm == "Affinity Propagation Cluster") {
			command = "cluster ap attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Cluster Fuzzifier") {
			command = "cluster fuzzifier attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Community cluster (GLay)") {
			command = "cluster glay clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "ConnectedComponents Cluster") {
			command = "cluster connectedcomponents attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Fuzzy C-Means Cluster") {
			command = "cluster fcml attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "MCL Cluster") {
			command = "cluster mcl attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "SCPS Cluster") {
			command = "cluster scps attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		}
		return command;
	}
}
