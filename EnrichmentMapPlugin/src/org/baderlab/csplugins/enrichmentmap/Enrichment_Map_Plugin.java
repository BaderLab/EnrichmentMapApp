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
import cytoscape.logger.CyLogger;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.view.CyNetworkView;
import cytoscape.data.readers.TextFileReader;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualStyle;


import javax.swing.*;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


public class Enrichment_Map_Plugin extends CytoscapePlugin {
    static Properties build_props = new Properties();
    static Properties plugin_props = new Properties();
    static String buildId ;
    static String pluginUrl;
    static String userManualUrl;

    private static boolean overrideHeatmapRevalidation = false;
//    static JCheckBoxMenuItem overrideHeatmapRevalidationItem;

    /**
     * Constructor
     */
    public Enrichment_Map_Plugin(){

        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //Enrichment map submenu
        JMenu submenu = new JMenu("Enrichment Map");

        //Enrichment map input  panel
        item = new JMenuItem("Load Enrichment Results");
        item.addActionListener(new LoadEnrichmentsPanelAction());
        submenu.add(item);

        //Post Analysis panel
        item = new JMenuItem("Post Analysis");
        item.addActionListener(new LoadPostAnalysisPanelAction());
        submenu.add(item);

        //TODO: Ruth's automatic Enrichment Map Annotaion.
//        item = new JMenuItem("Compute Potential Annotation");
//        item.addActionListener(new ComputeAnnotationAction());
//        submenu.add(item);


        //About Box
        item = new JMenuItem("About");
        item.addActionListener(new ShowAboutPanelAction());
        submenu.add(item);

        menu.add(submenu);

        // add LinkOut for MSigDb GSEA gene sets
        Properties cyto_props = CytoscapeInit.getProperties();
        if ( ! cyto_props.containsKey("nodelinkouturl.MSigDb.GSEA Gene sets"))
            cyto_props.put("nodelinkouturl.MSigDb.GSEA Gene sets", "http://www.broad.mit.edu/gsea/msigdb/cards/%ID%.html");
        // remove old nodelinkouturl (for legacy issues)
        if (cyto_props.containsKey("nodelinkouturl.MSigDb"))
            cyto_props.remove("nodelinkouturl.MSigDb");

        // read buildId properties:
        try {
            Enrichment_Map_Plugin.build_props = getPropertiesFromClasspath("org/baderlab/csplugins/enrichmentmap/buildID.props");
        } catch (IOException e) {
            // TODO: write Warning "Could not load 'buildID.props' - using default settings"
            Enrichment_Map_Plugin.build_props.setProperty("build.number", "0");
            Enrichment_Map_Plugin.build_props.setProperty("svn.revision", "0");
            Enrichment_Map_Plugin.build_props.setProperty("build.user", "user");
            Enrichment_Map_Plugin.build_props.setProperty("build.host", "host");
            Enrichment_Map_Plugin.build_props.setProperty("build.timestemp", "1900/01/01 00:00:00 +0000 (GMT)");
        }

        Enrichment_Map_Plugin.buildId = "Build: " + Enrichment_Map_Plugin.build_props.getProperty("build.number") +
                                        " from SVN: " + Enrichment_Map_Plugin.build_props.getProperty("svn.revision") +
                                        " by: " + Enrichment_Map_Plugin.build_props.getProperty("build.user") + "@" + Enrichment_Map_Plugin.build_props.getProperty("build.host") +
                                        " at: " + Enrichment_Map_Plugin.build_props.getProperty("build.timestamp") ;

        try {
            Enrichment_Map_Plugin.plugin_props = getPropertiesFromClasspath("org/baderlab/csplugins/enrichmentmap/plugin.props");
        } catch (IOException e) {
            // TODO: write Warning "Could not load 'plugin.props' - using default settings"
        }

        pluginUrl = Enrichment_Map_Plugin.plugin_props.getProperty("pluginURL", "http://www.baderlab.org/Software/EnrichmentMap");
        userManualUrl = pluginUrl + "/UserManual";

    }

