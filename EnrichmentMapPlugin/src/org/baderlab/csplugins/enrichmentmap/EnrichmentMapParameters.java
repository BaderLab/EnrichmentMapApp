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
 * <p>
 * Enrichment map Parameters define all the variables that are needed to create, manipulate, explore
 * and save an individual Enrichment map.  It stores the parsed files used to create it, all genes in the network,
 * all enrichment results, cuts-offs, expression files, and ranks
 */
public class EnrichmentMapParameters {

    private String NetworkName;
    private String attributePrefix;

    //Input File names
    //GMT - gene set definition file
    private String GMTFileName;
    //Expression files
    private String expressionFileName1;
    private String expressionFileName2;
    //Enrichment results files - data set 1
    private String enrichmentDataset1FileName1;
    private String enrichmentDataset1FileName2;
    //Enrichment results files - data set 2
    private String enrichmentDataset2FileName1;
    private String enrichmentDataset2FileName2;
    //Dataset Rank files
    private String dataset1RankedFile = null;
    private String dataset2RankedFile = null;
    //Class files - can only be specified when loading in GSEA results with an rpt
    //class files are a specific type of file used in an GSEA analysis indicating which class
    //each column of the expression file belongs to.  It is used in the application to
    //colour the heading on the columns accroding to class or phenotype they belong to.
    private String classFile1;
    private String classFile2;

    private String gseaHtmlReportFileDataset1 = null;
    private String gseaHtmlReportFileDataset2 = null;

    //FLAGS used in the analysis.
    //flag to indicate if the user has supplied a data/expression file
    private boolean Data = false;
    //flag to indicate if the user has supplied a second data/expression file
    private boolean Data2 = false;
    //flag to indicate there are two datasets.
    private boolean twoDatasets = false;
    //flag to indicate if there are FDR Q-values
    private boolean FDR = false;
    //flag to indicate if the results are from GSEA or generic
    private boolean GSEA = true;
    //flag to indicate if the similarity cut off is jaccard or overlap
    private boolean jaccard;
    private boolean similarityCutOffChanged = false;

    // DEFAULT VALUES for pvalue, qvalue, similarityCutOff and jaccard
    // will be assigned in the constructor
    //p-value cutoff
    private double pvalue;
    //pvalue slider bar
    private SliderBarPanel pvalueSlider;
    //fdr q-value cutoff
    private double qvalue;
    //qvalue slider bar
    private SliderBarPanel qvalueSlider;
    //similarity cutoff
    private double similarityCutOff;
    //similarity cutoff slider bar
    private SliderBarPanel similaritySlider;

    //DATA need to specify the Enrichment map
    //Hashmap stores the unique set of genes used in the gmt file
    private HashMap<String,Integer> genes;

    //when translating visual attribute of the gene list we need to be able to translate
    //the gene hash key into the gene name without tracing from the entire hash.
    //create the opposite of the gene hashmap so we can do this.
    private HashMap<Integer, String> hashkey2gene;
    private HashSet<Integer> datasetGenes;
    private int NumberOfGenes = 0;

    //In order to speed up union and intersections of nodes and edges in the network
    //store the genes that are present in the network.
    //get the intersection between the genes in the gene set file (gmt) and the genes in
    //expression file (dataset genes)
    //TODO: recompute enrcihmentmap genes on session re-load.
    private HashSet<Integer> enrichmentMapGenes;

    //Hashmap of the enrichment Results, It is is a hash of the GSEAResults or GenericResults objects
    private HashMap<String, EnrichmentResult> enrichmentResults1;
    private HashMap<String, EnrichmentResult> enrichmentResults2;
    //Hashmap of all genesets in the geneset file (gmt file)
    private HashMap<String, GeneSet> genesets;
    //Hashmap of the filtered Genesets.  After loading in the expression file the genesets are restricted
    //to contain only the proteins specified in the expression file.
    private HashMap<String, GeneSet> filteredGenesets;

    //The enrichment results that pass the thresholds.
    //If there are two datasets these list can be different.
    //Can't enforce a type on this hashmap because the enrichment results could to generic or GSEA results
    private HashMap<String, EnrichmentResult> enrichmentResults1OfInterest;
    private HashMap<String, EnrichmentResult> enrichmentResults2OfInterest;

    private HashMap<String, GeneSet> genesetsOfInterest;
    private HashMap<String, GeneSet> signatureGenesets;

    //Gene expression data used for the analysis.  There can be two distinct files
    //for each dataset.
    private GeneExpressionMatrix expression;
    private GeneExpressionMatrix expression2;

    //default phenotype values.  These values can be set in the advanced features of
    //the input panel.
    private String dataset1Phenotype1 = "UP";
    private String dataset1Phenotype2 = "DOWN";
    private String dataset2Phenotype1 = "UP";
    private String dataset2Phenotype2 = "DOWN";

    //class file designations that were loaded in from a session file.
    //need a temporary place for these class definition as
    private String[] temp_class1 = null;
    private String[] temp_class2 = null;

    private HashMap<String, GenesetSimilarity> genesetSimilarity = null;

    //list associated with slider bars.  As the slider bar is moved the removed nodes
    //and edges are stored in these lists
    //TODO: currently this is not stored in the session file.  So if the user moves the slider bar and saves a session the nodes or edges stored are lost.
    private ArrayList<Node> selectedNodes;
    private ArrayList<Edge> selectedEdges;

    //Heat map parameters for this enrichment map - user specified current normalization, and sorting.
    private HeatMapParameters hmParams;

