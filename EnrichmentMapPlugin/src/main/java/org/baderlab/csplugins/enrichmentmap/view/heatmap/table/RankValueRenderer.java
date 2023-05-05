package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.ExportTXTTask;

@SuppressWarnings("serial")
public class RankValueRenderer extends DefaultTableCellRenderer {

	public static final Color SIGNIFICANT_COLOR = Color.YELLOW;
	private final DecimalFormat format = new DecimalFormat("###.##");
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
		setBackground(table.getBackground());
		setText("");
		
		if(value instanceof RankValue) {
			RankValue rankValue = (RankValue) value;
			setText(ExportTXTTask.getRankText(format, rankValue));
			
			if(rankValue.isSignificant()) {
				setBackground(SIGNIFICANT_COLOR);
			}
		}
		
		return this;
	}

}
