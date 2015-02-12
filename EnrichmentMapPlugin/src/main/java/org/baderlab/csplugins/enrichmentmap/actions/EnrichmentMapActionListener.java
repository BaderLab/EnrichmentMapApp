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

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.actions;




import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.heatmap.task.UpdateHeatMapTask;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 1:25:36 PM
 * <p>
 * Class listener for node and edge selections.  For each enrichment map there is a separate instance of this
 * class specifying the enrichment map parameters, selected nodes, selected edges and heatmap panels
 */
public class EnrichmentMapActionListener implements RowsSetListener{
	
    private EnrichmentMap map;
    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;

    private List<CyNode> Nodes;
    private List<CyEdge> Edges;
    private CyApplicationManager applicationManager;
    private SynchronousTaskManager syncTaskManager;
    private final CytoPanel cytoPanelSouth;
    private FileUtil fileUtil;
    private StreamUtil streamUtil;

    private boolean heatMapUpdating;
    

    /**
     * Constructor for network action listener.
     *
     * @param params  - enrichment map parameters associated with this actionlistener
     */
    public EnrichmentMapActionListener(HeatMapPanel heatMapPanel_node,HeatMapPanel heatMapPanel_edge,
    		CyApplicationManager applicationManager,CySwingApplication application,
    		FileUtil fileUtil, StreamUtil streamUtil,SynchronousTaskManager syncTaskManager) {
        
    		this.applicationManager = applicationManager;
    		this.fileUtil = fileUtil;
    		this.streamUtil = streamUtil;
    		this.syncTaskManager = syncTaskManager;
        this.edgeOverlapPanel = heatMapPanel_edge;
        this.nodeOverlapPanel = heatMapPanel_node;
        heatMapUpdating = false;
        
        this.cytoPanelSouth = application.getCytoPanel(CytoPanelName.SOUTH);

           
    }

    public boolean isHeatMapUpdating() {
		return heatMapUpdating;
	}

