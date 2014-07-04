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

package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.autoannotate.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.NodeText;
import org.baderlab.csplugins.enrichmentmap.autoannotate.RunWordCloudObserver;
import org.baderlab.csplugins.enrichmentmap.autoannotate.WordUtils;
import org.baderlab.csplugins.enrichmentmap.view.AnnotationDisplayPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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

public class AutoAnnotatorTask extends AbstractTask {
	
	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private OpenBrowser browser;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private long networkID;
	private String nameColumnName;
	private String clusterColumnName;
	private CyServiceRegistrar registrar;
	private SynchronousTaskManager syncTaskManager;
	private AnnotationDisplayPanel displayPanel;
	
	private boolean interrupted;

	public AutoAnnotatorTask(CySwingApplication application, CyApplicationManager applicationManager, 
			OpenBrowser browser, CyNetworkViewManager networkViewManager, CyNetworkManager networkManager,
			AnnotationManager annotationManager, AnnotationDisplayPanel displayPanel, long networkID, String clusterColumnName, 
			String nameColumnName, CyServiceRegistrar registrar, SynchronousTaskManager syncTaskManager){
		
		this.application = application;
		this.applicationManager = applicationManager;
		this.browser = browser;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.displayPanel = displayPanel;
		this.networkID = networkID;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.registrar = registrar;
		this.syncTaskManager = syncTaskManager;
		
		this.interrupted = false;
		
	};
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		this.interrupted = true;
		return;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Annotating Enrichment Map");

    	CyNetwork network = networkManager.getNetwork(networkID);
    	CyNetworkView networkView = applicationManager.getCurrentNetworkView();

    	ArrayList<Cluster> clusters = makeClusters(network, networkView);
		runWordCloud();
		labelClusters(clusters, network);

		for (Cluster cluster : clusters) {
			cluster.drawAnnotations();
		}		
		displayPanel.addClusters(clusters);
		CytoPanel southPanel = application.getCytoPanel(CytoPanelName.SOUTH);
		southPanel.setSelectedIndex(southPanel.indexOfComponent(displayPanel));
	}
	
	private ArrayList<Cluster> makeClusters(CyNetwork network, CyNetworkView networkView) {
		ArrayList<Cluster> clusters = new ArrayList<Cluster>();
		
		AnnotationFactory<ShapeAnnotation> shapeFactory = (AnnotationFactory<ShapeAnnotation>) registrar.getService(AnnotationFactory.class, "(type=ShapeAnnotation.class)");    	
		AnnotationFactory<TextAnnotation> textFactory = (AnnotationFactory<TextAnnotation>) registrar.getService(AnnotationFactory.class, "(type=TextAnnotation.class)");
		
		List<CyNode> nodes = network.getNodeList();
		for (CyNode node : nodes) {
			Integer clusterNumber = network.getRow(node).get(this.clusterColumnName, Integer.class);
			if (clusterNumber != null) {
				View<CyNode> nodeView = networkView.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				
				String nodeName = network.getRow(node).get(nameColumnName, String.class);
				NodeText nodeText = new NodeText();
				nodeText.setName(nodeName);
				
				// empty values (no cluster) are given null
				boolean flag = true;
				for (Cluster cluster : clusters) {
	 				if (cluster.getClusterNumber() == clusterNumber && flag) {
						cluster.addNode(node);
						cluster.addCoordinates(coordinates);
						cluster.addNodeText(nodeText);
						flag = false;
					}
				}
				if (flag) {
					Cluster cluster = new Cluster(clusterNumber, network, networkView, annotationManager, clusterColumnName, shapeFactory, textFactory);
					cluster.addNode(node);
					cluster.addCoordinates(coordinates);
					cluster.addNodeText(nodeText);
					clusters.add(cluster);
				}
			}
		}
		Collections.sort(clusters);
		return clusters;
	}
	
	private void runWordCloud() {
		RunWordCloudTask rwc = new RunWordCloudTask(registrar, clusterColumnName, nameColumnName);
		TaskIterator task = new TaskIterator(rwc);
		RunWordCloudObserver observer = new RunWordCloudObserver();
		syncTaskManager.execute(task, observer);
		while (!observer.isComplete()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void labelClusters(ArrayList<Cluster> clusters, CyNetwork network) {	
		for (Cluster cluster : clusters) {
			cluster.setLabel("");
			int clusterNumber = cluster.getClusterNumber();
			List<CyRow> nodeTable = network.getDefaultNodeTable().getAllRows();
			for (CyRow row : nodeTable) {
				Integer rowClusterNumber = row.get(clusterColumnName, Integer.class);
				if (rowClusterNumber != null && rowClusterNumber == clusterNumber) {
					List<String> wordList = row.get("WC_Word", List.class);
					List<String> sizeList = row.get("WC_FontSize", List.class);
					List<String> clusterList = row.get("WC_Cluster", List.class);
					List<String> numberList = row.get("WC_Number", List.class);
					String label = WordUtils.biggestWord(wordList, sizeList, clusterList, numberList);
					cluster.setLabel(label);
					break;
				}
			}
		}
	}
}
