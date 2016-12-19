package org.baderlab.csplugins.enrichmentmap.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * MKTODO This class should only contain filtering parameters, to make it more cohesive.
 */
public class EMCreationParameters implements EnrichmentResultFilterParams {

	public static enum SimilarityMetric {
		JACCARD, OVERLAP, COMBINED
	}
	
	public static enum GreatFilter {
		HYPER, BINOM, BOTH, EITHER
	}
	
	private String attributePrefix; // MKTODO this shouldn't be here
	
	// Node filtering (gene-sets)
	private double pvalue;
	private double qvalue;
	private Optional<Integer> minExperiments;
	private NESFilter nesFilter;
	
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
	
	private Path globalGmtFile;
	
	private final Set<String> pValueColumnNames = new HashSet<>();
	private final Set<String> qValueColumnNames = new HashSet<>();
	private final Set<String> similarityCutoffColumnNames = new HashSet<>();
	

	public EMCreationParameters(
			String attributePrefix, 
			double pvalue,
			double qvalue,
			NESFilter nesFilter,
			Optional<Integer> minExperiments, // NODE
			SimilarityMetric similarityMetric,
			double similarityCutoff,
			double combinedConstant
	) { // EDGE
		this.similarityMetric = similarityMetric;
		this.attributePrefix = attributePrefix;
		this.pvalue = pvalue;
		this.qvalue = qvalue;
		this.nesFilter = nesFilter;
		this.minExperiments = minExperiments;
		this.similarityCutoff = similarityCutoff;
		this.combinedConstant = combinedConstant;
	}

	public void setGlobalGmtFile(Path path) {
		this.globalGmtFile = path;
	}
	
	public Path getGlobalGmtFile() {
		return globalGmtFile;
	}
	
	public String getAttributePrefix() {
		return attributePrefix;
	}

	@Override
	public double getPvalue() {
		return pvalue;
	}

	@Override
	public double getQvalue() {
		return qvalue;
	}

	@Override
	public NESFilter getNESFilter() {
		return nesFilter;
	}
	
	@Override
	public Optional<Integer> getMinExperiments() {
		return minExperiments;
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
	
	@Override
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

	public boolean isFdr() {
		return fdr;
	}

	public void setFdr(boolean fdr) {
		this.fdr = fdr;
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

	public void addPValueColumnName(String name) {
		pValueColumnNames.add(name);
	}

	public void addQValueColumnName(String name) {
		qValueColumnNames.add(name);
	}
	
	public void addSimilarityCutoffColumnName(String name) {
		similarityCutoffColumnNames.add(name);
	}
	
	public Set<String> getPValueColumnNames() {
		return new HashSet<>(pValueColumnNames);
	}
	
	public Set<String> getQValueColumnNames() {
		return new HashSet<>(qValueColumnNames);
	}
	
	public Set<String> getSimilarityCutoffColumnNames() {
		return new HashSet<>(similarityCutoffColumnNames);
	}
}
