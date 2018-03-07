package org.baderlab.csplugins.enrichmentmap.commands;

import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.COMBINED;
import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.JACCARD;
import static org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric.OVERLAP;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * This is just for the build command for backwards compatibility with the misspelled 'coeffecients' parameter. 
 * Use with @ContainsTunables.
 */
public class FilterTunablesLegacy extends FilterTunables {

	@Deprecated
	@Tunable(description = "Deprecated, use 'coefficients' instead.")
	public ListSingleSelection<String> coeffecients; // misspelled, but must keep for backwards compatibility
	
	
	public FilterTunablesLegacy() {
		coeffecients = new ListSingleSelection<String>("null", OVERLAP.name(), JACCARD.name(), COMBINED.name());
	}

	@Override
	public SimilarityMetric getSimilarityMetric() {
		String value;
		if(!"null".equals(coeffecients.getSelectedValue())) {
			// the old field overrides the new field
			value = coeffecients.getSelectedValue(); 
		} else {
			value = coefficients.getSelectedValue();
		}
		
		return SimilarityMetric.valueOf(value);
	}
}
