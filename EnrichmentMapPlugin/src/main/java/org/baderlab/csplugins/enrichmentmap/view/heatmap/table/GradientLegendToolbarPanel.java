package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.BorderLayout;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;

@SuppressWarnings("serial")
public class GradientLegendToolbarPanel extends JPanel {
	
	public static final int DEFAULT_HEIGHT = 25;
	private static final int BORDER_WIDTH = 1;

	public GradientLegendToolbarPanel(JTable table) {
		Objects.requireNonNull(table);
		setLayout(new BorderLayout());
		setOpaque(false);
		
		table.getSelectionModel().addListSelectionListener(e -> handleChange(table));
		table.getColumnModel().getSelectionModel().addListSelectionListener(e -> handleChange(table));
		table.getModel().addTableModelListener(e -> handleChange(table));
		setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
	}
	
	private void handleChange(JTable table) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		if(row >= 0 && col >= HeatMapTableModel.DESC_COL_COUNT) {
			renderLegend(table, row, col);
		} else {
			clear();
		}
	}
	
	public void clear() {
		removeAll();
		revalidate();
	}
	
	private void renderLegend(JTable table, int row, int col) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		Object value = tableModel.getValueAt(row, col);
		removeAll();
		
		if(value instanceof Double) {
			EMDataSet dataset = tableModel.getDataSet(col);
			HeatMapCellRenderer renderer = (HeatMapCellRenderer) table.getCellRenderer(row, col);
			DataSetColorRange colorRange = renderer.getRange(dataset, tableModel.getTransform());
			if(colorRange != null) {
				JPanel panel = new GradientLegendPanel(colorRange);
				add(panel, BorderLayout.CENTER);
			}
			revalidate();
		}
	}
}
