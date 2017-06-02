package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientRange;
import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientTheme;

public class ColorRenderer implements TableCellRenderer {
	
	private Map<EMDataSet,DataSetColorRange> colorRanges = new HashMap<>();
	

	public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		JLabel label = new JLabel();
		label.setOpaque(true); //MUST do this for background to show up.
		
		HeatMapTableModel model = (HeatMapTableModel) table.getModel();
		EMDataSet dataset = model.getDataSet(col);
		
		Transform transform = model.getTransform();
		DataSetColorRange range = getRange(dataset, transform);
		
		if(value instanceof Double) {
			Color color = getColor(range.getTheme(), range.getRange(), (Double)value);
			label.setBackground(color);
			Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, isSelected ? table.getSelectionForeground() : color);
			label.setBorder(border);
			
			if(Double.isFinite((Double)value))
				label.setToolTipText(value.toString());
		}
		
		return label;
	}
	
	
	public DataSetColorRange getRange(EMDataSet dataset, Transform transform) {
		return colorRanges.computeIfAbsent(dataset, ds -> DataSetColorRange.create(ds, transform));
	}
	
	private static Color getColor(ColorGradientTheme theme, ColorGradientRange range, Double measurement) {
		if (theme == null || range == null || measurement == null)
			return Color.GRAY;
		if(!Double.isFinite(measurement)) // missing data can result in NaN, log transformed value of -1 can result in -Infinity
			return theme.getNoDataColor();

		float rLow = (float)theme.getMinColor().getRed()   / 255f;
		float gLow = (float)theme.getMinColor().getGreen() / 255f;
		float bLow = (float)theme.getMinColor().getBlue()  / 255f;
		
		float rMid = (float)theme.getCenterColor().getRed()   / 255f;
		float gMid = (float)theme.getCenterColor().getGreen() / 255f;
		float bMid = (float)theme.getCenterColor().getBlue()  / 255f;
		 
		float rHigh = (float)theme.getMaxColor().getRed()   / 255f;
		float gHigh = (float)theme.getMaxColor().getGreen() / 255f;
		float bHigh = (float)theme.getMaxColor().getBlue()  / 255f;
		
		double median;
		if (range.getMinValue() >= 0)
			median = (range.getMaxValue() / 2);
		else
			median = 0.0;
		
		// This happens when you row-normalize but there is only one column. This is probably
		// not the best way to fix it...
		if(median == 0.0 && measurement == 0.0) {
			return theme.getCenterColor();
		} 
		
		if (measurement <= median) {
			float prop = (float) ((float) (measurement - range.getMinValue()) / (median - range.getMinValue()));
			float rVal = rLow + prop * (rMid - rLow);
			float gVal = gLow + prop * (gMid - gLow);
			float bVal = bLow + prop * (bMid - bLow);

			return new Color(rVal, gVal, bVal);
		} else {
			//Related to bug https://github.com/BaderLab/EnrichmentMapApp/issues/116
			//When there is differing max and mins for datasets then it will throw exception
			//for the dataset2 if the value is bigger than the max
			//This need to be fixed on the dataset but in the meantime if the value is bigger
			//than the max set it to the max
			if (measurement > range.getMaxValue())
				measurement = range.getMaxValue();

			float prop = (float) ((float) (measurement - median) / (range.getMaxValue() - median));
			float rVal = rMid + prop * (rHigh - rMid);
			float gVal = gMid + prop * (gHigh - gMid);
			float bVal = bMid + prop * (bHigh - bMid);

			return new Color(rVal, gVal, bVal);

		}
	}
}
