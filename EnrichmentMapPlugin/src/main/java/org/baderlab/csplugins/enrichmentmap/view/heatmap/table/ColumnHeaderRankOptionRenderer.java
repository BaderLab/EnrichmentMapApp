package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Component;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.RankingOption;

public class ColumnHeaderRankOptionRenderer implements TableCellRenderer {

	private final TableCellRenderer delegate;
	
	public ColumnHeaderRankOptionRenderer(JTable table) {
		this.delegate = Objects.requireNonNull(table.getTableHeader().getDefaultRenderer());
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		if(value instanceof RankingOption) {
			RankingOption rankingOption = (RankingOption) value;
			value = rankingOption.getTableHeaderText();
		}
		return delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
	}
	
}
