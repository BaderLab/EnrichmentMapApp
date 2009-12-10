/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Carl Song
 ** 	Parts of this class is adapted from org.cytoscape.coreplugin.cpath2.task.ExecuteGetRecordByCPathId
 ** Authors: Carl Song, Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
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

package org.baderlab.csplugins.enrichmentmap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.coreplugin.cpath2.cytoscape.BinarySifVisualStyleUtil;
import org.cytoscape.coreplugin.cpath2.web_service.CPathProperties;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.mskcc.biopax_plugin.mapping.MapNodeAttributes;
import org.mskcc.biopax_plugin.style.BioPaxVisualStyleUtil;
import org.mskcc.biopax_plugin.util.biopax.BioPaxUtil;
import org.mskcc.biopax_plugin.util.cytoscape.CytoscapeWrapper;
import org.mskcc.biopax_plugin.util.cytoscape.NetworkListener;
import org.mskcc.biopax_plugin.view.BioPaxContainer;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.CyAttributes;
import cytoscape.data.readers.GraphReader;
import cytoscape.ding.CyGraphLOD;
import cytoscape.ding.DingNetworkView;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.util.CyNetworkNaming;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;

/**
 * @author carlsong
 * 	adapted from org.cytoscape.coreplugin.cpath2.task.ExecuteGetRecordByCPathId
 */
public class PathwayCommonsImportTask implements Task {
    private TaskMonitor taskMonitor;
    private String cpath_id;
    private String networkTitle;
    private String xml;
    private CyNetwork cyNetwork;
    private boolean biopax;
    private boolean interrupted = false;
    private CyLayoutAlgorithm layoutAlgorithm = CyLayouts.getLayout("force-directed");

    /**
     * Constructor.
     *
     * @param ids           Array of cPath IDs.
     * @param biopax        BioPax or binary SIF
     * @param networkTitle  Network Title.
     */
    public PathwayCommonsImportTask(String cpath_id, boolean biopax) {
        this.cpath_id = cpath_id;
        this.biopax = biopax;
    }


    /**
     * Add Node Links Back to cPath Instance.
     * @param cyNetwork CyNetwork.
     */
    private void addLinksToCPathInstance(CyNetwork cyNetwork) {
        CyAttributes networkAttributes = Cytoscape.getNetworkAttributes();
        CPathProperties props = CPathProperties.getInstance();
        String serverName = props.getCPathServerName();
        String serverURL = props.getCPathUrl();
        String cPathServerDetailsUrl = networkAttributes.getStringAttribute
                (cyNetwork.getIdentifier(), "CPATH_SERVER_DETAILS_URL");
        if (cPathServerDetailsUrl == null) {
            networkAttributes.setAttribute(cyNetwork.getIdentifier(), "CPATH_SERVER_NAME", serverName);
            String url = serverURL.replaceFirst("webservice.do", "record2.do?id=");
            networkAttributes.setAttribute(cyNetwork.getIdentifier(), "CPATH_SERVER_DETAILS_URL", url);
        }
    }

    /**
     * Execute Post-Processing on BINARY SIF Network.
     *
     */
    private void postProcessingBinarySif() throws InterruptedException {
        CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

        //  Init the node attribute meta data, e.g. description, visibility, etc.
        MapNodeAttributes.initAttributes(nodeAttributes);

        //  Set the Quick Find Default Index
        Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                "quickfind.default_index", "biopax.node_label");

