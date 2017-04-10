package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class RankValueRenderer extends DefaultTableCellRenderer {

	private static final Color SIGNIFICANT_COLOR = Color.YELLOW;
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		if(value instanceof RankValue) {
			RankValue rankValue = (RankValue) value;
			Integer rank = rankValue.getRank();
			
			JLabel label = new JLabel();
			if(rank != null) {
				label.setText(String.valueOf(rank));
			}
			
			if (rankValue.isSignificant()) {
				label.setBackground(SIGNIFICANT_COLOR);
				label.setOpaque(true);
				label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.BOLD, (UIManager.getFont("TableHeader.font")).getSize() + 2));
			} else {
				label.setBackground(this.getBackground());
				label.setForeground(UIManager.getColor("TableHeader.foreground"));
				label.setFont(UIManager.getFont("TableHeader.font"));
			}
			
			return label;
		}
		return new JLabel();
	}

}
