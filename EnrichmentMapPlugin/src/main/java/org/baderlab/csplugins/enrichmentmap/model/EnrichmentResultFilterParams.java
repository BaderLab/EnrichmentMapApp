package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Optional;

public interface EnrichmentResultFilterParams {

	public static enum NESFilter {
		ALL, 
		POSITIVE, 
		NEGATIVE
	}
	
	double getPvalue();
	
	double getQvalue();
	
	NESFilter getNESFilter();
	
	Optional<Integer> getMinExperiments();
	
	boolean isFDR();
}
