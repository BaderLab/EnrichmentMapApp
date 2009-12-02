package org.baderlab.csplugins.enrichmentmap;

import cytoscape.*;
import cytoscape.util.CyNetworkNaming;
import cytoscape.ding.DingNetworkView;
import cytoscape.ding.CyGraphLOD;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.logger.CyLogger;
import cytoscape.view.CyNetworkView;
import cytoscape.data.CyAttributes;
import cytoscape.data.readers.GraphReader;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.VisualMappingManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.cytoscape.coreplugin.cpath2.cytoscape.BinarySifVisualStyleUtil;
import org.cytoscape.coreplugin.cpath2.web_service.*;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Attribute;
import org.jdom.Namespace;
import org.mskcc.biopax_plugin.mapping.MapNodeAttributes;
import org.mskcc.biopax_plugin.util.biopax.BioPaxUtil;
import org.mskcc.biopax_plugin.util.cytoscape.CytoscapeWrapper;
import org.mskcc.biopax_plugin.util.cytoscape.LayoutUtil;
import org.mskcc.biopax_plugin.util.cytoscape.NetworkListener;
import org.mskcc.biopax_plugin.view.BioPaxContainer;
import org.mskcc.biopax_plugin.style.BioPaxVisualStyleUtil;

import javax.swing.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ding.view.DGraphView;

public class PathwayCommonsImportNetwork {
	private TaskMonitor taskMonitor;
    private int cpath_id;
    private String networkTitle;
    private CyNetwork cyNetwork;
    private boolean biopax;

    /**
     * Constructor.
     *
     * @param ids           Array of cPath IDs.
     * @param biopax        BioPax or binary SIF
     * @param networkTitle  Network Title.
     */
    public PathwayCommonsImportNetwork(int cpath_id, boolean biopax, String networkTitle, TaskMonitor taskMonitor) {
        this.cpath_id = cpath_id;
        this.biopax = biopax;
        this.networkTitle = networkTitle;
        this.taskMonitor = taskMonitor;
    }

    public String getTitle() {
        return networkTitle;
    }

    /**
     * Our implementation of Task.run().
     * @throws IOException 
     * @throws HttpException 
     * @throws InterruptedException 
     */
    public void run() throws HttpException, IOException, InterruptedException {
    	//  Query webservice
		HttpClient client = new HttpClient();
		String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
		String query = "cmd=get_record_by_cpath_id&version=2.0&q=" + cpath_id + "&output=binary_sif";
		if (biopax)
			query = query.replace("binary_sif", "biopax");
		HttpMethodBase method = new GetMethod(liveUrl);
		System.out.println(liveUrl+"?"+query);
		method.setQueryString(URIUtil.encodeQuery(query));
        int statusCode = client.executeMethod(method);
        if (statusCode != 200) {
        	throw new InterruptedException("HTTP Status Code:  " + statusCode + ":  " + HttpStatus.getStatusText(statusCode) + ".");
        }
        //  Read in Content
        InputStream instream = method.getResponseBodyAsStream();
        ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = instream.read(buffer)) > 0) {
        	outstream.write(buffer, 0, len);
        }
        instream.close();
        
        String data = new String(outstream.toByteArray());

        //  Store query result to temp File
        String tmpDir = System.getProperty("java.io.tmpdir");
        File tmpFile;
        if (biopax) {
            tmpFile = File.createTempFile("temp", ".xml", new File(tmpDir));
        } else {
            tmpFile = File.createTempFile("temp", ".sif", new File(tmpDir));
        }
        tmpFile.deleteOnExit();
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(data);
        writer.close();

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
        if (biopax)
            postProcessingBioPAX();        
        else
            postProcessingBinarySif();     

        //  Fire a Network Loaded Event
        Object[] ret_val = new Object[2];
        ret_val[0] = cyNetwork;
        ret_val[1] = networkTitle;
        Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);

        //  Add Links Back to cPath Instance
        addLinksToCPathInstance (cyNetwork);
    }
    
    public void halt() {
    	
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
     * @param cyNetwork Cytoscape Network Object.
     * @throws InterruptedException 
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
        // getNodeDetails(cyNetwork, nodeAttributes);
        if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties().getProperty("viewThreshold"))) {

            //  Set up the right visual style
            VisualStyle visualStyle = BinarySifVisualStyleUtil.getVisualStyle();

            //  Set up the right layout algorithm.
            LayoutUtil layoutAlgorithm = new LayoutUtil();

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
     * @param cyNetwork Cytoscape Network Object.
     */
    private void postProcessingBioPAX() {
        if (cyNetwork.getNodeCount() < Integer.parseInt(CytoscapeInit.getProperties().getProperty("viewThreshold"))) {

            //  Set up the right visual style
            VisualStyle visualStyle = BioPaxVisualStyleUtil.getBioPaxVisualStyle();

            //  Set up the right layout algorithm.
            LayoutUtil layoutAlgorithm = new LayoutUtil();

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

        ArrayList batchList = createBatchArray(cyNetwork);

        for (int i=0; i<batchList.size(); i++) {
            ArrayList currentList = (ArrayList) batchList.get(i);
            long ids[] = new long [currentList.size()];
            for (int j=0; j<currentList.size(); j++) {
                CyNode node = (CyNode) currentList.get(j);
                ids[j] = Long.valueOf(node.getIdentifier());
            }
            try {
            	HttpClient client = new HttpClient();
        		String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
        		String query = "cmd=get_record_by_cpath_id&version=2.0&q=" + cpath_id + "&output=biopax";
        		HttpMethodBase method = new GetMethod(liveUrl);
        		method.setQueryString(URIUtil.encodeQuery(query));
                int statusCode = client.executeMethod(method);
                if (statusCode != 200) {
                	throw new InterruptedException("HTTP Status Code:  " + statusCode + ":  " + HttpStatus.getStatusText(statusCode) + ".");
                }
                //  Read in Content
                InputStream instream = method.getResponseBodyAsStream();
                ByteArrayOutputStream outstream = new ByteArrayOutputStream(4096);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = instream.read(buffer)) > 0) {
                	outstream.write(buffer, 0, len);
                }
                instream.close();
                
                String nodeData = new String(outstream.toByteArray());

                StringReader reader = new StringReader(nodeData);
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
                }
                int percentComplete = (int) (100.0 * (i / (double) batchList.size()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JDOMException e) {
                e.printStackTrace();
            }
        }
    }

    private ArrayList createBatchArray(CyNetwork cyNetwork) {
        CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
        int max_ids_per_request = 50;
        ArrayList masterList = new ArrayList();
        ArrayList currentList = new ArrayList();
        Iterator nodeIterator = cyNetwork.nodesIterator();
        int counter = 0;
        while (nodeIterator.hasNext()) {
            CyNode node = (CyNode) nodeIterator.next();
            String label = nodeAttributes.getStringAttribute(node.getIdentifier(),
                    BioPaxVisualStyleUtil.BIOPAX_NODE_LABEL);

            //  If we already have details on this node, skip it.
            if (label == null) {
                currentList.add(node);
                counter++;
            }
            if (counter > max_ids_per_request) {
                masterList.add(currentList);
                currentList = new ArrayList();
                counter = 0;
            }
        }
        if (currentList.size() > 0) {
            masterList.add(currentList);
        }
        return masterList;
    }

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
}