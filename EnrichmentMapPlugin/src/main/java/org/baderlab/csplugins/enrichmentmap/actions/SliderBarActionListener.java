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

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;

import java.util.Iterator;
import java.util.ArrayList;

import giny.model.Node;
import giny.model.Edge;
import giny.view.NodeView;
import giny.view.EdgeView;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.view.SliderBarPanel;

/**
 * Created by
 * User: risserlin
 * Date: Feb 24, 2009
 * Time: 3:29:39 PM
 * <p>
 * slider bar move action
 */
public class SliderBarActionListener implements ChangeListener {

    private SliderBarPanel panel;
    private EnrichmentMapParameters params;

    private ArrayList<HiddenNodes> hiddenNodes;
    private ArrayList<Edge> hiddenEdges;

    //attribute for dataset 1 that the slider bar is specific to
    private String attrib_dataset1;
    //attribute for dataset 2 that the slider bar is specific to
    private String attrib_dataset2;

    private boolean onlyEdges = false;

    /**
     * Class constructor
     *
     * @param panel
     * @param params - enchrichment map parameters for current map
     * @param attrib1 - attribute for dataset 1 that the slider bar is specific to (i.e. p-value or q-value)
     * @param attrib2 - attribute for dataset 2 that the slider bar is specific to (i.e. p-value or q-value)
     */
    public SliderBarActionListener(SliderBarPanel panel, EnrichmentMapParameters params, String attrib1, String attrib2, boolean onlyEdges) {
        this.panel = panel;
        this.params = params;
        hiddenNodes = new ArrayList();
        hiddenEdges = new ArrayList();

        attrib_dataset1 = attrib1;
        attrib_dataset2 = attrib2;

        this.onlyEdges = onlyEdges;

    }

