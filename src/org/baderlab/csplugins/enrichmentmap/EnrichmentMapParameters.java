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
import giny.model.Node;
import giny.model.Edge;

import java.util.*;
import java.io.File;

import cytoscape.CytoscapeInit;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 12:04:28 PM
 */
public class EnrichmentMapParameters {

    private String NetworkName;
    private String attributePrefix;

    //GMT and GSEA output files
    private String GMTFileName;

    //flag to indicate if the user has supplied a data file
    private boolean Data = false;
    private String GCTFileName1;

    //flag to indicate if the user has supplied a data file
    private boolean Data2 = false;
    private String GCTFileName2;

    private String enrichmentDataset1FileName1;
    private String enrichmentDataset1FileName2;

    private String enrichmentDataset2FileName1;
    private String enrichmentDataset2FileName2;

    private boolean twoDatasets = false;

    // DEFAULT VALUES for pvalue, qvalue, jaccardCutOff and jaccard
    // will be assigned in the constructor
    //p-value cutoff
    private double pvalue;
    //pvalue slider bar
    private SliderBarPanel pvalueSlider;

     //flag to indicate if there are FDR Q-values
    private boolean FDR = false;
    //fdr q-value cutoff
    private double qvalue;
    //qvalue slider bar
    private SliderBarPanel qvalueSlider;

    private double jaccardCutOff;
    private boolean jaccard;
    private boolean jaccardCutOffChanged = false;

    //flag to indicate if the results are from GSEA or generic
    private boolean GSEA = true;

    //Hashmap stores the unique set of genes used in the gmt file
    private HashMap<String,Integer> genes;
    private HashSet datasetGenes;
    private int NumberOfGenes = 0;

    //Hashmap of the GSEA Results, It is is a hash of the GSEAResults objects
    private HashMap enrichmentResults1;
    private HashMap enrichmentResults2;
    private HashMap<String, GeneSet> genesets;
    private HashMap<String, GeneSet> filteredGenesets;

    //The GSEA results that pass the thresholds.
    //If there are two datasets these list can be different.
    private HashMap enrichmentResults1OfInterest;
    private HashMap enrichmentResults2OfInterest;

    private HashMap<String, GeneSet> genesetsOfInterest;

    private GeneExpressionMatrix expression;
    private GeneExpressionMatrix expression2;

    private String dataset1Phenotype1 = "UP";
    private String dataset1Phenotype2 = "DOWN";
    private String dataset2Phenotype1 = "UP";
    private String dataset2Phenotype2 = "DOWN";

    private String classFile1;
    private String classFile2;

    private HashMap<String, GenesetSimilarity> genesetSimilarity;

    private ArrayList<Node> selectedNodes;
    private ArrayList<Edge> selectedEdges;

    private HeatMapParameters hmParams;

    //Rank files
    private String dataset1RankedFile = null;
    private String dataset2RankedFile = null;

    private HashMap<Integer, Ranking> dataset1Rankings;
    private HashMap<Integer, Ranking> dataset2Rankings;

    private Properties cyto_prop;
    private double defaultPvalueCutOff;
    private double defaultQvalueCutOff;
    private double defaultJaccardCutOff;
    private double defaultOverlapCutOff;
    private String default_overlap_metric;
    private Boolean disable_heatmap_autofocus;
    private Boolean disable_genesetSummary_autofocus;

