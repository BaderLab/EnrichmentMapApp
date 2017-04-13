package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.view.model.CyNetworkView;

public class EMStyleOptions {
	
	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final Predicate<AbstractDataSet> filter;
	private final ChartOptions chartOptions;
	private final boolean postAnalysis;
	private final boolean publicationReady;
	
	/**
	 * It is assumed that all the given DataSets come from the same EnrichmentMap.
	 */
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map, Predicate<AbstractDataSet> filter,
			ChartOptions chartOptions, boolean postAnalysis, boolean publicationReady) {
		this.networkView = networkView;
		this.map = map;
		this.filter = filter;
		this.chartOptions = chartOptions;
		this.postAnalysis = postAnalysis;
		this.publicationReady = publicationReady;
	}
	
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map) {
		this(networkView, map, x -> true, null, false, false);
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public Collection<AbstractDataSet> getDataSets() {
		return map.getDataSetList().stream().filter(filter).collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public String getAttributePrefix() {
		return map.getParams().getAttributePrefix();
	}
	
	public ChartOptions getChartOptions() {
		return chartOptions;
	}
	
	public boolean isPublicationReady() {
		return publicationReady;
	}

	public boolean isPostAnalysis() {
		return postAnalysis;
	}
	
	public static class ChartOptions {
		
		private final ChartData data;
		private final ChartType type;
		private final ColorScheme colorScheme;
		private final boolean showLabels;
		
		public ChartOptions(ChartData data, ChartType type, ColorScheme colorScheme, boolean showLabels) {
			this.data = data;
			this.type = type;
			this.colorScheme = colorScheme;
			this.showLabels = showLabels;
		}

		public ChartData getData() {
			return data;
		}

		public ChartType getType() {
			return type;
		}

		public ColorScheme getColorScheme() {
			return colorScheme;
		}

		public boolean isShowLabels() {
			return showLabels;
		}
	}
}