	/**
     * intialize the parameters needed for this instance of the action
     */
    private boolean initialize(CyNetwork network){
    		//get the static enrichment map manager.
        EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
        this.map = manager.getMap(network.getSUID());
        if(map != null){
        		if(map.getParams().isData() && map.getParams().getHmParams() == null){        
        			//create a heatmap parameters instance for this action listener
        			HeatMapParameters hmParams = new HeatMapParameters(edgeOverlapPanel, nodeOverlapPanel);
        			hmParams.initColorGradients(this.map.getDataset(EnrichmentMap.DATASET1).getExpressionSets());
        			//associate the newly created heatmap parameters with the current enrichment map paramters
        			this.map.getParams().setHmParams(hmParams);
        		}
        
        		this.Nodes = this.map.getParams().getSelectedNodes();
        		this.Edges = this.map.getParams().getSelectedEdges();
        		return true;
        }
        return false;
    }
    /**
     * Handle network action.  This method handles edge or node selection or unselections.
     *
     * @param event
     */
    public void handleEvent(RowsSetEvent e) {
        //TODO: improve performance of calculating the Union of genesets (Nodes) and intersection of overlaps (Edges)
        // Meanwhile we have a flag to skip the updating of the Heatmap, which can be toggled by a check-mark in the EM-Menu
    	heatMapUpdating = true;

    	boolean override_revalidate_heatmap = EnrichmentMapUtils.isOverrideHeatmapRevalidation();
        
        //get the current network
        CyNetwork network = this.applicationManager.getCurrentNetwork();
        CyNetworkView view = this.applicationManager.getCurrentNetworkView();

        //only handle event if it is a selected node

        if(network != null && e != null && (e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable())){
        		if(initialize(network)){
        
        			//There is no flag to indicate that this is only an edge/node selection
        			//After select get the nodes and the edges that were selected.
        			if( ! override_revalidate_heatmap ) {
        		
        				//get the edges
        				List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);

        				Edges.clear();
        				Edges.addAll(selectedEdges);
        		
        				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);

        				Nodes.clear();
        				Nodes.addAll(selectedNodes);
        				
        				//once we have amalgamated all the nodes and edges, launch a task to update the heatmap.
        				UpdateHeatMapTask updateHeatmap = new UpdateHeatMapTask(map, Nodes, Edges, edgeOverlapPanel, nodeOverlapPanel, cytoPanelSouth,applicationManager);
        				Observer observer = new Observer();
        				syncTaskManager.execute(new TaskIterator(updateHeatmap), observer);
        				while (!observer.isFinished()) {
        					try {
								Thread.sleep(1);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
        				}
        				
        				
        				//if the network has been autoannotated we need to make sure the clusters have been selected
        				//also only handle the node selection events (not edges)
        				//TODO:need a cleaner way to find out if the currentView has an annotation
        				if(AutoAnnotationManager.getInstance().getAnnotationPanel()!=null && !AutoAnnotationManager.getInstance().isClusterTableUpdating()
        						&& e.getSource() == network.getDefaultNodeTable()){
        					
        					//go through all the clusters for this network to see if any of the cluster have all of their nodes selected
        					HashMap<CyNetworkView, AutoAnnotationParameters> annotations = AutoAnnotationManager.getInstance().getNetworkViewToAutoAnnotationParameters();
        					if(annotations.containsKey(view)){
        						AnnotationSet currentAnnotation = annotations.get(view).getSelectedAnnotationSet();
        						TableModel clusterTableModel = currentAnnotation.getClusterTable().getModel();
								ListSelectionModel clusterListSelectionModel = currentAnnotation.getClusterTable().getSelectionModel();
								
								//if there are clusters to add or to remove only do it once we have gone through all the clusters - to avoid race conditions.
        						clusterListSelectionModel.setValueIsAdjusting(true);
								
        						TreeMap<Integer, Cluster> clusters = currentAnnotation.getClusterMap();
        						//go through each cluster - figure out which ones need to be selected and
        						//which ones need to deselected
        						//If any nodes in a cluster are no longer selected then deselect cluster
        						//If all nodes in a cluster are selected then select cluster (in table and annotation)
        						for(Cluster cluster:clusters.values()){
        						        					
        							boolean select = true;
        							boolean unselectCluster = false;
        							for (CyNode node : cluster.getNodes()) {
        								//if any of the nodes that belong to this cluster are not in the selected set
        								//And the cluster is current marked as selected 
        								//then unselect the cluster
        								if (!selectedNodes.contains(node) && cluster.isSelected()) {
        									unselectCluster = true;
        									break;
        								}
        								//if any of the nodes that belong to this cluster are not in the selected set
        								//then do not select this cluster.
        								if (!selectedNodes.contains(node)) {
        									select = false;
        									break;
        								}
        							}
        							
        							//one last check, if the cluster is already selected and all its nodes are
        							//already selected then this is not a new selection event
        							if(select == true && cluster.isSelected())
        								select = false;
        							
        							//Cluster has been selected
        							//if all nodes in a cluster are selected
        							//update the cluster table
        							if (select) {
        								//set flag to tell listener that it shouldn't reselect the nodes as the user manually selected them. 
        								currentAnnotation.setManualSelection(true);
        								for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
        									if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
        										clusterListSelectionModel.addSelectionInterval(rowIndex, rowIndex);
        										//AutoAnnotationManager.getInstance().flushPayloadEvents();
        										break;
        									}
        								}
        							}
        							
        							//Cluster has been unselected
        							//update the cluster table
        							if(unselectCluster){
        								//set flag to tell listener that it shouldn't reselect the nodes as the user manually selected them. 
        								currentAnnotation.setManualSelection(true);
        								for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
        									if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
        										clusterListSelectionModel.removeSelectionInterval(rowIndex, rowIndex);
        										//AutoAnnotationManager.getInstance().flushPayloadEvents();
        										break;
        									}//end of if
        								}//end of for
        								
        							}//end of if unselectedcluster

        						}//end of For going through all clusters
        						
        						//if there are clusters to add or to remove only do it once we have gone through all the clusters - to avoid race conditions.
        						clusterListSelectionModel.setValueIsAdjusting(false);

        						
        					}
        					
        				}
        				
        			}
        		}
        }//end of if e.getSource check
        heatMapUpdating = false;
    }

}
