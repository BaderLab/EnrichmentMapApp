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




import java.util.*;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 4:11:11 PM
 * <p>
 * Create visual representation of enrichment map in cytoscape
 */
public class CreateEnrichmentMapNetworkTask extends AbstractTask {

    private EnrichmentMap map;
    
    private CyApplicationManager applicationManager;
    private CyNetworkManager networkManager;    
    private CyNetworkFactory networkFactory;
    private CyTableFactory tableFactory;
    private CyTableManager tableManager;
    private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    
    private HashMap<String, GenesetSimilarity> geneset_similarities;

    //enrichment map name
    private String mapName;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    
    public static String node_table_suffix = "node_attribs";
    public static String edge_table_suffix = "edge_attribs";

    /**
     * Class constructor - current task monitor
     *
     * @param params - enrichment map parameters for current map
     * @param taskMonitor - current task monitor
     */
    public CreateEnrichmentMapNetworkTask(EnrichmentMap map, CyNetworkFactory networkFactory, CyApplicationManager applicationManager, CyNetworkManager networkManager,CyTableFactory tableFactory,CyTableManager tableManager,MapTableToNetworkTablesTaskFactory maptabletonetworktable) {
        this(map);
        this.networkFactory = networkFactory;
        this.applicationManager = applicationManager;
        this.networkManager = networkManager;
        this.tableFactory = tableFactory;
        this.tableManager = tableManager;
        this.mapTableToNetworkTable = maptabletonetworktable;
    }

    /**
     * Class constructor
     *
     * @param params - enrichment map parameters for current map
     */
    public CreateEnrichmentMapNetworkTask(EnrichmentMap map) {
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
        
        
            //on multiple runs of the program some of the nodes or all of them might already
            //be created but it is possible that they have different values for the attributes.  How do
            //we resolve this?
            CyNetwork network;
            if(map.getParams().getAttributePrefix() == null)
            		map.getParams().setAttributePrefix();
            String prefix = map.getParams().getAttributePrefix();
            
            //create the new network.
    			network = networkFactory.createNetwork();
    			
    			
            //Check to see if there is already a name specifed for the network
            //We still need to calculate the number of networks so that we can specify the paramters for
            //each network.

            if(map.getName() == null){            		                                                                  
               map.setName(prefix+mapName);                              
            }
            network.getRow(network).set(CyNetwork.NAME,map.getName());
            
            //set the NetworkID in the EM parameters
            map.getParams().setNetworkID(network.getSUID());
            
          //create the Node attributes table
			CyTable nodeTable = createNodeAttributes(network, map.getName().trim(),prefix);
			//create the edge attributes table
			CyTable edgeTable = createEdgeAttributes(network, map.getName().trim(),prefix);
            
            // store path to GSEA report in Network Attribute
            if (map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
     //TODO:Add network attributes           
               /* CyTable network_table = network.getDefaultNetworkTable();
                network_table.createColumn(EnrichmentMapVisualStyle.NETW_REPORT1_DIR,String.class,false);
                CyRow network_row = network_table.getRow(network);
                if (map.getParams().getFiles().containsKey(EnrichmentMap.DATASET1) && map.getParams().getFiles().get(EnrichmentMap.DATASET1).getGseaHtmlReportFile() != null) {
                    String report1Path = map.getParams().getFiles().get(EnrichmentMap.DATASET1).getGseaHtmlReportFile();
                    // On Windows we need to replace the Back-Slashes by forward-Slashes.
                    // Otherwise we might produce special characters (\r, \n, \t, ...) 
                    // when editing the attribute in Cytoscape.
                    // Anyway Windows supports slashes as separator in all NT based versions 
                    // (NT4, 2000, XP, Vista and newer)
                    report1Path = report1Path.replaceAll("\\\\", "/"); 
                    report1Path = report1Path.substring(0, report1Path.lastIndexOf('/') );
                    network_row.set(EnrichmentMapVisualStyle.NETW_REPORT1_DIR,report1Path);
                }
                if (map.getParams().getFiles().containsKey(EnrichmentMap.DATASET2) && map.getParams().getFiles().get(EnrichmentMap.DATASET2).getGseaHtmlReportFile() != null) {
                    String report2Path = map.getParams().getFiles().get(EnrichmentMap.DATASET2).getGseaHtmlReportFile();
                    // On Windows we need to replace the Back-Slashes by forward-Slashes.
                    // Otherwise we might produce special characters (\r, \n, \t, ...) 
                    // when editing the attribute in Cytoscape.
                    // Anyway Windows supports slashes as separator in all NT based versions 
                    // (NT4, 2000, XP, Vista and newer)
                    report2Path = report2Path.replaceAll("\\\\", "/");
                    report2Path = report2Path.substring(0, report2Path.lastIndexOf('/') );
                    network_row.set(EnrichmentMapVisualStyle.NETW_REPORT2_DIR,
                            report2Path);
                }*/
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
                       
            HashMap<String, GeneSet> genesetsOfInterest = map.getDataset(EnrichmentMap.DATASET1).getGenesetsOfInterest().getGenesets();
            HashMap<String, GeneSet> genesetsOfInterest_set2 = null;
            if(map.getParams().isTwoDatasets())
            		genesetsOfInterest_set2 = map.getDataset(EnrichmentMap.DATASET2).getGenesetsOfInterest().getGenesets();
             
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


                CyNode node = network.addNode();
                network.getRow(node).set(CyNetwork.NAME, current_name);               

                //Add the description to the node
                GeneSet gs = null;
                GeneSet gs2 = null;
                if(!map.getParams().isTwoDatasets())
                    gs = (GeneSet)genesetsOfInterest.get(current_name);
                else{
                    if(genesetsOfInterest.containsKey(current_name))
                        gs = (GeneSet)genesetsOfInterest.get(current_name);
                    if(genesetsOfInterest_set2.containsKey(current_name))
                        gs2 = (GeneSet)genesetsOfInterest_set2.get(current_name);

                    if(gs == null && gs2 != null)
                        gs = gs2;
                }         
               CyRow current_row = nodeTable.getRow(node.getSUID());
               current_row.set( prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());
                
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
                    current_row.set( prefix+ EnrichmentMapVisualStyle.GENES, gene_list);
                }

                if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                    GSEAResult current_result = (GSEAResult) enrichmentResults1.get(current_name);
                    setGSEAResultDataset1Attributes(current_row, current_result,prefix);
                }
                else{
                    GenericResult current_result = (GenericResult) enrichmentResults1.get(current_name);
                    setGenericResultDataset1Attributes(current_row, current_result, prefix);
                }

