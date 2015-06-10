package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ConcurrentModificationException;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreatePostAnalysisVisualStyleTask extends AbstractTask {

	private final EnrichmentMap map;
	
	private final CyApplicationManager applicationManager;
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	private final EquationCompiler equationCompiler;
	private final CyEventHelper eventHelper;
	
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    private BuildDiseaseSignatureTaskResult taskResult;
    
	
	public CreatePostAnalysisVisualStyleTask(EnrichmentMap map,
			CyApplicationManager applicationManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			EquationCompiler equationCompiler,
			CyEventHelper eventHelper,
			VisualMappingFunctionFactory vmfFactoryContinuous,
			VisualMappingFunctionFactory vmfFactoryDiscrete,
			VisualMappingFunctionFactory vmfFactoryPassthrough) {
		this.map = map;
		this.applicationManager = applicationManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.equationCompiler = equationCompiler;
		this.eventHelper = eventHelper;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.vmfFactoryPassthrough = vmfFactoryPassthrough;
	}
	
	public void setBuildDiseaseSignatureTaskResult(BuildDiseaseSignatureTaskResult result) {
		this.taskResult = result;
	}


	/**
	 * Note: 
	 * Cytoscape does not provide a way to uniquely identify a visual style.
	 * Here we use the name we previously generated to attempt to identify the visual style.
	 * This is just a heuristic, it is possible the user changed the name.
	 * In that case a new visual style will be generated. 
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
		if(taskResult == null)
			return;
		
		String prefix = map.getParams().getAttributePrefix();
		String vs_name = prefix + PostAnalysisVisualStyle.NAME;
		CyNetworkView view = applicationManager.getCurrentNetworkView();

        PostAnalysisVisualStyle pa_vs = new PostAnalysisVisualStyle(map.getParams(), equationCompiler, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
        
        VisualStyle vs = attemptToGetExistingStyle(vs_name);
		if(vs == null) {
        	vs = visualStyleFactory.createVisualStyle(vs_name);
        	pa_vs.createVisualStyle(vs, prefix);
        	pa_vs.applyNetworkSpeficifProperties(taskResult, prefix);
            visualMappingManager.addVisualStyle(vs);
        }
        else {
        	// update node bypass and edge width equations
        	pa_vs.applyNetworkSpeficifProperties(taskResult, prefix);
        }
        
		eventHelper.flushPayloadEvents(); // view won't update properly without this
		visualMappingManager.setCurrentVisualStyle(vs);
		
		try {
			vs.apply(view);
		} catch(ConcurrentModificationException e) {}
		
        view.updateView();
	}

}
