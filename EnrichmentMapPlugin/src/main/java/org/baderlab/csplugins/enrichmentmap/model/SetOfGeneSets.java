package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class represents a set of genesets.  In GSEA the set of genesets is contained in a gmt file.
 */
public class SetOfGeneSets {

	/** 
	 * The set of genesets
	 * Hash Key = name of gene set
	 * Hash Value = Gene set
	 * 
	 * Note: Must declare the type as HashMap because it forces GSON to deserialize using 
	 * an actual HashMap, otherwise it wants to use a LinkedTreeMap which uses much more memory.
	 */
	private HashMap<String, GeneSet> geneSets = new HashMap<>();


	/**
	 * FilterGenesets - restrict the genes contained in each gene set to only
	 * the genes found in the expression file.
	 */
	public void filterGeneSets(Set<Integer> datasetGenes) {
		HashMap<String, GeneSet> filteredGenesets = new HashMap<>();

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
	
	public Collection<String> getGeneSetNames() {
		return Collections.unmodifiableCollection(geneSets.keySet());
	}
	
	public Map<String, GeneSet> getGeneSets() {
		return geneSets;
	}

	public void setGeneSets(HashMap<String, GeneSet> geneSets) {
		this.geneSets = geneSets;
	}

	public void addGeneSet(String key, GeneSet geneSet) {
		synchronized (geneSet) {
			geneSets.put(key, geneSet);
		}
	}
	

	public GeneSet getGeneSetByName(String name) {
		if (geneSets != null) {
			if (geneSets.containsKey(name))
				return geneSets.get(name);
		}
		
		return null;
	}
	
	/**
	 * @return The number of gene sets in this set
	 */
	public int size() {
		return geneSets.size();
	}

	public void clear() {
		geneSets.clear();
	}
	
	public boolean isEmpty() {
		return geneSets.isEmpty();
	}
	

	@Override
	public int hashCode() {
		return geneSets.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof SetOfGeneSets) {
			SetOfGeneSets other = (SetOfGeneSets) obj;
			return geneSets.equals(other.geneSets);
		}
		return false;
	}
	
	
}