    public EnrichmentMapParameters() {
        this.enrichmentResults1 = new HashMap();
        this.enrichmentResults2 = new HashMap();
        this.genes = new HashMap();
        this.datasetGenes = new HashSet();
        this.genesets = new HashMap();
        this.filteredGenesets = new HashMap();
        this.enrichmentResults1OfInterest = new HashMap();
        this.enrichmentResults2OfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();
        this.selectedNodes = new ArrayList<Node>();
        this.selectedEdges = new ArrayList<Edge>();

        this.jaccardCutOffChanged = false;
        //default Values from Cytoscape properties
        this.cyto_prop = CytoscapeInit.getProperties() ;
        this.defaultPvalueCutOff  = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_pvalue",  "0.05") );
        this.defaultQvalueCutOff  = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_qvalue",  "0.25") );
        this.defaultJaccardCutOff = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_jaccard", "0.25") );
        this.defaultOverlapCutOff = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_overlap", "0.50") );
        this.default_overlap_metric = this.cyto_prop.getProperty("EnrichmentMap.default_overlap_metric", "jaccard");
        this.disable_heatmap_autofocus = Boolean.parseBoolean( this.cyto_prop.getProperty("EnrichmentMap.disable_heatmap_autofocus", "false") );
        this.disable_genesetSummary_autofocus = Boolean.parseBoolean( this.cyto_prop.getProperty("EnrichmentMap.disable_genesetSummary_autofocus", "false") );

        //assign the defaults:
        this.pvalue = this.defaultPvalueCutOff;
        this.qvalue = this.defaultQvalueCutOff;

        //choose Jaccard or Overlap as default
        if ( getOverlapMetricDefault().equalsIgnoreCase("overlap") ){
            this.jaccardCutOff = this.defaultOverlapCutOff;
            this.jaccard = false;
        } else {
            this.jaccardCutOff = this.defaultJaccardCutOff;
            this.jaccard = true;
        }
    }

    public EnrichmentMapParameters(String propFile){
        this();

        //Create a hashmap to contain all the values in the rpt file.
        HashMap props = new HashMap();

        String [] lines = propFile.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");
            //there should be two values on each line of the rpt file.
            if(tokens.length == 2 )
                props.put(tokens[0] ,tokens[1]);
        }

        this.NetworkName = (String)props.get("NetworkName");
        this.attributePrefix = (String)props.get("attributePrefix");

        this.GMTFileName = (String)props.get("GMTFileName");
        this.GCTFileName1 = (String)props.get("GCTFileName1");

        this.enrichmentDataset1FileName1 = (String)props.get("enerichmentDataset1FileName1");
        this.enrichmentDataset1FileName2 = (String)props.get("enrichmentDataset1FileName2");

        this.dataset1Phenotype1 = (String)props.get("dataset1Phenotype1");
        this.dataset1Phenotype2 = (String)props.get("dataset1Phenotype2");
        this.dataset2Phenotype1 = (String)props.get("dataset2Phenotype1");
        this.dataset2Phenotype2 = (String)props.get("dataset2Phenotype2");

        if(((String)props.get("classFile1")).equalsIgnoreCase("null") )
            this.classFile1 = null;
        else
            this.classFile1 = (String)props.get("classFile1");
        if(((String)props.get("classFile2")).equalsIgnoreCase("null"))
            this.classFile2 = null;
        else
            this.classFile2 = (String)props.get("classFile2");

        //boolean flags
        if(((String)props.get("twoDatasets")).equalsIgnoreCase("true"))
            this.twoDatasets = true;
        if(((String)props.get("jaccard")).equalsIgnoreCase("false"))
            this.jaccard = false;
         if(((String)props.get("GSEA")).equalsIgnoreCase("false"))
            this.GSEA = false;
        if(((String)props.get("Data")).equalsIgnoreCase("true"))
            this.Data = true;
        if(((String)props.get("Data2")).equalsIgnoreCase("true"))
            this.Data2 = true;
        if(((String)props.get("FDR")).equalsIgnoreCase("true"))
            this.FDR = true;

