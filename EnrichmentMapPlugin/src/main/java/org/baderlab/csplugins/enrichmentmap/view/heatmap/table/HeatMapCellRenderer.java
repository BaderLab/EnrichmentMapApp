package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.Transform;

public class HeatMapCellRenderer implements TableCellRenderer {

	private final Map<Pair<EMDataSet,Transform>,Optional<DataSetColorRange>> colorRanges = new HashMap<>();
	private final static DecimalFormat format = new DecimalFormat("###.##");
	private boolean showValue;
	
	
	@Override
	public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		JLabel label = new JLabel();
		label.setOpaque(true); //MUST do this for background to show up.
		
		if(value instanceof Number) {
			double d = ((Number)value).doubleValue();
			
			HeatMapTableModel model = (HeatMapTableModel) table.getModel();
			Color color = getColorFor(model, col, d);
			label.setBackground(color);
			Border border = BorderFactory.createMatteBorder(1, 1, 1, 1, isSelected ? table.getSelectionForeground() : color);
			label.setBorder(border);
			
			String text = getText(d);
			label.setToolTipText(text);
			
			if(showValue && Double.isFinite(d)) {
				label.setText(text);
				label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.PLAIN, (UIManager.getFont("TableHeader.font")).getSize()-2));
	      	   	label.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		}
		
		return label;
	}
	
	
	public void setShowValues(boolean showValue) {
		this.showValue = showValue;
	}
	
	public boolean getShowValues() {
		return showValue;
	}
	
	
	public static String getText(double d) {
		return Double.isFinite(d) ? format.format(d) : "NaN";
	}
	
	public static DecimalFormat getFormat() {
		return format;
	}

	private static Color getColor(HeatMapTableModel model, int col, double d, BiFunction<EMDataSet,Transform,DataSetColorRange> getColorRange) {
		EMDataSet dataset = model.getDataSet(col);
		Transform transform = model.getTransform();
		DataSetColorRange range = getColorRange.apply(dataset, transform);
		if(range != null) {
			Color color = range.getColor(d);
			return color;
		} else {
			return Color.GRAY;
		}
	}
	
	public static Color getColor(HeatMapTableModel model, int col, double d) {
		return getColor(model, col, d, (dataset,transform) -> DataSetColorRange.create(dataset.getExpressionSets(), transform).orElse(null));
	}
	
	private Color getColorFor(HeatMapTableModel model, int col, double d) {
		return getColor(model, col, d, this::getRange);
	}
	
	public DataSetColorRange getRange(EMDataSet dataset, Transform transform) {
		// creating the color range for Transform.ROW_NORMALIZED consumes memory, so cache the value
		return colorRanges.computeIfAbsent(Pair.of(dataset, transform), 
			pair -> DataSetColorRange.create(pair.getLeft().getExpressionSets(), pair.getRight())
		).orElse(null);
	}
	

}
