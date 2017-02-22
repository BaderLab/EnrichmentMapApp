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

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		if(value instanceof RankValue) {
			RankValue rankValue = (RankValue) value;
			JLabel label = new JLabel(String.valueOf(rankValue.getRank().getRank()));
			
			if (rankValue.isSignificant()) {
				label.setBackground(Color.YELLOW);
				label.setOpaque(true);
				label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.BOLD,
						(UIManager.getFont("TableHeader.font")).getSize() + 2));
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
