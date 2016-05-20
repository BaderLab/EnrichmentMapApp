package org.baderlab.csplugins.enrichmentmap;

import org.inferred.freebuilder.FreeBuilder;

/**
 * Parameters used for specifying filters and cutoffs for post-analysis.
 */
@FreeBuilder
public interface FilterParameters {

	public FilterType getType();

	public double getValue();
	
	
	class Builder extends FilterParameters_Builder {
		public Builder() {
			FilterType filterType = FilterType.NO_FILTER;
			setType(filterType);
			setValue(filterType.defaultValue);
		}
	}
	
}
