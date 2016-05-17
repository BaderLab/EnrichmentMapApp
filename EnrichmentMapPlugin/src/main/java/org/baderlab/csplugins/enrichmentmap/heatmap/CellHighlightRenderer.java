package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.model.SignificantGene;

@SuppressWarnings("serial")
public class CellHighlightRenderer extends DefaultTableCellRenderer {

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel label;

		if(value != null) {
			label = new JLabel(value.toString());

			if(value instanceof SignificantGene) {
				label.setBackground(Color.YELLOW);
				label.setOpaque(true);
				label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.BOLD, (UIManager.getFont("TableHeader.font")).getSize() + 2));
			} else {
				label.setBackground(this.getBackground());
				label.setForeground(UIManager.getColor("TableHeader.foreground"));
				label.setFont(UIManager.getFont("TableHeader.font"));
			}
		} else {
			label = new JLabel();
		}

		return label;
	}

}
