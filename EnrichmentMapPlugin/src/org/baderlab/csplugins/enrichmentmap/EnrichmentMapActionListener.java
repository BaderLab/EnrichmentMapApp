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

package org.baderlab.csplugins.enrichmentmap;

import giny.model.Edge;
import giny.model.Node;
import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.awt.event.ActionListener;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyMenus;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;
import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeToolBar;

import javax.swing.*;

/**
 * Created by
 * User: risserlin
 * Date: Feb 2, 2009
 * Time: 1:25:36 PM
 */
public class EnrichmentMapActionListener implements  GraphViewChangeListener {

    private EnrichmentMapParameters params;
    private OverlappingGenesPanel edgeOverlapPanel;
    private OverlappingGenesPanel nodeOverlapPanel;
    private SummaryPanel summaryPanel;

    private List<Node> Nodes;
    private List<Edge> Edges;

    private final CytoPanel cytoPanel;
    private final CytoPanel cytoSidePanel;
    
    public static String disable_heatmap_autofocus = Enrichment_Map_Plugin.cyto_prop.getProperty("EnrichmentMap.disable_heatmap_autofocus", "false");
    public static String disable_genesetSummary_autofocus = Enrichment_Map_Plugin.cyto_prop.getProperty("EnrichmentMap.disable_genesetSummary_autofocus", "false");


    public EnrichmentMapActionListener(EnrichmentMapParameters params) {
        this.params = params;

        EnrichmentMapManager manager = EnrichmentMapManager.getInstance();

        //initialize the cyto panel to have the expression viewing.
        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);
        cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
        //final URL url = new URL("http","www.baderlab.org","/wiki/common/network_bader_website_icon.gif");
        //final Icon icon = new ImageIcon(url);
        if(params.isData()){
            edgeOverlapPanel = manager.getEdgesOverlapPanel();
            nodeOverlapPanel = manager.getNodesOverlapPanel();

            HeatMapParameters hmParams = new HeatMapParameters(edgeOverlapPanel, nodeOverlapPanel);
            hmParams.initColorGradients(params.getExpression());
            params.setHmParams(hmParams);
        }

        summaryPanel = (EnrichmentMapManager.getInstance()).getSummaryPanel();

        Nodes = params.getSelectedNodes();
        Edges = params.getSelectedEdges();
     
    }

    public void graphViewChanged(GraphViewChangeEvent event){
        if(event.isEdgesSelectedType()){

            Edge[] selectedEdges = event.getSelectedEdges();

            //Add all the selected edges to the list of edges
            for(int i=0;i<selectedEdges.length;i++){
                //check to see that the edge isn't already in the list
                if(!Edges.contains(selectedEdges[i]))
                    Edges.add(selectedEdges[i]);
            }

            createEdgesData();

        }
        if(event.isNodesSelectedType()){

            Node[] selectedNodes = event.getSelectedNodes();

            //Add all the selected nodes to the list of nodes
            for(int i=0;i<selectedNodes.length;i++){
                //check to see that the node isn't already in the list
                if(!Nodes.contains(selectedNodes[i]))
                    Nodes.add(selectedNodes[i]);
            }

            createNodesData();
        }
        if(event.isNodesUnselectedType()){
            Node[] unselectedNodes = event.getUnselectedNodes();

            //remove all the unselected nodes from the list of nodes
            for(int i = 0; i < unselectedNodes.length; i++){
                //check to make sure that the node is in the list
                if(Nodes.contains(unselectedNodes[i]))
                    Nodes.remove(unselectedNodes[i]);
            }

            if(!Nodes.isEmpty())
                createNodesData();

            if(Nodes.isEmpty() && Edges.isEmpty())
                clearPanels();
        }
        if(event.isEdgesUnselectedType()){

            Edge[] unselectedEdges = event.getUnselectedEdges();

            //Add all the selected edges to the list of edges
            for(int i=0;i<unselectedEdges.length;i++){
                 //check to see that the edge isn't already in the list
                 if(Edges.contains(unselectedEdges[i]))
                      Edges.remove(unselectedEdges[i]);
                 }

            if(!Edges.isEmpty())
                createEdgesData();

            if(Nodes.isEmpty() && Edges.isEmpty())
                clearPanels();
        }
    }

  public void createEdgesData(){

      summaryPanel.updateEdgeInfo(Edges.toArray());
      if ( ! disable_genesetSummary_autofocus.equalsIgnoreCase("TRUE") )
    	  cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
      summaryPanel.revalidate();

      if(params.isData()){
        edgeOverlapPanel.updatePanel(params);
        if ( ! disable_heatmap_autofocus.equalsIgnoreCase("TRUE") ) {
        	cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
        }
        edgeOverlapPanel.revalidate();

      }

  }

  private void createNodesData(){

        summaryPanel.updateNodeInfo(Nodes.toArray());
        if ( ! disable_genesetSummary_autofocus.equalsIgnoreCase("TRUE") ) 
        	cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(summaryPanel));
        summaryPanel.revalidate();

        if(params.isData()){
            nodeOverlapPanel.updatePanel(params);
            if ( ! disable_heatmap_autofocus.equalsIgnoreCase("TRUE") ) {
            	cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
            }
            nodeOverlapPanel.revalidate();
        }

  }

    public void clearPanels(){
        summaryPanel.clearInfo();
        if(params.isData()){
            nodeOverlapPanel.clearPanel();
            edgeOverlapPanel.clearPanel();
            if ( ! disable_heatmap_autofocus.equalsIgnoreCase("TRUE") ) {
	            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(nodeOverlapPanel));
	            cytoPanel.setSelectedIndex(cytoPanel.indexOfComponent(edgeOverlapPanel));
            }
        }
    }
}
