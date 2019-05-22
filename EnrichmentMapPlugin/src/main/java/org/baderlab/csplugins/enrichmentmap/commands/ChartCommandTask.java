package org.baderlab.csplugins.enrichmentmap.commands;

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

import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

public class ChartCommandTask extends AbstractTask {

	@Inject private ControlPanelMediator controlPanelMediator;
	
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@Tunable(description = "Sets the chart data to show.")
	public ListSingleSelection<String> data;
	
	@Tunable(description = "Sets the chart type.")
	public ListSingleSelection<String> type;
	
	@Tunable(description = "Sets the chart colors.")
	public ListSingleSelection<String> colors;
	
	@Tunable
	public boolean showChartLabels = true;
	
	
	public ChartCommandTask() {
		data   = lssFromEnum(NES_VALUE, P_VALUE, FDR_VALUE, PHENOTYPES, DATA_SET, EXPRESSION_DATA, NONE); // want NES to be the default
		type   = lssFromEnum(RADIAL_HEAT_MAP, HEAT_STRIPS, HEAT_MAP); // don't include DATASET_PIE
		colors = lssFromEnum(ColorScheme.values());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		// validate network
		CyNetworkView networkView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(networkView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		ChartData chartData = ChartData.valueOf(data.getSelectedValue());
		ChartType chartType = chartData == DATA_SET ? DATASET_PIE : ChartType.valueOf(type.getSelectedValue());
		ColorScheme colorScheme = ColorScheme.valueOf(colors.getSelectedValue());
		
		// validate
		if(chartData == ChartData.EXPRESSION_DATA && !networkTunable.isAssociatedEnrichmenMap())
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with a GeneMANIA or STRING network created from an EnrichmentMap network");
		if(chartData == ChartData.EXPRESSION_DATA && !(chartType == ChartType.RADIAL_HEAT_MAP || chartType == ChartType.HEAT_STRIPS))
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with chartType=RADIAL_HEAT_MAP or chartType=HEAT_STRIPS");
		if(chartData == ChartData.PHENOTYPES && !map.isTwoPhenotypeGeneric())
			throw new IllegalArgumentException("data=PHENOTYPES can only be used with two-phenotype generic networks.");
		if(chartData == ChartData.FDR_VALUE && !map.getParams().isFDR())
			throw new IllegalArgumentException("data=FDR_VALUE cannot be used on this network");
		
		ChartOptions options = new ChartOptions(chartData, chartType, colorScheme, showChartLabels);
		
		Map<Long,ViewParams> viewParamsMap = controlPanelMediator.getAllViewParams();
		ViewParams params = viewParamsMap.get(networkView.getSUID());
		params.setChartOptions(options);
		controlPanelMediator.reset(params);
	}

}
