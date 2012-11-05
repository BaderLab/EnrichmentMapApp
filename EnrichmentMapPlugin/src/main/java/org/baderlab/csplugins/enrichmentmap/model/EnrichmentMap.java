package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;

/***
 *  An Enrichment Map object contains the minimal information needed to build an 
 *  enrichment map. 
 * @author risserlin
 *
 */

public class EnrichmentMap {
	
	//name of Enrichment map
	private String name = null;
	
	//The set of Datasets
	private HashMap<String,DataSet> datasets;
	
    //Hashmap of all the similarities between all the genesets
    //key = geneset1 + geneset2
    //value = geneset similarity 
	private HashMap<String, GenesetSimilarity> genesetSimilarity;
    
    //The set of genes defined in the Enrichment map
	private HashMap<String, Integer> genes;
    
    //when translating visual attribute of the gene list we need to be able to translate
    //the gene hash key into the gene name without tracing from the entire hash.
    //create the opposite of the gene hashmap so we can do this.
	private HashMap<Integer, String> hashkey2gene;
        
    // Temporary constants for Dataset 1 and Dataset 2
    // Will eventually get rid of them
    final public static String DATASET1 = "Dataset 1";
    final public static String DATASET2 = "Dataset 2";
    
    //reference to the parameters used to create this map
    private EnrichmentMapParameters params;
    
    private int NumberOfGenes = 0;
    
    /*
     * Class Constructor
     * Given - EnrichmentnMapParameters 
     * create a new enrichment map.  The parameters contain all cut-offs and file names for the analysis
     */
    
    public EnrichmentMap(EnrichmentMapParameters params) {
    	
    		this.params = params;
    		this.name = null;
    		
    		this.datasets = new HashMap<String, DataSet>();
    		//initialize a new Dataset if the params have enrichment result or a GMT file
    		if((params.getEnrichmentDataset1FileName1() != null && !params.getEnrichmentDataset1FileName1().isEmpty())
    			|| (params.getGMTFileName() != null && !params.getGMTFileName().isEmpty())
    			|| (params.getExpressionFileName1() != null && !params.getExpressionFileName1().isEmpty())){
    			DataSet dataset1 = new DataSet(this,EnrichmentMap.DATASET1);
    			this.datasets.put(EnrichmentMap.DATASET1, dataset1);
    		}
    		//intialize a new Dataset 2 if the params have enrichment results for a second dataset.
    		if((params.getEnrichmentDataset2FileName1() != null && !params.getEnrichmentDataset2FileName1().isEmpty())
    			|| (params.getExpressionFileName2() != null && !params.getExpressionFileName2().isEmpty())){
    			DataSet dataset2 = new DataSet(this,EnrichmentMap.DATASET2);
    			this.datasets.put(EnrichmentMap.DATASET1, dataset2);
    		}
    		
    		
    		this.genes = new HashMap<String, Integer>();
    		this.hashkey2gene = new HashMap<Integer, String>();
    		
    		this.genesetSimilarity = new HashMap<String, GenesetSimilarity>();
    		
    		initialize_files();
		
    }
    
    public EnrichmentMap(EnrichmentMapParameters params, String name){
    		this(params);
    		this.name = name;
    }
    
    /**
     * Method to copy the contents of one set of parameters into another instance
     *
     * @param copy the parameters from the copy to this instance.
     */
    public void copy(EnrichmentMap copy){
    		this.name = copy.getName();
    		
    		//genes
    		this.genes = copy.getGenes();
    		this.hashkey2gene = copy.getHashkey2gene();
    		
    		this.genesetSimilarity = copy.getGenesetSimilarity();
    		
    		this.params = copy.getParams();
    		
    		this.datasets = copy.getDatasets();
    		
    }
    /**
     * Method to transfer files specified in the parameters to the objects they correspond to
     */
    