        if(twoDatasets){
            if(Data2)
                this.GCTFileName2 = (String)props.get("GCTFileName2");
            this.enrichmentDataset2FileName1 = (String)props.get("enerichmentDataset2FileName1");
            this.enrichmentDataset2FileName2 = (String)props.get("enrichmentDataset2FileName2");
        }
        //cutoffs
        setPvalue(Double.parseDouble((String)props.get("pvalue")));
        setQvalue(Double.parseDouble((String)props.get("qvalue")));
        this. jaccardCutOff = Double.parseDouble((String)props.get("jaccardCutOff"));
        this.jaccardCutOffChanged = true;

        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth);

        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth);

    }

    //Constructor for Enrichment Map Parameters that take another instance of enrichment map parameters
    //And copies its contents.
    //The assumptionis that these parameters were populated by the input window and therefore only  contain
    //info for file names, cutoffs, and phenotypes.
    public EnrichmentMapParameters(EnrichmentMapParameters copy){
        this();

        this.GMTFileName = copy.getGMTFileName();
        this.GCTFileName1 = copy.getGCTFileName1();
        this.GCTFileName2 = copy.getGCTFileName2();

        this.dataset1RankedFile = copy.getDataset1RankedFile();
        this.dataset2RankedFile = copy.getDataset2RankedFile();

        this.enrichmentDataset1FileName1 = copy.getEnrichmentDataset1FileName1();
        this.enrichmentDataset1FileName2 = copy.getEnrichmentDataset1FileName2();
        this.enrichmentDataset2FileName1 = copy.getEnrichmentDataset2FileName1();
        this.enrichmentDataset2FileName2 = copy.getEnrichmentDataset2FileName2();

        this.dataset1Phenotype1 = copy.getDataset1Phenotype1();
        this.dataset1Phenotype2 = copy.getDataset1Phenotype2();
        this.dataset2Phenotype1 = copy.getDataset2Phenotype1();
        this.dataset2Phenotype2 = copy.getDataset2Phenotype2();

        this.pvalue = copy.getPvalue();
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth);

        this.qvalue = copy.getQvalue();
        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth);

        this.jaccardCutOff = copy.getJaccardCutOff();

        this.Data = copy.isData();
        this.Data2 = copy.isData2();
        this.twoDatasets = copy.isTwoDatasets();
        this.GSEA = copy.isGSEA();
        this.jaccard = copy.isJaccard();
        this.jaccardCutOffChanged = copy.jaccardCutOffChanged;
    }


    //Check to see if the current set of enrichment map parameters has the minimal amount
    //of information to run enrichment maps.
    //If it is a GSEA run then gmt,gct,2 enrichment files are needed
    //If it is a generic run then gmt and 1 enrichment file is needed
    //if there are two datasets then depending on type it requires the same as above.
    //returns a string specifying the files that are missing and and empty string if
    //everything is ok
    public String checkMinimalRequirements(){
        String errors = "";

        //minimal for either analysis
        if(this.GMTFileName.equalsIgnoreCase("") || !checkFile(this.GMTFileName))
            errors = errors + "GMT file can not be found \n";
        if(this.enrichmentDataset1FileName1.equalsIgnoreCase("") || !checkFile(this.enrichmentDataset1FileName1))
            errors = errors + "Dataset 1, enrichment file 1 can not be found\n";
        if(this.twoDatasets){
            if(this.enrichmentDataset2FileName1.equalsIgnoreCase("") || !checkFile(this.enrichmentDataset2FileName1))
                errors = errors + "Dataset 2, enrichment file 1 can not be found\n";
        }
        //GSEA inputs
        if(this.GSEA){
            if(this.enrichmentDataset1FileName2.equalsIgnoreCase("") || !checkFile(this.enrichmentDataset1FileName2))
                errors = errors + "Dataset 1, enrichment file 2 can not be found\n";
            if(this.twoDatasets){
                if(this.enrichmentDataset2FileName2.equalsIgnoreCase("") || !checkFile(this.enrichmentDataset2FileName2))
                    errors = errors + "Dataset 2, enrichment file 2 can not be found\n";
            }
        }

        //check to see if there are two datasets if the two gct files are the same
        if((this.twoDatasets) && (this.GCTFileName1.equalsIgnoreCase(this.GCTFileName2))){
            this.Data2 = false;
            this.GCTFileName2 = "";
        }


        return errors;
    }

    private boolean checkFile(String filename){
           //check to see if the files exist and are readable.
           //if the file is unreadable change the color of the font to red
           //otherwise the font should be black.
           if(filename != null){
               File tempfile = new File(filename);
               if(!tempfile.canRead())
                   return false;
           }
           return true;
       }

    public EnrichmentMapParameters(String GMTFileName,  double pvalue, double qvalue) {
        this();
        this.GMTFileName = GMTFileName;
        this.pvalue = pvalue;
        this.qvalue = qvalue;

    }

    public boolean isJaccard() {
        return jaccard;
    }

    public void setJaccard(boolean jaccard) {
        this.jaccard = jaccard;
    }

    public HashMap getEnrichmentResults1() {
        return enrichmentResults1;
    }

    public void setEnrichmentResults1(HashMap enrichmentResults1) {
        this.enrichmentResults1 = enrichmentResults1;
    }

    public HashMap getEnrichmentResults2() {
        return enrichmentResults2;
    }

    public void setEnrichmentResults2(HashMap enrichmentResults2) {
        this.enrichmentResults2 = enrichmentResults2;
    }

    public HashMap getEnrichmentResults1OfInterest() {
        return enrichmentResults1OfInterest;
    }

    public void setEnrichmentResults1OfInterest(HashMap enrichmentResults1OfInterest) {
        this.enrichmentResults1OfInterest = enrichmentResults1OfInterest;
    }

    public HashMap getEnrichmentResults2OfInterest() {
        return enrichmentResults2OfInterest;
    }

    public void setEnrichmentResults2OfInterest(HashMap enrichmentResults2OfInterest) {
        this.enrichmentResults2OfInterest = enrichmentResults2OfInterest;
    }

    public HashMap getGenesetsOfInterest() {

        return genesetsOfInterest;
    }

    public void setGenesetsOfInterest(HashMap genesetsOfInterest) {
        this.genesetsOfInterest = genesetsOfInterest;



    }

    public HashMap getGenesets() {
        return genesets;
    }

    public void setGenesets(HashMap genesets) {
        this.genesets = genesets;
    }

    public HashMap getFilteredGenesets() {
        return filteredGenesets;
    }

    public void setFilteredGenesets(HashMap filteredGenesets) {
        this.filteredGenesets = filteredGenesets;
    }

    public String getGMTFileName() {

        return GMTFileName;
    }

    public void setGMTFileName(String GMTFileName) {
        this.GMTFileName = GMTFileName;
    }

    public String getGCTFileName1() {
        return GCTFileName1;
    }

    public void setGCTFileName1(String GCTFileName) {
        this.GCTFileName1 = GCTFileName;
    }


    public String getGCTFileName2() {
        return GCTFileName2;
    }

    public void setGCTFileName2(String GCTFileName) {
        this.GCTFileName2 = GCTFileName;
    }

    public String getEnrichmentDataset1FileName1() {
        return enrichmentDataset1FileName1;
    }

    public void setEnrichmentDataset1FileName1(String enrichmentDataset1FileName1) {
        this.enrichmentDataset1FileName1 = enrichmentDataset1FileName1;
    }

    public String getEnrichmentDataset1FileName2() {
        return enrichmentDataset1FileName2;
    }

    public void setEnrichmentDataset1FileName2(String enrichmentDataset1FileName2) {
        this.enrichmentDataset1FileName2 = enrichmentDataset1FileName2;
    }

    public String getEnrichmentDataset2FileName1() {
        return enrichmentDataset2FileName1;
    }

    public void setEnrichmentDataset2FileName1(String enrichmentDataset2FileName1) {
        this.enrichmentDataset2FileName1 = enrichmentDataset2FileName1;
    }

    public String getEnrichmentDataset2FileName2() {
        return enrichmentDataset2FileName2;
    }

    public void setEnrichmentDataset2FileName2(String enrichmentDataset2FileName2) {
        this.enrichmentDataset2FileName2 = enrichmentDataset2FileName2;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;

    }

    public double getQvalue() {
        return qvalue;
    }

    public void setQvalue(double qvalue) {
        this.qvalue = qvalue;

    }

    public HashMap getGenes() {
        return genes;
    }

    public void setGenes(HashMap genes) {
        this.genes = genes;
    }

    public HashSet getDatasetGenes() {
        return datasetGenes;
    }

    public void setDatasetGenes(HashSet datasetGenes) {
        this.datasetGenes = datasetGenes;
    }

    public int getNumberOfGenes() {
        return NumberOfGenes;
    }

    public void setNumberOfGenes(int numberOfGenes) {
        NumberOfGenes = numberOfGenes;
    }

    public double getJaccardCutOff() {
        return jaccardCutOff;
    }

    public void setJaccardCutOff(double jaccardCutOff) {
        this.jaccardCutOff = jaccardCutOff;
    }

    public String getNetworkName() {
        return NetworkName;
    }

    public void setNetworkName(String networkName) {
        NetworkName = networkName;
    }

    public String getAttributePrefix() {
        return attributePrefix;
    }

    public void setAttributePrefix(String attributePrefix) {
        this.attributePrefix = attributePrefix;
    }

    public boolean isTwoDatasets() {
        return twoDatasets;
    }

    public void setTwoDatasets(boolean twoDatasets) {
        this.twoDatasets = twoDatasets;
    }

    public void noFilter(){
        this.filteredGenesets = genesets;
    }

    public void filterGenesets(){
        //iterate through each geneset and filter each one
         for(Iterator j = genesets.keySet().iterator(); j.hasNext(); ){

             String geneset2_name = j.next().toString();
             GeneSet current_set = (GeneSet) genesets.get(geneset2_name);

             //compare the HashSet of dataset genes to the HashSet of the current Geneset
             //only keep the genes from the geneset that are in the dataset genes
             HashSet<Integer> geneset_genes = current_set.getGenes();

             //Get the intersection between current geneset and dataset genes
             Set<Integer> intersection = new HashSet<Integer>(geneset_genes);
             intersection.retainAll(datasetGenes);

             //Add new geneset to the filtered set of genesets
             HashSet<Integer> new_geneset = new HashSet<Integer>(intersection);
             GeneSet new_set = new GeneSet(geneset2_name,current_set.getDescription());
             new_set.setGenes(new_geneset);

             this.filteredGenesets.put(geneset2_name,new_set);

         }
        //once we have filtered the genesets clear the original genesets object
        genesets.clear();
    }

    //Check to see that there are genes in the filtered  genesets
    //If the ids do not match up, after a filtering there will be no genes in any of the
    //genesets
    //Return true if Genesets have genes, return false if all the genesets are empty
    public boolean checkGenesets(){

        for(Iterator j = filteredGenesets.keySet().iterator(); j.hasNext(); ){
             String geneset2_name = j.next().toString();
             GeneSet current_set = (GeneSet) filteredGenesets.get(geneset2_name);

             //get the genes in the geneset
             HashSet<Integer> geneset_genes = current_set.getGenes();

            //if there is at least one gene in any of the genesets then the ids match.
            if(!geneset_genes.isEmpty())
                return true;

        }

        return false;

    }

    public void dispose(){
        genesets.clear();
        genesetsOfInterest.clear();
        enrichmentResults1.clear();
        enrichmentResults2.clear();
        genes.clear();
        datasetGenes.clear();
        filteredGenesets.clear();
        enrichmentResults1OfInterest.clear();
        enrichmentResults2OfInterest.clear();

    }

    public boolean isGSEA() {
        return GSEA;
    }

    public void setGSEA(boolean GSEA) {
        this.GSEA = GSEA;
    }

    public boolean isData() {
        return Data;
    }

    public void setData(boolean data) {
        Data = data;
    }

    public boolean isData2() {
        return Data2;
    }

    public void setData2(boolean data2) {
        Data2 = data2;
    }

    public boolean isFDR() {
        return FDR;
    }

    public void setFDR(boolean FDR) {
        this.FDR = FDR;
    }

    public GeneExpressionMatrix getExpression() {
        return expression;
    }

    public void setExpression(GeneExpressionMatrix expression) {
        this.expression = expression;
    }

    public GeneExpressionMatrix getExpression2() {
        return expression2;
    }

    public void setExpression2(GeneExpressionMatrix expression2) {
        this.expression2 = expression2;
    }

    public HashMap<String, GenesetSimilarity> getGenesetSimilarity() {
        return genesetSimilarity;
    }

    public void setGenesetSimilarity(HashMap<String, GenesetSimilarity> genesetSimilarity) {
        this.genesetSimilarity = genesetSimilarity;
    }

    public String getClassFile1() {
        return classFile1;
    }

    public void setClassFile1(String classFile1) {
        this.classFile1 = classFile1;
    }

    public String getClassFile2() {
        return classFile2;
    }

    public void setClassFile2(String classFile2) {
        this.classFile2 = classFile2;
    }

    public String getDataset1Phenotype1() {
        return dataset1Phenotype1;
    }

    public void setDataset1Phenotype1(String dataset1Phenotype1) {
        this.dataset1Phenotype1 = dataset1Phenotype1;
    }

    public String getDataset1Phenotype2() {
        return dataset1Phenotype2;
    }

    public void setDataset1Phenotype2(String dataset1Phenotype2) {
        this.dataset1Phenotype2 = dataset1Phenotype2;
    }

    public String getDataset2Phenotype1() {
        return dataset2Phenotype1;
    }

    public void setDataset2Phenotype1(String dataset2Phenotype1) {
        this.dataset2Phenotype1 = dataset2Phenotype1;
    }

    public String getDataset2Phenotype2() {
        return dataset2Phenotype2;
    }

    public void setDataset2Phenotype2(String dataset2Phenotype2) {
        this.dataset2Phenotype2 = dataset2Phenotype2;
    }

    public SliderBarPanel getPvalueSlider() {
        return pvalueSlider;
    }

    public SliderBarPanel getQvalueSlider() {
        return qvalueSlider;
    }

    public ArrayList<Node> getSelectedNodes() {
        return selectedNodes;
    }

    public ArrayList<Edge> getSelectedEdges() {
        return selectedEdges;
    }

    public void setSelectedNodes(ArrayList<Node> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void setSelectedEdges(ArrayList<Edge> selectedEdges) {
        this.selectedEdges = selectedEdges;
    }

    public HeatMapParameters getHmParams() {
        return hmParams;
    }

    public void setHmParams(HeatMapParameters hmParams) {
        this.hmParams = hmParams;
    }

    public String getDataset1RankedFile() {
        return dataset1RankedFile;
    }

    public void setDataset1RankedFile(String dataset1RankedFile) {
        this.dataset1RankedFile = dataset1RankedFile;
    }

    public String getDataset2RankedFile() {
        return dataset2RankedFile;
    }

    public void setDataset2RankedFile(String dataset2RankedFile) {
        this.dataset2RankedFile = dataset2RankedFile;
    }

    public HashMap getDataset1Rankings() {
        return dataset1Rankings;
    }

    public void setDataset1Rankings(HashMap<Integer,Ranking> dataset1Rankings) {
        this.dataset1Rankings = dataset1Rankings;
    }

    public HashMap getDataset2Rankings() {
        return dataset2Rankings;
    }

    public void setDataset2Rankings(HashMap<Integer,Ranking> dataset2Rankings) {
        this.dataset2Rankings = dataset2Rankings;
    }


    public String toString(){
        String paramVariables = "";

        paramVariables += "NetworkName\t" + NetworkName + "\n";
        paramVariables += "attributePrefix\t" + attributePrefix + "\n";

        //file names
        paramVariables += "GMTFileName\t" + GMTFileName + "\n";
        paramVariables += "GCTFileName1\t" + GCTFileName1 + "\n";
        paramVariables += "GCTFileName2\t" + GCTFileName2 + "\n";
        paramVariables += "enerichmentDataset1FileName1\t" + enrichmentDataset1FileName1 + "\n";
        paramVariables += "enrichmentDataset1FileName2\t" + enrichmentDataset1FileName2 + "\n";

        paramVariables += "enerichmentDataset2FileName1\t" + enrichmentDataset2FileName1 + "\n";
        paramVariables += "enrichmentDataset2FileName2\t" + enrichmentDataset2FileName2 + "\n";

        paramVariables += "dataset1Phenotype1\t" + dataset1Phenotype1  + "\n";
        paramVariables += "dataset1Phenotype2\t" + dataset1Phenotype2   + "\n";
        paramVariables += "dataset2Phenotype1\t" + dataset2Phenotype1  + "\n";
        paramVariables += "dataset2Phenotype2\t" + dataset2Phenotype2  + "\n";

        paramVariables += "classFile1\t" + classFile1  + "\n";
        paramVariables += "classFile2\t" + classFile2  + "\n";

        //boolean flags
        paramVariables += "twoDatasets\t" + twoDatasets + "\n";
        paramVariables += "jaccard\t" + jaccard + "\n";
        paramVariables += "GSEA\t" + GSEA + "\n";
        paramVariables += "Data\t" + Data + "\n";
        paramVariables += "Data2\t" + Data2 + "\n";
        paramVariables += "FDR\t" + FDR + "\n";

        //cutoffs
        paramVariables += "pvalue\t" + pvalue + "\n";
        paramVariables += "qvalue\t" + qvalue + "\n";
        paramVariables += "jaccardCutOff\t" + jaccardCutOff + "\n";


        return paramVariables;
    }


   //Given a hashmap, go through it and print all the objects(not the keys)
    public String printHashmap(HashMap map ){
       String result = "";

       for(Iterator i = map.keySet().iterator();i.hasNext();){
           Object key = i.next();
           result += key.toString() + "\t" + map.get(key).toString() + "\n";
       }
       return result;

   }

    //repopulate hashmap,
    public HashMap repopulateHashmap(String fileInput, int type ){

        //Create a hashmap to contain all the values in the rpt file.
        HashMap newMap = new HashMap();

        String [] lines = fileInput.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");

            //the first token is the key and the rest of the line is the object
            //depending on the type there is different data

            //GenesetSimilarity
            if(type == 0)
                newMap.put(tokens[0] ,new GenesetSimilarity(tokens));

            //Genesets
            if(type == 1)
                if(tokens.length>=3)
                    newMap.put(tokens[0], new GeneSet(tokens));

            //Genes
            if(type == 2)
                newMap.put(tokens[0],Integer.parseInt(tokens[1]));

            //GseaResult
            if(type == 3)
                newMap.put(tokens[0], new GSEAResult(tokens));

            if(type == 4)
                newMap.put(tokens[0], new GenericResult(tokens));


        }

    return newMap;
    }

    public String getOverlapMetricDefault() {
        return this.default_overlap_metric;

    }

    public void setDefaultJaccardCutOff(double defaultJaccardCutOff) {
        this.defaultJaccardCutOff = defaultJaccardCutOff;
    }

    public double getDefaultJaccardCutOff() {
        return defaultJaccardCutOff;
    }

    public void setDefaultOverlapCutOff(double defaultOverlapCutOff) {
        this.defaultOverlapCutOff = defaultOverlapCutOff;
    }

    public double getDefaultOverlapCutOff() {
        return defaultOverlapCutOff;
    }

    public boolean isDisableHeatmapAutofocus() {
        return this.disable_heatmap_autofocus ;
    }

    public boolean isDisableGenesetSummaryAutofocus() {
        return this.disable_genesetSummary_autofocus ;
    }

    public void setJaccardCutOffChanged(boolean jaccardCutOffChanged) {
        this.jaccardCutOffChanged = jaccardCutOffChanged;
    }

    public boolean isJaccardCutOffChanged() {
        return jaccardCutOffChanged;
    }

}
