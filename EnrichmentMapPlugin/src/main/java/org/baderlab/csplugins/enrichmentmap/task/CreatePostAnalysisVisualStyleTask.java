package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ConcurrentModificationException;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.LegacyPostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@Deprecated
public class CreatePostAnalysisVisualStyleTask extends AbstractTask {

	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyEventHelper eventHelper;
	@Inject private @Passthrough VisualMappingFunctionFactory vmfFactoryPassthrough;

	@Inject private LegacyPostAnalysisVisualStyle.Factory paStyleFactory;
	@Inject private Provider<WidthFunction> widthFunctionProvider;
	
	private final EnrichmentMap map;
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
	private Optional<VisualStyle> attemptToGetExistingStyle(String name) {
		for(VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if(vs.getTitle() != null && vs.getTitle().equals(name)) {
				return Optional.of(vs);
			}
		}
		return Optional.empty();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Create Post-Analysis Visual Style");
		if(taskResult == null)
			return;
		
		if(map.isLegacy())
			applyLegacyVisualStyle(taskMonitor);
		else
			applyVisualStyle(taskMonitor);
	}
		
	private void applyVisualStyle(TaskMonitor taskMonitor) {
		String prefix = map.getParams().getAttributePrefix();
		String vsName = EMStyleBuilder.getStyleName(map);
		
		Optional<VisualStyle> currentStyle = attemptToGetExistingStyle(vsName);
		if(currentStyle.isPresent()) {
			CyNetwork network = networkManager.getNetwork(map.getNetworkID());
			VisualStyle vs = currentStyle.get();
			WidthFunction widthFunction = widthFunctionProvider.get();
			widthFunction.setEdgeWidths(network, prefix, taskMonitor);
			LegacyPostAnalysisVisualStyle.useFormulaForEdgeWidth(vs, prefix, vmfFactoryPassthrough);
		}
		
		// TODO is there a better way to specify the PA node color?
//		LegacyPostAnalysisVisualStyle.createNodeBypassForColor(taskResult);
		
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		eventHelper.flushPayloadEvents(); // view won't update properly without this
		view.updateView();
	}
	
	private void applyLegacyVisualStyle(TaskMonitor taskMonitor) {
		String prefix = map.getParams().getAttributePrefix();
		String title = prefix + LegacyPostAnalysisVisualStyle.NAME;
		CyNetworkView view = applicationManager.getCurrentNetworkView();

		LegacyPostAnalysisVisualStyle pa_vs = paStyleFactory.create(map);
		pa_vs.applyNetworkSpeficifProperties(taskResult, prefix, taskMonitor);

		Optional<VisualStyle> currentStyle = attemptToGetExistingStyle(title);
		final VisualStyle vs;
		
		if (currentStyle.isPresent()) {
			vs = currentStyle.get();
		} else {
			vs = visualStyleFactory.createVisualStyle(title);
			pa_vs.createVisualStyle(vs, prefix);
			visualMappingManager.addVisualStyle(vs);
		}

		visualMappingManager.setCurrentVisualStyle(vs);

		try {
			vs.apply(view);
		} catch (ConcurrentModificationException e) {
		}

		eventHelper.flushPayloadEvents(); // view won't update properly without this
		view.updateView();
	}
}
