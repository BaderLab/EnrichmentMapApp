package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

@Deprecated
public class RawExpressionValueRenderer extends JLabel implements TableCellRenderer {

	private static final long serialVersionUID = 1L;

	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	HeatMapTableModel ogt = new HeatMapTableModel();

	public RawExpressionValueRenderer() {
		this.isBordered = isBordered;
		setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Color newColor = null;
		Double numberValue = 0.0;

		if((value != null) && (value instanceof ExpressionTableValue)) {
			ExpressionTableValue avalue = (ExpressionTableValue) value;

			numberValue = avalue.getExpression_value();

			newColor = avalue.getExpression_color();
		}

		DecimalFormat formatter = new DecimalFormat("###.##");
		Object result = formatter.format(numberValue.doubleValue());

		setText(result.toString());
		setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.PLAIN,
				(UIManager.getFont("TableHeader.font")).getSize() - 2));
		setHorizontalAlignment(SwingConstants.RIGHT);

		TableModel tc = table.getModel();

		setBackground(newColor);
		if(isBordered) {
			if(isSelected) {
				if(selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(0, 0, 0, 0, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
			} else {
				if(unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(0, 0, 0, 0, table.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}

		setToolTipText("Exp value: " + numberValue);

		return this;
	}

	/*
	 * public Component getTableCellRendererComponent(JTable table, Object
	 * value, boolean isSelected, boolean hasFocus, int row, int column) {
	 * Object result = null; JLabel label = null;
	 * 
	 * Number numberValue; Color colorValue;
	 * 
	 * 
	 * if (( value != null) && (value instanceof ExpressionTableValue)) {
	 * ExpressionTableValue avalue = (ExpressionTableValue)value;
	 * 
	 * numberValue = (Number)avalue.getExpression_value(); DecimalFormat
	 * formatter = new DecimalFormat("###.##"); result =
	 * formatter.format(numberValue.doubleValue());
	 * 
	 * label = new JLabel(result.toString());
	 * 
	 * colorValue = avalue.getExpression_color();
	 * 
	 * label.setBackground(colorValue); label.setOpaque(true); label.setFont(new
	 * Font((UIManager.getFont("TableHeader.font")).getName(), Font.PLAIN,
	 * (UIManager.getFont("TableHeader.font")).getSize()-2));
	 * label.setHorizontalAlignment(SwingConstants.RIGHT);
	 * 
	 * label.setToolTipText("Exp value: " + numberValue); }
	 * 
	 * return label; }
	 */

}
