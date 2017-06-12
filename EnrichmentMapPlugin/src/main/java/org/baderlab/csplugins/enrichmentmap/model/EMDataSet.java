package org.baderlab.csplugins.enrichmentmap.model;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * An Enrichment Map Data Set consists of:
 * <ul><li>Set of Genesets</li>
 * <li>Set of Genes</li>
 * <li>Enrichments of those genesets</li>
 * <li>Expression of genes used to calculate the enrichment</li></ul>
 */
public class EMDataSet extends AbstractDataSet {
	
	public static enum Method {
		GSEA, Generic, Specialized;
		
		public String getLabel() {
			switch(this) {
				case GSEA: default: return "GSEA";
				case Generic:       return "Generic/gProfiler";
				case Specialized:   return "DAVID/BINGO/Great";
			}
		}
	}
	
	private Method method;

	/** The set of enrichments. An enrichment result can either be an Generic or GSEA result. */
	private SetOfEnrichmentResults enrichments = new SetOfEnrichmentResults();

	private GeneExpressionMatrix expressionSets = new GeneExpressionMatrix();
	private boolean dummyExpressionData;
	private Color color;

	/** Hashmap of all genesets in the geneset file (gmt file). It only holds temporary data used during analysis. */
	private SetOfGeneSets setOfGeneSets = new SetOfGeneSets();

	/** The set of genes in the analysis (there might be genes in the gmt file that are not in expression set). */
	private Set<Integer> dataSetGenes = new HashSet<>();

	/** The list of files associated with this Dataset. */
	private DataSetFiles dataSetFiles;

	protected EMDataSet(EnrichmentMap map, String name, Method method, DataSetFiles files) {
		super(map, name);
		this.method = method;

		//get the file name parameters for this map
		//initialize all the filenames from the parameters for this dataset
		if (name != null) {
			this.dataSetFiles = files;
			this.setOfGeneSets.setFilename(files.getGMTFileName());
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

	public SetOfGeneSets getSetOfGeneSets() {
		return setOfGeneSets;
	}

	public void setSetOfGeneSets(SetOfGeneSets setOfGeneSets) {
		this.setOfGeneSets = setOfGeneSets;
	}

	public Set<Integer> getDataSetGenes() {
		return dataSetGenes;
	}

	public void setDataSetGenes(Set<Integer> dataSetGenes) {
		this.dataSetGenes = dataSetGenes;
	}

	public DataSetFiles getDataSetFiles() {
		return dataSetFiles;
	}

	public void setDataSetFiles(DataSetFiles dataSetFiles) {
		this.dataSetFiles = dataSetFiles;
	}

	public boolean isDummyExpressionData() {
		return dummyExpressionData;
	}

	public void setDummyExpressionData(boolean dummyExpressionData) {
		this.dummyExpressionData = dummyExpressionData;
	}

	@Override
	public int hashCode() {
		final int prime = 11;
		int result = 5;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
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
		EMDataSet other = (EMDataSet) obj;
		if (method != other.method)
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "EMDataSet [name=" + getName() + ", method=" + method + "]";
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
}
