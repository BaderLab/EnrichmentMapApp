package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class represents a set of genesets.  In GSEA the set of genesets is contained in a gmt file.
 */
public class SetOfGeneSets {

	//name of results (ie. Dataset1 or name specified by user)
	private String name;

	//the set of genesets
	// Hash Key = name of gene set
	// Hash Value = Gene set
	private Map<String, GeneSet> geneSets;

	//filename
	private String filename;

	//Total universe size (without filtering)
	private int totalUniverseSize;

	//Genesets can have multiple sources.  
	//Keep a list of all the different types of genesets that are contained in this set of genesets.
	private Set<String> geneSetTypes = new HashSet<String>();

	public SetOfGeneSets() {
		name = "";
		filename = "";
		geneSets = new HashMap<>();
		totalUniverseSize = 0;
	}

	/**
	 * Create a set of Genesets on re-load Given - the dataset these genesets
	 * are associated with and the loaded in EM property file
	 */
	public SetOfGeneSets(String ds, HashMap<String, String> props) {
		if (props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%name"))
			this.name = props.get(ds + "%" + this.getClass().getSimpleName() + "%name");
		
		if (props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%filename"))
			this.filename = props.get(ds + "%" + this.getClass().getSimpleName() + "%filename");
		
		if (props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%genesets")) {
			String genesettypes_set = props.get(ds + "%" + this.getClass().getSimpleName() + "%GenesetTypes");
			genesettypes_set.replace("[", "");
			genesettypes_set.replace("]", "");
			String[] tokens = genesettypes_set.split("\\,");
		
			for (int i = 0; i < tokens.length; i++)
				this.geneSetTypes.add(tokens[i]);
		}
	}

	/**
	 * FilterGenesets - restrict the genes contained in each gene set to only
	 * the genes found in the expression file.
	 */
	public void filterGeneSets(Set<Integer> datasetGenes) {
		Map<String, GeneSet> filteredGenesets = new HashMap<>();

		//iterate through each geneset and filter each one
		for (String geneSetName : geneSets.keySet()) {
			GeneSet geneSet = geneSets.get(geneSetName);
			Set<Integer> genesetGenes = geneSet.getGenes();
			
			Set<Integer> intersection = new HashSet<>(genesetGenes);
			intersection.retainAll(datasetGenes);
			
			GeneSet newGeneSet = new GeneSet(geneSetName, geneSet.getDescription(), intersection);
			filteredGenesets.put(geneSetName, newGeneSet);
		}
		
		geneSets = filteredGenesets;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, GeneSet> getGeneSets() {
		return geneSets;
	}

	public void setGeneSets(HashMap<String, GeneSet> genesets) {
		this.geneSets = genesets;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setGeneSetTypes(HashSet<String> types) {
		this.geneSetTypes = types;
	}

	public Set<String> getGeneSetTypes() {
		return this.geneSetTypes;
	}

	public void addGeneSetType(String type) {
		if (!geneSetTypes.contains(type))
			geneSetTypes.add(type);
	}

	public GeneSet getGeneSetByName(String name) {
		if (geneSets != null) {
			if (geneSets.containsKey(name))
				return geneSets.get(name);
		}
		
		return null;
	}

	public void clear() {
		geneSets.clear();
		geneSetTypes.clear();
	}
}
