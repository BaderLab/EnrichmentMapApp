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
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;


/**
 * Use with the @ContainsTunables annotation.
 */
public class FilterTunables {

	//Parameter Tuneables
	@Tunable(description = "P-value Cutoff", groups = { "User Input", "Parameters" }, gravity = 17.0, tooltip = "P-value between 0 and 1.")
	public Double pvalue = 0.005;

	@Tunable(description = "FDR Q-value Cutoff", groups = { "User Input", "Parameters" }, gravity = 18.0, tooltip = "FDR Q-value between 0 and 1.")
	public Double qvalue = 0.1;

	@Tunable(description = "Similarity Cutoff", groups = { "User Input", "Parameters" }, gravity = 19.0, tooltip = "coeffecient between 0 and 1.")
	public Double similaritycutoff = 0.25;

	@Tunable(description = "Similarity Coeffecient", groups = { "User Input", "Parameters" }, gravity = 20.0, tooltip = "coeffecient between 0 and 1.")
	public ListSingleSelection<String> coefficients;

	@Tunable
	public double combinedConstant = LegacySupport.combinedConstant_default;
	
	@Tunable
	public ListSingleSelection<String> edgeStrategy;
	
	@Tunable
	public Integer minExperiments = null;
	
	@Tunable
	public ListSingleSelection<String> nesFilter;
	
	@Tunable
	public boolean filterByExpressions = true;
	
	@Tunable
	public String networkName = null;
	
	
	@Inject private LegacySupport legacySupport;
	
	
	public FilterTunables() {
		coefficients = new ListSingleSelection<String>(OVERLAP.name(), JACCARD.name(), COMBINED.name());
		
		edgeStrategy = enumNames(EdgeStrategy.values());
		edgeStrategy.setSelectedValue(EdgeStrategy.AUTOMATIC.name());
		
		nesFilter = enumNames(NESFilter.values());
		nesFilter.setSelectedValue(NESFilter.ALL.name());
	}

	
	public EMCreationParameters getCreationParameters() throws IllegalArgumentException {
		String prefix = legacySupport.getNextAttributePrefix();
		return new EMCreationParameters(prefix, pvalue, qvalue, getNesFilter(), 
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
