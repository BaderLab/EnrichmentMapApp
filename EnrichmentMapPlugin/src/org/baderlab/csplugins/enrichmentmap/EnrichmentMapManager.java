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

    //create only one instance of the summary and parameter panel.
    private SummaryPanel summaryPanel;
    private ParametersPanel parameterPanel;
    private OverlappingGenesPanel nodesOverlapPanel;
    private OverlappingGenesPanel edgesOverlapPanel;

    private EnrichmentMapInputPanel inputWindow;

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

        // to catch network creation / destruction events
        Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);

        // to catch network selection / focus events
        Cytoscape.getDesktop().getNetworkViewManager().getSwingPropertyChangeSupport()
                 .addPropertyChangeListener(this);

        final CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.SOUTH);
        CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);

        summaryPanel = new SummaryPanel();
        cytoSidePanel.add("Geneset Summary", summaryPanel);

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
                    summaryPanel.clearInfo();
                    nodesOverlapPanel.clearPanel();
                    edgesOverlapPanel.clearPanel();

                     EnrichmentMapParameters currentNetworkParams = cyNetworkList.get(networkId);
                    //update the parameters panel
                    parameterPanel.updatePanel(currentNetworkParams);

                    //update the input window to contain the parameters of the selected network
                    inputWindow.updateContents(currentNetworkParams);

                    summaryPanel.updateNodeInfo(currentNetworkParams.getSelectedNodes().toArray());

                    nodesOverlapPanel.updatePanel(currentNetworkParams);
                    edgesOverlapPanel.updatePanel(currentNetworkParams);

                    nodesOverlapPanel.revalidate();
                    edgesOverlapPanel.revalidate();


                } else {
                    //if the new network has just been created make sure the panels have been cleared
                    if(event.getPropertyName().equals(CytoscapeDesktop.NETWORK_VIEW_CREATED)){
                        summaryPanel.clearInfo();
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
    }

    /*
     * Determines if any network views we have created remains.
     *
      * @return boolean if any network views that we have created remain.
     */
    private boolean networkViewsRemain() {

        // interate through our network list checking if their views exists
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


    public SummaryPanel getSummaryPanel() {
        return summaryPanel;
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
}
