package org.baderlab.csplugins.enrichmentmap.view.legend;

import javax.swing.Icon;

import org.jfree.chart.JFreeChart;

public interface LegendContent {
	
	public static final String NODE_COLOR_HEADER = "Node Fill Color: Phenotype * (1-P_value)";
	public static final String NODE_CHART_HEADER = "Node Chart Colors";
	public static final String NODE_SHAPE_HEADER = "Node Shape";

	static final int LEGEND_ICON_SIZE = 18;
	
	
	public JFreeChart getChart();
	
	public String getChartLabel();
	
	public Icon getGeneSetNodeShape();
	
	public Icon getSignatureNodeShape();
	
	public ColorLegendPanel getNodeColorLegend();
}