                //if we are using two datasets check to see if there is data for this node
                if(map.getParams().isTwoDatasets()){
                    if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                        if(enrichmentResults2.containsKey(current_name)){
                            GSEAResult second_result = (GSEAResult) enrichmentResults2.get(current_name);
                            setGSEAResultDataset2Attributes(current_row, second_result,prefix);

                        }

                    }
                    else{
                        if(enrichmentResults2.containsKey(current_name)){
                            GenericResult second_result = (GenericResult) enrichmentResults2.get(current_name);
                            setGenericResultDataset2Attributes(current_row, second_result,prefix);

                        }

                    }
                }

                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) {
                    taskMonitor.setProgress(percentComplete);
                    taskMonitor.setStatusMessage("Building Enrichment Map " + currentProgress + " of " + maxValue);                    
                }
                currentProgress++;

            }

            //Add any additional nodes from the second dataset that haven't been added yet
            if(map.getParams().isTwoDatasets()){
                for(Iterator<String> i = genesetsOfInterest_set2.keySet().iterator(); i.hasNext(); ){
                    String current_name =i.next();

                    //is this already a node from the first subset
                    if(genesetsOfInterest.containsKey(current_name)){
                        //Don't need to add it
                    }
                    else{
                    		CyNode node = network.addNode();
                        network.getRow(node).set(CyNetwork.NAME, current_name); 

                        //Add the description to the node
                        GeneSet gs =null;
                        GeneSet gs2 = null;
                        if(!map.getParams().isTwoDatasets())
                            gs = (GeneSet)genesetsOfInterest.get(current_name);
                        else{
                            if(genesetsOfInterest.containsKey(current_name))
                                gs = (GeneSet)genesetsOfInterest.get(current_name);
                            if(genesetsOfInterest_set2.containsKey(current_name))
                                gs2 = (GeneSet)genesetsOfInterest_set2.get(current_name);

                            if(gs == null && gs2 != null)
                                gs = gs2;
                        }
                        CyRow current_row = nodeTable.getRow(node.getSUID());
                        current_row.set( prefix+ EnrichmentMapVisualStyle.GS_DESCR, gs.getDescription());
                        
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

                            current_row.set(prefix+EnrichmentMapVisualStyle.GENES, gene_list);
                        }

                        if(map.getParams().getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                            if(enrichmentResults1.containsKey(current_name)){
                                GSEAResult result = (GSEAResult) enrichmentResults1.get(current_name);
                                setGSEAResultDataset1Attributes(current_row,result, prefix);
                            }

                            GSEAResult second_result = (GSEAResult) enrichmentResults2.get(current_name);
                            setGSEAResultDataset2Attributes(current_row, second_result,prefix);
                        }
                        else{
                            if(enrichmentResults1.containsKey(current_name)){
                                GenericResult result = (GenericResult) enrichmentResults1.get(current_name);
                                setGenericResultDataset1Attributes(current_row,result, prefix);
                            }

                            GenericResult second_result = (GenericResult) enrichmentResults2.get(current_name);
                            setGenericResultDataset2Attributes(current_row, second_result,prefix);
                        }
                    }
                }
            }
            int k = 0;
            //iterate through the similarities to create the edges
            for(Iterator<String> j = geneset_similarities.keySet().iterator(); j.hasNext(); ){
                String current_name =j.next().toString();
                GenesetSimilarity current_result = geneset_similarities.get(current_name);


                //only create edges where the jaccard coefficient to great than
                //and if both nodes exist
                if(current_result.getSimilarity_coeffecient()>=map.getParams().getSimilarityCutOff() &&
                		!getNodesWithValue(network,nodeTable,prefix + EnrichmentMapVisualStyle.NAME, current_result.getGeneset1_Name()).isEmpty() &&
                		!getNodesWithValue(network,nodeTable,prefix + EnrichmentMapVisualStyle.NAME,current_result.getGeneset2_Name()).isEmpty()){
                    CyNode node1 = getUniqueNodeWithValue(network,nodeTable,prefix + EnrichmentMapVisualStyle.NAME,current_result.getGeneset1_Name());
                    CyNode node2 = getUniqueNodeWithValue(network,nodeTable,prefix + EnrichmentMapVisualStyle.NAME,current_result.getGeneset2_Name());
                                                          
                    CyEdge edge = network.addEdge(node1, node2, false);
                    String edge_type;
                    
                    //TODO: Add attribute to edge specified as type.
                    //in order to create multiple edges we need to create different edge types between the same two nodes
                    if(current_result.getEnrichment_set() == 1)
                    		edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET1;                        
                    else if(current_result.getEnrichment_set() == 2)
                        edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE_SET2;
                    else
                        edge_type = EnrichmentMapParameters.ENRICHMENT_INTERACTION_TYPE;
                                                                            
                    CyRow current_edgerow = edgeTable.getRow(edge.getSUID());
                    current_edgerow.set(CyNetwork.NAME,current_name);
                    current_edgerow.set(CyEdge.INTERACTION, current_result.getInteractionType());
                    current_edgerow.set( prefix+EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, current_result.getSimilarity_coeffecient());
                    current_edgerow.set( prefix+ EnrichmentMapVisualStyle.OVERLAP_SIZE, current_result.getSizeOfOverlap());
                    current_edgerow.set( prefix + EnrichmentMapVisualStyle.ENRICHMENT_SET  , current_result.getEnrichment_set());

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

                        current_edgerow.set( prefix+EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);
                    }

                }
            }
            
            //register the network and tables
            this.networkManager.addNetwork(network);

            //TODO:change back to creating our own table.  Currently can only map to a string column.
    	        //in mean time use the default node table
            //this.tableManager.addTable(nodeTable);
            //this.tableManager.addTable(edgeTable);
                
            //super.insertTasksAfterCurrentTask( this.mapTableToNetworkTable.createTaskIterator(nodeTable,true,nets,CyNode.class ));
            //super.insertTasksAfterCurrentTask( this.mapTableToNetworkTable.createTaskIterator(nodeTable));            
            //super.insertTasksAfterCurrentTask( this.mapTableToNetworkTable.createTaskIterator(edgeTable,true,nets,CyEdge.class));
                                   
            //register the new Network with EM
            EnrichmentMapManager EMmanager = EnrichmentMapManager.getInstance();
            EMmanager.registerNetwork(network,map);
            
            map.getParams().setNetworkID(network.getSUID());

            
           
        return true;
    }

    /**
     * set node attributes for dataset1 generic results
     *
     * @param node - node to associated attributes to
     * @param result - generic results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGenericResultDataset1Attributes(CyRow current_row, GenericResult result, String prefix){
    		
    		if(result == null)
			return;
        
        //format the node name
        String formattedName = formatLabel(result.getName());

        current_row.set( prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        current_row.set( prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        current_row.set( prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-result.getPvalue()));
        }
        else{
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 2 generic results
     *
     * @param node - node to associated attributes to
     * @param result - generic results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGenericResultDataset2Attributes(CyRow current_row, GenericResult result, String prefix){
    		
    		if( result == null)
    			return;

        //format the node name
        String formattedName = formatLabel(result.getName());

        current_row.set( prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        current_row.set( prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        current_row.set( prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-result.getPvalue()));
        }
        else{
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 1 gsea results
     *
     * @param node - node to associated attributes to
     * @param result - gsea results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGSEAResultDataset1Attributes(CyRow current_row, GSEAResult result, String prefix ){
    		
    		if(result == null)
			return;    	
        //format the node name
        String formattedName = formatLabel(result.getName());

        current_row.set( prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        current_row.set( prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        current_row.set( prefix + EnrichmentMapVisualStyle.PVALUE_DATASET1, result.getPvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, result.getFdrqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, result.getFwerqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET1, result.getGsSize());
        current_row.set( prefix + EnrichmentMapVisualStyle.ES_DATASET1, result.getES());
        current_row.set( prefix + EnrichmentMapVisualStyle.NES_DATASET1, result.getNES());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
        		double current_pvalue = result.getPvalue();
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  (1-current_pvalue));
        }
        else{
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET1,  ((-1) * (1-result.getPvalue())));
        }
    }

    /**
     * set node attributes for dataset 2 gsea results
     *
     * @param node - node to associated attributes to
     * @param result - gsea results object to get values of the attributes from
     * @param prefix - attribute prefix
     */
    private void setGSEAResultDataset2Attributes(CyRow current_row, GSEAResult result, String prefix){
    	
    		if(result == null)
			return;    	
        //format the node name
        String formattedName = formatLabel(result.getName());

        current_row.set( prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formattedName);
        current_row.set( prefix + EnrichmentMapVisualStyle.NAME, result.getName());
        current_row.set( prefix + EnrichmentMapVisualStyle.PVALUE_DATASET2, result.getPvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, result.getFdrqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, result.getFwerqvalue());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SIZE_DATASET2, result.getGsSize());
        current_row.set( prefix + EnrichmentMapVisualStyle.ES_DATASET2, result.getES());
        current_row.set( prefix + EnrichmentMapVisualStyle.NES_DATASET2, result.getNES());
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_TYPE, EnrichmentMapVisualStyle.GS_TYPE_ENRICHMENT);
        current_row.set( prefix + EnrichmentMapVisualStyle.GS_SOURCE, result.getSource());
        if(result.getNES()>=0){
        		double current_pvalue = result.getPvalue();
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  (1-current_pvalue));
        }
        else{
        		current_row.set( prefix+ EnrichmentMapVisualStyle.COLOURING_DATASET2,  ((-1) * (1-result.getPvalue())));
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

    
    //create the Nodes attribute table
    public CyTable createNodeAttributes(CyNetwork network, String name, String prefix){
    		//TODO:change back to creating our own table.  Currently can only map to a string column.
    	    //in mean time use the default node table
    		//CyTable nodeTable = tableFactory.createTable(/*name*/ prefix + "_" + node_table_suffix, CyNetwork.SUID, Long.class, true, true);
    		
    		CyTable nodeTable = network.getDefaultNodeTable();
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.GS_DESCR, String.class, false); 
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.FORMATTED_NAME, String.class, false);
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.NAME, String.class, false);
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.GS_SOURCE, String.class, false);
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.GS_TYPE, String.class, false);
    		nodeTable.createListColumn(prefix + EnrichmentMapVisualStyle.GENES, String.class, false);
    		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.PVALUE_DATASET1, Double.class, false);    		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.COLOURING_DATASET1, Double.class, false);   		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.ES_DATASET1, Double.class, false);    		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.NES_DATASET1, Double.class, false);    		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, Double.class, false);    		
    		nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.FWER_QVALUE_DATASET1, Double.class, false);		
    		nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET1, Integer.class, false);
    		
    		//only create dataset2 if this map has two datasets
    		if(map.getDatasets().size()>1){
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.PVALUE_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.COLOURING_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.ES_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.NES_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+EnrichmentMapVisualStyle.FWER_QVALUE_DATASET2, Double.class, false);
    			nodeTable.createColumn(prefix+ EnrichmentMapVisualStyle.GS_SIZE_DATASET2, Integer.class, false);
    		}
    		return nodeTable;
    }
    
    //create the edge attribue table
    public CyTable createEdgeAttributes(CyNetwork network, String name, String prefix){
    		//TODO:change back to creating our own table.  Currently can only map to a string column.
	    //in mean time use the default edge table
    		//CyTable edgeTable = tableFactory.createTable(/*name*/ prefix + "_" + edge_table_suffix, CyNetwork.SUID,Long.class, true, true);
		CyTable edgeTable = network.getDefaultEdgeTable();
		
    		edgeTable.createColumn(prefix+EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, Double.class, false);
    		edgeTable.createColumn(prefix+EnrichmentMapVisualStyle.OVERLAP_SIZE, Integer.class, false);
    		edgeTable.createListColumn(prefix+EnrichmentMapVisualStyle.OVERLAP_GENES, String.class, false);
    		edgeTable.createColumn(prefix+EnrichmentMapVisualStyle.ENRICHMENT_SET, Integer.class, false);
    		
    		return edgeTable;
    }
    
    //TODO:move this method to utilities method.
    private static Set<CyNode> getNodesWithValue(
            final CyNetwork net, final CyTable table,
             final String colname, final Object value)
    {
         final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
         final Set<CyNode> nodes = new HashSet<CyNode>();
         final String primaryKeyColname = table.getPrimaryKey().getName();
         for (final CyRow row : matchingRows)
         {
            final Long nodeId = row.get(primaryKeyColname, Long.class);
             if (nodeId == null)
                 continue;
             final CyNode node = net.getNode(nodeId);
             if (node == null)
                 continue;
             nodes.add(node);
         }
         return nodes;
     }
    
  //TODO:move this method to utilities method.
    private static CyNode getUniqueNodeWithValue(
            final CyNetwork net, final CyTable table,
             final String colname, final Object value)
    {
         final Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
         //if this id matches more than one node then don't return anything
         if(matchingRows.size() > 1 || matchingRows.size() <= 0 )
        	 	return null;
                 
         final String primaryKeyColname = table.getPrimaryKey().getName();
         for (final CyRow row : matchingRows)
         {
            final Long nodeId = row.get(primaryKeyColname, Long.class);
             if (nodeId == null)
                 continue;
             final CyNode node = net.getNode(nodeId);
             if (node == null)
                 continue;
             return node;
         }
         return null;
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

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		computeMap();		
		
	}

}
