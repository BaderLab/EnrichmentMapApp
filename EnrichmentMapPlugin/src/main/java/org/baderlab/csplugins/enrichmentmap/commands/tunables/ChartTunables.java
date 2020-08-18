package org.baderlab.csplugins.enrichmentmap.commands.tunables;

import static org.baderlab.csplugins.enrichmentmap.commands.tunables.CommandUtil.lssFromEnum;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.DATA_SET;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.EXPRESSION_DATA;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.FDR_VALUE;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.NES_VALUE;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.NONE;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.PHENOTYPES;
import static org.baderlab.csplugins.enrichmentmap.style.ChartData.P_VALUE;
import static org.baderlab.csplugins.enrichmentmap.style.ChartType.DATASET_PIE;
import static org.baderlab.csplugins.enrichmentmap.style.ChartType.HEAT_MAP;
import static org.baderlab.csplugins.enrichmentmap.style.ChartType.HEAT_STRIPS;
import static org.baderlab.csplugins.enrichmentmap.style.ChartType.RADIAL_HEAT_MAP;

import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ChartTunables {

	@Tunable(description = "Sets the chart data to show.")
	public ListSingleSelection<String> data;
	
	@Tunable(description = "Sets the chart type.")
	public ListSingleSelection<String> type;
	
	@Tunable(description = "Sets the chart colors.")
	public ListSingleSelection<String> colors;
	
	@Tunable
	public boolean showChartLabels = true;
	
	
	public ChartTunables() {
		data   = lssFromEnum(NES_VALUE, P_VALUE, FDR_VALUE, PHENOTYPES, DATA_SET, EXPRESSION_DATA, NONE); // want NES to be the default
		type   = lssFromEnum(RADIAL_HEAT_MAP, HEAT_STRIPS, HEAT_MAP); // don't include DATASET_PIE
		colors = lssFromEnum(ColorScheme.values());
	}
	
	
	public ChartData getChartData() {
		 return ChartData.valueOf(data.getSelectedValue());
	}
	
	public ChartType getChartType() {
		return getChartData() == DATA_SET ? DATASET_PIE : ChartType.valueOf(type.getSelectedValue());
	}
	
	public ColorScheme getColorScheme() {
		return ColorScheme.valueOf(colors.getSelectedValue());
	}
	
	public boolean showChartLabels() {
		return showChartLabels;
	}
}