    //Rankings for Dataset1 and Dataset2.
    //Also stored in the set of ranking and specified as "Dataset 1 Ranking" and "Dataset 2 Ranking"
    private HashMap<Integer, Ranking> dataset1Rankings;
    private HashMap<Integer, Ranking> dataset2Rankings;

    private HashMap<Integer, Integer> rank2geneDataset1;
    private HashMap<Integer, Integer> rank2geneDataset2;

    //Set of Rankings - (HashMap of Hashmaps)
    //Stores the dataset rank files if they were loaded on input but also has
    //the capability of storing more rank files
    private HashMap<String, HashMap<Integer, Ranking>> ranks;

    private Properties cyto_prop;
    private double defaultPvalueCutOff;
    private double defaultQvalueCutOff;
    private double defaultJaccardCutOff;
    private double defaultOverlapCutOff;
    private String defaultSimilarityMetric;
    private Boolean disable_heatmap_autofocus;
    private String defaultSortMethod;
    private String defaultDistanceMetric;

    final public static String ENRICHMENT_INTERACTION_TYPE = "Geneset_Overlap"; //TODO: change to "Geneset_Overlap"
    private PostAnalysisParameters paParams;
    
    private String enrichment_edge_type;

    /**
     * Default constructor to create a fresh instance.
     */
    public EnrichmentMapParameters() {
        this.enrichmentResults1 = new HashMap<String, EnrichmentResult>();
        this.enrichmentResults2 = new HashMap<String, EnrichmentResult>();
        this.genes = new HashMap<String, Integer>();
        this.hashkey2gene = new HashMap<Integer, String>();
        this.datasetGenes = new HashSet<Integer>();
//        this.hashkey2bitindex = new HashMap<Integer, Integer>();
//        this.bitindex2hashkey = new HashMap<Integer, Integer>();
        this.enrichmentMapGenes = new HashSet<Integer>();
        this.genesets = new HashMap<String, GeneSet>();
        this.filteredGenesets = new HashMap<String, GeneSet>();
        this.enrichmentResults1OfInterest = new HashMap<String, EnrichmentResult>();
        this.enrichmentResults2OfInterest = new HashMap<String, EnrichmentResult>();
        this.genesetsOfInterest = new HashMap<String, GeneSet>();
        this.signatureGenesets = new HashMap<String, GeneSet>();
        this.selectedNodes = new ArrayList<Node>();
        this.selectedEdges = new ArrayList<Edge>();
        this.ranks = new HashMap<String, HashMap<Integer, Ranking>>();
        this.dataset1Rankings = new HashMap<Integer, Ranking>();
        this.dataset2Rankings = new HashMap<Integer, Ranking>();

        this.similarityCutOffChanged = false;
        //default Values from Cytoscape properties
        this.cyto_prop = CytoscapeInit.getProperties() ;
        this.defaultPvalueCutOff       = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_pvalue",  "0.005") );
        this.defaultQvalueCutOff       = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_qvalue",  "0.1") );
        this.defaultJaccardCutOff      = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_jaccard", "0.25") );
        this.defaultOverlapCutOff      = Double.parseDouble( this.cyto_prop.getProperty("EnrichmentMap.default_overlap", "0.50") );
        this.defaultSimilarityMetric   = this.cyto_prop.getProperty("EnrichmentMap.default_similarity_metric",
                this.cyto_prop.getProperty("EnrichmentMap.default_overlap_metric", "overlap")); // looking for Property "EnrichmentMap.default_overlap_metric" for legacy reasons
        this.disable_heatmap_autofocus = Boolean.parseBoolean( this.cyto_prop.getProperty("EnrichmentMap.disable_heatmap_autofocus", "false") );

        //get the default heatmap sort algorithm
        this.defaultSortMethod = this.cyto_prop.getProperty("EnrichmentMap.default_sort_method", HeatMapParameters.sort_hierarchical_cluster);

        //get the default distance metric algorithm
        this.defaultDistanceMetric = this.cyto_prop.getProperty("EnrichmentMap.default_distance_metric", HeatMapParameters.pearson_correlation);

        //assign the defaults:
        this.pvalue = this.defaultPvalueCutOff;
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        this.qvalue = this.defaultQvalueCutOff;
        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.qvalue);

