package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ConcurrentModificationException;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.PostAnalysisVisualStyle;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class CreatePostAnalysisVisualStyleTask extends AbstractTask {

	private final EnrichmentMap map;

	@Inject private PostAnalysisVisualStyle.Factory paStyleFactory;
	@Inject private CyApplicationManager applicationManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyEventHelper eventHelper;

	private BuildDiseaseSignatureTaskResult taskResult;

	public interface Factory {
		CreatePostAnalysisVisualStyleTask create(EnrichmentMap map);
	}
	
	@Inject
	public CreatePostAnalysisVisualStyleTask(@Assisted EnrichmentMap map) {
		this.map = map;
	}

	public void setBuildDiseaseSignatureTaskResult(BuildDiseaseSignatureTaskResult result) {
		this.taskResult = result;
	}

	/**
	 * Note: Cytoscape does not provide a way to uniquely identify a visual
	 * style. Here we use the name we previously generated to attempt to
	 * identify the visual style. This is just a heuristic, it is possible the
	 * user changed the name. In that case a new visual style will be generated.
	 */
	private VisualStyle attemptToGetExistingStyle(String vs_name) {
		for(VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if(vs.getTitle() != null && vs.getTitle().equals(vs_name)) {
				return vs;
			}
		}
		return null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Create Post-Analysis Visual Style");
		if(taskResult == null)
			return;

		String prefix = map.getParams().getAttributePrefix();
		String vs_name = prefix + PostAnalysisVisualStyle.NAME;
		CyNetworkView view = applicationManager.getCurrentNetworkView();

		PostAnalysisVisualStyle pa_vs = paStyleFactory.create(map.getParams());
		pa_vs.applyNetworkSpeficifProperties(taskResult, prefix, taskMonitor);

		VisualStyle vs = attemptToGetExistingStyle(vs_name);
		if(vs == null) {
			vs = visualStyleFactory.createVisualStyle(vs_name);
			pa_vs.createVisualStyle(vs, prefix);
			visualMappingManager.addVisualStyle(vs);
		}

		visualMappingManager.setCurrentVisualStyle(vs);

		try {
			vs.apply(view);
		} catch(ConcurrentModificationException e) {
		}

		eventHelper.flushPayloadEvents(); // view won't update properly without this

		view.updateView();
	}

}
