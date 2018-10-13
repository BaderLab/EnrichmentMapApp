package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

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
	
	@Tunable
	public ListSingleSelection<String> data;
	
	@Tunable
	public ListSingleSelection<String> type;
	
	@Tunable
	public ListSingleSelection<String> colors;
	
	@Tunable
	public boolean showLabels;
	
	
	public ChartCommandTask() {
		data   = lssFromEnum(ChartData.values(), x -> true);
		type   = lssFromEnum(ChartType.values(), ct -> ct != ChartType.DATASET_PIE);
		colors = lssFromEnum(ColorScheme.values(), x -> true);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		// validate network
		CyNetworkView networkView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(networkView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		Map<Long,ViewParams> viewParamsMap = controlPanelMediator.getAllViewParams();
		ViewParams params = viewParamsMap.get(networkView.getSUID());
		ChartOptions chartOptions = params.getChartOptions();
		
		ChartData chartData = "null".equals(data.getSelectedValue()) ? chartOptions.getData() : ChartData.valueOf(data.getSelectedValue());
		ChartType chartType = "null".equals(type.getSelectedValue()) ? chartOptions.getType() : ChartType.valueOf(type.getSelectedValue());
		ColorScheme colorScheme = "null".equals(colors.getSelectedValue()) ? chartOptions.getColorScheme() : ColorScheme.valueOf(colors.getSelectedValue());
		
		// validate
		if(chartData == ChartData.EXPRESSION_DATA && !networkTunable.isAssociatedEnrichmenMap())
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with a GeneMANIA or STRING network created from an EnrichmentMap network");
		if(chartData == ChartData.EXPRESSION_DATA && !(chartType == ChartType.RADIAL_HEAT_MAP || chartType == ChartType.HEAT_STRIPS))
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with chartType=RADIAL_HEAT_MAP or chartType=HEAT_STRIPS");
		if(chartData == ChartData.PHENOTYPES && !map.isTwoPhenotypeGeneric())
			throw new IllegalArgumentException("data=PHENOTYPES can only be used with two-phenotype generic networks.");
		if(chartData == ChartData.FDR_VALUE && !map.getParams().isFDR())
			throw new IllegalArgumentException("data=FDR_VALUE cannot be used on this network");
		
		if(chartData == ChartData.DATA_SET)
			chartType = ChartType.DATASET_PIE;
		
		ChartOptions options = new ChartOptions(chartData, chartType, colorScheme, showLabels);
		params.setChartOptions(options);
		controlPanelMediator.reset(params);
	}

	
	private static ListSingleSelection<String> lssFromEnum(Enum<?>[] values, Predicate<Enum<?>> valueTester) {
		List<String> names = new ArrayList<>(values.length);
		names.add("null"); // important, need to know if the user did not set the value
		for(Enum<?> value : values) {
			if(valueTester.test(value)) {
				names.add(value.name());
			}
		}
		return new ListSingleSelection<>(names);
	}

	
}
