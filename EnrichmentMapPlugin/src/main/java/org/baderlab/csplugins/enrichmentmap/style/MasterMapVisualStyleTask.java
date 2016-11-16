package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Optional;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
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

public class MasterMapVisualStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	
	@Inject private Provider<MasterMapVisualStyle> masterMapVisualStyleProvider;
	
	private final MasterMapStyleOptions options;
	private final CyCustomGraphics2<?> chart;
	
	public interface Factory {
		MasterMapVisualStyleTask create(MasterMapStyleOptions options, CyCustomGraphics2<?> chart);
	}
	
	@Inject
	public MasterMapVisualStyleTask(@Assisted MasterMapStyleOptions options,
			@Assisted @Nullable CyCustomGraphics2<?> chart) {
		this.options = options;
		this.chart = chart;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply Visual Style");
		applyVisualStyle(chart);
//		updateChartValues();
		taskMonitor.setStatusMessage("");
	}
	
//	private void updateChartValues() {
//		List<DataSet> datasets = new ArrayList<>(options.getDataSets());
//		datasets.sort(Comparator.naturalOrder());
//		
//		// For each node (gene-set)
//		CyNetwork network = options.getNetworkView().getModel();
//		CyTable nodeTable = network.getDefaultNodeTable();
//		for(CyRow row : nodeTable.getAllRows()) {
//			String genesetName = row.get(CyNetwork.NAME, String.class);
//			
//			String chartdef =
//				datasets.stream()
//				.map(ds -> ds.getEnrichments().getEnrichments().get(genesetName))
//				.map(this::getScore)
//				.map(Object::toString)
//				.collect(Collectors.joining(", ", getDefaultChartName() + ": ", ""));
//
//			MasterMapVisualStyle.NODE_CHART_VALUES.set(row, chartdef);
//		}
//	}
//	
//	private double getScore(EnrichmentResult result) {
//		if(result == null)
//			return 0.0;
//		
//		double nes;
//		if(result instanceof GSEAResult)
//			nes = ((GSEAResult)result).getNES();
//		else
//			nes = ((GenericResult)result).getNES();
//			
//		if(nes >= 0)
//			return 1 - result.getPvalue();
//		else
//			return (-1) * (1 - result.getPvalue());
//	}
	
	private void applyVisualStyle(CyCustomGraphics2<?> chart) {
		VisualStyle vs = getVisualStyle(options.getEnrichmentMap());
		
		MasterMapVisualStyle masterMapStyle = masterMapVisualStyleProvider.get();
		masterMapStyle.updateProperties(vs, options, chart);
		
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
