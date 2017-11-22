package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class SigGeneSetTableModel extends AbstractTableModel {

	public static final int COL_WANTED = 0, COL_NAME = 1, COL_GENES = 2, COL_OVERLAP = 3;
	
	private List<SigGeneSetDescriptor> geneSets;
	
	public SigGeneSetTableModel(List<SigGeneSetDescriptor> geneSets) {
		this.geneSets = geneSets;
	}
	
	public List<SigGeneSetDescriptor> getGeneSetDescriptors() {
		return geneSets;
	}
	
	@Override
	public int getRowCount() {
		return geneSets.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int row, int col) {
		SigGeneSetDescriptor geneSet = geneSets.get(row);
		switch(col) {
			case COL_WANTED:  return geneSet.isWanted();
			case COL_NAME:    return geneSet.getName();
			case COL_GENES:   return geneSet.getGeneCount();
			case COL_OVERLAP: return geneSet.getMaxOverlap();
		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		switch(col) {
			case COL_WANTED:  return "Import";
			case COL_NAME:    return "Name";
			case COL_GENES:   return "Genes";
			case COL_OVERLAP: return "Max Overlap";
		}
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
			case COL_WANTED:  return Boolean.class;
			case COL_NAME:    return String.class;
			case COL_GENES:   return Integer.class;
			case COL_OVERLAP: return Integer.class;
		}
		return null;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == COL_WANTED;
	}
	
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		if(col == 0 && value instanceof Boolean) {
			geneSets.get(row).setWanted((boolean)value);
			fireTableCellUpdated(row, col);
		}
	}
	
	public void setAllWanted(boolean wanted) {
		int rowCount = getRowCount();
		for(int row = 0; row < rowCount; row++) {
			geneSets.get(row).setWanted(wanted);
		}
		fireTableChanged(new TableModelEvent(this, 0, rowCount-1, COL_WANTED, TableModelEvent.UPDATE));
	}

}
