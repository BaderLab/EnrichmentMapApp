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



import java.util.Collection;
import java.util.Iterator;
import java.util.List;


import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.Enrichment_Map_Plugin;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
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
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.util.swing.FileUtil;

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
	
	private CyNetwork network;
    private EnrichmentMap map;
    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;

    private List<CyNode> Nodes;
    private List<CyEdge> Edges;
    private CyApplicationManager applicationManager;
    private final CytoPanel cytoPanelSouth;
    private FileUtil fileUtil;
    private StreamUtil streamUtil;
    

    /**
     * Constructor for network action listener.
     *
     * @param params  - enrichment map parameters associated with this actionlistener
     */
    public EnrichmentMapActionListener(HeatMapPanel heatMapPanel_node,HeatMapPanel heatMapPanel_edge,
    		CyApplicationManager applicationManager,CySwingApplication application,
    		FileUtil fileUtil, StreamUtil streamUtil) {
        
    		this.applicationManager = applicationManager;
    		this.fileUtil = fileUtil;
    		this.streamUtil = streamUtil;
        this.edgeOverlapPanel = heatMapPanel_edge;
        this.nodeOverlapPanel = heatMapPanel_node;
        
        this.cytoPanelSouth = application.getCytoPanel(CytoPanelName.SOUTH);

           
    }

    /**
     * intialize the parameters needed for this instance of the action
     */
    private void initialize(){
    		//get the static enrichment map manager.
        EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
        
        this.map = manager.getMap(network.getSUID());
        
        if(map.getParams().isData()){        
            //create a heatmap parameters instance for this action listener
            HeatMapParameters hmParams = new HeatMapParameters(edgeOverlapPanel, nodeOverlapPanel,fileUtil,streamUtil);
            hmParams.initColorGradients(this.map.getDataset(EnrichmentMap.DATASET1).getExpressionSets());
            //associate the newly created heatmap parameters with the current enrichment map paramters
            this.map.getParams().setHmParams(hmParams);
        }
        
        this.Nodes = this.map.getParams().getSelectedNodes();
        this.Edges = this.map.getParams().getSelectedEdges();
        
    }
    /**
     * Handle network action.  This method handles edge or node selection or unselections.
     *
     * @param event
     */
    public void handleEvent(RowsSetEvent e) {
        //TODO: improve performance of calculating the Union of genesets (Nodes) and intersection of overlaps (Edges)
        // Meanwhile we have a flag to skip the updating of the Heatmap, which can be toggled by a check-mark in the EM-Menu
        boolean override_revalidate_heatmap = EnrichmentMapUtils.isOverrideHeatmapRevalidation();
        
        
        //get the current network
        this.network = this.applicationManager.getCurrentNetwork();
        initialize();
        
        //There is no flag to indicate that this is only an edge/node selection
        //After select get the nodes and the edges that were selected.
        if( ! override_revalidate_heatmap ) {
        		
        		//get the edges
        		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);

        		Edges.clear();
        		Edges.addAll(selectedEdges);
        		
            //if (Edges.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Edge_Limit",  "100") ) )
            if(Edges.size()>0)
        			createEdgesData();

            //get the nodes.
            List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);

            Nodes.clear();
            Nodes.addAll(selectedNodes);
            //if (Nodes.size() <= Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.Heatmap_Node_Limit",  "50") ) )
            if(Nodes.size()>0)
            		createNodesData();
            
            if(Nodes.isEmpty() && Edges.isEmpty())
                clearPanels();
        }
    }

  public void createEdgesData(){

      if(map.getParams().isData()){
        edgeOverlapPanel.updatePanel(map);
        if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
        		// If the state of the cytoPanelWest is HIDE, show it
            if (cytoPanelSouth.getState() == CytoPanelState.HIDE) {
          	  	cytoPanelSouth.setState(CytoPanelState.DOCK);
            }

           // Select my panel
          int index = cytoPanelSouth.indexOfComponent(this.edgeOverlapPanel);
          if (index == -1) {
          	 	return;
          }
          cytoPanelSouth.setSelectedIndex(index);
        }
        edgeOverlapPanel.revalidate();

      }

  }

  private void createNodesData(){

        if(map.getParams().isData()){
            nodeOverlapPanel.updatePanel(map);
            if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
            	// If the state of the cytoPanelWest is HIDE, show it
                if (cytoPanelSouth.getState() == CytoPanelState.HIDE) {
              	  	cytoPanelSouth.setState(CytoPanelState.DOCK);
                }

               // Select my panel
              int index = cytoPanelSouth.indexOfComponent(this.nodeOverlapPanel);
              if (index == -1) {
              	 	return;
              }
             cytoPanelSouth.setSelectedIndex(index);
            }
            nodeOverlapPanel.revalidate();
        }

  }

    public void clearPanels(){
        if(map.getParams().isData()){
            nodeOverlapPanel.clearPanel();
            edgeOverlapPanel.clearPanel();
            if ( ! map.getParams().isDisableHeatmapAutofocus() ) {
            		//cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
	            //cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
            }
        }
    }

}