    /**
     * Go through the current map and hide or unhide any nodes or edges associated with the threshold change.
     *
     * @param e
     */
    public void stateChanged(ChangeEvent e){

        //check to see if the event is associated with only edges
        if(onlyEdges){
            hideEdgesOnly(e);
            return;
        }

        JSlider source = (JSlider)e.getSource();
        Double max_cutoff = source.getValue()/panel.getPrecision();
        Double min_cutoff = source.getMinimum()/panel.getPrecision();

        panel.setLabel(source.getValue());

        CyNetwork network = Cytoscape.getCurrentNetwork();
        CyNetworkView view = Cytoscape.getCurrentNetworkView();
        CyAttributes attributes = Cytoscape.getNodeAttributes();

        int[] nodes = network.getNodeIndicesArray();

       //get the prefix of the current network
       String prefix = params.getAttributePrefix();

        /*There are two different ways to hide and restore nodes.
        *if you hide the nodes from the view perspective the node is still in the underlying
        * network but it is just not visible.  So when the node is restored it is in the exact
        * same state and location.  The only problem with this way is if you try and re-layout
        * your network it behaves as if all the nodes that are hidden are still there and the layout
        * does not change.
        * if you hide the node from the network perspective the node is deleted from the network
        * and when the node is restored (granted that you have tracked references of the "hidden"
        * nodes and edges) it restored to the top right of the panel and the user is required to
        * relayout the nodes. (can also be done programmatically)
        */

 /*       for(int i = 0; i< nodes.length; i++){
           Node currentNode = network.getNode(nodes[i]);
           NodeView currentView = Cytoscape.getCurrentNetworkView().getNodeView(currentNode);
           Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

           if((pvalue_dataset1 > max_cutoff) || (pvalue_dataset1 < min_cutoff)){
               if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);
                  if((pvalue_dataset2 > max_cutoff) || (pvalue_dataset2 < min_cutoff)){
                        view.hideGraphObject(currentView);
                  }
                   else{
                      view.showGraphObject(currentView);
                      //restore the edges as well
                      int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                      for(int m = 0;m< edges.length;m++){
                          EdgeView currentEdgeView = view.getEdgeView(edges[m]);
                          view.showGraphObject(currentEdgeView);
                      }

                  }

               }
               else{
                   view.hideGraphObject(currentView);
               }
            }
            else{
               view.showGraphObject(currentView);
               //restore the edges as well
               int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
               for(int m = 0;m< edges.length;m++){
                   EdgeView currentEdgeView = view.getEdgeView(edges[m]);
                   view.showGraphObject(currentEdgeView);
               }
           }
       }
*/

       //go through all the existing nodes to see if we need to hide any new nodes.
       for(int i = 0; i< nodes.length; i++){
           Node currentNode = network.getNode(nodes[i]);
           NodeView currentView = view.getNodeView(currentNode);
           
           // skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
           if( attributes.hasAttribute(currentNode.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE)
               && ! EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT.equalsIgnoreCase(attributes.getStringAttribute(currentNode.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE)) )
               continue;
           
           
           Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

           //possible that there isn't a p-value for this geneset
           if(pvalue_dataset1 == null)
            pvalue_dataset1 = 0.99;

           if((pvalue_dataset1 > max_cutoff) || (pvalue_dataset1 < min_cutoff)){
               if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);

                   if(pvalue_dataset2 == null)
                        pvalue_dataset2 = 0.99;

                  if((pvalue_dataset2 > max_cutoff) || (pvalue_dataset2 < min_cutoff)){

                        int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                        for(int m = 0;m< edges.length;m++)
                            hiddenEdges.add(network.getEdge(edges[m]));

                        hiddenNodes.add(new HiddenNodes(currentNode, currentView.getXPosition(), currentView.getYPosition()));
                        network.hideNode(currentNode);
                  }

               }
               else{
                   int edges[] = network.getAdjacentEdgeIndicesArray(currentNode.getRootGraphIndex(),true,true,true);
                   for(int m = 0;m< edges.length;m++)
                            hiddenEdges.add(network.getEdge(edges[m]));
                   hiddenNodes.add(new HiddenNodes(currentNode, currentView.getXPosition(), currentView.getYPosition()));
                   network.hideNode(currentNode);
                  }
           }
       }

        //go through all the hidden nodes to see if we need to restore any of them
        ArrayList<HiddenNodes> unhiddenNodes = new ArrayList();
        ArrayList<Edge> unhiddenEdges = new ArrayList();

        for(Iterator j = hiddenNodes.iterator();j.hasNext();){
            HiddenNodes currentHN = (HiddenNodes)j.next();
            Node currentNode = currentHN.getNode();
            Double pvalue_dataset1 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset1);

            //possible that there isn't a p-value for this geneset
           if(pvalue_dataset1 == null)
            pvalue_dataset1 = 0.99;

            if((pvalue_dataset1 <= max_cutoff) && (pvalue_dataset1 >= min_cutoff)){

                network.restoreNode(currentNode);
                NodeView currentNodeView = view.getNodeView(currentNode);
                currentNodeView.setXPosition(currentHN.getX());
                currentNodeView.setYPosition(currentHN.getY());
                view.updateView();
                unhiddenNodes.add(currentHN);



            }
            if(params.isTwoDatasets()){
                   Double pvalue_dataset2 = attributes.getDoubleAttribute(currentNode.getIdentifier(), prefix + attrib_dataset2);

                 if(pvalue_dataset2 == null)
                        pvalue_dataset2 = 0.99;

                if((pvalue_dataset2 <= max_cutoff) && (pvalue_dataset2 >= min_cutoff)){
                        network.restoreNode(currentNode);
                        NodeView currentNodeView = view.getNodeView(currentNode);
                        currentNodeView.setXPosition(currentHN.getX());
                        currentNodeView.setYPosition(currentHN.getY());
                        unhiddenNodes.add(currentHN);


                  }
           }

        }

        //For the unhidden edges we need to restore its edges with nodes that exist in the network
        //restore edges where both nodes are in the network.
         for(Iterator k = hiddenEdges.iterator();k.hasNext();){
            Edge currentEdge = (Edge)k.next();
            if((network.getNode(currentEdge.getSource().getRootGraphIndex()) != null) &&
                (network.getNode(currentEdge.getTarget().getRootGraphIndex()) != null)){
                network.restoreEdge(currentEdge);
                unhiddenEdges.add(currentEdge);
            }
         }

        //remove the unhidden nodes from the list of hiddenNodes.
        for(Iterator k = unhiddenNodes.iterator();k.hasNext();)
            hiddenNodes.remove(k.next());

        //remove the unhidden edges from the list of hiddenEdges.
        for(Iterator k = unhiddenEdges.iterator();k.hasNext();)
            hiddenEdges.remove(k.next());
        
        view.redrawGraph(true,true);
        view.updateView();

   }

    public void hideEdgesOnly(ChangeEvent e){
        JSlider source = (JSlider)e.getSource();
        Double min_cutoff = source.getValue()/panel.getPrecision();
        Double max_cutoff = source.getMaximum()/panel.getPrecision();

        panel.setLabel(source.getValue());

        CyNetwork network = Cytoscape.getCurrentNetwork();
        CyNetworkView view = Cytoscape.getCurrentNetworkView();
        CyAttributes attributes = Cytoscape.getEdgeAttributes();

        int[] edges = network.getEdgeIndicesArray();

        //get the prefix of the current network
        String prefix = params.getAttributePrefix();


        for(int i = 0; i< edges.length; i++){
            Edge currentEdge = network.getEdge(edges[i]);

            // skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
            if( attributes.hasAttribute(currentEdge.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE)
                && ! EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT.equalsIgnoreCase(attributes.getStringAttribute(currentEdge.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE)) )
                continue;

            Double similarity_cutoff = attributes.getDoubleAttribute(currentEdge.getIdentifier(), prefix + attrib_dataset1);

            //possible that there isn't a p-value for this geneset
            if(similarity_cutoff == null)
             similarity_cutoff = 0.1;

            if((similarity_cutoff > max_cutoff) || (similarity_cutoff < min_cutoff)){
                hiddenEdges.add(currentEdge);
                network.hideEdge(currentEdge);
            }
        }

        //go through all the hidden edges to see if we need to restore any of them
        ArrayList<Edge> unhiddenEdges = new ArrayList<Edge>();

        for(Iterator j = hiddenEdges.iterator();j.hasNext();){
            Edge currentEdge = (Edge)j.next();
            Double similarity_curoff = attributes.getDoubleAttribute(currentEdge.getIdentifier(), prefix + attrib_dataset1);

            //possible that there isn't a p-value for this geneset
            if(similarity_curoff == null)
                similarity_curoff = 0.1;


            if((similarity_curoff <= max_cutoff) && (similarity_curoff >= min_cutoff)){
            		//need to check if the edge that is about to be restored does not actually connect to a node that 
            		//is already hidden
            		if((network.getNode(currentEdge.getSource().getRootGraphIndex()) != null) &&
                   (network.getNode(currentEdge.getTarget().getRootGraphIndex()) != null)){
                        network.restoreEdge(currentEdge);
                        unhiddenEdges.add(currentEdge);
                    }

            }
        }

        //remove the unhidden edges from the list of hiddenEdges.
        for(Iterator k = unhiddenEdges.iterator();k.hasNext();)
            hiddenEdges.remove(k.next());

        view.redrawGraph(true,true);
        view.updateView();

    }

    private class HiddenNodes{
           Node node;
           double x;
           double y;

           public HiddenNodes(Node node, double x , double y){
               this.node = node;
               this.x = x;
               this.y = y;
           }

        public Node getNode() {
            return node;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

    }

}
