package org.baderlab.csplugins.enrichmentmap.task;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class ApplyEMStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	
	@Inject private Provider<EMStyleBuilder> styleBuilderProvider;

	private final EMStyleOptions options;
	private final CyCustomGraphics2<?> chart;
	private final boolean updateChartOnly;

	public interface Factory {
		ApplyEMStyleTask create(EMStyleOptions options, CyCustomGraphics2<?> chart, boolean updateChartOnly);
	}

	@Inject
	public ApplyEMStyleTask(@Assisted EMStyleOptions options, @Assisted @Nullable CyCustomGraphics2<?> chart,
			@Assisted boolean updateChartOnly) {
		this.options = options;
		this.chart = chart;
		this.updateChartOnly = updateChartOnly;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply Visual Style");
		applyVisualStyle();
		taskMonitor.setStatusMessage("");
	}

	private void applyVisualStyle() {
		CyNetworkView view = options.getNetworkView();
		VisualStyle vs = getVisualStyle(options.getEnrichmentMap());
		
		if (!vs.equals(visualMappingManager.getVisualStyle(view)))
			visualMappingManager.setVisualStyle(vs, view);
		
		if (updateChartOnly)
			styleBuilderProvider.get().updateNodeChart(vs, options, chart);
		else
			styleBuilderProvider.get().updateProperties(vs, options, chart);
	}

	private VisualStyle getVisualStyle(EnrichmentMap map) {
		String vsName = EMStyleBuilder.getStyleName(map);
		VisualStyle vs = getExistingVisualStyle(vsName);

		if (vs == null) {
			vs = visualStyleFactory.createVisualStyle(vsName);
			visualMappingManager.addVisualStyle(vs);
		}
		
		return vs;
	}

	/**
	 * Note: Cytoscape does not provide a way to uniquely identify a visual
	 * style. Here we use the name we previously generated to attempt to
	 * identify the visual style. This is just a heuristic, it is possible the
	 * user changed the name. In that case a new visual style will be generated.
	 */
	private VisualStyle getExistingVisualStyle(String name) {
		for (VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if (vs.getTitle() != null && vs.getTitle().equals(name))
				return vs;
		}
		
		return null;
	}
}
