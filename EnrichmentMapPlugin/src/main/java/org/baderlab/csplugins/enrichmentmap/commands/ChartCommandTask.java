package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.ChartTunables;
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

import com.google.inject.Inject;

public class ChartCommandTask extends AbstractTask {

	@Inject private ControlPanelMediator controlPanelMediator;
	
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@ContainsTunables @Inject
	public ChartTunables chartTunable;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		// validate network
		CyNetworkView networkView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(networkView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		ChartData chartData = chartTunable.getChartData();
		ChartType chartType = chartTunable.getChartType();
		ColorScheme colorScheme = chartTunable.getColorScheme();
		
		// validate
		if(chartData == ChartData.EXPRESSION_DATA && !networkTunable.isAssociatedEnrichmenMap())
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with a GeneMANIA or STRING network created from an EnrichmentMap network");
		if(chartData == ChartData.EXPRESSION_DATA && !(chartType == ChartType.RADIAL_HEAT_MAP || chartType == ChartType.HEAT_STRIPS))
			throw new IllegalArgumentException("data=EXPRESSION_DATA can only be used with chartType=RADIAL_HEAT_MAP or chartType=HEAT_STRIPS");
		if(chartData == ChartData.PHENOTYPES && !map.isTwoPhenotypeGeneric())
			throw new IllegalArgumentException("data=PHENOTYPES can only be used with two-phenotype generic networks.");
		if(chartData == ChartData.FDR_VALUE && !map.getParams().isFDR())
			throw new IllegalArgumentException("data=FDR_VALUE cannot be used on this network");
		
		ChartOptions options = new ChartOptions(chartData, chartType, colorScheme, chartTunable.showChartLabels());
		
		Map<Long,ViewParams> viewParamsMap = controlPanelMediator.getAllViewParams();
		ViewParams params = viewParamsMap.get(networkView.getSUID());
		params.setChartOptions(options);
		controlPanelMediator.reset(params);
	}

}
