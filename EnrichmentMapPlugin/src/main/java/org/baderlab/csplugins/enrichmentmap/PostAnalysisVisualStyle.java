package org.baderlab.csplugins.enrichmentmap;

import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;

public class PostAnalysisVisualStyle {

	private final VisualMappingFunctionFactory vmfFactoryContinuous;
    private final VisualMappingFunctionFactory vmfFactoryDiscrete;
    private final VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    
	private final EnrichmentMapVisualStyle delegateStyle;
	
	public PostAnalysisVisualStyle(PostAnalysisParameters paParams, EnrichmentMapParameters emParsms, VisualMappingFunctionFactory vmfFactoryContinuous, 
			                       VisualMappingFunctionFactory vmfFactoryDiscrete, VisualMappingFunctionFactory vmfFactoryPassthrough) {
		
		this.delegateStyle = new EnrichmentMapVisualStyle(emParsms, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough);
		
		this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;   
	}
	
	
	public void applyVisualStyle(VisualStyle vs, String prefix) {
		delegateStyle.applyVisualStyle(vs, prefix);
		
		createPostAnalysisAppearance(vs, prefix);
	}


	// First figure out how to handle the bypasses, and get rid of the flickering
	// Then get rid of the extra columns that you added before... or create the columns here if you have to
	// Then compute the formula stuff
	private void createPostAnalysisAppearance(VisualStyle vs, String prefix) {
		
	}
}
