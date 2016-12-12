package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * An Enrichment Map Dataset consists of:
 * Set of Genesets
 * Set of Genes
 * Enrichments of those genesets
 * Expression of genes used to calculate the enrichment
 */

public class DataSet implements Comparable<DataSet> {
	
	public static enum Method {
		GSEA, Generic, Specialized
	}
	
	//name of Dataset
	private String name;
	private Method method;

	// The set of enrichments
	//An enrichment result can either be an Generic or GSEA result.
	private SetOfEnrichmentResults enrichments = new SetOfEnrichmentResults();

	//The Expression
	private GeneExpressionMatrix expressionSets = new GeneExpressionMatrix();
	private boolean dummyExpressionData;

	//Hashmap of all genesets in the geneset file (gmt file)
	private SetOfGeneSets setofgenesets = new SetOfGeneSets();
	private SetOfGeneSets genesetsOfInterest = new SetOfGeneSets();
	private Map<String,Long> nodeSuids = new HashMap<>();

	//The set of genes in the analysis
	//(there might be genes in the gmt file that are not in expression set)
	private Set<Integer> datasetGenes = new HashSet<>();

	//Enrichment Map
	//A dataset is associated with an Enrichment map.
	//TODO: Can a dataset be associated to multiple Enrichment maps?
	private EnrichmentMap map;

	//The list of files associated with this Dataset
	private DataSetFiles datasetFiles;

	protected DataSet(EnrichmentMap map, String name, Method method, DataSetFiles files) {
		this.map = map;
		this.name = name;
		this.method = method;

		//get the file name parameters for this map
		//initialize all the filenames from the parameters for this dataset
		if(name != null) {
			this.datasetFiles = files;
			this.setofgenesets.setFilename(files.getGMTFileName());
			this.enrichments.setFilename1(files.getEnrichmentFileName1());
			this.enrichments.setFilename2(files.getEnrichmentFileName2());
			this.enrichments.setPhenotype1(files.getPhenotype1());
			this.enrichments.setPhenotype2(files.getPhenotype2());
			this.expressionSets.setPhenotypes(files.getTemp_class1());
			this.expressionSets.setFilename(files.getExpressionFileName());
		} else {
			System.out.println("There are no files initialized for this Dataset, named:" + name + "\n");
		}

	}

	@Override
	public int compareTo(DataSet ds2) {
		return this.getName().compareTo(ds2.getName());
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
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

	public Set<Integer> getDatasetGenes() {
		return datasetGenes;
	}

	public void setDatasetGenes(Set<Integer> datasetGenes) {
		this.datasetGenes = datasetGenes;
	}

	public DataSetFiles getDatasetFiles() {
		return datasetFiles;
	}

	public void setDatasetFiles(DataSetFiles datasetFiles) {
		this.datasetFiles = datasetFiles;
	}

	public boolean isDummyExpressionData() {
		return dummyExpressionData;
	}

	public void setDummyExpressionData(boolean dummyExpressionData) {
		this.dummyExpressionData = dummyExpressionData;
	}

	public Map<String,Long> getNodeSuids() {
		return nodeSuids;
	}
	
	@Override
	public int hashCode() {
		final int prime = 11;
		int result = 5;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataSet other = (DataSet) obj;
		if (method != other.method)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DataSet [name=" + name + ", method=" + method + "]";
	}
}
