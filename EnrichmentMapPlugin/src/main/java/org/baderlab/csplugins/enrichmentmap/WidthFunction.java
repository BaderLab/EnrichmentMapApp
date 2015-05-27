package org.baderlab.csplugins.enrichmentmap;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle.EdgeWidthParams;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

public class WidthFunction extends AbstractFunction {
	
	public static final String NAME = "EM_width";
	
	private final CyNetworkManager networkManager;
	private final EnrichmentMapManager enrichmentMapManager;
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	
	
	public WidthFunction(CyNetworkManager networkManager, VisualMappingFunctionFactory vmfFactoryContinuous, EnrichmentMapManager enrichmentMapManager) {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "SUID", "The SUID for the current edge row.") });
		this.networkManager = networkManager;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.enrichmentMapManager = enrichmentMapManager;
	}

	public String getName() { 
		return NAME; 
	}
 
	public String getFunctionSummary() { 
		return "Calculate edge width for EnrichmentMap networks. (Automatically created)"; 
	}

	public Class<Double> getReturnType() { 
		return Double.class; 
	}

	
	/**
	 * Return the CyNetwork that contains the given edge SUID.
	 */
	private CyNetwork getNetwork(long edgeSUID) {
		for(CyNetwork network : networkManager.getNetworkSet()) {
			if(network.getEdge(edgeSUID) != null) {
				return network;
			}
		}
		return null;
	}
	
	
	private static boolean isSignature(String interaction) {
		return PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE.equals(interaction)
		    || PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET1.equals(interaction)
		    || PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2.equals(interaction);
	}
	
	
	public Double evaluateFunction(final Object[] args) {
		long edgeSuid = (Long)args[0];
		CyNetwork network = getNetwork(edgeSuid);
		EdgeWidthParams params = EdgeWidthParams.restore(network);
		CyRow row = network.getDefaultEdgeTable().getRow(edgeSuid);
		EnrichmentMap map = enrichmentMapManager.getMap(network.getSUID());
		String prefix = map.getParams().getAttributePrefix();
		
		String interaction = row.get(CyEdge.INTERACTION, String.class);
		
		if(isSignature(interaction)) {
			String cutoffType = row.get(prefix + EnrichmentMapVisualStyle.CUTOFF_TYPE, String.class);
			FilterMetric filterMetric = fromString(cutoffType);
			if(filterMetric == null) {
				return 1.0;
			}
			
			if(filterMetric == FilterMetric.HYPERGEOM || filterMetric == FilterMetric.MANN_WHIT) {
				Double pvalue, cutoff;
				if(filterMetric == FilterMetric.HYPERGEOM) {
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.HYPERGEOM_CUTOFF, Double.class);
				}
				else {
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_CUTOFF, Double.class);
				}
				
				if(pvalue == null || cutoff == null)
					return 1.0;
				if(pvalue <= cutoff/100)
					return params.pa_lessThan100;
				else
					if(pvalue <= cutoff/10)
						return params.pa_lessThan10;
					else
						return params.pa_greater;
			}
		} 
		else {
			// Can use a continuous mapping object to perform calculation even though it won't be added to the visual style.
	        ContinuousMapping<Double,Double> conmapping_edgewidth = (ContinuousMapping<Double,Double>) vmfFactoryContinuous.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, Double.class, BasicVisualLexicon.EDGE_WIDTH);
	                
	        Double under_width = 0.5;
	        Double min_width = params.em_lower;
	        Double max_width = params.em_upper;
	        Double over_width = 6.0;

	        // Create boundary conditions                  less than,   equals,  greater than
	        BoundaryRangeValues<Double> bv4 = new BoundaryRangeValues<Double>(under_width, min_width, min_width);
	        BoundaryRangeValues<Double> bv5 = new BoundaryRangeValues<Double>(max_width, max_width, over_width);
	        conmapping_edgewidth.addPoint(map.getParams().getSimilarityCutOff(), bv4);
	        conmapping_edgewidth.addPoint(1.0, bv5);
	        
	        return conmapping_edgewidth.getMappedValue(row);
		}
		
		return 1.0;
	}
	
	
	private static FilterMetric fromString(String val) {
		for(FilterMetric metric : FilterMetric.values()) {
			if(metric.toString().equals(val)) {
				return metric;
			}
		}
		return null;
	}
	
}