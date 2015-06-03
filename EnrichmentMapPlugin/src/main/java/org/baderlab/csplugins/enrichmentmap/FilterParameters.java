package org.baderlab.csplugins.enrichmentmap;

import java.util.EnumMap;



/**
 * Parameters used for specifying filters and cutoffs for post-analysis.
 */
public class FilterParameters {

    /**
     * Filter type constants shown in the UI.
     */
    public enum FilterType { 
    	NO_FILTER("-- no filter --", 0.0),
    	HYPERGEOM("Hypergeometric Test", 0.05), // MKTODO confirm this default, Ruth opened a bug about this at some point 
    	MANN_WHIT("Mann-Whitney", 0.05), 
    	PERCENT("Overlap X percent of EM gs", 0.25), 
    	NUMBER("Overlap has at least X genes", 5), 
    	SPECIFIC("Overlap X percent of Signature gs", 0.25);
    	
    	public final String display;
    	public final double defaultValue;
    	
    	private FilterType(String display, double defaultValue) { 
    		this.display = display; 
    		this.defaultValue = defaultValue;
    	}
    	
    	public String toString() { 
    		return display; 
    	}
    }
    
    private FilterType type = FilterType.NO_FILTER;
    private final EnumMap<FilterType,Double> values;
    
    
    public FilterParameters() {
    	values = new EnumMap<>(FilterType.class);
    	for(FilterType type : FilterType.values())
    		values.put(type, type.defaultValue);
    }
    
    public FilterParameters(FilterParameters source) {
    	this.type = source.type;
    	this.values = new EnumMap<>(source.values);
    }
    
	public FilterType getType() {
		return type;
	}
	
	public void setType(FilterType type) {
		this.type = type;
	}
	
	public double getValue(FilterType type) {
		return values.get(type);
	}
	
	public void setValue(FilterType type, double value) {
		values.put(type, value);
	}
}
