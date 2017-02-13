package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
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
	public static final ColumnDescriptor<Double> EDGE_WIDTH_FORMULA_COLUMN = new ColumnDescriptor<>("Edge_width_formula", Double.class);
	// Column in network table that holds the edge parameters
	public static final ColumnDescriptor<String> NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN = new ColumnDescriptor<>("EM_Edge_width_parameters", String.class);
	
	
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
	    return networkTable.getColumn(NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN.with(null,null)) != null;
	}
	
	
	public void setEdgeWidths(CyNetwork network, String prefix, TaskMonitor taskMonitor) {
		createColumns(network, prefix);
		calculateAndSetEdgeWidths(network, prefix, taskMonitor);
	}
	
	
	private void createColumns(CyNetwork network, String prefix) {
		CyTable networkTable = network.getDefaultNetworkTable();
		NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN.createColumnIfAbsent(networkTable, null, null);
		CyTable edgeTable = network.getDefaultEdgeTable();
		EDGE_WIDTH_FORMULA_COLUMN.createColumnIfAbsent(edgeTable, prefix, null);
	}
	
	private void calculateAndSetEdgeWidths(CyNetwork network, String prefix, TaskMonitor taskMonitor) {
		EdgeWidthParams edgeWidthParams = EdgeWidthParams.restore(network);
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
//		String widthAttribute = prefix + EDGE_WIDTH_FORMULA_COLUMN;
		
		int n = network.getDefaultEdgeTable().getRowCount();
		int i = 0;
		
		for(CyRow row : network.getDefaultEdgeTable().getAllRows()) {
			if(taskMonitor != null) {
				taskMonitor.setProgress((double)i/(double)n);
			}
			i++;
			
			String interaction = row.get(CyEdge.INTERACTION, String.class);
			
			if(isSignature(interaction)) {
				String cutoffType = Columns.EDGE_CUTOFF_TYPE.get(row, prefix, null);
				PostAnalysisFilterType filterType = PostAnalysisFilterType.fromDisplayString(cutoffType);
				if(filterType == null) {
					EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, null, null);
					continue;
				}
				
				Double pvalue, cutoff;
				switch(filterType) {
				case MANN_WHIT_TWO_SIDED:
					pvalue = Columns.EDGE_MANN_WHIT_TWOSIDED_PVALUE.get(row, prefix);
					cutoff = Columns.EDGE_MANN_WHIT_CUTOFF.get(row, prefix); 
					break;
				case MANN_WHIT_GREATER:
					pvalue = Columns.EDGE_MANN_WHIT_GREATER_PVALUE.get(row, prefix);
					cutoff = Columns.EDGE_MANN_WHIT_CUTOFF.get(row, prefix); 
					break;
				case MANN_WHIT_LESS:
					pvalue = Columns.EDGE_MANN_WHIT_LESS_PVALUE.get(row, prefix);
					cutoff = Columns.EDGE_MANN_WHIT_CUTOFF.get(row, prefix); 
					break;
				default:
					pvalue = Columns.EDGE_HYPERGEOM_PVALUE.get(row, prefix);
					cutoff = Columns.EDGE_HYPERGEOM_CUTOFF.get(row, prefix); 
					break;
				}
				
				if(pvalue == null || cutoff == null) {
					EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, null);
				}
				else if(pvalue <= cutoff/100) {
					EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, edgeWidthParams.pa_lessThan100);
				}
				else if(pvalue <= cutoff/10) {
					EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, edgeWidthParams.pa_lessThan10);
				}
				else {
					EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, edgeWidthParams.pa_greater);
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
				conmapping_edgewidth.addPoint(map.getParams().getSimilarityCutoff(), bv4);
				conmapping_edgewidth.addPoint(1.0, bv5);
				
				Double value = conmapping_edgewidth.getMappedValue(row);
				EDGE_WIDTH_FORMULA_COLUMN.set(row, prefix, value);
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
				String val = NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN.get(network.getRow(network), null);
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
			NETWORK_EDGE_WIDTH_PARAMETERS_COLUMN.set(row, null, val);
		}
	}
	
	
}