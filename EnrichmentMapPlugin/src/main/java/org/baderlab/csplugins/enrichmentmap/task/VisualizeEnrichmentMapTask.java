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

package org.baderlab.csplugins.enrichmentmap.task;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayouts;
import cytoscape.visual.*;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;

import java.io.File;
import java.util.*;

import giny.model.Node;
import giny.model.Edge;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 4:11:11 PM
 * <p>
 * Create visual representation of enrichment map in cytoscape
 */
public class VisualizeEnrichmentMapTask implements Task {

    private EnrichmentMap map;

    private HashMap<String, GenesetSimilarity> geneset_similarities;

    //enrichment map name
    private String mapName;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;

    /**
     * Class constructor - current task monitor
     *
     * @param params - enrichment map parameters for current map
     * @param taskMonitor - current task monitor
     */
    public VisualizeEnrichmentMapTask(EnrichmentMap map, TaskMonitor taskMonitor) {
        this(map);
        this.taskMonitor = taskMonitor;
    }

    /**
     * Class constructor
     *
     * @param params - enrichment map parameters for current map
     */
    public VisualizeEnrichmentMapTask(EnrichmentMap map) {
        this.map = map;
        this.geneset_similarities = map.getGenesetSimilarity();
        mapName = "Enrichment Map";

    }

