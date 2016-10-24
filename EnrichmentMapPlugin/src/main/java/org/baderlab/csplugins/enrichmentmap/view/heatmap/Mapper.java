package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/* 
 * NOTE: This file is no longer used by TableSorter.java.  It's here
 * only to avoid bad links and to provide another example of a table
 * model. It is not used by any of our examples any more. 
 */

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behaviour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its model, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class Mapper extends AbstractTableModel implements TableModelListener {
	protected HeatMapTableModel model;

	public TableModel getModel() {
		return model;
	}

	public void setModel(TableModel model) {
		this.model = (HeatMapTableModel) model;
		model.addTableModelListener(this);
	}

	public Object getValueAt(int aRow, int aColumn) {
		return model.getValueAt(aRow, aColumn);
	}

	public void setValueAt(Object aValue, int aRow, int aColumn) {
		model.setValueAt(aValue, aRow, aColumn);
	}

	public Object getExpValueAt(int aRow, int aColumn) {
		return model.getExpValueAt(aRow, aColumn);
	}

	public void setExpValueAt(Object aValue, int aRow, int aColumn) {
		model.setExpValueAt(aValue, aRow, aColumn);
	}

	public int getRowCount() {
		return (model == null) ? 0 : model.getRowCount();
	}

	public int getColumnCount() {
		return (model == null) ? 0 : model.getColumnCount();
	}

	public String getColumnName(int aColumn) {
		return model.getColumnName(aColumn);
	}

	public Class getColumnClass(int aColumn) {
		return model.getColumnClass(aColumn);
	}

	public boolean isCellEditable(int row, int column) {
		return model.isCellEditable(row, column);
	}

	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}
}