        //choose Jaccard or Overlap as default
        if ( getOverlapMetricDefault().equalsIgnoreCase("overlap") ){
            this.similarityCutOff = this.defaultOverlapCutOff;
            this.jaccard = false;
        } else {
            this.similarityCutOff = this.defaultJaccardCutOff;
            this.jaccard = true;
        }
        this.enrichment_edge_type = ENRICHMENT_INTERACTION_TYPE;

        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth, true, this.similarityCutOff);
    }


    /**
     * Constructor to create enrichment map parameters from a cytoscape property file while restoring a Session
     * (property file is created when an enrichment map session is saved)
     *
     *  @param propFile     the name of the property file as a String
     */
    public EnrichmentMapParameters(String propFile){
        this();

        //Create a hashmap to contain all the values in the rpt file.
        HashMap<String, String> props = new HashMap<String, String>();

        String [] lines = propFile.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");
            //there should be two values on each line of the rpt file.
            if(tokens.length == 2 )
                props.put(tokens[0] ,tokens[1]);
        }

        this.NetworkName = props.get("NetworkName");
        this.attributePrefix = props.get("attributePrefix");

        if ( props.containsKey("enrichment_edge_type") )
            this.enrichment_edge_type = props.get("enrichment_edge_type");
        else
            this.enrichment_edge_type = "pp"; // legacy setting: assume it's "pp" if it's not specified in the props file 
        
        this.GMTFileName = props.get("GMTFileName");

        if(props.get("expressionFileName1")!= null)
            this.expressionFileName1 = props.get("expressionFileName1");
        //account for legacy issue with rename of parameter
        else
            this.expressionFileName1 = props.get("GCTFileName1");

        this.enrichmentDataset1FileName1 = props.get("enerichmentDataset1FileName1");//TODO: fix Typo and take care of legacy issue!
        this.enrichmentDataset1FileName2 = props.get("enrichmentDataset1FileName2");

        this.gseaHtmlReportFileDataset1 = props.get("gseaHtmlReportFileDataset1");
        this.gseaHtmlReportFileDataset2 = props.get("gseaHtmlReportFileDataset2");

        this.dataset1Phenotype1 = props.get("dataset1Phenotype1");
        this.dataset1Phenotype2 = props.get("dataset1Phenotype2");
        this.dataset2Phenotype1 = props.get("dataset2Phenotype1");
        this.dataset2Phenotype2 = props.get("dataset2Phenotype2");

        //rank files 1
        if(props.get("rankFile1")!= null){
            if((props.get("rankFile1")).equalsIgnoreCase("null") )
                this.dataset1RankedFile = null;
            else
                this.dataset1RankedFile = props.get("rankFile1");
        }

        if(props.get("classFile1") != null){
            if((props.get("classFile1")).equalsIgnoreCase("null") )
                this.classFile1 = null;
            else
                this.classFile1 = props.get("classFile1");
        }
        if(props.get("classFile2")!= null){
            if((props.get("classFile2")).equalsIgnoreCase("null"))
                this.classFile2 = null;
            else
                this.classFile2 = props.get("classFile2");
        }

        //Get the class one array from the prop file
        if(props.get("class1")!= null){
            if((props.get("class1")).equalsIgnoreCase("null") )
                this.temp_class1 = null;
            else{
                String classes = props.get("class1");
                String [] set = classes.split(",");

                this.temp_class1 = new String[set.length];

                for (int i = 0; i < set.length; i++) {
                    this.temp_class1[i] = set[i];
                }
            }
        }
        //Get the class two array from the prop file
        if(props.get("class2")!= null){
            if((props.get("class2")).equalsIgnoreCase("null") )
                this.temp_class2 = null;
            else{
                String classes = props.get("class2");
                String [] set = classes.split(",");

                this.temp_class2 = new String[set.length];

                for (int i = 0; i < set.length; i++) {
                    this.temp_class2[i] = set[i];
                }
            }
        }

        //boolean flags
        if((props.get("twoDatasets")).equalsIgnoreCase("true"))
            this.twoDatasets = true;
        if((props.get("jaccard")).equalsIgnoreCase("false"))
            this.jaccard = false;
         if((props.get("GSEA")).equalsIgnoreCase("false"))
            this.GSEA = false;
        if((props.get("Data")).equalsIgnoreCase("true"))
            this.Data = true;
        if((props.get("Data2")).equalsIgnoreCase("true"))
            this.Data2 = true;
        if((props.get("FDR")).equalsIgnoreCase("true"))
            this.FDR = true;

        if(twoDatasets){
            if(Data2){
                if(props.get("expressionFileName2")!= null)
                    this.expressionFileName2 = props.get("expressionFileName2");
                //account for legacy issue with rename of parameter
                else
                    this.expressionFileName2 = props.get("GCTFileName2");
            }
            this.enrichmentDataset2FileName1 = props.get("enerichmentDataset2FileName1");
            this.enrichmentDataset2FileName2 = props.get("enrichmentDataset2FileName2");
            //rankfile 2
            if(props.get("rankFile2") != null){
                if((props.get("rankFile2")).equalsIgnoreCase("null") )
                    this.dataset2RankedFile = null;
                 else
                    this.dataset2RankedFile = props.get("rankFile2");
            }
        }
        //cutoffs
        setPvalue(Double.parseDouble(props.get("pvalue")));
        setQvalue(Double.parseDouble(props.get("qvalue")));

        //older version had the similarityCutOff specified as jaccardCutOff.
        //need to check if this is an old session file
        String cutoff;
        if(props.get("jaccardCutOff") != null)
            cutoff = props.get("jaccardCutOff");
        else
            cutoff = props.get("similarityCutOff");

        if(cutoff != null){
            this.similarityCutOff = Double.parseDouble(cutoff);
            this.similarityCutOffChanged = true;
        }
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth, true, this.similarityCutOff);

    }


   /* Method to copy the input contents of an enrichment map paremeter set
    * Only copy parameters specified in the input window
    *
    * Given -
    */

    /**
     * Method to copy the input contents of an enrichment map paremeter set
     * Only copy parameters specified in the input window
     *
     * @param copy -  a parameters set to copy from.
     */
    public void copyInputParameters(EnrichmentMapParameters copy){

        this.GMTFileName = copy.getGMTFileName();
        this.expressionFileName1 = copy.getExpressionFileName1();
        this.expressionFileName2 = copy.getExpressionFileName2();

        this.dataset1RankedFile = copy.getDataset1RankedFile();
        this.dataset2RankedFile = copy.getDataset2RankedFile();

        this.enrichmentDataset1FileName1 = copy.getEnrichmentDataset1FileName1();
        this.enrichmentDataset1FileName2 = copy.getEnrichmentDataset1FileName2();
        this.enrichmentDataset2FileName1 = copy.getEnrichmentDataset2FileName1();
        this.enrichmentDataset2FileName2 = copy.getEnrichmentDataset2FileName2();

        this.gseaHtmlReportFileDataset1 = copy.getGseaHtmlReportFileDataset1();
        this.gseaHtmlReportFileDataset2 = copy.getGseaHtmlReportFileDataset2();

        this.dataset1Phenotype1 = copy.getDataset1Phenotype1();
        this.dataset1Phenotype2 = copy.getDataset1Phenotype2();
        this.dataset2Phenotype1 = copy.getDataset2Phenotype1();
        this.dataset2Phenotype2 = copy.getDataset2Phenotype2();

        this.pvalue = copy.getPvalue();
        //create the slider for this pvalue
        pvalueSlider = new SliderBarPanel(0,this.pvalue,"P-value Cutoff",this, EnrichmentMapVisualStyle.PVALUE_DATASET1, EnrichmentMapVisualStyle.PVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.pvalue);

        this.qvalue = copy.getQvalue();
        //create the slider for the qvalue
        qvalueSlider = new SliderBarPanel(0,this.qvalue,"Q-value Cutoff",this, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET1, EnrichmentMapVisualStyle.FDR_QVALUE_DATASET2,ParametersPanel.summaryPanelWidth, false, this.qvalue);

        this.similarityCutOff = copy.getSimilarityCutOff();
        //create the slider for the similarity cutoff
        similaritySlider = new SliderBarPanel(this.similarityCutOff,1,"Similarity Cutoff",this, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT, EnrichmentMapVisualStyle.SIMILARITY_COEFFECIENT,ParametersPanel.summaryPanelWidth,true, this.similarityCutOff);


        this.Data = copy.isData();
        this.Data2 = copy.isData2();
        this.twoDatasets = copy.isTwoDatasets();
        this.GSEA = copy.isGSEA();
        this.jaccard = copy.isJaccard();
        this.similarityCutOffChanged = copy.similarityCutOffChanged;

       //missing the classfiles in the copy --> bug ticket #61
        this.classFile1 = copy.getClassFile1();
        this.classFile2 = copy.getClassFile2();
   }


   /**
    * Method to copy the contents of one set of parameters into another instance
    *
    * @param copy the parameters to copy from.
    */
   public void copy(EnrichmentMapParameters copy){

       this.NetworkName = copy.getNetworkName();

        this.GMTFileName = copy.getGMTFileName();
        this.expressionFileName1 = copy.getExpressionFileName1();
        this.expressionFileName2 = copy.getExpressionFileName2();

        this.dataset1RankedFile = copy.getDataset1RankedFile();
        this.dataset2RankedFile = copy.getDataset2RankedFile();
        this.ranks = copy.getRanks();

        this.enrichmentDataset1FileName1 = copy.getEnrichmentDataset1FileName1();
        this.enrichmentDataset1FileName2 = copy.getEnrichmentDataset1FileName2();
        this.enrichmentDataset2FileName1 = copy.getEnrichmentDataset2FileName1();
        this.enrichmentDataset2FileName2 = copy.getEnrichmentDataset2FileName2();

        this.gseaHtmlReportFileDataset1 = copy.getGseaHtmlReportFileDataset1();
        this.gseaHtmlReportFileDataset2 = copy.getGseaHtmlReportFileDataset2();

        this.dataset1Phenotype1 = copy.getDataset1Phenotype1();
        this.dataset1Phenotype2 = copy.getDataset1Phenotype2();
        this.dataset2Phenotype1 = copy.getDataset2Phenotype1();
        this.dataset2Phenotype2 = copy.getDataset2Phenotype2();

        this.pvalue = copy.getPvalue();
        this.pvalueSlider = copy.getPvalueSlider();

        this.qvalue = copy.getQvalue();
        qvalueSlider = copy.getQvalueSlider();

        this.similarityCutOff = copy.getSimilarityCutOff();
        this.similaritySlider = copy.getSimilaritySlider();

        this.Data = copy.isData();
        this.Data2 = copy.isData2();
        this.twoDatasets = copy.isTwoDatasets();
        this.GSEA = copy.isGSEA();
        this.FDR = copy.isFDR();
        this.jaccard = copy.isJaccard();

        this.similarityCutOffChanged = copy.similarityCutOffChanged;

        //copy HashMaps genes and hash2genes
        this.genes = copy.getGenes();
        this.hashkey2gene = copy.getHashkey2gene();
        this.NumberOfGenes = copy.getNumberOfGenes();
        this.datasetGenes = copy.getDatasetGenes();

        this.genesets = copy.getGenesets();
        this.genesetsOfInterest = copy.getGenesetsOfInterest();
        this.signatureGenesets = copy.getSignatureGenesets();
        this.filteredGenesets = copy.getFilteredGenesets();
        this.enrichmentResults1 = copy.getEnrichmentResults1();
        this.enrichmentResults2 = copy.getEnrichmentResults2();
        this.enrichmentResults1OfInterest = copy.getEnrichmentResults1OfInterest();
        this.enrichmentResults2OfInterest = copy.getEnrichmentResults2OfInterest();

        this.expression = copy.getExpression();
        this.expression2 = copy.getExpression2();

        //missing the classfiles in the copy --> bug ticket #61
        this.classFile1 = copy.getClassFile1();
        this.classFile2 = copy.getClassFile2();
        this.temp_class1 = copy.getTemp_class1();
        this.temp_class2 = copy.getTemp_class2();
        this.dataset1Rankings = copy.getDataset1Rankings();
        this.dataset2Rankings = copy.getDataset2Rankings();

        this.selectedEdges = copy.getSelectedEdges();
        this.selectedNodes = copy.getSelectedNodes();
        this.genesetSimilarity = copy.getGenesetSimilarity();
        this.hmParams = copy.getHmParams();
        this.attributePrefix = copy.getAttributePrefix();
        this.enrichment_edge_type = copy.getEnrichment_edge_type();

       this.rank2geneDataset1 = copy.getRank2geneDataset1();
       this.rank2geneDataset2 = copy.getRank2geneDataset2();
       }

   /**
    * Checks all values of the EnrichmentMapInputPanel
    * to see if the current set of enrichment map parameters has the minimal amount
    * of information to run enrichment maps.
    *
    * If it is a GSEA run then gmt,gct,2 enrichment files are needed
    * If it is a generic run then gmt and 1 enrichment file is needed
    * if there are two datasets then depending on type it requires the same as above.
    *
    * @return A String with error messages (one error per line) or empty String if everything is okay.
    */
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
        if((this.twoDatasets) && (this.expressionFileName1.equalsIgnoreCase(this.expressionFileName2))){
            this.Data2 = false;
            this.expressionFileName2 = "";
        }


        return errors;
    }

    /**
     * Check to see if the file is readable.
     *
     * @param filename - name of file to be checked
     * @return boolean - true if file is readable, false if it is not.
     */
    protected boolean checkFile(String filename){
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

    /**
         * FilterGenesets - restrict the genes contained in each gene set to only
         * the genes found in the expression file.
         */
        public void filterGenesets(){

            //iterate through each geneset and filter each one
             for(Iterator j = genesets.keySet().iterator(); j.hasNext(); ){

                 String geneset2_name = j.next().toString();
                 GeneSet current_set =  genesets.get(geneset2_name);

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

    /**
     * Compute the enrichment genes
     *
     */
    public void computeEnrichmentMapGenes(){
        enrichmentMapGenes = new HashSet<Integer>();
        for (Iterator<String> i = genesetsOfInterest.keySet().iterator(); i.hasNext(); ){
            String setName = i.next();
            enrichmentMapGenes.addAll(genesetsOfInterest.get(setName).getGenes());
        }
    }
        /**
         * Check to see that there are genes in the filtered  genesets
         * If the ids do not match up, after a filtering there will be no genes in any of the genesets
         * @return true if Genesets have genes, return false if all the genesets are empty
         */
        public boolean checkGenesets(){

            for(Iterator j = filteredGenesets.keySet().iterator(); j.hasNext(); ){
                 String geneset2_name = j.next().toString();
                 GeneSet current_set = filteredGenesets.get(geneset2_name);

                 //get the genes in the geneset
                 HashSet<Integer> geneset_genes = current_set.getGenes();

                //if there is at least one gene in any of the genesets then the ids match.
                if(!geneset_genes.isEmpty())
                    return true;

            }

            return false;

        }
     /**
     * String representation of EnrichmentMapParameters.
     * Is used to store the persistent Attributes as a property file in the Cytoscape Session file.
     *
     * @see java.lang.Object#toString()
     */
    public String toString(){
        StringBuffer paramVariables = new StringBuffer();

        paramVariables.append( "NetworkName\t" + NetworkName + "\n");
        paramVariables.append("attributePrefix\t" + attributePrefix + "\n");
        paramVariables.append("enrichment_edge_type\t" + enrichment_edge_type + "\n");

        //file names
        paramVariables.append("GMTFileName\t" + GMTFileName + "\n");
        paramVariables.append("expressionFileName1\t" + expressionFileName1 + "\n");
        paramVariables.append("expressionFileName2\t" + expressionFileName2 + "\n");
        paramVariables.append("enerichmentDataset1FileName1\t" + enrichmentDataset1FileName1 + "\n");//TODO: fix Typo and take care of legacy issue!
        paramVariables.append("enrichmentDataset1FileName2\t" + enrichmentDataset1FileName2 + "\n");

        paramVariables.append("enerichmentDataset2FileName1\t" + enrichmentDataset2FileName1 + "\n");
        paramVariables.append("enrichmentDataset2FileName2\t" + enrichmentDataset2FileName2 + "\n");

        paramVariables.append("gseaHtmlReportFileDataset1\t" + gseaHtmlReportFileDataset1 + "\n");
        paramVariables.append("gseaHtmlReportFileDataset2\t" + gseaHtmlReportFileDataset2 + "\n");

        paramVariables.append("dataset1Phenotype1\t" + dataset1Phenotype1  + "\n");
        paramVariables.append("dataset1Phenotype2\t" + dataset1Phenotype2   + "\n");
        paramVariables.append("dataset2Phenotype1\t" + dataset2Phenotype1  + "\n");
        paramVariables.append("dataset2Phenotype2\t" + dataset2Phenotype2  + "\n");

        paramVariables.append("classFile1\t" + classFile1  + "\n");
        paramVariables.append("classFile2\t" + classFile2  + "\n");

        //Write the classes/phenotypes as a comma separated list.
        if(this.isData()){
            String[] current_pheno = expression.getPhenotypes();
            if (current_pheno != null){
                StringBuffer output = new StringBuffer();
                for(int j = 0; j < current_pheno.length;j++)
                     output.append(current_pheno[j] + ",");
                paramVariables.append("class1\t" + output.toString() + "\n");
            }
        }
        if(this.isData2()){
            String[] current_pheno = expression2.getPhenotypes();
            if (current_pheno != null){
                StringBuffer output = new StringBuffer();
                for(int j = 0; j < current_pheno.length;j++)
                     output.append(current_pheno[j] + ",");
                paramVariables.append("class2\t" + output.toString() + "\n");
            }
        }

        //rank files
        paramVariables.append("rankFile1\t" + dataset1RankedFile + "\n");
        paramVariables.append("rankFile2\t" + dataset2RankedFile + "\n");

        //boolean flags
        paramVariables.append("twoDatasets\t" + twoDatasets + "\n");
        paramVariables.append("jaccard\t" + jaccard + "\n");
        paramVariables.append("GSEA\t" + GSEA + "\n");
        paramVariables.append("Data\t" + Data + "\n");
        paramVariables.append("Data2\t" + Data2 + "\n");
        paramVariables.append("FDR\t" + FDR + "\n");

        //cutoffs
        paramVariables.append("pvalue\t" + pvalue + "\n");
        paramVariables.append("qvalue\t" + qvalue + "\n");
        paramVariables.append("similarityCutOff\t" + similarityCutOff + "\n");


        return paramVariables.toString();
    }

    /**
     * go through Hashmap and print all the objects
     * @param map - any type of hashmap
     * @return string representation of the hash with the "key tab object newline" representation.
     */
    public String printHashmap(HashMap map ){
       StringBuffer result = new StringBuffer();

       for(Iterator i = map.keySet().iterator(); i.hasNext(); ){
            Object key = i.next();
            result.append( key.toString() + "\t" + map.get(key).toString() + "\n");
       }
       return result.toString();

   }

    /**
     * This method repopulates a properly specified Hashmap from the given file and type.
     *
     * @param fileInput - file name where the hash map is stored.
     * @param type - the type of hashmap in the file.  The hashes are repopulated
     * based on the property file stored in the session file.  The property file
     * specifies the type of objects contained in each file and this is needed in order
     * to create the proper hash in the current set of parameters.
     * types are GeneSet(1), Genes(2), GSEAResult(3), GenericResult(4), Int to String (5), Ranking (6)
     * @return  properly constructed Hashmap repopulated from the specified file.
     */
    public HashMap repopulateHashmap(String fileInput, int type ){
        //TODO: for Type-safety we should generate and return individual HashMaps specifying the correct Types

        //Create a hashmap to contain all the values in the rpt file.
        HashMap newMap;

        boolean incrementRank = false;

        String [] lines = fileInput.split("\n");

        //GeneSet
        if(type == 1)
            newMap = new HashMap<String, GeneSet>();
        //Genes
        else if(type == 2)
            newMap = new HashMap<String, Integer>();
         //GSEAResults
        else if(type == 3)
            newMap = new HashMap<String, GSEAResult>();
        //GenericResults
        else if(type == 4)
            newMap = new HashMap<String, GenericResult>();
        //Hashmap key to gene
        else if(type == 5)
            newMap = new HashMap<Integer, String>();
        //Hashmap gene key to ranking
        else if(type == 6){
            newMap = new HashMap<Integer, Ranking>();

            //issue with ranks from old session files where if there is a rank of -1
            // any heatmap that has that gene will be missing it.
            //ticket #152
            //scan the rank file to see if there is a negative rank
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String[] tokens = line.split("\t");

                if(Integer.parseInt(tokens[3]) < 0)
                    incrementRank = true;
            }

        }
        //Hashmap rank to genekey
        else if(type ==7)
            newMap = new HashMap<Integer, Integer>();
        else
            newMap = new HashMap();



        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");

            //the first token is the key and the rest of the line is the object
            //depending on the type there is different data

            //Genesets
            if(type == 1)
                if(tokens.length>=3)
                    newMap.put(tokens[0], new GeneSet(tokens));

            //Genes
            if(type == 2)
            // need to control the Type of the Objects inside the HashMap, otherwise
            // we can't store the List of Genes to new Nodes and Edges in a restored Session
            // e.g. in in the Signature-Post-Analysis
                newMap.put(tokens[0], Integer.parseInt(tokens[1]));

            //GseaResult
            if(type == 3)
                newMap.put(tokens[0], new GSEAResult(tokens));

            if(type == 4)
                     //legacy issue, check to see if the line has enough items
                    //Generic results were being printed with blank lines between the results
                    //ignore blanks lines
                    if(tokens.length > 3)
                        newMap.put(tokens[0], new GenericResult(tokens));

            //HashMap Key 2 Genes
            if(type == 5) {
            // need to control the Type of the Objects inside the HashMap, otherwise
            // we can't store the List of Genes to new Nodes and Edges in a restored Session
            // e.g. in in the Signature-Post-Analysis
                newMap.put(Integer.parseInt(tokens[0]),tokens[1]);

            }

            //Rankings
            if(type == 6){
                if(incrementRank){
                    Integer newRank = (Integer.parseInt(tokens[3]) + 1);
                    tokens[3] = newRank.toString();
                }
                newMap.put(Integer.parseInt(tokens[0]),new Ranking(tokens));
            }
            //rank to gene id
            if(type == 7)
                newMap.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));

        }

        return newMap;
    }
    
    
    /**
     * @return true if we have at least one list of gene ranks
     */
    public boolean haveRanks() {
        if (this.ranks.size() > 0)
            return true;
        else
            return false;
    }
    
    
    
    /**
     *  given the hash key representing a gene return the gene name
     *
     * @param hash - the hash key representing a gene
     * @return String - gene name
     */
    public String getGeneFromHashKey(Integer hash){
        String gene = null;
        if(hashkey2gene != null || !hashkey2gene.isEmpty())
            gene =  hashkey2gene.get(hash);
        return gene;

    }

    // Class Getters and Setters

    public boolean isJaccard() {
        return jaccard;
    }

    public void setJaccard(boolean jaccard) {
        this.jaccard = jaccard;
    }

    public HashMap<String, EnrichmentResult> getEnrichmentResults1() {
        return enrichmentResults1;
    }

    public void setEnrichmentResults1(HashMap<String, EnrichmentResult> enrichmentResults1) {
        this.enrichmentResults1 = enrichmentResults1;
    }

    public HashMap<String, EnrichmentResult> getEnrichmentResults2() {
        return enrichmentResults2;
    }

    public void setEnrichmentResults2(HashMap<String, EnrichmentResult> enrichmentResults2) {
        this.enrichmentResults2 = enrichmentResults2;
    }

    public HashMap<String, EnrichmentResult> getEnrichmentResults1OfInterest() {
        return enrichmentResults1OfInterest;
    }

    public void setEnrichmentResults1OfInterest(HashMap<String, EnrichmentResult> enrichmentResults1OfInterest) {
        this.enrichmentResults1OfInterest = enrichmentResults1OfInterest;
    }

    public HashMap<String, EnrichmentResult> getEnrichmentResults2OfInterest() {
        return enrichmentResults2OfInterest;
    }

    public void setEnrichmentResults2OfInterest(HashMap<String, EnrichmentResult> enrichmentResults2OfInterest) {
        this.enrichmentResults2OfInterest = enrichmentResults2OfInterest;
    }

    public HashMap<String, GeneSet> getGenesetsOfInterest() {

        return genesetsOfInterest;
    }

    public void setGenesetsOfInterest(HashMap<String, GeneSet> genesetsOfInterest) {
        this.genesetsOfInterest = genesetsOfInterest;

    }

    public HashMap<String, GeneSet> getGenesets() {
        return genesets;
    }

    public void setGenesets(HashMap<String, GeneSet> genesets) {
        this.genesets = genesets;
    }

    public HashMap<String, GeneSet> getFilteredGenesets() {
        return filteredGenesets;
    }

    public void setFilteredGenesets(HashMap<String, GeneSet> filteredGenesets) {
        this.filteredGenesets = filteredGenesets;
    }

    public String getGMTFileName() {

        return GMTFileName;
    }

    public void setGMTFileName(String GMTFileName) {
        this.GMTFileName = GMTFileName;
    }

    public String getExpressionFileName1() {
        return expressionFileName1;
    }

    public void setExpressionFileName1(String GCTFileName) {
        this.expressionFileName1 = GCTFileName;
    }


    public String getExpressionFileName2() {
        return expressionFileName2;
    }

    public void setExpressionFileName2(String GCTFileName) {
        this.expressionFileName2 = GCTFileName;
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

    public HashMap<String, Integer> getGenes() {
        return genes;
    }

    public void setGenes(HashMap<String, Integer> genes) {
        this.genes = genes;
    }

    public HashSet<Integer> getDatasetGenes() {
        return datasetGenes;
    }

    public void setDatasetGenes(HashSet<Integer> datasetGenes) {
        this.datasetGenes = datasetGenes;
    }

    public int getNumberOfGenes() {
        return NumberOfGenes;
    }

    public void setNumberOfGenes(int numberOfGenes) {
        NumberOfGenes = numberOfGenes;
    }

    public double getSimilarityCutOff() {
        return similarityCutOff;
    }

    public void setSimilarityCutOff(double similarityCutOff) {
        this.similarityCutOff = similarityCutOff;
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

    /**
     * @return flag to indicate there are two datasets
     */
    public boolean isTwoDatasets() {
        return twoDatasets;
    }

    public void setTwoDatasets(boolean twoDatasets) {
        this.twoDatasets = twoDatasets;
    }

    public void noFilter(){
        this.filteredGenesets = genesets;
    }


    /**
     * @return flag to indicate if the results are from GSEA or generic
     */
    public boolean isGSEA() {
        return GSEA;
    }

    public void setGSEA(boolean GSEA) {
        this.GSEA = GSEA;
    }

    /**
     * @return flag to indicate if the user has supplied a data/expression file
     */
    public boolean isData() {
        return Data;
    }

    public void setData(boolean data) {
        Data = data;
    }

    /**
     * @return flag to indicate if the user has supplied a second data/expression file
     */
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

    /**
     * @param gseaHtmlReportFileDataset1 the gseaHtmlReportFileDataset1 to set
     */
    public void setGseaHtmlReportFileDataset1(String gseaHtmlReportFileDataset1) {
        this.gseaHtmlReportFileDataset1 = gseaHtmlReportFileDataset1;
    }


    /**
     * @return the gseaHtmlReportFileDataset1
     */
    public String getGseaHtmlReportFileDataset1() {
        return gseaHtmlReportFileDataset1;
    }


    /**
     * @param gseaHtmlReportFileDataset2 the gseaHtmlReportFileDataset2 to set
     */
    public void setGseaHtmlReportFileDataset2(String gseaHtmlReportFileDataset2) {
        this.gseaHtmlReportFileDataset2 = gseaHtmlReportFileDataset2;
    }


    /**
     * @return the gseaHtmlReportFileDataset2
     */
    public String getGseaHtmlReportFileDataset2() {
        return gseaHtmlReportFileDataset2;
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

    public SliderBarPanel getSimilaritySlider() {
        return similaritySlider;
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

    public HashMap<Integer, Ranking> getDataset1Rankings() {
        return dataset1Rankings;
    }

    public void setDataset1Rankings(HashMap<Integer,Ranking> dataset1Rankings) {
        this.dataset1Rankings = dataset1Rankings;

        //also add the ranking file to the set of ranks
        if(this.ranks != null)
            this.ranks.put("Dataset 1 Ranking", this.dataset1Rankings);
    }

    public HashMap<Integer, Ranking> getDataset2Rankings() {
        return dataset2Rankings;
    }

    public void setDataset2Rankings(HashMap<Integer,Ranking> dataset2Rankings) {
        this.dataset2Rankings = dataset2Rankings;

        //also add the ranking file to the set of ranks
        if(this.ranks != null)
            this.ranks.put("Dataset 2 Ranking", this.dataset2Rankings);
    }

    public HashMap<Integer, Integer> getRank2geneDataset1() {
        return rank2geneDataset1;
    }

    public void setRank2geneDataset1(HashMap<Integer, Integer> rank2geneDataset1) {
        this.rank2geneDataset1 = rank2geneDataset1;
    }

    public HashMap<Integer, Integer> getRank2geneDataset2() {
        return rank2geneDataset2;
    }

    public void setRank2geneDataset2(HashMap<Integer, Integer> rank2geneDataset2) {
        this.rank2geneDataset2 = rank2geneDataset2;
    }

    public String getOverlapMetricDefault() {
        return this.defaultSimilarityMetric;

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

    public void setDisableHeatmapAutofocus(boolean disable_heatmap_autofocus) {
        this.disable_heatmap_autofocus = disable_heatmap_autofocus;
    }
    
    public String getDefaultSortMethod() {
        return defaultSortMethod;
    }

    public void setDefaultSortMethod(String defaultSortMethod) {
        this.defaultSortMethod = defaultSortMethod;

        //also update the property in the cytoscape property file
        this.cyto_prop = CytoscapeInit.getProperties() ;
        cyto_prop.setProperty("EnrichmentMap.default_sort_method",defaultSortMethod);
    }

    public void setSimilarityCutOffChanged(boolean similarityCutOffChanged) {
        this.similarityCutOffChanged = similarityCutOffChanged;
    }

    public boolean isSimilarityCutOffChanged() {
        return similarityCutOffChanged;
    }

    public HashMap<Integer, String> getHashkey2gene() {
        return hashkey2gene;
    }

    public void setHashkey2gene(HashMap<Integer, String> hashkey2gene) {
        this.hashkey2gene = hashkey2gene;
    }

    public HashMap<String, HashMap<Integer, Ranking>> getRanks() {
        return ranks;
    }

    public void setRanks(HashMap<String, HashMap<Integer, Ranking>> ranks) {
        this.ranks = ranks;
    }

    public void addRanks(String ranks_name, HashMap<Integer, Ranking> new_rank){
        if(this.ranks != null)
            this.ranks.put(ranks_name, new_rank);
    }

    public HashMap<Integer,Ranking> getRanksByName(String ranks_name){
        if(this.ranks != null){
            return this.ranks.get(ranks_name);
        }
        else{
            return null;
        }
    }

    public String[] getTemp_class1() {
        return temp_class1;
    }

    public void setTemp_class1(String[] temp_class1) {
        this.temp_class1 = temp_class1;
    }

    public String[] getTemp_class2() {
        return temp_class2;
    }

    public void setTemp_class2(String[] temp_class2) {
        this.temp_class2 = temp_class2;
    }

    public HashSet<Integer> getEnrichmentMapGenes() {
        return enrichmentMapGenes;
    }

    public void setEnrichmentMapGenes(HashSet<Integer> enrichmentMapGenes) {
        this.enrichmentMapGenes = enrichmentMapGenes;
    }

    /**
     * @param paParams store reference to PostAnalysisParameters instance associated with this Enrichment Map
     */
    public void setPaParams(PostAnalysisParameters paParams) {
        this.paParams = paParams;
    }

    /**
     * @return reference to PostAnalysisParameters instance associated with this Enrichment Map.<BR>
     *         If no instance exists, a new one will be created.
     */
    public PostAnalysisParameters getPaParams() {
        if (this.paParams == null)
            this.paParams = new PostAnalysisParameters(this);
        return this.paParams;
    }


    /**
     * @param signatureGenesets the signatureGenesets to set
     */
    public void setSignatureGenesets(HashMap<String, GeneSet> signatureGenesets) {
        this.signatureGenesets = signatureGenesets;
    }


    /**
     * @return the signatureGenesets
     */
    public HashMap<String, GeneSet> getSignatureGenesets() {
        return signatureGenesets;
    }


    /**
     * @param enrichment_edge_type the enrichment_edge_type to set
     */
    public void setEnrichment_edge_type(String enrichment_edge_type) {
        this.enrichment_edge_type = enrichment_edge_type;
    }


    /**
     * @return the enrichment_edge_type
     */
    public String getEnrichment_edge_type() {
        return enrichment_edge_type;
    }

    public String getDefaultDistanceMetric() {
        return defaultDistanceMetric;
    }

    public void setDefaultDistanceMetric(String defaultDistanceMetric) {
        this.defaultDistanceMetric = defaultDistanceMetric;
    }
}
