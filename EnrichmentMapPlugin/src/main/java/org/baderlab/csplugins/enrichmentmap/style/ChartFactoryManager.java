package org.baderlab.csplugins.enrichmentmap.style;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

import com.google.inject.Singleton;

@Singleton
public class ChartFactoryManager {

	private Map<String, CyCustomGraphics2Factory<?>> factories = new HashMap<>();

	public void addFactory(CyCustomGraphics2Factory<?> factory, Map<Object, Object> serviceProps) {
		factories.put(factory.getId(), factory);
	}

	public void removeFactory(CyCustomGraphics2Factory<?> factory, Map<Object, Object> serviceProps) {
		factories.remove(factory.getId());
	}

	public CyCustomGraphics2Factory<?> getChartFactory(final String id) {
		return factories.get(id);
	}
}
