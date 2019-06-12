package org.baderlab.csplugins.enrichmentmap.commands.tunables;


import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.COMBINED;
import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.JACCARD;
import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.OVERLAP;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;


/**
 * Use with the @ContainsTunables annotation.
 */
public class FilterTunables {

	@Tunable(description = "P-value Cutoff (value between 0 and 1). Gene set nodes with a p-value lower than the given value will not be included in the network.")
	public Double pvalue = 0.005;

	@Tunable(description = "FDR Q-value Cutoff (value between 0 and 1). Gene set nodes with a q-value lower than the one entered will not be included in the network.")
	public Double qvalue = 0.1;

	@Tunable(description = "Similarity Cutoff (value between 0 and 1). Edges with a similarity score lower than the one entered will not be included in the network.")
	public Double similaritycutoff = 0.25;

	@Tunable(description = "Used to choose the formula used to calculate the similarity score.")
	public ListSingleSelection<String> coefficients;

	@Tunable(description = "When coefficients=COMBINED this parameter is used to determine what percentage to use for JACCARD and OVERLAP when combining their value."
			+ "Value between 0 and 1, where 0 means 100% JACCARD and 0% OVERLAP, and 1 means 0% JACCARD and 100% OVERLAP.")
	public double combinedConstant = LegacySupport.combinedConstant_default;
	
	@Tunable(description = "DISTINCT: Create separate edges for each data set when appropriate. A separate similarity score will be calculated for each data set. "
			+ "COMPOUND: Gene sets with the same name are combined (set union) and then the similarity score is calculated. "
			+ "AUTOMATIC: EnrichmentMap decides which of the previous options to use. See the EnrichmentMap documentation for more details.")
	public ListSingleSelection<String> edgeStrategy;
	
	@Tunable(description = "A gene set must be included in this many data sets to be included in the network (optional).")
	public Integer minExperiments = null;
	
	@Tunable(description = "POSITIVE: Only gene sets from the positive enrichment file will be included.\n" + 
			"NEGATIVE: Only gene sets from the negative enrichment file will be included.\n" + 
			"All: Both enrichment files will be included")
	public ListSingleSelection<String> nesFilter;
	
	@Tunable(description = "If true then genes that are contained in the gene set (GMT) files or the enrichment files,"
			+ " but are not contained in the expression files will not be included in the network.")
	public boolean filterByExpressions = true;
	
	@Tunable(description = "The name of the EnrichmentMap network. If not provided then EnrichmentMap will automatically generate a name "
			+ "for the network based on the name of the first data set.")
	public String networkName = null;
	
	// Not a tunable, for use by integration tests
	public String attributePrefix = null;
	
	
	@Inject private LegacySupport legacySupport;
	
	
	public FilterTunables() {
		coefficients = new ListSingleSelection<String>(OVERLAP.name(), JACCARD.name(), COMBINED.name());
		
		edgeStrategy = enumNames(EdgeStrategy.values());
		edgeStrategy.setSelectedValue(EdgeStrategy.AUTOMATIC.name());
		
		nesFilter = enumNames(NESFilter.values());
		nesFilter.setSelectedValue(NESFilter.ALL.name());
	}

	
	public EMCreationParameters getCreationParameters() throws IllegalArgumentException {
		String attPrefix = this.attributePrefix == null ? EMStyleBuilder.Columns.NAMESPACE_PREFIX : attributePrefix;
		String stylePrefix = legacySupport.getNextStylePrefix();
		
		return new EMCreationParameters(attPrefix, stylePrefix, pvalue, qvalue, getNesFilter(), 
					Optional.ofNullable(minExperiments), filterByExpressions,
					getSimilarityMetric(), similaritycutoff, combinedConstant, 
					getEdgeStrategy());
	}
	
	public static ListSingleSelection<String> enumNames(Enum<?>[] values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}
	
	
	public SimilarityMetric getSimilarityMetric() throws IllegalArgumentException {
		try {
			return SimilarityMetric.valueOf(coefficients.getSelectedValue());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("coefficients is invalid: '" + coefficients.getSelectedValue() + "'");
		}
	}
	
	public NESFilter getNesFilter() throws IllegalArgumentException {
		try {
			return NESFilter.valueOf(nesFilter.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("nesFilter is invalid: '" + nesFilter.getSelectedValue() + "'");
		}
	}
	
	public EdgeStrategy getEdgeStrategy() throws IllegalArgumentException {
		try {
			return EdgeStrategy.valueOf(edgeStrategy.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("edgeStrategy is invalid: '" + edgeStrategy.getSelectedValue() + "'");
		}
	}
	
	@Override
	public String toString() {
		return String.format(
			"pvalue:%f, qvalue:%f, nesFilter:%s, minExperiments:%d, similarityMetric:%s, similarityCutoff:%f, combinedConstant:%f", 
			pvalue, qvalue, nesFilter.getSelectedValue(), minExperiments, coefficients.getSelectedValue(), similaritycutoff, combinedConstant);
	}
}
