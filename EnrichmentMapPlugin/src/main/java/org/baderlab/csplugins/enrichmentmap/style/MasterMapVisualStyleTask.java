package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class MasterMapVisualStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	
	@Inject private Provider<MasterMapVisualStyle> masterMapVisualStyleProvider;
	
	private final MasterMapStyleOptions options;
	
	
	public interface Factory {
		MasterMapVisualStyleTask create(MasterMapStyleOptions options);
	}
	
	@Inject
	public MasterMapVisualStyleTask(@Assisted MasterMapStyleOptions options) {
		this.options = options;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply Visual Style");
		applyVisualStyle();
		taskMonitor.setStatusMessage("");
	}

	
	private void applyVisualStyle() {
		VisualStyle vs = getVisualStyle(options.getEnrichmentMap());
		
		MasterMapVisualStyle masterMapStyle = masterMapVisualStyleProvider.get();
		masterMapStyle.applyVisualStyle(vs, options);
		
		CyNetworkView view = options.getNetworkView();
		vs.apply(view);
		view.updateView();
	}
	
	
	private VisualStyle getVisualStyle(EnrichmentMap map) {
		String prefix = map.getParams().getAttributePrefix();
		String vsName = prefix + MasterMapVisualStyle.DEFAULT_NAME_SUFFIX;
		
		Optional<VisualStyle> currentStyle = attemptToGetExistingStyle(vsName);
		
		if(currentStyle.isPresent()) {
			return currentStyle.get();
		} else {
			VisualStyle vs = visualStyleFactory.createVisualStyle(vsName);
			visualMappingManager.addVisualStyle(vs);
			visualMappingManager.setCurrentVisualStyle(vs);
			return vs;
		}
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
}
