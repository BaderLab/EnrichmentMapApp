package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class ColumnListener extends MouseAdapter {
	protected JTable table;
	protected int sortCol = 0;
	protected boolean isSortAsc = true;
	protected int m_result = 0;
	protected int columnsCount = 1;

	public ColumnListener(JTable t) {
		table = t;
	}

	public void mouseClicked(MouseEvent e) {
		TableColumnModel colModel = table.getColumnModel();
		int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
		int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

		if(modelIndex < 0)
			return;

		if(sortCol == modelIndex)
			isSortAsc = !isSortAsc;
		else
			sortCol = modelIndex;

		for(int i = 0; i < columnsCount; i++) {
			TableColumn column = colModel.getColumn(i);
			column.setHeaderValue(table.getColumnName(column.getModelIndex()));
		}
		table.getTableHeader().repaint();

		table.repaint();
	}
}