    public void onCytoscapeExit(){

    }

    /**
     * SaveSessionStateFiles collects all the data stored in the Enrichment maps
     * and creates property files for each network listing the variables needed to rebuild the map.
     * All data(Hashmaps) collections needed for the Enrichment map are stored in separate files specified by the name
     * of the network with specific file endings to indicate what type of data is stored in the files (i.e. ENR for enrichment,
     * genes for genes...).
     *
     * @param pFileList - pointer to the set of files to be added to the session
     */
    public void saveSessionStateFiles(List<File> pFileList){
        // Create an empty file on system temp directory

        String tmpDir = System.getProperty("java.io.tmpdir");
        System.out.println("java.io.tmpdir: [" + tmpDir + "]");

        //get the networks
        HashMap<String, EnrichmentMapParameters> networks = EnrichmentMapManager.getInstance().getCyNetworkList();

        //create a props file for each network
        for(Iterator<String> i = networks.keySet().iterator(); i.hasNext();){
            String networkId = i.next().toString();
            EnrichmentMapParameters params = networks.get(networkId);
            String name = Cytoscape.getNetwork(networkId).getTitle();

            //get the network name specified in the parameters
            String param_name = params.getNetworkName();

            //check to see if the name of the network matches the one specified in it parameters
            //if the two names differ then use the network name specified by the user
            if(!name.equalsIgnoreCase(param_name))
                params.setNetworkName(name);

            //property file
            File session_prop_file = new File(tmpDir, name+".props");

            //gene set file - contains filtered gene sets so it is not a replica of the initial file loaded
            //this conserves time and space.
            File gmt = new File(tmpDir, name+".gmt");
            File gmt_set2 = new File(tmpDir, name+".set2.gmt");
            //genes involved in the analysis
            File genes = new File(tmpDir, name+".genes.txt");
            File hkgenes = new File(tmpDir, name+".hashkey2genes.txt");

            //enrichment results 1 file
            File enrichmentresults1 = new File(tmpDir, name+".ENR1.txt");
            File enrichmentresults1Ofinterest = new File(tmpDir, name+".SubENR1.txt");

            //enrichment results 2 file - only initialized if they are part of the analysis.
            File enrichmentresults2;
            File enrichmentresults2Ofinterest;
            File expression1;
            File expression2;

            //geneset file for PostAnalysis Signature Genesets
            File siggmt = new File(tmpDir, name+".signature.gmt");

            File ranks1geneids;
            File ranks2geneids;

            //write out files.
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(session_prop_file));
                writer.write(params.toString());
                writer.close();

                BufferedWriter gmtwriter = new BufferedWriter(new FileWriter(gmt));
                gmtwriter.write(params.printHashmap(params.getGenesetsOfInterest()));
                gmtwriter.close();
                pFileList.add(gmt);

                //if there are two distinct expression files we also need to output the second set of genesetsof interest
                if(params.isTwoDistinctExpressionSets()){
                    BufferedWriter gmtwriter_set2 = new BufferedWriter(new FileWriter(gmt_set2));
                    gmtwriter_set2.write(params.printHashmap(params.getGenesetsOfInterest_set2()));
                    gmtwriter_set2.close();
                    pFileList.add(gmt_set2);
                }


                if (!params.getSignatureGenesets().isEmpty() ) {
                    BufferedWriter sigGmtwriter = new BufferedWriter(new FileWriter(siggmt));
                    sigGmtwriter.write(params.printHashmap(params.getSignatureGenesets()));
                    sigGmtwriter.close();
                    pFileList.add(siggmt);
                }

                BufferedWriter geneswriter = new BufferedWriter(new FileWriter(genes));
                geneswriter.write(params.printHashmap(params.getGenes()));
                geneswriter.close();
                pFileList.add(genes);

                BufferedWriter hashkey2geneswriter = new BufferedWriter(new FileWriter(hkgenes));
                hashkey2geneswriter.write(params.printHashmap(params.getHashkey2gene()));
                hashkey2geneswriter.close();
                pFileList.add(hkgenes);

                BufferedWriter enr1writer = new BufferedWriter(new FileWriter(enrichmentresults1));
                enr1writer.write(params.printHashmap(params.getEnrichmentResults1()));
                enr1writer.close();
                pFileList.add(enrichmentresults1);

                BufferedWriter subenr1writer = new BufferedWriter(new FileWriter(enrichmentresults1Ofinterest));
                subenr1writer.write(params.printHashmap(params.getEnrichmentResults1OfInterest()));
                subenr1writer.close();
                pFileList.add(enrichmentresults1Ofinterest);

                //output the ranks specific to GSEA for leading edge analysis rank to gene conversion file
                if(params.getRank2geneDataset1() != null){
                    ranks1geneids = new File(tmpDir, name+".RANKS1Genes.txt");
                    BufferedWriter r2g12writer = new BufferedWriter(new FileWriter(ranks1geneids));
                    r2g12writer.write(params.printHashmap(params.getRank2geneDataset1()));
                    r2g12writer.close();
                    pFileList.add(ranks1geneids);
                }

                //save all the rank files
                if(!params.getRanks().isEmpty()){
                    HashMap<String, HashMap<Integer, Ranking>> all_ranks = params.getRanks();

                    for(Iterator j = all_ranks.keySet().iterator(); j.hasNext(); ){
                        String ranks_name = j.next().toString();
                        // as ranks names that contain dots make problems, when restoring a session,
                        // we'll replace them by underscores:
                        if (ranks_name.contains("."))
                            ranks_name.replace('.', '_');
                        File current_ranks = new File(tmpDir, name+"."+ranks_name+".RANKS.txt");
                        BufferedWriter subrank1writer = new BufferedWriter(new FileWriter(current_ranks));
                        subrank1writer.write(params.printHashmap(all_ranks.get(ranks_name)));
                        subrank1writer.close();
                        pFileList.add(current_ranks);
                    }
                }

                if(params.isTwoDatasets()){
                    enrichmentresults2 = new File(tmpDir, name+".ENR2.txt");
                    BufferedWriter enr2writer = new BufferedWriter(new FileWriter(enrichmentresults2));
                    enr2writer.write(params.printHashmap(params.getEnrichmentResults2()));
                    enr2writer.close();
                    pFileList.add(enrichmentresults2);

                    enrichmentresults2Ofinterest = new File(tmpDir, name+".SubENR2.txt");
                    BufferedWriter subenr2writer = new BufferedWriter(new FileWriter(enrichmentresults2Ofinterest));
                    subenr2writer.write(params.printHashmap(params.getEnrichmentResults1OfInterest()));
                    subenr2writer.close();
                    pFileList.add(enrichmentresults2Ofinterest);

                    //output the ranks specific to GSEA for leading edge analysis rank to gene conversion file
                    if(params.getRank2geneDataset2() != null){

                        ranks2geneids = new File(tmpDir, name+".RANKS2Genes.txt");
                        BufferedWriter r2g22writer = new BufferedWriter(new FileWriter(ranks2geneids));
                        r2g22writer.write(params.printHashmap(params.getRank2geneDataset2()));
                        r2g22writer.close();
                        pFileList.add(ranks2geneids);
                    }
                }

                if(params.isData()){
                    expression1 = new File(tmpDir, name+".expression1.txt");
                    BufferedWriter expression1writer = new BufferedWriter(new FileWriter(expression1));
                    expression1writer.write(params.getExpression().toString());
                    expression1writer.close();
                    pFileList.add(expression1);
                }
                if(params.isData2()){
                    expression2 = new File(tmpDir, name+".expression2.txt");
                    BufferedWriter expression2writer = new BufferedWriter(new FileWriter(expression2));
                    expression2writer.write(params.getExpression2().toString());
                    expression2writer.close();
                    pFileList.add(expression2);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            pFileList.add(session_prop_file);
        }
    }

