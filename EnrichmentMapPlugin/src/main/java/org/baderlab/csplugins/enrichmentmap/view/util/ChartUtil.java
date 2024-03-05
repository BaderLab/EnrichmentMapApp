package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Color;
import java.awt.Paint;
import java.text.Collator;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.style.AbstractColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ColorScheme;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;

public final class ChartUtil {

	public static final Color TRANSPARENT_COLOR = new Color(0x00, 0x00, 0x00, 0);
	private static final Color DEF_CHART_ITEM_COLOR = Color.decode("#F5F5F5");
	
	private ChartUtil() { }
	
	
	public static List<CyColumnIdentifier> getSortedColumnIdentifiers(
			String attributePrefix,
			Collection<EMDataSet> dataSets, 
			AbstractColumnDescriptor columnDescriptor,
			CyColumnIdentifierFactory columnIdFactory
	) {
		List<CyColumnIdentifier> columns = dataSets.stream()
				.map(ds -> columnDescriptor.with(attributePrefix, ds))  // column name
				.map(columnIdFactory::createColumnIdentifier) // column id
				.collect(Collectors.toList());
		
		// Sort the columns by name, so the chart items have the same order as the data set list
		Collator collator = Collator.getInstance();
		Collections.sort(columns, (CyColumnIdentifier o1, CyColumnIdentifier o2) -> 
			collator.compare(o1.getColumnName(), o2.getColumnName())
		);
		
		return columns;
	}
	
	public static List<CyColumnIdentifier> getColumnIdentifier(
			String attributePrefix, 
			AbstractColumnDescriptor columnDescriptor,
			CyColumnIdentifierFactory columnIdFactory
	) {
		
		String colName = columnDescriptor.with(attributePrefix);
		var id = columnIdFactory.createColumnIdentifier(colName);
		return List.of(id);
	}
	
	
	public static List<EMDataSet> sortDataSets(Collection<EMDataSet> dataSets) {
		List<EMDataSet> list = new ArrayList<>(dataSets);
		
		// Sort them by name
		Collator collator = Collator.getInstance();
		Collections.sort(list, (EMDataSet o1, EMDataSet o2) -> {
			return collator.compare(o1.getName(), o2.getName());
		});
		
		return list;
	}
	
	/**
	 * @return List whose first item is the minimum value of the range, and whose second item is the maximum value.
	 */
	@SuppressWarnings("unchecked")
	public static Range calculateGlobalRange(CyNetwork network, List<CyColumnIdentifier> dataColumns, boolean includeZero) {
		List<CyNode> nodes = network.getNodeList();
		
		if (!nodes.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns();
			Map<String, CyColumn> columnMap = columns.stream().collect(Collectors.toMap(CyColumn::getName, c -> c));
			
			for (final CyColumnIdentifier colId : dataColumns) {
				final CyColumn column = columnMap.get(colId.getColumnName());
				
				if (column == null)
					continue;
				
				final Class<?> colType = column.getType();
				final Class<?> colListType = column.getListElementType();
				
				if (Number.class.isAssignableFrom(colType) ||
						(List.class.isAssignableFrom(colType) && Number.class.isAssignableFrom(colListType))) {
					for (final CyNode n : nodes) {
						List<? extends Number> values = null;
						final CyRow row = network.getRow(n);
						
						if (List.class.isAssignableFrom(colType))
							values = (List<? extends Number>) row.getList(column.getName(), colListType);
						else if (row.isSet(column.getName()))
							values = Collections.singletonList((Number)row.get(column.getName(), colType));
						
						double[] mm = minMax(min, max, values);
						min = mm[0];
						max = mm[1];
					}
				}
			}
			
			if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
				if(includeZero) {
					return new Range(Math.min(min, 0), Math.max(0, max));
				}
				
				return new Range(min, max);
			}
		} 
		
