package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

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
	private Map<String, GeneSet> genesets;

	//filename
	private String filename;

	//Total universe size (without filtering)
	private int totalUniverseSize;

	//Genesets can have multiple sources.  
	//Keep a list of all the different types of genesets that are contained in this set of genesets.
	private Set<String> GenesetTypes = new HashSet<String>();

	public SetOfGeneSets() {
		name = "";
		filename = "";
		genesets = new HashMap<>();
		totalUniverseSize = 0;
	}

	/*
	 * Create a set of Genesets on re-load Given - the dataset these genesets
	 * are associated with and the loaded in EM property file
	 */
	public SetOfGeneSets(String ds, HashMap<String, String> props) {
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%name"))
			this.name = props.get(ds + "%" + this.getClass().getSimpleName() + "%name");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%filename"))
			this.filename = props.get(ds + "%" + this.getClass().getSimpleName() + "%filename");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%genesets")) {
			String genesettypes_set = props.get(ds + "%" + this.getClass().getSimpleName() + "%GenesetTypes");
			genesettypes_set.replace("[", "");
			genesettypes_set.replace("]", "");
			String[] tokens = genesettypes_set.split("\\,");
			for(int i = 0; i < tokens.length; i++)
				this.GenesetTypes.add(tokens[i]);
		}
	}

	/**
	 * FilterGenesets - restrict the genes contained in each gene set to only
	 * the genes found in the expression file.
	 */
	public void filterGenesets(Set<Integer> datasetGenes) {
		//create a new hashmap to store the filtered geneset
		HashMap<String, GeneSet> filteredGenesets = new HashMap<String, GeneSet>();

		//iterate through each geneset and filter each one
		for(String geneset2_name : genesets.keySet()) {
			GeneSet current_set = genesets.get(geneset2_name);

			//compare the HashSet of dataset genes to the HashSet of the current Geneset
			//only keep the genes from the geneset that are in the dataset genes
			Set<Integer> intersection = Sets.intersection(current_set.getGenes(), datasetGenes);

			GeneSet new_set = new GeneSet(geneset2_name, current_set.getDescription(), intersection);

			filteredGenesets.put(geneset2_name, new_set);
		}
		
		genesets = filteredGenesets;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, GeneSet> getGenesets() {
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

	public void setGenesetTypes(HashSet<String> types) {
		this.GenesetTypes = types;
	}

	public Set<String> getGenesetTypes() {
		return this.GenesetTypes;
	}

	public void addGenesetType(String type) {
		if(!GenesetTypes.contains(type)) {
			GenesetTypes.add(type);

		}
	}

	public GeneSet getGeneSetByName(String name) {
		if(genesets != null) {
			if(genesets.containsKey(name))
				return genesets.get(name);
		}
		return null;
	}

	public String toString(String ds) {
		StringBuffer paramVariables = new StringBuffer();

		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%name\t" + name + "\n");
		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%filename\t" + filename + "\n");
		paramVariables.append(
				ds + "%" + this.getClass().getSimpleName() + "%GenesetTypes\t" + GenesetTypes.toString() + "\n");
		return paramVariables.toString();
	}

	public void clear() {
		genesets.clear();
		GenesetTypes.clear();
	}

}
