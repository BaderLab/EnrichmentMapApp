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

import cytoscape.Cytoscape;
import cytoscape.CyNetwork;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.CyNetworkView;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.view.cytopanels.CytoPanelState;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Mar 3, 2009
 * Time: 1:48:31 PM
 */
public class EnrichmentMapManager implements PropertyChangeListener {

    private static EnrichmentMapManager manager = null;

    private HashMap<String,EnrichmentMapParameters> cyNetworkList;
    private HashMap<String,PostAnalysisParameters> cyNetworkListPostAnalysis;

    //create only one instance of the  parameter and expression panels.
    private ParametersPanel parameterPanel;
    private OverlappingGenesPanel nodesOverlapPanel;
    private OverlappingGenesPanel edgesOverlapPanel;

    private EnrichmentMapInputPanel inputWindow;
    private PostAnalysisInputPanel analysisWindow;

    /**
     * Method to get instance of EnrichmentMapManager.
     *
     * @return EnrichmentMapManager
     */
    public static EnrichmentMapManager getInstance() {
        if(manager == null)
            manager = new EnrichmentMapManager();
        return manager;
    }

    /**
     * Property change listener - to get network/network view destroy events.
     *
     * @param event PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent event) {
        boolean relevantEventFlag = false;

        // network destroyed,  remove it from our list
        if (event.getPropertyName().equals(Cytoscape.NETWORK_DESTROYED)) {
            networkDestroyed((String) event.getNewValue());
            relevantEventFlag = true;
        }
        else if (event.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_DESTROYED)) {
            relevantEventFlag = true;
        }
        else if (event.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_CREATED)) {
            networkFocusEvent(event, false);
        }
        else if (event.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_FOCUSED)) {
            networkFocusEvent(event, false);
        }
        else if(event.getPropertyName().equals(Cytoscape.NETWORK_TITLE_MODIFIED)){
            CyNetwork cyNetwork = Cytoscape.getCurrentNetwork();
            String networkId = cyNetwork.getIdentifier();
            if ( cyNetworkList.containsKey(networkId) ) { // do we know that Network?
                EnrichmentMapParameters currentNetworkParams = cyNetworkList.get(networkId);
                currentNetworkParams.setNetworkName(cyNetwork.getTitle());
            }
        }
        if (relevantEventFlag && !networkViewsRemain()) {
            onZeroNetworkViewsRemain();
        }
    }

    /**
     * Constructor (private).
     *
     */
    private EnrichmentMapManager() {
        this.cyNetworkList = new HashMap<String,EnrichmentMapParameters>();
        this.cyNetworkListPostAnalysis = new HashMap<String,PostAnalysisParameters>();

        // to catch network creation / destruction events
        Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        // to catch network selection / focus events
        Cytoscape.getDesktop().getNetworkViewManager().getSwingPropertyChangeSupport()
                 .addPropertyChangeListener(this);

        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);
        CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);


        parameterPanel = new ParametersPanel();
        cytoSidePanel.add("Parameters", parameterPanel);

        cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parameterPanel));
        cytoSidePanel.setState(CytoPanelState.DOCK);

        nodesOverlapPanel = new OverlappingGenesPanel(true);
        edgesOverlapPanel = new OverlappingGenesPanel(false);

        cytoPanel.add("EM Overlap Expression viewer",edgesOverlapPanel);
        cytoPanel.add("EM Geneset Expression viewer",nodesOverlapPanel);
    }

    /**
     * Registers a newly created Network.
     *
     * @param cyNetwork Object.
     */
    public void registerNetwork(CyNetwork cyNetwork, EnrichmentMapParameters params) {

        if(!cyNetworkList.containsKey(cyNetwork.getIdentifier()))
            cyNetworkList.put(cyNetwork.getIdentifier(),params);

    }

    public void registerNetwork(CyNetwork cyNetwork, PostAnalysisParameters paParams) {

        if(!cyNetworkListPostAnalysis.containsKey(cyNetwork.getIdentifier()))
            cyNetworkListPostAnalysis.put(cyNetwork.getIdentifier(),paParams);

    }



    /**
     * Network Focus Event.
     *
     * @param event PropertyChangeEvent
     * @param sessionLoaded boolean
     */
    private void networkFocusEvent(PropertyChangeEvent event, boolean sessionLoaded) {
        // get network id
        String networkId = null;
        CyNetwork cyNetwork = null;
        Object newValue = event.getNewValue();

        //initialize the cyto panel to have the expression viewing.
        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);
        CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);

        if (event.getPropertyName().equals(Cytoscape.SESSION_LOADED)) {
            cyNetwork = Cytoscape.getCurrentNetwork();
            networkId = cyNetwork.getIdentifier();
        } else if (newValue instanceof CyNetwork) {
            cyNetwork = (CyNetwork) newValue;
            networkId = cyNetwork.getIdentifier();
        } else if (newValue instanceof String) {
            networkId = (String) newValue;
            cyNetwork = Cytoscape.getNetwork(networkId);
        }  else if (newValue instanceof CyNetworkView) {
            cyNetwork = ((CyNetworkView) newValue).getNetwork();
            networkId = cyNetwork.getIdentifier();
        }

        if (networkId != null) {
            // update view
            if (!sessionLoaded) {
                if (cyNetworkList.containsKey(networkId)) {
                    //clear the panels before re-initializing them
                    nodesOverlapPanel.clearPanel();
                    edgesOverlapPanel.clearPanel();

                     EnrichmentMapParameters currentNetworkParams = cyNetworkList.get(networkId);
                     PostAnalysisParameters  currentPaParams  = cyNetworkListPostAnalysis.get(networkId);
                    //update the parameters panel
                    parameterPanel.updatePanel(currentNetworkParams);

                    //update the input window to contain the parameters of the selected network
                    //only if there is a input window
                    if(inputWindow!=null)
                        inputWindow.updateContents(currentNetworkParams);
                    
                    if(analysisWindow!=null)
                        analysisWindow.updateContents(currentPaParams);

                    nodesOverlapPanel.updatePanel(currentNetworkParams);
                    edgesOverlapPanel.updatePanel(currentNetworkParams);

                    nodesOverlapPanel.revalidate();
                    edgesOverlapPanel.revalidate();


                } else {
                    //if the new network has just been created make sure the panels have been cleared
                    if(event.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_CREATED)){

                        nodesOverlapPanel.clearPanel();
                        edgesOverlapPanel.clearPanel();

                        nodesOverlapPanel.revalidate();
                        edgesOverlapPanel.revalidate();

                    }

                }
            }


        }
    }

    /*
     * Removes CyNetwork from our list if it has just been destroyed.
     *
     * @param networkID the ID of the CyNetwork just destroyed.
     */
    private void networkDestroyed(String networkId) {

        // get the index (if it exists) of this network in our list
        // if it exists, remove it
        if (cyNetworkList.containsKey(networkId)) {
            EnrichmentMapParameters currentNetworkParams = cyNetworkList.get(networkId);

            cyNetworkList.remove(networkId);
        }
        if (cyNetworkListPostAnalysis.containsKey(networkId)) {
            PostAnalysisParameters currentPaParams = cyNetworkListPostAnalysis.get(networkId);

            cyNetworkListPostAnalysis.remove(networkId);
        }
    }

    /*
     * Determines if any network views we have created remains.
     *
      * @return boolean if any network views that we have created remain.
     */
    private boolean networkViewsRemain() {

        // iterate through our network list checking if their views exists
        for (String id : cyNetworkList.keySet()) {

            // get the network view via id
            CyNetworkView cyNetworkView = Cytoscape.getNetworkView(id);

            if (cyNetworkView != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Event:  No Registered Network Views Remain.
     * May be subclassed.
     */
    protected void onZeroNetworkViewsRemain() {

    }


    public ParametersPanel getParameterPanel() {
        return parameterPanel;
    }

    public OverlappingGenesPanel getNodesOverlapPanel() {
        return nodesOverlapPanel;
    }

    public OverlappingGenesPanel getEdgesOverlapPanel() {
        return edgesOverlapPanel;
    }

    public HashMap<String, EnrichmentMapParameters> getCyNetworkList() {
        return cyNetworkList;
    }

    public EnrichmentMapParameters getParameters(String name){
        if(cyNetworkList.containsKey(name))
            return cyNetworkList.get(name);
        else
            return null;
    }

    public EnrichmentMapInputPanel getInputWindow() {
        return inputWindow;
    }

    public void setInputWindow(EnrichmentMapInputPanel inputWindow) {
        this.inputWindow = inputWindow;
    }

    public PostAnalysisInputPanel getAnalysisWindow() {
        return analysisWindow;
    }

    public void setAnalysisWindow(PostAnalysisInputPanel analysisWindow) {
        this.analysisWindow = analysisWindow;
    }
}