        //  Specify that this is a BINARY_NETWORK
        Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                BinarySifVisualStyleUtil.BINARY_NETWORK, Boolean.TRUE);

        //  Get all node details.
        getNodeDetails(cyNetwork, nodeAttributes);
        if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties().getProperty("viewThreshold"))) {

            //  Set up the right visual style
            VisualStyle visualStyle = BinarySifVisualStyleUtil.getVisualStyle();

            //  Now, create the view.
            //  Use local create view option, so that we don't mess up the visual style.
            CyNetworkView view = createNetworkView
                    (cyNetwork, cyNetwork.getTitle(), layoutAlgorithm, null);
            
            //  Now apply the visual style;
            //  Doing this as a separate step ensures that the visual style appears
            //  in the visual style drop-down menu.
            view.applyVizmapper(visualStyle);

            // Set up clickable node details.
            CytoscapeWrapper.initBioPaxPlugInUI();
            final BioPaxContainer bpContainer = BioPaxContainer.getInstance();
            NetworkListener networkListener = bpContainer.getNetworkListener();
            networkListener.registerNetwork(cyNetwork);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    CytoscapeWrapper.activateBioPaxPlugInTab(bpContainer);
                    bpContainer.showLegend();
                    Cytoscape.getCurrentNetworkView().fitContent();
                    String networkTitleWithUnderscores = networkTitle.replaceAll(": ", "");
                    networkTitleWithUnderscores = networkTitleWithUnderscores.replaceAll(" ", "_");
                    networkTitleWithUnderscores = CyNetworkNaming.getSuggestedNetworkTitle
                            (networkTitleWithUnderscores);
                    cyNetwork.setTitle(networkTitleWithUnderscores);
                }
            });
        }
    }

    /**
     * Execute Post-Processing on BioPAX Network.
     *
     */
    private void postProcessingBioPAX() {
        if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties().getProperty("viewThreshold"))) {

            //  Set up the right visual style
            VisualStyle visualStyle = BioPaxVisualStyleUtil.getBioPaxVisualStyle();
            
            //  Now, create the view.
            //  Use local create view option, so that we don't mess up the visual style.
            CyNetworkView view = createNetworkView(cyNetwork,
                    cyNetwork.getTitle(), layoutAlgorithm, null);

            //  Now apply the visual style;
            //  Doing this as a separate step ensures that the visual style appears
            //  in the visual style drop-down menu.
            view.applyVizmapper(visualStyle);
        }
    }

    /**
     * Gets Details for Each Node from Web Service API.
     * @throws InterruptedException 
     */
    private void getNodeDetails (CyNetwork cyNetwork,  CyAttributes nodeAttributes) throws InterruptedException {
        taskMonitor.setStatus("Retrieving node details...");
        taskMonitor.setPercentCompleted(0);
        try {
            StringReader reader = new StringReader(xml);
            BioPaxUtil bpUtil = new BioPaxUtil(reader, taskMonitor);
            ArrayList peList = bpUtil.getPhysicalEntityList();
            Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            for (int j=0; j<peList.size(); j++) {
                Element element = (Element) peList.get(j);
                String id = element.getAttributeValue("ID", ns);
                if (id != null) {
                    id = id.replaceAll("CPATH-", "");
                    MapNodeAttributes.mapNodeAttribute(element, id, nodeAttributes, bpUtil);
                }
                taskMonitor.setPercentCompleted((int) (100.0 * (j / (double) peList.size())));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JDOMException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param network
     * @param title
     * @param layout
     * @param vs
     * @return
     */
    private CyNetworkView createNetworkView (CyNetwork network, String title, CyLayoutAlgorithm
            layout, VisualStyle vs) {

		if (Cytoscape.viewExists(network.getIdentifier())) {
			return Cytoscape.getNetworkView(network.getIdentifier());
		}

		final DingNetworkView view = new DingNetworkView(network, title);
		view.setGraphLOD(new CyGraphLOD());
		view.setIdentifier(network.getIdentifier());
		view.setTitle(network.getTitle());
		Cytoscape.getNetworkViewMap().put(network.getIdentifier(), view);
		Cytoscape.setSelectionMode(Cytoscape.getSelectionMode(), view);

        VisualMappingManager VMM = Cytoscape.getVisualMappingManager();
        if (vs != null) {
			view.setVisualStyle(vs.getName());
            VMM.setVisualStyle(vs);
            VMM.setNetworkView(view);
        }

		if (layout == null) {
			layout = CyLayouts.getDefaultLayout();
		}

		Cytoscape.firePropertyChange(cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED,
                null, view);
		layout.doLayout(view);
		view.fitContent();
		view.redrawGraph(false, true);
		return view;
    }
    
    /* 
     * Interface Methods
     */
    public String getTitle() {
    	return "Loading Pathway from PathwayCommons Database";
    }
    
	public void halt() {
		interrupted = true;
		taskMonitor.setPercentCompleted(100);
		taskMonitor.setStatus("Failed!!!");
	}
	
    public void run() {
    	String queryString = "cmd=get_record_by_cpath_id&version=2.0&q=" + cpath_id + "&output=biopax";
		taskMonitor.setStatus("Retrieving pathway data from Pathway Commons...");
    	try {
    		PathwayCommonsWebAPI webAPI = new PathwayCommonsWebAPI();    	
			xml = webAPI.query(queryString);
			//  Parse pathway name and set as networkTitle
	    	StringReader xmlReader = new StringReader(xml);
	        BioPaxUtil bpUtil;
			bpUtil = new BioPaxUtil(xmlReader, taskMonitor);
			Namespace ns = Namespace.getNamespace("http://www.biopax.org/release/biopax-level2.owl#");
			ArrayList pathways = bpUtil.getPathwayList();
			if (pathways.size() > 0) {
				Element pathway = (Element) pathways.get(0);
		        Element name = pathway.getChild("NAME", ns);
		        networkTitle = name.getText();
			} else {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
					    "This cpath_id does not refer to a pathway!",
					    "Invalid cpath_id",
					    JOptionPane.ERROR_MESSAGE);
				halt();
			}
			if (!interrupted) {
		    	//  Query for sif version if necessary
		        //  Store query result to temp File
		        String data;        
		        String tmpDir = System.getProperty("java.io.tmpdir");
		        File tmpFile;
		        if (biopax) {
		        	data = xml;
		            tmpFile = File.createTempFile("temp", ".xml", new File(tmpDir));
		        } else {
		    		queryString = queryString.replace("biopax", "binary_sif");
		    		data = webAPI.query(queryString);
		            tmpFile = File.createTempFile("temp", ".sif", new File(tmpDir));
		    	}
		        tmpFile.deleteOnExit();
		        FileWriter writer = new FileWriter(tmpFile);
		        writer.write(data);
		        writer.close();
		        
		        taskMonitor.setStatus("Creating pathway network...");
		        //  Load up File via ImportHandler Framework
		        //  the biopax graph reader is going to be called
		        //  it will look for the network view title
		        //  via system properties, so lets set it now
		        if (networkTitle != null && networkTitle.length() > 0) {
		            System.setProperty("biopax.network_view_title", networkTitle);
		        }
		        GraphReader reader = Cytoscape.getImportHandler().getReader(tmpFile.getAbsolutePath());
		        // create network, without the view.
		        cyNetwork = Cytoscape.createNetwork(reader, false, null);
		        taskMonitor.setStatus("Processing network view...");
		        if (biopax)
		            postProcessingBioPAX();        
		        else
		            postProcessingBinarySif();     	
			}
    	} catch (Exception e) {
    		e.printStackTrace();
    		halt();
    	}
    	if (!interrupted) {
            //  Fire a Network Loaded Event
            Object[] ret_val = new Object[2];
            ret_val[0] = cyNetwork;
            ret_val[1] = networkTitle;
            Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);

            //  Add Links Back to cPath Instance
            addLinksToCPathInstance (cyNetwork);
            
            //  Apply layout
            CyNetworkView myNetworkView = Cytoscape.getNetworkView(cyNetwork.getIdentifier());
            CyLayoutAlgorithm gridLayout = CyLayouts.getLayout("force-directed");
            gridLayout.doLayout(myNetworkView); 
    	}
    }

	public void setTaskMonitor(TaskMonitor monitor)
			throws IllegalThreadStateException {
		this.taskMonitor = monitor;
	}
}