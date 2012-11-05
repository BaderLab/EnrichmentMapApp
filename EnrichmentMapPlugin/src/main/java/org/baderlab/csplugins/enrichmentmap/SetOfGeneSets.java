package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * Class represents a set of genesets.  In GSEA the set of genesets is contained
 * in a gmt file.
 */
public class SetOfGeneSets {
	
		//name of results (ie. Dataset1 or name specified by user)
		private String name;
		
		//the set of genesets
		// Hash Key = name of gene set
		// Hash Value = Gene set
		private HashMap<String, GeneSet> genesets;

		//filename
		private String filename;
		
		//Genesets can have multiple sources.  
		//Keep a list of all the different types of genesets that are contained in this set of genesets.
	    private HashSet<String> GenesetTypes = new HashSet<String>();
		
		public SetOfGeneSets(){
			name = "";
			filename = "";
			genesets = new HashMap<String, GeneSet>();
		}
		
		/**
	     * FilterGenesets - restrict the genes contained in each gene set to only
	     * the genes found in the expression file.
	     */
	    public void filterGenesets(HashSet<Integer> datasetGenes){
	    		
	    	//create a new hashmap to store the filtered geneset
	    	HashMap<String, GeneSet> filteredGenesets = new HashMap<String,GeneSet>(); 
	    		
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

	             filteredGenesets.put(geneset2_name,new_set);

	         }
	        //once we have filtered the genesets clear the original genesets object
	        genesets.clear();
	        
	        genesets = filteredGenesets;

	    }
		
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public HashMap<String, GeneSet> getGenesets() {
			return genesets;
		}

		public void setGenesets(HashMap<String, GeneSet> genesets) {
			this.genesets = genesets;
		}

		public String getFilename() {
			return filename;
		}

		public void setFilename(String filename) {
			this.filename = filename;
		}
		
		  public void setGenesetTypes(HashSet<String> types){
		        this.GenesetTypes = types;
		    }

		    public HashSet<String> getGenesetTypes(){
		       return this.GenesetTypes;
		    }

		    public void addGenesetType(String type){
		        if(!GenesetTypes.contains(type)){
		            GenesetTypes.add(type);

		        }
		    }


}
