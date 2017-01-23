package org.baderlab.csplugins.enrichmentmap.style;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ChartType {
	NONE(
			"org.baderlab.csplugins.NullChart",
			"-- None --",
			Collections.emptyMap()
	),
//	HEAT_MAP(
//			"org.cytoscape.HeatMapChart",
//			"Heat Map",
//			Collections.unmodifiableMap(Stream.of(
//	                new SimpleEntry<>("cy_orientation", "VERTICAL"),
//	                new SimpleEntry<>("cy_showDomainAxis", false),
//	                new SimpleEntry<>("cy_showRangeAxis", false))
//	                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))
//	),
	RING(
			"org.cytoscape.RingChart",
			"Ring",
			Collections.unmodifiableMap(Stream.of(
	                new SimpleEntry<>("cy_borderWidth", 0.0f))
	                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))
	),
	HEAT_STRIPS(
			"org.cytoscape.BarChart",
			"Heat Strips",
			Collections.unmodifiableMap(Stream.of(
					new SimpleEntry<>("cy_type", "HEAT_STRIPS"),
					new SimpleEntry<>("cy_orientation", "VERTICAL"),
					new SimpleEntry<>("cy_showDomainAxis", false),
					new SimpleEntry<>("cy_showRangeAxis", false))
					.collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))
	),
	LINE(
			"org.cytoscape.LineChart",
			"Line",
			Collections.unmodifiableMap(Stream.of(
	                new SimpleEntry<>("cy_lineWidth", 2.0f),
	                new SimpleEntry<>("cy_showDomainAxis", false),
					new SimpleEntry<>("cy_showRangeAxis", false))
	                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())))
	);
	
	private final String id;
	private final String label;
	private final Map<String, Object> properties;

	private ChartType(final String id, final String label, Map<String, Object> properties) {
		this.id = id;
		this.label = label;
		this.properties = properties;
	}
	
	public String getId() {
		return id;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