    /**
     * Compute, and create cytoscape enrichment map
     *
     * @return  true if successful
     */
    public boolean computeMap(){
        if(taskMonitor == null){
            throw new IllegalStateException("Task Monitor is not set");
        }
        try{

            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?
            CyNetwork network;
            if(map.getParams().getAttributePrefix() == null)
            		map.getParams().setAttributePrefix();
            String prefix = map.getParams().getAttributePrefix();

            //Check to see if there is already a name specifed for the network
            //We still need to calculate the number of networks so that we can specify the paramters for
            //each network.
            if(map.getName() != null){
               network = Cytoscape.createNetwork(map.getName());             
            }
            else{                              
               map.setName(prefix+mapName);
               network = Cytoscape.createNetwork(prefix + mapName);                
            }
            // store path to GSEA report in Network Attribute
            if (map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
                CyAttributes networkAttributes = Cytoscape
                        .getNetworkAttributes();
                if (map.getParams().getGseaHtmlReportFileDataset1() != null) {
                    String report1Path = map.getParams().getGseaHtmlReportFileDataset1();
                    // On Windows we need to replace the Back-Slashes by forward-Slashes.
                    // Otherwise we might produce special characters (\r, \n, \t, ...) 
                    // when editing the attribute in Cytoscape.
                    // Anyway Windows supports slashes as separator in all NT based versions 
                    // (NT4, 2000, XP, Vista and newer)
                    report1Path = report1Path.replaceAll("\\\\", "/"); 
                    report1Path = report1Path.substring(0, report1Path.lastIndexOf('/') );
                    networkAttributes.setAttribute(network.getIdentifier(),
                            EnrichmentMapVisualStyle.NETW_REPORT1_DIR,
                            report1Path);
                }
                if (map.getParams().getGseaHtmlReportFileDataset2() != null) {
                    String report2Path = map.getParams().getGseaHtmlReportFileDataset1();
                    // On Windows we need to replace the Back-Slashes by forward-Slashes.
                    // Otherwise we might produce special characters (\r, \n, \t, ...) 
                    // when editing the attribute in Cytoscape.
                    // Anyway Windows supports slashes as separator in all NT based versions 
                    // (NT4, 2000, XP, Vista and newer)
                    report2Path = report2Path.replaceAll("\\\\", "/");
                    report2Path = report2Path.substring(0, report2Path.lastIndexOf('/') );
                    networkAttributes.setAttribute(network.getIdentifier(),
                            EnrichmentMapVisualStyle.NETW_REPORT2_DIR,
                            report2Path);
                }
            }

           // HashMap<String, EnrichmentResult> enrichmentResults1OfInterest = params.getEM().getFilteredEnrichment(EnrichmentMap.DATASET1).getEnrichments();
            //HashMap<String, EnrichmentResult> enrichmentResults2OfInterest = params.getEM().getFilteredEnrichment(EnrichmentMap.DATASET2).getEnrichments();
           
            //Currently this supports two dataset
            //TODO:add multiple dataset support.
            //go through the datasets to get the enrichments
            //currently only 2 datasets are supported in the visualization
            HashMap<String, EnrichmentResult> enrichmentResults1 = null;
            HashMap<String, EnrichmentResult> enrichmentResults2 = null;
            Set<String> dataset_names = map.getDatasets().keySet();
            int count = 0;
            for(Iterator<String> m = dataset_names.iterator(); m.hasNext();){
            		if(count == 0)
            			//get the enrichment results from the first one and place it in enrichment results 1
            			enrichmentResults1 = map.getDataset(m.next()).getEnrichments().getEnrichments();
            		else
            			enrichmentResults2 = map.getDataset(m.next()).getEnrichments().getEnrichments();
            		count++;
            }
                       
            HashMap<String, GeneSet> genesetsOfInterest = map.getAllGenesetsOfInterest();
            HashMap<String, GeneSet> genesetsOfInterest_set2 = null;
            /*if(params.isTwoDistinctExpressionSets())
            		genesetsOfInterest_set2 = ((EnrichmentMap_multispecies)params.getEM()).getGenesetsOfInterest_set2();
             */
            int currentProgress = 0;
            int maxValue = genesetsOfInterest.size();

            //create the nodes
            //Each geneset of interest is a node
            //its size is dependent on the size of the geneset

            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?

            //iterate through the each of the GSEA Results of interest
            for(Iterator<String> i = genesetsOfInterest.keySet().iterator(); i.hasNext(); ){
                String current_name =i.next();


                Node node = Cytoscape.getCyNode(current_name,true);

                network.addNode(node);

                //Add the description to the node
                GeneSet gs = null;
                GeneSet gs2 = null;
                if(!map.getParams().isTwoDistinctExpressionSets())
                    gs = (GeneSet)genesetsOfInterest.get(current_name);
                else{
                    if(genesetsOfInterest.containsKey(current_name))
                        gs = (GeneSet)genesetsOfInterest.get(current_name);
                    if(genesetsOfInterest_set2.containsKey(current_name))
                        gs2 = (GeneSet)genesetsOfInterest_set2.get(current_name);

                    if(gs == null && gs2 != null)
                        gs = gs2;
                }
                CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());

                //create an attribute that stores the genes that are associated with this node as an attribute list
                //only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
                if(map.getHashkey2gene() != null){
                    List<String> gene_list = new ArrayList<String>();
                    HashSet<Integer> genes_hash = new HashSet<Integer>();

                    genes_hash.addAll(gs.getGenes());

                    if(gs2 != null)
                        genes_hash.addAll(gs2.getGenes());

                    for(Iterator<Integer> j=genes_hash.iterator(); j.hasNext();){
                        Integer current = j.next();
                        String gene = map.getGeneFromHashKey(current);
                        if(gene_list != null)
                            gene_list.add(gene);
                    }

                    nodeAttrs.setListAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.GENES, gene_list);
                }

                if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                    GSEAResult current_result = (GSEAResult) enrichmentResults1.get(current_name);
                    setGSEAResultDataset1Attributes(node, current_result,prefix);
                }
                else{
                    GenericResult current_result = (GenericResult) enrichmentResults1.get(current_name);
                    setGenericResultDataset1Attributes(node, current_result, prefix);
                }