    private void initialize_files(){
    		if(params.getGMTFileName() != null && !params.getGMTFileName().isEmpty())
			this	.getDataset(DATASET1).getSetofgenesets().setFilename(params.getGMTFileName());
		
    		//expression files
    		if(params.getExpressionFileName1() != null && !params.getExpressionFileName1().isEmpty())
			this	.getDataset(DATASET1).getExpressionSets().setFilename(params.getExpressionFileName1());
		if(params.getExpressionFileName2() != null && !params.getExpressionFileName2().isEmpty())
			this	.getDataset(DATASET2).getExpressionSets().setFilename(params.getExpressionFileName2());
		
		//enrichment results files
    		if(params.getEnrichmentDataset1FileName1() != null && !params.getEnrichmentDataset1FileName1().isEmpty())
    			this.getDataset(DATASET1).getEnrichments().setFilename1(params.getEnrichmentDataset1FileName1());
    		if(params.getEnrichmentDataset1FileName2() != null && !params.getEnrichmentDataset1FileName2().isEmpty())
    			this.getDataset(DATASET1).getEnrichments().setFilename2(params.getEnrichmentDataset1FileName2());
    		if(params.getEnrichmentDataset2FileName1() != null && !params.getEnrichmentDataset2FileName1().isEmpty())
    			this.getDataset(DATASET2).getEnrichments().setFilename1(params.getEnrichmentDataset2FileName1());
    		if(params.getEnrichmentDataset2FileName2() != null && !params.getEnrichmentDataset2FileName2().isEmpty())
    			this.getDataset(DATASET2).getEnrichments().setFilename2(params.getEnrichmentDataset2FileName2());
    		
    		//rank files - dataset1 
    		if(params.getDataset1RankedFile() != null && !params.getDataset1RankedFile().isEmpty())
    			if(params.getMethod().equals(EnrichmentMapParameters.method_GSEA)){
    				this.getDataset(DATASET1).getExpressionSets().createNewRanking(Ranking.GSEARanking);
    				this.getDataset(DATASET1).getExpressionSets().getRanksByName(Ranking.GSEARanking).setFilename(params.getDataset1RankedFile());
    			}
    			else{
    				this.getDataset(DATASET1).getExpressionSets().createNewRanking(DATASET1);
    				this.getDataset(DATASET1).getExpressionSets().getRanksByName(DATASET1).setFilename(params.getDataset1RankedFile());
    			
    			}
    		//rank files - dataset 2
    		if(params.getDataset2RankedFile() != null && !params.getDataset2RankedFile().isEmpty())
    			if(params.getMethod().equals(EnrichmentMapParameters.method_GSEA)){
    				this.getDataset(DATASET2).getExpressionSets().createNewRanking(Ranking.GSEARanking);
    				this.getDataset(DATASET2).getExpressionSets().getRanksByName(Ranking.GSEARanking).setFilename(params.getDataset2RankedFile());
    			}
    			else{
    				this.getDataset(DATASET2).getExpressionSets().createNewRanking(DATASET2);
    				this.getDataset(DATASET2).getExpressionSets().getRanksByName(DATASET2).setFilename(params.getDataset2RankedFile());
    			
    			}
    }
    
    
    
