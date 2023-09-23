package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


public class EMCreationParameters implements EnrichmentResultFilterParams {

	public static enum SimilarityMetric {
		JACCARD, OVERLAP, COMBINED
	}
	
	public static enum GreatFilter {
		HYPER, BINOM, BOTH, EITHER
	}
	
	public static enum EdgeStrategy {
		AUTOMATIC, DISTINCT, COMPOUND
	}
	
	private String attributePrefix;
	private String stylePrefix;
	
	// Node filtering (gene-sets)
	private double pvalue;
	private double qvalue;
	private Optional<Integer> minExperiments;
	private NESFilter nesFilter;
	private boolean filterByExpressions;
	private boolean parseBaderlab;
	
	// Edge filtering (similarity)
	private SimilarityMetric similarityMetric;
	private double similarityCutoff;
	private double combinedConstant;
	
	private GreatFilter greatFilter = GreatFilter.HYPER;
	private boolean fdr = false;
	private boolean david = false;
	private boolean emgmt = false;
	private double qvalueMin = 1.0;
	private double pvalueMin = 1.0;
	
	private String networkName;
	private EdgeStrategy edgeStrategy;
	private boolean runAutoAnnotate = false;
	private String layout = null;
	
	private transient boolean forceNES = false;
	
	/**
	 * This is calculated based on the edge strategy, number of data sets and expressions. 
	 * See FilterGenesetsByDatasetGenes and ComputeSimilarityTaskParallel.
	 */
	private boolean createDistinctEdges; 
	
	private String enrichmentEdgeType = "Geneset_Overlap";
	
	private final Set<String> pValueColumnNames = new HashSet<>();
	private final Set<String> qValueColumnNames = new HashSet<>();
	private final Set<String> similarityCutoffColumnNames = new HashSet<>();
	

	public EMCreationParameters(
			String attributePrefix, 
			String stylePrefix,
			double pvalue,
			double qvalue,
			NESFilter nesFilter,
			Optional<Integer> minExperiments,
			boolean filterByExpressions,
			boolean parseBaderlab,
			SimilarityMetric similarityMetric,
			double similarityCutoff,
			double combinedConstant,
			EdgeStrategy edgeStrategy
	) { 
		this.similarityMetric = similarityMetric;
		this.attributePrefix = attributePrefix;
		this.stylePrefix = stylePrefix;
		this.pvalue = pvalue;
		this.qvalue = qvalue;
		this.nesFilter = nesFilter;
		this.minExperiments = minExperiments;
		this.parseBaderlab = parseBaderlab;
		this.filterByExpressions = filterByExpressions;
		this.similarityCutoff = similarityCutoff;
		this.combinedConstant = combinedConstant;
		this.edgeStrategy = edgeStrategy;
	}

	
	public void setNetworkName(String name) {
		this.networkName = name;
	}
	
	public String getNetworkName() {
		return networkName;
	}
	
	public String getAttributePrefix() {
		return attributePrefix;
	}

	/**
	 * Before version 3.2 the attributePrefix was also used as the stylePrefix.
	 * Loading a session created with version 3.1 the stylePrefix will be null.
	 */
	public String getStylePrefix() {
		return (stylePrefix == null) ? attributePrefix : stylePrefix;
	}

	public boolean isRunAutoAnnotate() {
		return runAutoAnnotate;
	}

	public void setRunAutoAnnotate(boolean runAutoAnnotate) {
		this.runAutoAnnotate = runAutoAnnotate;
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
	
	public void setDavid(boolean david) {
		this.david = david;
	}
	
	public boolean isDavid() {
		return david;
	}
	
	public boolean isEMgmt() {
		return emgmt;
	}

	public void setEMgmt(boolean eMgmt) {
		emgmt = eMgmt;
	}

	public void setEdgeStrategy(EdgeStrategy edgeStrategy) {
		this.edgeStrategy = edgeStrategy;
	}
	
	public EdgeStrategy getEdgeStrategy() {
		return edgeStrategy;
	}
	
	public void setCreateDistinctEdges(boolean d) {
		this.createDistinctEdges = d;
	}
	
	public boolean getCreateDistinctEdges() {
		return createDistinctEdges;
	}

	public String getEnrichmentEdgeType() {
		return enrichmentEdgeType;
	}

	public void setEnrichmentEdgeType(String enrichmentEdgeType) {
		this.enrichmentEdgeType = enrichmentEdgeType;
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

	public boolean isFilterByExpressions() {
		return filterByExpressions;
	}
	
	public boolean isParseBaderlabGeneSets() {
		return parseBaderlab;
	}

	public void setForceNES(boolean forceNES) {
		this.forceNES = forceNES;
	}
	
	public boolean isForceNES() {
		return forceNES;
	}
	
	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	@Override
	public String toString() {
		return "EMCreationParameters [attributePrefix=" + attributePrefix + ", stylePrefix=" + stylePrefix + ", pvalue="
				+ pvalue + ", qvalue=" + qvalue + ", minExperiments=" + minExperiments + ", nesFilter=" + nesFilter
				+ ", filterByExpressions=" + filterByExpressions + ", parseBaderlab=" + parseBaderlab
				+ ", similarityMetric=" + similarityMetric + ", similarityCutoff=" + similarityCutoff
				+ ", combinedConstant=" + combinedConstant + ", greatFilter=" + greatFilter + ", fdr=" + fdr
				+ ", emgmt=" + emgmt + ", qvalueMin=" + qvalueMin + ", pvalueMin=" + pvalueMin + ", networkName="
				+ networkName + ", edgeStrategy=" + edgeStrategy + ", createDistinctEdges=" + createDistinctEdges
				+ ", enrichmentEdgeType=" + enrichmentEdgeType + ", pValueColumnNames=" + pValueColumnNames
				+ ", qValueColumnNames=" + qValueColumnNames + ", similarityCutoffColumnNames="
				+ similarityCutoffColumnNames + "]";
	}


	
}
