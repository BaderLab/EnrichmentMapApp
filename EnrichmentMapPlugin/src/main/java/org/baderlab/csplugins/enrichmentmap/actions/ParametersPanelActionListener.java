package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionListener;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;


public class ParametersPanelActionListener implements ActionListener {

	private EnrichmentMap map;
	private EnrichmentMapParameters params;

	public ParametersPanelActionListener(EnrichmentMap map) {
		this.map = map;
		this.params = map.getParams();
	}

	public void actionPerformed(java.awt.event.ActionEvent evt) {
		if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster)) {
			params.setDefaultSortMethod(HeatMapParameters.sort_hierarchical_cluster);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_none)) {
			params.setDefaultSortMethod(HeatMapParameters.sort_none);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_rank)) {
			params.setDefaultSortMethod(HeatMapParameters.sort_rank);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.sort_column)) {
			params.setDefaultSortMethod(HeatMapParameters.sort_column);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.pearson_correlation)) {
			params.setDefaultDistanceMetric(HeatMapParameters.pearson_correlation);
			//update the heatmap to reflect this change.
			params.getHmParams().getEdgeOverlapPanel().updatePanel(map);
			params.getHmParams().getNodeOverlapPanel().updatePanel(map);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.cosine)) {
			params.setDefaultDistanceMetric(HeatMapParameters.cosine);
			//update the heatmap to reflect this change.
			params.getHmParams().getEdgeOverlapPanel().updatePanel(map);
			params.getHmParams().getNodeOverlapPanel().updatePanel(map);
		} else if(evt.getActionCommand().equalsIgnoreCase(HeatMapParameters.euclidean)) {
			params.setDefaultDistanceMetric(HeatMapParameters.euclidean);
			//update the heatmap to reflect this change.
			params.getHmParams().getEdgeOverlapPanel().updatePanel(map);
			params.getHmParams().getNodeOverlapPanel().updatePanel(map);
		}

	}

}
