package org.baderlab.csplugins.enrichmentmap;



import giny.model.Node;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.util.URIUtil;
import org.cytoscape.coreplugin.cpath2.cytoscape.BinarySifVisualStyleUtil;
import org.cytoscape.coreplugin.cpath2.task.ExecuteGetRecordByCPathId;
import org.mskcc.biopax_plugin.mapping.MapNodeAttributes;
import org.mskcc.biopax_plugin.util.cytoscape.CytoscapeWrapper;
import org.mskcc.biopax_plugin.util.cytoscape.LayoutUtil;
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
import cytoscape.view.CyNetworkView;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;

public class  PathwayCommonsQueryTask implements Task {
	private Node node;
	private boolean queryByNodeName;
	private TaskMonitor taskMonitor;
	private boolean interrupted = false;
	
	PathwayCommonsQueryTask(Node node, boolean queryByNodeName) {
		this.node = node;
		this.queryByNodeName = queryByNodeName;
	}
	
	// Helper methods
	private List<String> getGeneList() {
		List<String> geneList = new LinkedList<String>();
		EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
		EnrichmentMapParameters params = manager.getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
		HashMap<String, GeneSet> geneSets = params.getGenesetsOfInterest();
		GeneSet thisSet = geneSets.get(node.toString());
        for(Iterator<Integer> i = thisSet.getGenes().iterator(); i.hasNext();)
            geneList.add(params.getGeneFromHashKey(i.next()));
        return geneList;
	}
	private String parseIDType(List<String> geneList) {
		String sample = geneList.get(0);
		if (Pattern.matches("^[0-9]+$", sample))
			return "ENTREZ_GENE";
		else if (Pattern.matches("^[A-Z][0-9]+$", sample))
			return "UNIPROT";
		else
			return "GENE_SYMBOL";
	}
	private int queryByNodeName() throws InterruptedException, HttpException, IOException {
		int result = -1;
		HttpClient client = new HttpClient();
		String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
		String query = "version=3.0&q=PATHWAY_NAME&format=html&cmd=get_by_keyword&snapshot_id=GLOBAL_FILTER_SETTINGS&record_type=PATHWAY";
		query = query.replace("PATHWAY_NAME", node.getIdentifier());
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
        
        String content = new String(outstream.toByteArray());
        System.out.println(content);
        Pattern pattern = Pattern.compile("id=(\\d+)\">Pathway:");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find())
        	result = Integer.parseInt(matcher.group(1));
        return result;
	}
	private int queryByGeneList() throws InterruptedException, HttpException, IOException {
		int id;
		List<String> geneList = getGeneList();
		if (geneList.size() > 25)
			throw new InterruptedException("Geneset too large!");
		HttpClient client = new HttpClient();
		String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
		String query = "cmd=get_pathways&version=2.0&q=GENESET&input_id_type=ID_TYPE";
		String genes = geneList.toString();
		genes = genes.substring(1, genes.length()-2);
		query = query.replace("GENESET", genes);
		query = query.replace("ID_TYPE", parseIDType(geneList));
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
        
        String content = new String(outstream.toByteArray());
        String[] lines = content.split("\n");
        HashMap<Integer, Integer> cpaths = new HashMap<Integer, Integer>(); 
        boolean header = true;
        for (String line : lines) {
        	String[] fields = line.split("\t");
        	if (fields.length == 4 && !header) {
        		Integer key = Integer.parseInt(fields[3]);
        		if (cpaths.containsKey(key))
        			cpaths.put(key, cpaths.get(key)+1);
        		else
        			cpaths.put(key, 1);
        	}
        	header = false;
        }
        if (cpaths.isEmpty())
        	throw new InterruptedException("No matching pathways found!");
        else {
        	int result = 0, maxcount = 0;
        	for (Integer key : cpaths.keySet()) {
        		if (cpaths.get(key) > maxcount) {
        			maxcount = cpaths.get(key);
        			result = key;
        		}
        	}
        	return result;
        }
	}
	private void loadPathwayNetwork(int cpath_id) throws HttpException, IOException, InterruptedException{
		HttpClient client = new HttpClient();
		String liveUrl = "http://www.pathwaycommons.org/pc/webservice.do";
		String query = "cmd=get_record_by_cpath_id&version=2.0&q=" + cpath_id + "&output=binary_sif";
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
        
        String content = new String(outstream.toByteArray());
        
        //  Store result to Temp File
        String tmpDir = System.getProperty("java.io.tmpdir");
        //  Branch based on download mode setting.
        File tmpFile;
        //if (format == CPathResponseFormat.BIOPAX) {
        //    tmpFile = File.createTempFile("temp", ".xml", new File(tmpDir));
        //} else {
            tmpFile = File.createTempFile("temp", ".sif", new File(tmpDir));
        //}
        tmpFile.deleteOnExit();

        //  write to temp file.
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(content);
        writer.close();

        //  Load up File via ImportHandler Framework
        //  the biopax graph reader is going to be called
        //  it will look for the network view title
        //  via system properties, so lets set it now
        String networkTitle = node.toString();
        System.setProperty("biopax.network_view_title", networkTitle);
        
        GraphReader reader = Cytoscape.getImportHandler().getReader(tmpFile.getAbsolutePath());

        CyNetwork cyNetwork = Cytoscape.createNetwork(reader, false, null);
        // Branch, based on download mode.
        //if (format == CPathResponseFormat.BINARY_SIF) {
            // create network, without the view.
        postProcessingBinarySif(cyNetwork);
        //} else {
            //  create network, without the view.
        //    postProcessingBioPAX(cyNetwork);             
        //}

        // Fire appropriate network event.
            
        // Temp variable
        CyNetwork mergedNetwork = null;
        //
        if (mergedNetwork == null) {
            //  Fire a Network Loaded Event
            Object[] ret_val = new Object[2];
            ret_val[0] = cyNetwork;
            ret_val[1] = networkTitle;
            Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, ret_val);
        } else {
            //  Fire a Network Modified Event;  causes Quick Find to Re-Index.
            Object[] ret_val = new Object[2];
            ret_val[0] = mergedNetwork;
            ret_val[1] = networkTitle;
            Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null,
                ret_val);
        }

        if (taskMonitor != null) {
            taskMonitor.setStatus("Done");
            taskMonitor.setPercentCompleted(100);
        }
	}
	private void postProcessingBinarySif(final CyNetwork cyNetwork) {
        CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

        //  Init the node attribute meta data, e.g. description, visibility, etc.
        MapNodeAttributes.initAttributes(nodeAttributes);

        //  Set the Quick Find Default Index
        Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                "quickfind.default_index", "biopax.node_label");

        //  Specify that this is a BINARY_NETWORK
        Cytoscape.getNetworkAttributes().setAttribute(cyNetwork.getIdentifier(),
                BinarySifVisualStyleUtil.BINARY_NETWORK, Boolean.TRUE);

        if (!interrupted) {
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
                    }
                });
            }
        } else {
            //  If we have requested a halt, and we have a network, destroy it.
            if (cyNetwork != null) {
                Cytoscape.destroyNetwork(cyNetwork);
            }
        }
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
	// Interface methods
	public String getTitle() {
		return "Loading Pathway from PathwayCommons Database";
	}

	public void halt() {
		taskMonitor.setPercentCompleted(100);
		taskMonitor.setStatus("Failed!!!");
	}

	public void run() {
		taskMonitor.setStatus("Querying PathwayCommons Database...");
		try {
			taskMonitor.setPercentCompleted(0);
			int cpath_id;
			if (queryByNodeName)
				cpath_id = queryByNodeName();
			else
				cpath_id = queryByGeneList();
			taskMonitor.setStatus("Retrieving best matching pathway...");
			taskMonitor.setPercentCompleted(50);
			PathwayCommonsImportNetwork importNetwork = new PathwayCommonsImportNetwork(cpath_id, false, "dummy", taskMonitor);
			importNetwork.run();
			taskMonitor.setPercentCompleted(100);
			taskMonitor.setStatus("Pathway network loaded.");
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, e.getMessage()); // needs correct parent Frame
			System.out.println(e.getMessage());
			halt();
		}
		System.out.println("Task Complete");
	}

	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}
	
}