    /**
     * Restore Enrichment maps
     *
     * @param pStateFileList - list of files associated with thie session
     */
    public void restoreSessionState(List<File> pStateFileList) {

        if ((pStateFileList == null) || (pStateFileList.size() == 0)) {
            //No previous state to restore
            return;
        }

        try {
            //go through the prop files first to create the correct objects to be able
            //to add other files to.
            for(int i = 0; i < pStateFileList.size(); i++){

                File prop_file = pStateFileList.get(i);

                if(prop_file.getName().contains(".props")){

                    TextFileReader reader = new TextFileReader(prop_file.getAbsolutePath());
                    reader.read();
                    String fullText = reader.getText();

                    //Given the file with all the parameters create a new parameter
                    EnrichmentMapParameters params = new EnrichmentMapParameters(fullText);

                    //get the network name
                    String param_name = params.getNetworkName();

                    //get the network name from the file name
                    String[] fullname = prop_file.getName().split("Enrichment_Map_Plugin_");
                    String  props_name = (fullname[1].split("\\."))[0];
                    String name = param_name;

                    //check to see if the network name matches the name of the file
                    //the network name specified in the props file is different from the name of the props
                    //file then assume the name in the props file is wrong and set it to the file name (legacy issue)
                    //related to bug ticket #49
                    if(!props_name.equalsIgnoreCase(param_name)){
                        name = props_name;
                        params.setNetworkName(name);
                    }

                    //register network and parameters
                    EnrichmentMapManager.getInstance().registerNetwork(Cytoscape.getNetwork(name),params);
                }
            }
            //go through the rest of the files
            for(int i = 0; i < pStateFileList.size(); i++){

                File prop_file = pStateFileList.get(i);

                //get the network name and network and parameters
                //for this file
                String[] fullname = prop_file.getName().split("Enrichment_Map_Plugin_");
                String name;
                if(fullname.length > 1)
                    name = (fullname[1].split("\\."))[0];
                else
                    continue;

                EnrichmentMapParameters params = EnrichmentMapManager.getInstance().getParameters(name);

                if(params == null)
                    System.out.println("network for file" + prop_file.getName() + " does not exist.");
                else if((!prop_file.getName().contains(".props"))
                        && (!prop_file.getName().contains(".expression1.txt"))
                        && (!prop_file.getName().contains(".expression2.txt"))){
                    //read the file
                    TextFileReader reader = new TextFileReader(prop_file.getAbsolutePath());
                    reader.read();
                    String fullText = reader.getText();

                    if(prop_file.getName().contains(".gmt")){
                        if (prop_file.getName().contains(".signature.gmt"))
                            params.setSignatureGenesets(params.repopulateHashmap(fullText, 1));
                        else if(prop_file.getName().contains(".set2.gmt"))
                            params.setGenesetsOfInterest_set2(params.repopulateHashmap(fullText,1));
                        else
                            params.setGenesetsOfInterest(params.repopulateHashmap(fullText,1));
                    }
                    if(prop_file.getName().contains(".genes.txt")){
                        HashMap<String, Integer> genes = params.repopulateHashmap(fullText,2);
                        params.setGenes(genes);
                        // Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
                        params.setNumberOfGenes( Math.max( params.getNumberOfGenes(), Collections.max(genes.values())+1 ));
                    }
                    if(prop_file.getName().contains(".hashkey2genes.txt")){
                        HashMap<Integer,String> hashkey2gene = params.repopulateHashmap(fullText,5);
                        params.setHashkey2gene(hashkey2gene);
                        // Ticket #107 : restore also gene count (needed to determine the next free hash in case we do PostAnalysis with a restored session)
                        params.setNumberOfGenes( Math.max( params.getNumberOfGenes(), Collections.max(hashkey2gene.keySet())+1 ));
                    }



                    if(prop_file.getName().contains(".ENR1.txt")){
                        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                            params.setEnrichmentResults1(params.repopulateHashmap(fullText,3));
                        else
                            params.setEnrichmentResults1(params.repopulateHashmap(fullText,4));

                    }
                    if(prop_file.getName().contains(".SubENR1.txt")){
                        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                            params.setEnrichmentResults1OfInterest(params.repopulateHashmap(fullText,3));
                        else
                            params.setEnrichmentResults1OfInterest(params.repopulateHashmap(fullText,4));
                    }
                    //have to keep this method just in case old session files have ranks saved in this way
                    //it would only happen for sessions saved with version 0.8
                    if(prop_file.getName().contains(".RANKS1.txt")){
                        params.setDataset1Rankings(params.repopulateHashmap(fullText,6));
                    }
                    //create file to deal with GSEA ranks for leading edge analysis.
                    if(prop_file.getName().contains(".RANKS1Genes.txt")){
                        params.setRank2geneDataset1(params.repopulateHashmap(fullText,7));
                    }
                    if(prop_file.getName().contains(".RANKS.txt")){
                        //we need to get the name of this set of rankings
                        // network_name.ranking_name.ranks.txt --> split by "." and get 2
                        String[] file_name_tokens = (prop_file.getName()).split("\\.");
                        String ranks_name;
                        if(file_name_tokens.length == 4)
                            ranks_name = file_name_tokens[1];
                        else
                            //file name is not structured properly --> default to file name
                            ranks_name = prop_file.getName();
                        params.addRanks(ranks_name,params.repopulateHashmap(fullText,6));
                    }


                    if(params.isTwoDatasets()){
                        if(prop_file.getName().contains(".ENR2.txt")){
                            if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                                params.setEnrichmentResults2(params.repopulateHashmap(fullText,3));
                            else
                                params.setEnrichmentResults2(params.repopulateHashmap(fullText,4));
                        }
                        if(prop_file.getName().contains(".SubENR2.txt")){
                            if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                                params.setEnrichmentResults2OfInterest(params.repopulateHashmap(fullText,3));
                            else
                                params.setEnrichmentResults2OfInterest(params.repopulateHashmap(fullText,4));
                        }
                        //create file to deal with GSEA ranks for leading edge analysis.
                        if(prop_file.getName().contains(".RANKS2Genes.txt")){
                            params.setRank2geneDataset2(params.repopulateHashmap(fullText,7));
                        }
                        //have to keep this method just in case old session files have ranks saved in this way
                        //it would only happen for sessions saved with version 0.8
                        if(prop_file.getName().contains(".RANKS2.txt")){
                            params.setDataset2Rankings(params.repopulateHashmap(fullText,6));
                        }
                    }

                }

            }

            //load the expression files.  Load them last because they require
            //info from the parameters
            for(int i = 0; i < pStateFileList.size(); i++){

                File prop_file = pStateFileList.get(i);

                if(prop_file.getName().contains("expression1.txt")){
                    String[] fullname = prop_file.getName().split("Enrichment_Map_Plugin_");
                    String  name = (fullname[1].split("\\."))[0];

                    EnrichmentMapParameters params = EnrichmentMapManager.getInstance().getParameters(name);

                    //Load the GCT file
                    params.setExpressionFileName1(prop_file.getAbsolutePath());
                    ExpressionFileReaderTask expressionFile1 = new ExpressionFileReaderTask(params,1);
                    expressionFile1.run();
                    params.getExpression().rowNormalizeMatrix();
                }
                if(prop_file.getName().contains("expression2.txt")){
                    //get the network name and network and parameters
                    //for this file
                    String[] fullname = prop_file.getName().split("Enrichment_Map_Plugin_");
                    String  name = (fullname[1].split("\\."))[0];

                    EnrichmentMapParameters params = EnrichmentMapManager.getInstance().getParameters(name);

                    params.setExpressionFileName2(prop_file.getAbsolutePath());
                    ExpressionFileReaderTask expressionFile2 = new ExpressionFileReaderTask(params,2);
                    expressionFile2.run();
                    params.getExpression2().rowNormalizeMatrix();

                    //if there are two expression sets and there is a second set of genesets of interest then we
                    //are dealing with two distinct expression files.
                    if(!params.getGenesetsOfInterest_set2().isEmpty()){
                        params.setTwoDistinctExpressionSets(true);
                        params.setDatasetGenes(new HashSet<Integer>((Set<Integer>)params.getExpression().getGeneIds()));
                        params.setDatasetGenes_set2(new HashSet<Integer>((Set<Integer>)params.getExpression2().getGeneIds()));
                    }

                }

            }

            //register the action listeners for all the networks.
            EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
            HashMap networks = manager.getCyNetworkList();

            //iterate over the networks
            for(Iterator j = networks.keySet().iterator();j.hasNext();){
                String currentNetwork = (String)j.next();
                CyNetworkView view = Cytoscape.getNetworkView(currentNetwork);
                EnrichmentMapParameters params = (EnrichmentMapParameters)networks.get(currentNetwork);

                //for each map compute the similarity matrix, (easier than storing it)
                //compute the geneset similarities
                ComputeSimilarityTask similarities = new ComputeSimilarityTask(params, ComputeSimilarityTask.ENRICHMENT);
                similarities.run();
                HashMap<String, GenesetSimilarity> similarity_results = similarities.getGeneset_similarities();
                params.setGenesetSimilarity(similarity_results);

                // also compute geneset similarities between Enrichment- and Signature Genesets (if any)
                if (! params.getSignatureGenesets().isEmpty()){
                    ComputeSimilarityTask sigSimilarities = new ComputeSimilarityTask(params, ComputeSimilarityTask.SIGNATURE);
                    sigSimilarities.run();
                    HashMap<String, GenesetSimilarity> sig_similarity_results = sigSimilarities.getGeneset_similarities();

                    params.getGenesetSimilarity().putAll(sig_similarity_results);
                }

                //add the click on edge listener
                view.addGraphViewChangeListener(new EnrichmentMapActionListener(params));

                //set the last network to be the one viewed
                //and initialize the parameters panel
                if(!j.hasNext()){
                    Cytoscape.setCurrentNetwork(currentNetwork);
                    ParametersPanel paramPanel = manager.getParameterPanel();
                    paramPanel.updatePanel(params);
                    paramPanel.revalidate();
                }

                //make sure the visual style is set to the right on for this network
                String vs_name = params.getAttributePrefix() + "_Enrichment_map_style";

                // get the VisualMappingManager and CalculatorCatalog
            VisualMappingManager manager_vs = Cytoscape.getVisualMappingManager();
            CalculatorCatalog catalog = manager_vs.getCalculatorCatalog();
            VisualStyle vs = catalog.getVisualStyle(vs_name);

            view.setVisualStyle(vs.getName()); // not strictly necessary

            }

        } catch (Exception ee) {
            ee.printStackTrace();
        }

        // remove old nodelinkouturl (for legacy issues)
        Properties cyto_props = CytoscapeInit.getProperties();
        if (cyto_props.containsKey("nodelinkouturl.MSigDb"))
            cyto_props.remove("nodelinkouturl.MSigDb");

    }

    private Properties getPropertiesFromClasspath(String propFileName) throws IOException {
        // loading properties file from the classpath
        Properties props = new Properties();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + propFileName
                    + "' not found in the classpath");
        }

        props.load(inputStream);
        return props;
    }

    /**
     * @param overrideHeatmapRevalidation the overrideHeatmapRevalidation to set
     */
    public static void setOverrideHeatmapRevalidation(
            boolean overrideHeatmapRevalidation) {
        Enrichment_Map_Plugin.overrideHeatmapRevalidation = overrideHeatmapRevalidation;
    }

    /**
     * @return the overrideHeatmapRevalidation
     */
    public static boolean isOverrideHeatmapRevalidation() {
        return overrideHeatmapRevalidation;
    }


}

