package org.baderlab.csplugins.enrichmentmap.model;

public class EMCreationParameters {

	public static enum Method {
		GSEA, Generic, Specialized
	}
	
	public static enum SimilarityMetric {
		JACCARD, OVERLAP, COMBINED
	}
	
	public static enum GreatFilter {
		HYPER, BINOM, BOTH, EITHER
	}
	
	
	private Method method;
	private String attributePrefix; // MKTODO this shouldn't be here
	
	// Node filtering (gene-sets)
	private double pvalue;
	private double qvaule;
	
	// Edge filtering (similarity)
	private SimilarityMetric similarityMetric;
	private double similarityCutoff;
	private double combinedConstant;
	
	private GreatFilter greatFilter = GreatFilter.HYPER;
	private boolean fdr = false;
	private boolean emgmt = false;
	private double qvalueMin = 1.0;
	private double pvalueMin = 1.0;
	
	// TEMPORARY - this shouldn't be here need to generalize this or put in normal model
	private boolean isDistinctExpressionSets;
	// TEMPORARY - this shouldn't be here this won't work for mastermap
	private String enrichmentEdgeType = "Geneset_Overlap";
	
	
	public EMCreationParameters(Method method, String attributePrefix, 
			SimilarityMetric similarityMetric, double pvalue,  double qvaule, double similarityCutoff, double combinedConstant) {
		this.method = method;
		this.similarityMetric = similarityMetric;
		this.attributePrefix = attributePrefix;
		this.pvalue = pvalue;
		this.qvaule = qvaule;
		this.similarityCutoff = similarityCutoff;
		this.combinedConstant = combinedConstant;
	}

	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method;
	}
	
	public String getAttributePrefix() {
		return attributePrefix;
	}

	public double getPvalue() {
		return pvalue;
	}

	public double getQvalue() {
		return qvaule;
	}

	public double getSimilarityCutoff() {
		return similarityCutoff;
	}

	public double getCombinedConstant() {
		return combinedConstant;
	}
	
	public SimilarityMetric getSimilarityMetric() {
		return similarityMetric;
	}
	
	public double getQvalueMin() {
		return qvalueMin;
	}

	public void setQvalueMin(double qvalueMin) {
		this.qvalueMin = qvalueMin;
	}

	public double getPvalueMin() {
		return pvalueMin;
	}

	public void setPvalueMin(double pvalueMin) {
		this.pvalueMin = pvalueMin;
	}

	public GreatFilter getGreatFilter() {
		return greatFilter;
	}

	public void setGreatFilter(GreatFilter greatFilter) {
		this.greatFilter = greatFilter;
	}
	
	public boolean isFDR() {
		return fdr;
	}

	public void setFDR(boolean fdr) {
		this.fdr = fdr;
	}
	
	public boolean isEMgmt() {
		return emgmt;
	}

	public void setEMgmt(boolean eMgmt) {
		emgmt = eMgmt;
	}

	
	public void setDistinctExpressionSets(boolean d) {
		this.isDistinctExpressionSets = d;
	}
	
	public boolean isDistinctExpressionSets() {
		return isDistinctExpressionSets;
	}

	public String getEnrichmentEdgeType() {
		return enrichmentEdgeType;
	}

	public void setEnrichmentEdgeType(String enrichmentEdgeType) {
		this.enrichmentEdgeType = enrichmentEdgeType;
	}


	public double getQvaule() {
		return qvaule;
	}


	public void setQvaule(double qvaule) {
		this.qvaule = qvaule;
	}


	public boolean isFdr() {
		return fdr;
	}


	public void setFdr(boolean fdr) {
		this.fdr = fdr;
	}


	public boolean isEmgmt() {
		return emgmt;
	}


	public void setEmgmt(boolean emgmt) {
		this.emgmt = emgmt;
	}


	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}


	public void setSimilarityMetric(SimilarityMetric similarityMetric) {
		this.similarityMetric = similarityMetric;
	}


	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}


	public void setSimilarityCutoff(double similarityCutoff) {
		this.similarityCutoff = similarityCutoff;
	}


	public void setCombinedConstant(double combinedConstant) {
		this.combinedConstant = combinedConstant;
	}
}
