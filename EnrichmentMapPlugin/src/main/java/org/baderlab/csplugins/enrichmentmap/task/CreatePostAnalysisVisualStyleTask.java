package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreatePostAnalysisVisualStyleTask extends AbstractTask {

	private final EnrichmentMap map;
	private final PostAnalysisParameters paParams;
	
	private final CyApplicationManager applicationManager;
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	private final EquationCompiler equationCompiler;
	
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    private BuildDiseaseSignatureTaskResult taskResult;
    
	
	public CreatePostAnalysisVisualStyleTask(EnrichmentMap map,
			PostAnalysisParameters paParams,
			CyApplicationManager applicationManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			EquationCompiler equationCompiler,
			VisualMappingFunctionFactory vmfFactoryContinuous,
			VisualMappingFunctionFactory vmfFactoryDiscrete,
			VisualMappingFunctionFactory vmfFactoryPassthrough) {
		this.map = map;
		this.paParams = paParams;
		
		this.applicationManager = applicationManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.equationCompiler = equationCompiler;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.vmfFactoryPassthrough = vmfFactoryPassthrough;
	}
	
	public void setBuildDiseaseSignatureTaskResult(BuildDiseaseSignatureTaskResult result) {
		this.taskResult = result;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(taskResult == null) {
			return;
		}
		
		String prefix = map.getParams().getAttributePrefix();
		String vs_name = prefix + "Post_analysis_style";
		CyNetworkView view = applicationManager.getCurrentNetworkView();

        PostAnalysisVisualStyle em_vs = new PostAnalysisVisualStyle(paParams, map.getParams(), equationCompiler, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
        VisualStyle vs = visualStyleFactory.createVisualStyle(vs_name);
        em_vs.applyVisualStyle(taskResult, vs, prefix);                
        
        visualMappingManager.addVisualStyle(vs);
        visualMappingManager.setCurrentVisualStyle(vs);
        
        vs.apply(view);
        view.updateView();
	}

}