    /**
     * Check to see that there are genes in the filtered  genesets
     * If the ids do not match up, after a filtering there will be no genes in any of the genesets
     * @return true if Genesets have genes, return false if all the genesets are empty
     */
    public boolean checkGenesets(){

        for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        	HashMap<String,GeneSet> genesets = (datasets.get(k.next())).getSetofgenesets().getGenesets();
        	for(Iterator j = genesets.keySet().iterator(); j.hasNext(); ){
        		String geneset2_name = j.next().toString();
        		GeneSet current_set = genesets.get(geneset2_name);

        		//get the genes in the geneset
        		HashSet<Integer> geneset_genes = current_set.getGenes();

        		//if there is at least one gene in any of the genesets then the ids match.
        		if(!geneset_genes.isEmpty())
        			return true;

        	}
        }
        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
            return true;
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
    
    
    /*
     * Given a set of genesets
     * Go through the genesets and extract all the genes
     * Return - hashmap of genes to hash keys
     * (used to create an expression file when it is not present so user can use expression viewer to navigate
     * genes in a geneset without have to generate their own dummy expression file)
     */
    public HashMap<String,Integer> getGenesetsGenes(HashMap<String, GeneSet> current_genesets){

        HashMap<String, Integer> genesetGenes = new HashMap<String, Integer>();

        for(Iterator j = current_genesets.keySet().iterator(); j.hasNext(); ){

            String geneset_name = j.next().toString();
            GeneSet current_set =  current_genesets.get(geneset_name);

            //compare the HashSet of dataset genes to the HashSet of the current Geneset
            //only keep the genes from the geneset that are in the dataset genes
            HashSet<Integer> geneset_genes = current_set.getGenes();

            for(Iterator k = geneset_genes.iterator();k.hasNext(); ){
                Integer current_genekey = (Integer)k.next();
                //get the current geneName
                if(hashkey2gene.containsKey(current_genekey)){
                    String name = hashkey2gene.get(current_genekey);
                    genesetGenes.put(name, current_genekey);
                }

            }
        }
        return genesetGenes;

    }
    
    
    /*
     * Filter all the genesets by the dataset genes
     * If there are multiple sets of genesets make sure to filter by the specific dataset genes
     */
    public void filterGenesets(){
    	for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        	DataSet current_set = datasets.get(k.next());
          	current_set.getSetofgenesets().filterGenesets(current_set.getDatasetGenes());
        	
    	}
    }
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/* 
	 * Return a hash of all the genesets in the set of genesets
	 * regardless of which dataset it comes from
	 * 
	 */
	public HashMap<String, GeneSet> getAllGenesets(){
		
		//go through each dataset and get the genesets from each
		HashMap<String,GeneSet> all_genesets = new HashMap<String,GeneSet>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        	HashMap<String,GeneSet> current_genesets = (datasets.get(k.next())).getSetofgenesets().getGenesets();
        	all_genesets.putAll(current_genesets);
		}
		return all_genesets;
	}
	
	/*
	 * Return a hash of all the genesets in the set of genesets of interest
	 * regardless of which dataset it comes from
	 */
	public HashMap<String, GeneSet> getAllGenesetsOfInterest(){
		//go through each dataset and get the genesets from each
		HashMap<String,GeneSet> all_genesetsOfInterest = new HashMap<String,GeneSet>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        	HashMap<String,GeneSet> current_genesets = (datasets.get(k.next())).getGenesetsOfInterest().getGenesets();
        	all_genesetsOfInterest.putAll(current_genesets);
		}
		return all_genesetsOfInterest;
	}

	public HashMap<String, GenesetSimilarity> getGenesetSimilarity() {
		return genesetSimilarity;
	}

	public void setGenesetSimilarity(
			HashMap<String, GenesetSimilarity> genesetSimilarity) {
		this.genesetSimilarity = genesetSimilarity;
	}

	public HashMap<String, Integer> getGenes() {
		return genes;
	}
		
	public void setGenes(HashMap<String, Integer> genes) {
		this.genes = genes;
	}

	
	

	public int getNumberOfGenes() {
		return NumberOfGenes;
	}

	public void setNumberOfGenes(int numberOfGenes) {
		NumberOfGenes = numberOfGenes;
	}

	public HashMap<String, DataSet> getDatasets() {
		return datasets;
	}

	public void setDatasets(HashMap<String, DataSet> datasets) {
		this.datasets = datasets;
	}
	
	public void addDataset(String datasetName, DataSet dataset){
		if(this.datasets != null)
			this.datasets.put(datasetName, dataset);
	}
	
	public DataSet getDataset(String datasetname){
		if(this.datasets != null && this.datasets.containsKey(datasetname))
			return this.datasets.get(datasetname);
		else
			return null;
	}
	
	public EnrichmentMapParameters getParams() {
		return params;
	}

	public void setParams(EnrichmentMapParameters params) {
		this.params = params;
	}

	public HashMap<Integer, String> getHashkey2gene() {
		return hashkey2gene;
	}
	
	public void setHashkey2gene(HashMap<Integer, String> hashkey2gene) {
		this.hashkey2gene = hashkey2gene;
	}
    
    public HashSet<String> getAllRankNames(){
    		HashSet<String> allRankNames = new HashSet<String>();
    		//go through each Dataset
    		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
    			allRankNames.addAll((datasets.get(k.next())).getExpressionSets().getAllRanksNames());
    			
    		}
    		return allRankNames;
    }
    
    public HashMap<String, Ranking> getAllRanks(){
    		HashMap<String,Ranking> allranks = new HashMap<String, Ranking>();
    		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
    			allranks.putAll((datasets.get(k.next())).getExpressionSets().getRanks());
    			
    		}
    		return allranks;
    		
    }
    
    public Ranking getRanksByName(String ranks_name){
    		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
    			String current_dataset = k.next();
			if((datasets.get(current_dataset)).getExpressionSets().getAllRanksNames().contains(ranks_name)){
				return datasets.get(current_dataset).getExpressionSets().getRanksByName(ranks_name);
			}
    		}
		return null;
    }
    /*
	 * Return a hash of all different type of genesets from all the datasets
	 * regardless of which dataset it comes from
	 */
	public HashSet<String> getAllGenesetTypes(){
		//go through each dataset and get the genesets from each
		HashSet<String> all_genesetTypes = new HashSet<String>();
		for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        		HashSet<String> current_genesets_types = (datasets.get(k.next())).getSetofgenesets().getGenesetTypes();
        		all_genesetTypes.addAll(current_genesets_types);
		}
		return all_genesetTypes;
	}
}
