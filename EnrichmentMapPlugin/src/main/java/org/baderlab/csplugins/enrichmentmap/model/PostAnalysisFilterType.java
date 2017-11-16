package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;

/**
 * Filter type constants shown in the UI.
 */
public enum PostAnalysisFilterType {
	
	NO_FILTER("-- no filter --", 0.0), 
	HYPERGEOM("Hypergeometric Test", 0.05), 
	MANN_WHIT_TWO_SIDED("Mann-Whitney (Two-Sided)", 0.05), 
	MANN_WHIT_GREATER("Mann-Whitney (One-Sided Greater)", 0.05), 
	MANN_WHIT_LESS("Mann-Whitney (One-Sided Less)", 0.05), 
	NUMBER("Overlap has at least X genes", 5), 
	PERCENT("Overlap is X percent of EM gs", 25), 
	SPECIFIC("Overlap is X percent of Sig gs", 25);

	// For backwards compatibility
	public static final String OLD_MANN_WHIT_LABEL = "Mann-Whitney";

	public final String display;
	public final double defaultValue;

	private PostAnalysisFilterType(String display, double defaultValue) {
		this.display = display;
		this.defaultValue = defaultValue;
	}

	public String toString() {
		return display;
	}

	public boolean isMannWhitney() {
		switch(this) {
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_GREATER:
			case MANN_WHIT_LESS:
				return true;
			default:
				return false;
		}
	}

	public MannWhitneyUTestSided.Type mannWhitneyTestType() {
		switch(this) {
			case MANN_WHIT_TWO_SIDED:
				return MannWhitneyUTestSided.Type.TWO_SIDED;
			case MANN_WHIT_GREATER:
				return MannWhitneyUTestSided.Type.GREATER;
			case MANN_WHIT_LESS:
				return MannWhitneyUTestSided.Type.LESS;
			default:
				return null;
		}
	}

	public static PostAnalysisFilterType fromDisplayString(String val) {
		if(OLD_MANN_WHIT_LABEL.equals(val))
			return MANN_WHIT_TWO_SIDED;

		for(PostAnalysisFilterType metric : values()) {
			if(metric.display.equals(val)) {
				return metric;
			}
		}
		return null;
	}
	
	/**
	 * Returns a new mutable Map that contains all the default values.
	 */
	public static Map<PostAnalysisFilterType,String> createMapOfDefaults() {
		Map<PostAnalysisFilterType,String> map = new HashMap<>();
		for(PostAnalysisFilterType type : PostAnalysisFilterType.values()) {
			map.put(type, String.valueOf(type.defaultValue));
		}
		return map;
	}
	
	@Deprecated
	public static Map<PostAnalysisFilterType,Double> createMapOfDefaultsOld() {
		Map<PostAnalysisFilterType,Double> map = new HashMap<>();
		for(PostAnalysisFilterType type : PostAnalysisFilterType.values()) {
			map.put(type, type.defaultValue);
		}
		return map;
	}
	
	
	public boolean isValid(Number value) {
		switch(this) {
			case NO_FILTER:
				return true;
			case HYPERGEOM:
				return value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0;
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_LESS:
			case MANN_WHIT_GREATER:
				return value != null && value.doubleValue() >= 0.0 && value.intValue() <= 1.0;
			case PERCENT:
			case SPECIFIC:
				return value != null && value.intValue() >= 0 && value.intValue() <= 100;
			case NUMBER:
				return value != null && value.intValue() >= 0;
			default:
				return false;
		}
	}
	
	public String getErrorMessage() {
		switch(this) {
			case HYPERGEOM:
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_LESS:
			case MANN_WHIT_GREATER:
				return "Must be between 0 and 1";
			case PERCENT:
			case SPECIFIC:
				return "Must be between 0 and 100";
			case NUMBER:
				return "Must be greater than 0";
			default:
				return null;
		}

	}
}