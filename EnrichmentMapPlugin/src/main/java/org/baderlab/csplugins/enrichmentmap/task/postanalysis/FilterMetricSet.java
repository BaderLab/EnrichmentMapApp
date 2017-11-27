package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;

/**
 * Each data set may be filtered using a different universe size in the case of hypergeometric
 * or a different rank file in the case of mann-whitney. However all the datasets must use
 * the same type of filter. 
 * 
 * @author mkucera
 *
 */
public class FilterMetricSet {

	private final Map<String,FilterMetric> metrics = new HashMap<>();
	
	private final PostAnalysisFilterType type;
	
	public FilterMetricSet(PostAnalysisFilterType type) {
		this.type = Objects.requireNonNull(type);
	}

	public PostAnalysisFilterType getType() {
		return type;
	}
	
	public void put(String dataSetName, FilterMetric metric) {
		if(metric.getFilterType() != type)
			throw new IllegalArgumentException("Wrong PostAnalysisFilterType");
		metrics.put(dataSetName, metric);
	}
	
	public FilterMetric get(String dataSetName) {
		return metrics.get(dataSetName);
	}
	
	public Collection<String> getDataSetNames() {
		return metrics.keySet();
	}
}