                //if we are using two datasets check to see if there is data for this node
                if(map.getParams().isTwoDatasets()){
                    if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                        if(enrichmentResults2.containsKey(current_name)){
                            GSEAResult second_result = (GSEAResult) enrichmentResults2.get(current_name);
                            setGSEAResultDataset2Attributes(node, second_result,prefix);

                        }

                    }
                    else{
                        if(enrichmentResults2.containsKey(current_name)){
                            GenericResult second_result = (GenericResult) enrichmentResults2.get(current_name);
                            setGenericResultDataset2Attributes(node, second_result,prefix);

                        }

                    }
                }

                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                    taskMonitor.setPercentCompleted(percentComplete);
                    taskMonitor.setStatus("Building Enrichment Map " + currentProgress + " of " + maxValue);
                    taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                }
                currentProgress++;



            }

            //Add any additional nodes from the second dataset that haven't been added yet
/*            if(params.isTwoDatasets()){
                for(Iterator<String> i = enrichmentResults2OfInterest.keySet().iterator(); i.hasNext(); ){
                    String current_name =i.next();

                    //is this already a node from the first subset
                    if(enrichmentResults1OfInterest.containsKey(current_name)){
                        //Don't need to add it
                    }
                    else{
                        Node node = Cytoscape.getCyNode(current_name, true);

                        network.addNode(node);

                        //Add the description to the node
                        GeneSet gs =null;
                        GeneSet gs2 = null;
                        if(!params.isTwoDistinctExpressionSets())
                            gs = (GeneSet)genesetsOfInterest.get(current_name);
                        else{
                            if(genesetsOfInterest.containsKey(current_name))
                                gs = (GeneSet)genesetsOfInterest.get(current_name);
                            if(genesetsOfInterest_set2.containsKey(current_name))
                                gs2 = (GeneSet)genesetsOfInterest_set2.get(current_name);

                            if(gs == null && gs2 != null)
                                gs = gs2;
                        }
                        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
                        nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());

                        //create an attribute that stores the genes that are associated with this node as an attribute list
                        //only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
                        if(params.getEM().getHashkey2gene() != null){
                            List<String> gene_list = new ArrayList<String>();
                            HashSet<Integer> genes_hash = new HashSet<Integer>();
                            genes_hash.addAll(gs.getGenes());

                            if(gs2 != null)
                                genes_hash.addAll(gs2.getGenes());

                            for(Iterator<Integer> j=genes_hash.iterator(); j.hasNext();){
                                Integer current = j.next();
                                String gene = params.getEM().getGeneFromHashKey(current);
                                if(gene_list != null)
                                    gene_list.add(gene);
                            }

                            nodeAttrs.setListAttribute(node.getIdentifier(), prefix+EnrichmentMapVisualStyle.GENES, gene_list);
                        }

                        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                            if(enrichmentResults1.containsKey(current_name)){
                                GSEAResult result = (GSEAResult) enrichmentResults1.get(current_name);
                                setGSEAResultDataset1Attributes(node,result, prefix);
                            }

                            GSEAResult second_result = (GSEAResult) enrichmentResults2OfInterest.get(current_name);
                            setGSEAResultDataset2Attributes(node, second_result,prefix);
                        }
                        else{
                            if(enrichmentResults1.containsKey(current_name)){
                                GenericResult result = (GenericResult) enrichmentResults1.get(current_name);
                                setGenericResultDataset1Attributes(node,result, prefix);
                            }

                            GenericResult second_result = (GenericResult) enrichmentResults2OfInterest.get(current_name);
                            setGenericResultDataset2Attributes(node, second_result,prefix);
                        }
                    }
                }
            }*/
            int k = 0;
            //iterate through the similarities to create the edges
            for(Iterator<String> j = geneset_similarities.keySet().iterator(); j.hasNext(); ){
                String current_name =j.next().toString();
                GenesetSimilarity current_result = geneset_similarities.get(current_name);


                //only create edges where the jaccard coefficient to great than
                if(current_result.getSimilarity_coeffecient()>=map.getParams().getSimilarityCutOff()){
                    Node node1 = Cytoscape.getCyNode(current_result.getGeneset1_Name(),false);
                    Node node2 = Cytoscape.getCyNode(current_result.getGeneset2_Name(),false);

                    Edge edge;

                    //in order to create multiple edges we need to create different edge types between the same two nodes
                    if(current_result.getEnrichment_set() == 1)
                        edge = (Edge) Cytoscape.getCyEdge(node1,  node2, Semantics.INTERACTION,EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1, true);
                    else if(current_result.getEnrichment_set() == 2)
                        edge = (Edge) Cytoscape.getCyEdge(node1,  node2, Semantics.INTERACTION,EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2,true);
                    else
                        edge = (Edge) Cytoscape.getCyEdge(node1,  node2, Semantics.INTERACTION,EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE,true);


                    network.addEdge(edge);

                    //Cytoscape.getNetworkView(network.getIdentifier()).addEdgeContextMenuListener(getEMEdgeContextMenuListener(current_result));

                    CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
                    edgeAttrs.setAttribute(edge.getIdentifier(), prefix+EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, current_result.getSimilarity_coeffecient());
                    edgeAttrs.setAttribute(edge.getIdentifier(), prefix+ EnrichmentMapVisualStyle.OVERLAP_SIZE, current_result.getSizeOfOverlap());
                    edgeAttrs.setAttribute(edge.getIdentifier(), prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET  , current_result.getEnrichment_set());

                    //create an attribute that stores the genes that are associated with this edge as an attribute list
                    //only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
                    if(map.getHashkey2gene() != null){
                        List<String> gene_list = new ArrayList<String>();
                        HashSet<Integer> genes_hash = current_result.getOverlapping_genes();
                        for(Iterator<Integer> i=genes_hash.iterator(); i.hasNext();){
                            Integer current = i.next();
                            String gene = map.getGeneFromHashKey(current);
                            if(gene_list != null)
                                gene_list.add(gene);
                        }

                        edgeAttrs.setListAttribute(edge.getIdentifier(), prefix+EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);
                    }

                }
            }

            CyNetworkView view = Cytoscape.createNetworkView( network );

            // get the VisualMappingManager and CalculatorCatalog
            VisualMappingManager manager = Cytoscape.getVisualMappingManager();
            CalculatorCatalog catalog = manager.getCalculatorCatalog();


            String vs_name = prefix + "Enrichment_map_style";
            // check to see if a visual style with this name already exists
            VisualStyle vs = catalog.getVisualStyle(vs_name);

            if (vs == null) {
                // if not, create it and add it to the catalog
                // Create the visual style
                EnrichmentMapVisualStyle em_vs = new EnrichmentMapVisualStyle(vs_name,map.getParams());

                vs = em_vs.createVisualStyle(network, prefix);
                //vs = createVisualStyle(network,prefix);

                catalog.addVisualStyle(vs);
            }

            view.setVisualStyle(vs.getName()); // not strictly necessary

            // actually apply the visual style
            manager.setVisualStyle(vs);
            view.redrawGraph(true,true);


            view.applyLayout(CyLayouts.getLayout("force-directed"));


            //register the new Network
            EnrichmentMapManager EMmanager = EnrichmentMapManager.getInstance();
            EMmanager.registerNetwork(network,map);

            //initialize parameter panel with info for this network
            ParametersPanel parametersPanel = EMmanager.getParameterPanel();
            parametersPanel.updatePanel(map);
            final CytoscapeDesktop desktop = Cytoscape.getDesktop();
            final CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
            cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parametersPanel));

            //set focus to EnrichmentMapInputPanel (otherwise it is reverted to the Editor)
            EnrichmentMapInputPanel emInputPanel = EMmanager.getInputWindow();
            final CytoPanel cytoControlPanel = desktop.getCytoPanel(SwingConstants.WEST);
            cytoControlPanel.setSelectedIndex(cytoControlPanel.indexOfComponent(emInputPanel));

            //add the click on node/edge listener
            view.addGraphViewChangeListener(new EnrichmentMapActionListener(map));

            //make sure the network is registered so that Quickfind works
            Cytoscape.firePropertyChange(cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED, network, view);


        } catch(IllegalThreadStateException e){
            taskMonitor.setException(e, "Unable to compute similarity coefficients");
            return false;
        }

        return true;
    }

    /**
     * set node attributes for dataset1 generic results
     *
     * @param node - node to associated attributes to
     * @param result - generic results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGenericResultDataset1Attributes(Node node, GenericResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
        String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-result.getPvalue()));
        }
        else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 2 generic results
     *
     * @param node - node to associated attributes to
     * @param result - generic results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGenericResultDataset2Attributes(Node node, GenericResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
        String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-result.getPvalue()));
        }
        else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 1 gsea results
     *
     * @param node - node to associated attributes to
     * @param result - gsea results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGSEAResultDataset1Attributes(Node node, GSEAResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        //format the node name
        String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, result.getFwerqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.ES_DATASET1, result.getES());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NES_DATASET1, result.getNES());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
        		double current_pvalue = result.getPvalue();
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-current_pvalue));
        }
        else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 2 gsea results
     *
     * @param node - node to associated attributes to
     * @param result - gsea results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGSEAResultDataset2Attributes(Node node, GSEAResult result, String prefix){

        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();

        //format the node name
        String formattedName = formatLabel(result.getName());

        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, result.getFwerqvalue());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.ES_DATASET2, result.getES());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.NES_DATASET2, result.getNES());
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        nodeAttrs.setAttribute(node.getIdentifier(), prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-result.getPvalue()));
        }
        else{
            nodeAttrs.setAttribute(node.getIdentifier(), prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-result.getPvalue())));
        }

    }

    /**
     * Wrap label
     *
     * @param label - current one line representation of label
     * @return formatted, wrapped label
     */
    static String formatLabel(String label){
        String formattedLabel = "";

        int i = 0;
        int k = 1;

        //only wrap at spaces
        String[] tokens = label.split(" ");
        //first try and wrap label based on spacing
        if(tokens.length > 1){
            int current_count = 0;
            for(int j = 0; j< tokens.length;j++){
                if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength){
                    formattedLabel = formattedLabel + tokens[j] + " ";
                    current_count = current_count + tokens[j].length();
                }
                else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
                    formattedLabel = formattedLabel + "\n" + tokens[j] + " ";
                    current_count = tokens[j].length();
                }
            }
        }
        else{
            tokens = label.split("_");

            if(tokens.length > 1){
                int current_count = 0;
                for(int j = 0; j< tokens.length;j++){
                    if(j != 0)
                        formattedLabel = formattedLabel +  "_";
                    if(current_count + tokens[j].length() <= EnrichmentMapVisualStyle.maxNodeLabelLength){
                        formattedLabel = formattedLabel + tokens[j] ;
                        current_count = current_count + tokens[j].length();
                    }
                    else if(current_count + tokens[j].length() > EnrichmentMapVisualStyle.maxNodeLabelLength) {
                        formattedLabel = formattedLabel + "\n" + tokens[j] ;
                        current_count = tokens[j].length();
                    }
                }
            }

            //if there is only one token wrap it anyways.
            else if(tokens.length == 1){
                while(i<=label.length()){

                    if(i+EnrichmentMapVisualStyle.maxNodeLabelLength > label.length())
                        formattedLabel = formattedLabel + label.substring(i, label.length()) + "\n";
                    else
                        formattedLabel = formattedLabel + label.substring(i, k* EnrichmentMapVisualStyle.maxNodeLabelLength) + "\n";
                    i = (k * EnrichmentMapVisualStyle.maxNodeLabelLength) ;
                    k++;
                }
            }
        }

        return formattedLabel;
    }


    /**
     * Run the Task.
     */
    public void run() {
        computeMap();
    }

    /**
     * Non-blocking call to interrupt the task.
     */
    public void halt() {
        this.interrupted = true;
    }

    /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Building Enrichment Map");
    }

}
