package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;
import java.util.HashSet;

/*
 * An Enrichment Map Dataset consists of:
 * Set of Genesets
 * Set of Genes
 * Enrichments of those genesets
 * Expression of genes used to calculate the enrichment
 */

public class DataSet {
		//name of Dataset
		private String name = null;
		
		// The set of enrichments
		//An enrichment result can either be an Generic or GSEA result.
		private SetOfEnrichmentResults enrichments; 
		
		//The Expression
		private GeneExpressionMatrix expressionSets;
		
	    //Hashmap of all genesets in the geneset file (gmt file)
	    private SetOfGeneSets setofgenesets;
	    private SetOfGeneSets genesetsOfInterest;
	    
	    //The set of genes in the analysis
	    //(there might be genes in the gmt file that are not in expression set)
	    private HashSet<Integer> datasetGenes;
	    	    	    
	    //Enrichment Map
	    //A dataset is associated with an Enrichment map.
	    //TODO: Can a dataset be associated to multiple Enrichment maps?
	    private EnrichmentMap map;
	    
	    public DataSet(EnrichmentMap map){
	    		this.map = map;
	    	
	    		this.datasetGenes = new HashSet<Integer>();
	        
	    		this.setofgenesets = new SetOfGeneSets();
	    		this.genesetsOfInterest = new SetOfGeneSets();
		
	    		this.enrichments = new SetOfEnrichmentResults();
	    		this.expressionSets = new GeneExpressionMatrix();
    		
	    		//get the file name parameters for this map
	    		EnrichmentMapParameters params = map.getParams();
	    		//initialize all the filenames from the parameters for this dataset
	    		this.setofgenesets.setFilename(params.getGMTFileName());
	    		if(name == null || name == EnrichmentMap.DATASET1){
	    			this.enrichments.setFilename1(params.getEnrichmentDataset1FileName1());
	    			this.enrichments.setFilename2(params.getEnrichmentDataset1FileName2());
	    			this.expressionSets.setFilename(params.getExpressionFileName1());
	    		}
	    		else if(name == EnrichmentMap.DATASET2){
	    			this.enrichments.setFilename1(params.getEnrichmentDataset2FileName1());
	    			this.enrichments.setFilename2(params.getEnrichmentDataset2FileName2());
	    			this.expressionSets.setFilename(params.getExpressionFileName2());
	    		}
	    			
	    }
	    
	    public DataSet(EnrichmentMap map, String name){
	    		this(map);
	    		this.name = name;
	    }
	    
	    
	    public void copy(DataSet copy){
	    	
	    		this.map = copy.getMap();
	    		//gene sets
	    		this.setofgenesets = copy.getSetofgenesets();
	    		this.genesetsOfInterest = copy.getGenesetsOfInterest();
    		
	    		this.enrichments = copy.getEnrichments();
	    		this.expressionSets = copy.getExpressionSets();
	    		this.datasetGenes = copy.getDatasetGenes();
	    }
	    
	    
	    /*
	     * Using the genesetsOfInterest filter the enrichment results
	     * return - a set of enrichment Results contain only the genesets of interest.
	     */
	   /* public SetOfEnrichmentResults getFilteredEnrichmentResults(){
	    	
	    }*/
	    
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public SetOfEnrichmentResults getEnrichments() {
			return enrichments;
		}

		public void setEnrichments(SetOfEnrichmentResults enrichments) {
			this.enrichments = enrichments;
		}

		public GeneExpressionMatrix getExpressionSets() {
			return expressionSets;
		}

		public void setExpressionSets(GeneExpressionMatrix expressionSets) {
			this.expressionSets = expressionSets;
		}

		public EnrichmentMap getMap() {
			return map;
		}

		public void setMap(EnrichmentMap map) {
			this.map = map;
		}

		public SetOfGeneSets getSetofgenesets() {
			return setofgenesets;
		}

		public void setSetofgenesets(SetOfGeneSets setofgenesets) {
			this.setofgenesets = setofgenesets;
		}

		public SetOfGeneSets getGenesetsOfInterest() {
			return genesetsOfInterest;
		}

		public void setGenesetsOfInterest(SetOfGeneSets genesetsOfInterest) {
			this.genesetsOfInterest = genesetsOfInterest;
		}

		public HashSet<Integer> getDatasetGenes() {
			return datasetGenes;
		}

		public void setDatasetGenes(HashSet<Integer> datasetGenes) {
			this.datasetGenes = datasetGenes;
		}

		

}
