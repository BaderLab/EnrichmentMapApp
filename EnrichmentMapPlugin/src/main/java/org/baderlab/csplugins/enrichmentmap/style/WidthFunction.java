package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.FilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class WidthFunction {
	
	public static final double DEFAULT_WIDTH_EM_LOWER = 1.0;
	public static final double DEFAULT_WIDTH_EM_UPPER = 5.0;
	public static final double DEFAULT_WIDTH_PA_LESS_THAN_100 = 8.0;
	public static final double DEFAULT_WIDTH_PA_LESS_THAN_10 = 4.5;
	public static final double DEFAULT_WIDTH_PA_GREATER = 1.0;
	
	
	// Column in edge table that holds the formula
	public static final String EDGE_WIDTH_FORMULA_COLUMN = "Edge_width_formula";
	// Column in network table that holds the edge parameters
	public static final String EDGE_WIDTH_PARAMETERS_COLUMN = "EM_Edge_width_parameters";
	
	
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	private final EnrichmentMapManager emManager;
	
	@Inject
	public WidthFunction(@Continuous VisualMappingFunctionFactory vmfFactoryContinuous, EnrichmentMapManager emManager) {
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.emManager = emManager;
	}

	private static boolean isSignature(String interaction) {
		return PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE.equals(interaction)
		    || PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET1.equals(interaction)
		    || PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE_SET2.equals(interaction);
	}
	
	public static boolean appliesTo(CyNetwork network) {
		CyTable networkTable = network.getDefaultNetworkTable();
	    return networkTable.getColumn(EDGE_WIDTH_PARAMETERS_COLUMN) != null;
	}
	
	
	public void setEdgeWidths(CyNetwork network, String prefix, TaskMonitor taskMonitor) {
		createColumns(network, prefix);
		calculateAndSetEdgeWidths(network, prefix, taskMonitor);
	}
	
	
	private void createColumns(CyNetwork network, String prefix) {
		CyTable networkTable = network.getDefaultNetworkTable();
		if(networkTable.getColumn(EDGE_WIDTH_PARAMETERS_COLUMN) == null) {
			networkTable.createColumn(EDGE_WIDTH_PARAMETERS_COLUMN, String.class, false);
		}

		String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
		CyTable edgeTable = network.getDefaultEdgeTable();
		if(edgeTable.getColumn(widthAttribute) == null) {
			edgeTable.createColumn(widthAttribute, Double.class, false);
		}
	}
	
	private void calculateAndSetEdgeWidths(CyNetwork network, String prefix, TaskMonitor taskMonitor) {
		EdgeWidthParams edgeWidthParams = EdgeWidthParams.restore(network);
		EnrichmentMap map = emManager.getMap(network.getSUID());
		String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
		
		int n = network.getDefaultEdgeTable().getRowCount();
		int i = 0;
		
		for(CyRow row : network.getDefaultEdgeTable().getAllRows()) {
			if(taskMonitor != null) {
				taskMonitor.setProgress((double)i/(double)n);
			}
			i++;
			
			String interaction = row.get(CyEdge.INTERACTION, String.class);
			
			if(isSignature(interaction)) {
				String cutoffType = row.get(prefix + EnrichmentMapVisualStyle.CUTOFF_TYPE, String.class);
				FilterType filterType = FilterType.fromDisplayString(cutoffType);
				if(filterType == null) {
					row.set(widthAttribute, null);
					continue;
				}
				
				Double pvalue, cutoff;
				switch(filterType) {
				case MANN_WHIT_TWO_SIDED:
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_TWOSIDED_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_CUTOFF, Double.class);
					break;
				case MANN_WHIT_GREATER:
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_GREATER_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_CUTOFF, Double.class);
					break;
				case MANN_WHIT_LESS:
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_LESS_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.MANN_WHIT_CUTOFF, Double.class);
					break;
				default:
					pvalue = row.get(prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE, Double.class);
					cutoff = row.get(prefix + EnrichmentMapVisualStyle.HYPERGEOM_CUTOFF, Double.class);
					break;
				}
				
				if(pvalue == null || cutoff == null) {
					row.set(widthAttribute, null);
				}
				else if(pvalue <= cutoff/100) {
					row.set(widthAttribute, edgeWidthParams.pa_lessThan100);
				}
				else if(pvalue <= cutoff/10) {
					row.set(widthAttribute, edgeWidthParams.pa_lessThan10);
				}
				else {
					row.set(widthAttribute, edgeWidthParams.pa_greater);
				}
				
			} 
			else {
				// Can use a continuous mapping object to perform calculation even though it won't be added to the visual style.
				ContinuousMapping<Double,Double> conmapping_edgewidth = (ContinuousMapping<Double,Double>) vmfFactoryContinuous.createVisualMappingFunction(prefix + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT, Double.class, BasicVisualLexicon.EDGE_WIDTH);
	
				Double under_width = 0.5;
				Double min_width = edgeWidthParams.em_lower;
				Double max_width = edgeWidthParams.em_upper;
				Double over_width = 6.0;
	
				// Create boundary conditions                  less than,   equals,  greater than
				BoundaryRangeValues<Double> bv4 = new BoundaryRangeValues<Double>(under_width, min_width, min_width);
				BoundaryRangeValues<Double> bv5 = new BoundaryRangeValues<Double>(max_width, max_width, over_width);
				conmapping_edgewidth.addPoint(map.getParams().getSimilarityCutOff(), bv4);
				conmapping_edgewidth.addPoint(1.0, bv5);
				
				Double value = conmapping_edgewidth.getMappedValue(row);
				row.set(widthAttribute, value);
			}
		}
	}
	
	
	/**
	 * Parameters typically used by the EdgeWidthDialog and stored in the network table.
	 */
	public static class EdgeWidthParams {
		public final double em_lower;
		public final double em_upper;
		public final double pa_lessThan100;
		public final double pa_lessThan10;
		public final double pa_greater;
		
		public EdgeWidthParams(double em_lower, double em_upper, double pa_lessThan100, double pa_lessThan10, double pa_greater) {
			this.em_lower = em_lower;
			this.em_upper = em_upper;
			this.pa_lessThan100 = pa_lessThan100;
			this.pa_lessThan10 = pa_lessThan10;
			this.pa_greater = pa_greater;
		}
		
		public static EdgeWidthParams defaultValues() {
			return new EdgeWidthParams(DEFAULT_WIDTH_EM_LOWER, DEFAULT_WIDTH_EM_UPPER,
					                   DEFAULT_WIDTH_PA_LESS_THAN_100, DEFAULT_WIDTH_PA_LESS_THAN_10, DEFAULT_WIDTH_PA_GREATER);
		}
		
		public static EdgeWidthParams restore(CyNetwork network) {
			try {
				String val = network.getRow(network).get(EDGE_WIDTH_PARAMETERS_COLUMN, String.class);
				String[] params = val.split(",");
				double em_lower = Double.parseDouble(params[0]);
				double em_upper = Double.parseDouble(params[1]);
				double pa_lessThan100 = Double.parseDouble(params[2]);
				double pa_lessThan10 = Double.parseDouble(params[3]);
				double pa_greater = Double.parseDouble(params[4]);
				return new EdgeWidthParams(em_lower, em_upper, pa_lessThan100, pa_lessThan10, pa_greater);
			} catch(NullPointerException | ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
				return defaultValues();
			}
		}
		
		public void save(CyNetwork network) {
			CyRow row = network.getRow(network);
			String val = String.format("%f,%f,%f,%f,%f", em_lower, em_upper, pa_lessThan100, pa_lessThan10, pa_greater);
			row.set(EDGE_WIDTH_PARAMETERS_COLUMN, val);
		}
	}
	
	
}