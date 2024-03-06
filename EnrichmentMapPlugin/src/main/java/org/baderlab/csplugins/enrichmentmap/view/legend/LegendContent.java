package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.awt.Paint;
import java.util.Map;

import javax.swing.Icon;

import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.jfree.chart.JFreeChart;

public interface LegendContent {
	
	public static final String NODE_COLOR_HEADER = "Node Fill Color: Phenotype * (1-P_value)";
	public static final String NODE_CHART_HEADER = "Node Charts";
	public static final String NODE_SHAPE_HEADER = "Node Shape";
	public static final String NODE_CHART_COLOR_HEADER = "Node Chart Colors";
	public static final String NODE_DATA_SET_COLOR_HEADER = "Data Set Color";
	public static final String EDGE_COLOR_HEADER = "Edge Stroke Color";

	public static final int LEGEND_ICON_SIZE = 18;
	
	
	public JFreeChart getChart();
	
	public String getChartLabel();
	
	public Icon getGeneSetNodeShape();
	
	public Icon getSignatureNodeShape();
	
	public ColorLegendPanel getNodePosLegend();
	
	public ColorLegendPanel getNodeNegLegend();
	
	public ColorLegendPanel getChartPosLegend();
	
	public ColorLegendPanel getChartNegLegend();
	
	public Map<Object,Paint> getEdgeColors();
	
	public Map<Object,Paint> getDataSetColors();
	
	public EMStyleOptions getOptions();
}
