package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Map;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;

import com.google.inject.Singleton;

@Singleton
public class CustomChartListener {

	private static final String FACTORY_ID = "org.cytoscape.BarChart";
	private CyCustomGraphics2Factory<?> factory;

	public void addFactory(CyCustomGraphics2Factory<?> factory, Map<Object, Object> serviceProps) {
		if (FACTORY_ID.equals(factory.getId())) {
			this.factory = factory;
		}
	}

	public void removeFactory(CyCustomGraphics2Factory<?> factory, Map<Object, Object> serviceProps) {
		this.factory = null;
	}

	public CyCustomGraphics2Factory<?> getFactory() {
		return factory;
	}
}
