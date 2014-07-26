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
import java.util.List;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
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
	
	private CySwingApplication application;
	private CyNetwork network;
	private CyNetworkView view;
	private String clusterColumnName;
	private String nameColumnName;
	private String algorithm;
	private CyServiceRegistrar registrar;
	private String annotationSetName;
	private CyTableManager tableManager;
	private AutoAnnotationPanel annotationPanel;
	private DialogTaskManager dialogTaskManager;

	public AutoAnnotationTask (CySwingApplication application, 
			AutoAnnotationManager autoAnnotationManager, 
			CyNetworkView selectedView, String clusterColumnName, String nameColumnName, 
			String algorithm, String annotationSetName, DialogTaskManager dialogTaskManager, 
			CyServiceRegistrar registrar, CyTableManager tableManager){
		
		this.application = application;
		this.annotationPanel = autoAnnotationManager.getAnnotationPanel();
		this.view = selectedView;
		this.network = view.getModel();
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.algorithm = algorithm;
		this.annotationSetName = annotationSetName;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
		this.tableManager = tableManager;
	};

	@Override
	public void cancel() {
		cancelled = true;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Annotating Enrichment Map");

		if (algorithm != "") {
			taskMonitor.setProgress(0.1);
			taskMonitor.setStatusMessage("Clustering nodes...");
			runClusterMaker(taskMonitor);
		}
		
		taskMonitor.setProgress(0.3);
		taskMonitor.setStatusMessage("Creating clusters...");
		if (cancelled) return;
		
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(true);
    	AnnotationSet clusters = makeClusters(network, view, annotationSetName);
    	
    	taskMonitor.setProgress(0.5);
    	taskMonitor.setStatusMessage("Running WordCloud...");
    	if (cancelled) return;
    	
    	runWordCloud(taskMonitor);
    	
    	taskMonitor.setProgress(0.7);
    	taskMonitor.setStatusMessage("Annotating Clusters...");
    	// TODO Visualizing clusters separately
    	if (cancelled) return;
    	
		Long clusterTableSUID = network.getDefaultNetworkTable().getRow(network.getSUID()).get(annotationSetName, Long.class);
    	CyTable clusterSetTable = tableManager.getTable(clusterTableSUID);
    	String annotationSetName = clusters.getCloudNamePrefix();
    	// Generate the labels for the clusters
    	for (Cluster cluster : clusters.getClusterMap().values()) {
    		AutoAnnotationUtils.updateClusterLabel(cluster, network, annotationSetName, clusterSetTable);
    	}
    	// Add these clusters to the table on the annotationPanel
    	annotationPanel.addClusters(clusters);
    	annotationPanel.updateSelectedView(view);
		CytoPanel westPanel = application.getCytoPanel(CytoPanelName.WEST);
		westPanel.setSelectedIndex(westPanel.indexOfComponent(annotationPanel));
		
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(false);
		
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Done!");
	}
	
	private void runClusterMaker(TaskMonitor taskMonitor) {
		// Delete potential existing columns - sometimes clusterMaker doesn't do this
		if (network.getDefaultNodeTable().getColumn(clusterColumnName) != null) {
			network.getDefaultNodeTable().deleteColumn(clusterColumnName);
		}
		
		// Tries to get edge attributes that make clusterMaker work better
		String edgeAttribute = "--None--";
		for (CyColumn edgeColumn : network.getDefaultEdgeTable().getColumns()) {
			String edgeName = edgeColumn.getName();
			if (edgeName.toLowerCase().contains("overlap_size") ||
				edgeName.toLowerCase().contains("similarity")){
				edgeAttribute = edgeName;
			}
		}
		
		// Executes the task inside of clusterMaker
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(getCommand(algorithm, edgeAttribute, network.toString()));
		ExecutorObserver observer = new ExecutorObserver();
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
			Cluster cluster = null;
			
			// Get coordinates 
			View<CyNode> nodeView = networkView.getNodeView(node);
			double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			double[] coordinates = {x, y};

			if (columnType == Integer.class) { // Discrete clustering
				Integer clusterNumber;
				clusterNumber = network.getRow(node).get(clusterColumnName, Integer.class);
				if (clusterNumber != null) { // empty values (no cluster) are given null
					if (clusterMap.keySet().contains(clusterNumber)) {
						// Cluster already exists
						cluster = clusterMap.get(clusterNumber);
					} else {
						// First node in a new cluster
						cluster = new Cluster(clusterNumber, annotationSet);
						annotationSet.addCluster(cluster);
					}
				} else {
					continue;
				}
			} else if (columnType == List.class) { // Fuzzy clustering
				List<Integer> clusterNumbers = new ArrayList<Integer>();
				clusterNumbers = network.getRow(node).get(clusterColumnName, List.class);
				for (int i = 0; i < clusterNumbers.size(); i++) {
					int clusterNumber = clusterNumbers.get(i);
					if (clusterMap.keySet().contains(clusterNumber)) {
						// Cluster already exists
						cluster = clusterMap.get(clusterNumber);
					} else {
						// First node in a new cluster
						cluster = new Cluster(clusterNumber, annotationSet);
						annotationSet.addCluster(cluster);
					}
				}
			} // No other possible columnTypes (since the dropdown only contains these types
			cluster.addNode(node);
			cluster.addCoordinates(coordinates);
		}
		return annotationSet;
	}
	
	private void runWordCloud(TaskMonitor taskMonitor) {
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud build clusterColumnName=\"" + clusterColumnName + "\" nameColumnName=\""
				+ nameColumnName + "\"" + " cloudNamePrefix=\"" + annotationSetName + "\"";
		commands.add(command);
		ExecutorObserver observer = new ExecutorObserver();
		TaskIterator taskIterator = executor.createTaskIterator(commands, null);
		registrar.getService(DialogTaskManager.class).execute(taskIterator, observer);
		// Prevents task from continuing to execute until wordCloud has finished
		while (!observer.isFinished()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private String getCommand(String algorithm, String edgeAttribute, String networkName) {
		String command = "";
		if (algorithm == "Affinity Propagation Cluster") {
			command = "cluster ap adjustLoops=true attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__APCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Cluster Fuzzifier") {
			command = "cluster fuzzifier adjustLoops=false attribute=\"" + edgeAttribute + "\" "
					+ "clusterAttribute=\"__fuzzifierCluster\" createGroups=false "
					+ "network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Community cluster (GLay)") {
			command = "cluster glay clusterAttribute=\"__glayCluster\" createGroups=false network=\""
					+ networkName + "\" restoreEdges=false selectedOnly=false "
					+ "showUI=false undirectedEdges=true";
		} else if (algorithm == "ConnectedComponents Cluster") {
			command = "cluster connectedcomponents adjustLoops=true attribute=\"" + edgeAttribute + "\" clusterAttribute=\""
					+ "__ccCluster\" createGroups=false network=\"" + networkName
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Fuzzy C-Means Cluster") {
			command = "cluster fcml adjustLoops=false attribute=\"" + edgeAttribute + "\" "
					+ "clusterAttribute=\"__fcmCluster\" createGroups=false "
					+ "estimateClusterNumber=true network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "MCL Cluster") {
			command = "cluster mcl adjustLoops=false attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__mclCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "SCPS Cluster") {
			command = "cluster scps adjustLoops=false attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__scpsCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" restoreEdges=false "
					+ "selectedOnly=false showUI=false undirectedEdges=true";
		}
		return command;
	}
}
