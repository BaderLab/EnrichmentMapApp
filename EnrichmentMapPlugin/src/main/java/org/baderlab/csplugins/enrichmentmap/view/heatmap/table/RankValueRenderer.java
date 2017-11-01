package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class RankValueRenderer extends DefaultTableCellRenderer {

	public static final Color SIGNIFICANT_COLOR = Color.YELLOW;
	private final DecimalFormat format = new DecimalFormat("###.##");
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		if(value instanceof RankValue) {
			RankValue rankValue = (RankValue) value;
			
			JLabel label = new JLabel(getRankText(format, rankValue));
			
			if (rankValue.isSignificant()) {
				label.setBackground(SIGNIFICANT_COLOR);
				label.setOpaque(true);
//				label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.BOLD, (UIManager.getFont("TableHeader.font")).getSize() + 2));
				label.setFont(UIManager.getFont("TableHeader.font"));
			} else {
				label.setBackground(this.getBackground());
				label.setForeground(UIManager.getColor("TableHeader.foreground"));
				label.setFont(UIManager.getFont("TableHeader.font"));
			}
			
			return label;
		}
		return new JLabel();
	}
	
	
	public static String getRankText(DecimalFormat format, RankValue rankValue) {
		Double score = rankValue.getScore();
		if(score == null) {
			Integer rank = rankValue.getRank();
			if(rank != null) {
				return String.valueOf(rank);
			}
		} else {
			return format.format(score);
		}
		return "";
	}

}
