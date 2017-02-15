package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class ColorAndValueRenderer extends ColorRenderer {

	private final DecimalFormat format = new DecimalFormat("###.##");
	
	@Override
	public JLabel getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		JLabel label = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		if(value instanceof Double) {
			String text = format.format((Double)value);
			label.setText(text);
			label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.PLAIN, (UIManager.getFont("TableHeader.font")).getSize()-2));
      	   	label.setHorizontalAlignment(SwingConstants.RIGHT);
		}
		return label;
	}

}
