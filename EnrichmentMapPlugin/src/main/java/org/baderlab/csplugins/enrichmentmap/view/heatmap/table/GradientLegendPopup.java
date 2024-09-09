package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;

public class GradientLegendPopup {

	public static void show(JTable table) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		if(row < 0 || col < HeatMapTableModel.DESC_COL_COUNT) {
			return;
		}
		
		var tableModel = (HeatMapTableModel) table.getModel();
		var value = tableModel.getValueAt(row, col);
		if(!(value instanceof Double)) {
			return;
		}
		
		EMDataSet dataset = tableModel.getDataSet(col);
		HeatMapCellRenderer renderer = (HeatMapCellRenderer) table.getCellRenderer(row, col);
		DataSetColorRange colorRange = renderer.getRange(dataset, tableModel.getTransform());
		
		JPanel gradient = new GradientLegendPanel(colorRange);
		gradient.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		JLabel label = new JLabel(getValue(table, row, col));
		label.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 0));
        
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(gradient, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(160, 56));
		
		Rectangle cellRect = table.getCellRect(row, col, false);
		Point p = new Point(cellRect.x, cellRect.y + cellRect.height);
		
		JPopupMenu popup = new JPopupMenu();
		popup.add(panel);
		popup.show(table, p.x, p.y);
	}
	
	
	private static String getValue(JTable table, int row, int col) {
		int modelRow = table.convertRowIndexToModel(row);
		int modelCol = table.convertColumnIndexToModel(col);
		
		var tableModel = (HeatMapTableModel) table.getModel();
		var value = tableModel.getValueAt(modelRow, modelCol);
		
		if(value instanceof Number) {
			double d = ((Number)value).doubleValue();
			return HeatMapCellRenderer.getText(d);
		}
		
		return null;
	}
}
