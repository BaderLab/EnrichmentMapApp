package org.baderlab.csplugins.enrichmentmap.task;

import java.awt.Color;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.style.MasterMapStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class TogglePublicationVisualStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyEventHelper eventHelper;
	@Inject private MasterMapVisualStyleTask.Factory visualStyleTaskFactory;
	@Inject private MasterMapVisualStyle masterMapVisualStyle;
	
	private final MasterMapStyleOptions options;
	private final CyCustomGraphics2<?> chart;
	
	public interface Factory {
		TogglePublicationVisualStyleTask create(MasterMapStyleOptions options, CyCustomGraphics2<?> chart);
	}
	
	@Inject
	public TogglePublicationVisualStyleTask(
			@Assisted MasterMapStyleOptions options,
			@Assisted @Nullable CyCustomGraphics2<?> chart
	) {
		this.options = options;
		this.chart = chart;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Toggle Publication-Ready Visual Style");

		VisualStyle currentStyle = visualMappingManager.getVisualStyle(options.getNetworkView());
		
		if (currentStyle == null)
			return;
		
		String currentTitle = currentStyle.getTitle();

		if (MasterMapVisualStyle.isPublicationReady(currentTitle)) {
			// If the current style is the publication one, then attempt to switch back...
			MasterMapVisualStyleTask task = visualStyleTaskFactory.create(options, chart);
			insertTasksAfterCurrentTask(task);
		} else {
			// If not, set the publication-ready style...
			String title = currentTitle + MasterMapVisualStyle.PUBLICATION_SUFFIX;
			VisualStyle style = getStyle(title);
			
			if (style == null) {
				// create a copy of the current style
				style = visualStyleFactory.createVisualStyle(currentStyle);
				style.setTitle(title);

				visualMappingManager.addVisualStyle(style);
			} else {
				masterMapVisualStyle.updateProperties(style, options, chart);
			}
			
			// Always reset these properties:
			style.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
			style.setDefaultValue(BasicVisualLexicon.NODE_LABEL, "");
			style.removeVisualMappingFunction(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
			style.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);
			
			visualMappingManager.setVisualStyle(style, options.getNetworkView());
			eventHelper.flushPayloadEvents(); // view won't update properly without this
		}
	}
	
	private VisualStyle getStyle(String title) {
		for (VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if (vs.getTitle() != null && vs.getTitle().equals(title))
				return vs;
		}
		
		return null;
	}
}