		return new Range(0.0, 0.0);
	}
	
	
	public static List<Color> getChartColors(ChartOptions options, boolean forStyle) {
		ColorScheme colorScheme = options != null ? options.getColorScheme() : null;
		List<Color> colors = colorScheme != null ? colorScheme.getColors() : null;
		ChartData data = options != null ? options.getData() : null;
		
		if (colors == null || colors.size() < 3) // UP, ZERO, DOWN:
			colors = Arrays.asList(Color.LIGHT_GRAY, Color.WHITE, Color.DARK_GRAY);
		
		if(forStyle) {
			// The 3-color schemes need to be swapped when the chart only includes positive numbers.
			// Swap UP and ZERO colors if q or p-value (it should not have negative values!)
			if ((data == ChartData.FDR_VALUE || data == ChartData.P_VALUE) && colors.size() == 3) {
				colors = Arrays.asList(colors.get(1), colors.get(0));
			} 
//			else if(data == ChartData.LOG10_PVAL && colors.size() == 3) {
//				colors = Arrays.asList(colors.get(0), colors.get(1));
//			}
		}
				
		return colors;
	}
	
	public static JFreeChart createRadialHeatMapLegend(List<EMDataSet> dataSets, ChartOptions options) {
		// All the slices must have the same size
		final DefaultPieDataset pieDataset = new DefaultPieDataset();
		
		for (EMDataSet ds : dataSets)
			pieDataset.setValue(ds.getName(), 1);
		
		JFreeChart chart = ChartFactory.createPieChart(
				null, // chart title
				pieDataset, // data
				false, // include legend
				true, // tooltips
				false); // urls
		
        chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(UIManager.getColor("Table.background"));
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        
		final PiePlot plot = (PiePlot) chart.getPlot();
		plot.setCircular(true);
		plot.setOutlineVisible(false);
		plot.setBackgroundPaint(UIManager.getColor("Table.background"));
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setShadowPaint(TRANSPARENT_COLOR);
		plot.setShadowXOffset(0.0);
		plot.setShadowYOffset(0.0);
		plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}"));
		plot.setLabelFont(UIManager.getFont("Label.font").deriveFont(LookAndFeelUtil.getSmallFontSize()));
		plot.setLabelPaint(UIManager.getColor("Label.foreground"));
		plot.setLabelBackgroundPaint(TRANSPARENT_COLOR);
		plot.setLabelOutlinePaint(TRANSPARENT_COLOR);
		plot.setLabelShadowPaint(TRANSPARENT_COLOR);
		plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0}"));
		
		for (EMDataSet ds : dataSets)
			plot.setSectionPaint(ds.getName(), DEF_CHART_ITEM_COLOR);
		
		return chart;
	}
	
	@SuppressWarnings("serial")
	public static JFreeChart createHeatMapLegend(List<EMDataSet> dataSets, ChartOptions options) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		for (EMDataSet ds : dataSets)
			dataset.addValue(1, options.getData().toString(), ds.getName());
		
		final JFreeChart chart = ChartFactory.createBarChart(
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL,
				false, // include legend
				true, // tooltips
				false); // urls
		
		chart.setAntiAlias(true);
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(UIManager.getColor("Table.background"));
        chart.setBackgroundImageAlpha(0.0f);
        chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 20.0));
        
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setBackgroundPaint(UIManager.getColor("Table.background"));
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setDomainGridlinesVisible(false);
	    plot.setRangeGridlinesVisible(false);
		
		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
        domainAxis.setVisible(true);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);
        domainAxis.setTickLabelFont(UIManager.getFont("Label.font").deriveFont(LookAndFeelUtil.getSmallFontSize()));
        domainAxis.setTickLabelPaint(UIManager.getColor("Label.foreground"));
        domainAxis.setTickLabelInsets(new RectangleInsets(0.0, 0.0, 0.0, 15.0));
        domainAxis.setCategoryMargin(0.0);
        
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);
		
	    final BarRenderer renderer = new BarRenderer() {
	    	@Override
	    	public Paint getItemPaint(int row, int column) {
	    		return DEF_CHART_ITEM_COLOR;
	    	}
	    };
	    plot.setRenderer(renderer);
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setDrawBarOutline(true);
		renderer.setShadowVisible(false);
		renderer.setItemMargin(0.0);
		renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{1}", NumberFormat.getInstance()));
		
		return chart;
	}
	
	@SuppressWarnings("serial")
	public static JFreeChart createHeatStripsLegend(List<EMDataSet> dataSets, ChartOptions options) {
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		int total = dataSets.size();
		int lowerBound = options.getData() == ChartData.NES_VALUE ? -total : 0;
		int v = lowerBound / 2;
		
		for (int i = 0; i < total; i++) {
			if (v == 0.0) v = 1; // Just to make sure there is always a bar for each data set name
			dataset.addValue(v++, options.getData().toString(), dataSets.get(i).getName());
		}
		
		final JFreeChart chart = ChartFactory.createBarChart(
				null, // chart title
				null, // domain axis label
				null, // range axis label
				dataset, // data
				PlotOrientation.VERTICAL,
				false, // include legend
				true, // tooltips
				false); // urls
		
		chart.setAntiAlias(true);
		chart.setBorderVisible(false);
		chart.setBackgroundPaint(UIManager.getColor("Table.background"));
		chart.setBackgroundImageAlpha(0.0f);
		chart.setPadding(new RectangleInsets(0.0, 0.0, 0.0, 0.0));

		final CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setOutlineVisible(false);
		plot.setBackgroundPaint(UIManager.getColor("Table.background"));
		plot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);

		final CategoryAxis domainAxis = (CategoryAxis) plot.getDomainAxis();
		domainAxis.setVisible(true);
		domainAxis.setAxisLineVisible(false);
		domainAxis.setTickMarksVisible(false);
		domainAxis.setTickLabelFont(UIManager.getFont("Label.font").deriveFont(LookAndFeelUtil.getSmallFontSize()));
		domainAxis.setLabelPaint(UIManager.getColor("Label.foreground"));
		domainAxis.setMaximumCategoryLabelLines(1);
		domainAxis.setCategoryMargin(0.0);
        
        if (total > 4) {
	        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
	        domainAxis.setMaximumCategoryLabelWidthRatio(0.5f);
        } else {
        	domainAxis.setMaximumCategoryLabelLines(2);
        }
        
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setVisible(false);
        
	    final BarRenderer renderer = new BarRenderer() {
	    	@Override
	    	public Paint getItemPaint(int row, int column) {
	    		return DEF_CHART_ITEM_COLOR;
	    	}
	    };
	    plot.setRenderer(renderer);
		renderer.setBarPainter(new StandardBarPainter());
		renderer.setDrawBarOutline(true);
		renderer.setShadowVisible(false);
		renderer.setItemMargin(0.0);
		renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{1}", NumberFormat.getInstance()));
		
		return chart;
	}
	
	private static double[] minMax(double min, double max, final List<? extends Number> values) {
		if (values != null) {
			for (final Number v : values) {
				if (v != null) {
					final double dv = v.doubleValue();
					min = Math.min(min, dv);
					max = Math.max(max, dv);
				}
			}
		}
		
		return new double[]{ min, max };
	}
}
