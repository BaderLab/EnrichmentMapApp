package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.util.List;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;

@SuppressWarnings("serial")
public class SigGeneSetTableModel extends AbstractTableModel {

	public static final int COL_WANTED = 0, COL_NAME = 1, COL_GENES = 2, COL_OVERLAP = 3;
	
	private final List<SigGeneSetDescriptor> geneSets;
	private final PostAnalysisFilterType filterType;
	
	public SigGeneSetTableModel(List<SigGeneSetDescriptor> geneSets, PostAnalysisFilterType filterType) {
		this.geneSets = geneSets;
		this.filterType = filterType;
	}
	
	public List<SigGeneSetDescriptor> getGeneSetDescriptors() {
		return geneSets;
	}
	
	public SigGeneSetDescriptor getDescriptor(int row) {
		return geneSets.get(row);
	}
	
	@Override
	public int getRowCount() {
		return geneSets.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}
	
	public PostAnalysisFilterType getFilterType() {
		return filterType;
	}
	
	public int getPassedCount() {
		return (int) geneSets.stream().filter(SigGeneSetDescriptor::passes).count();
	}

	public int getSelectedCount() {
		return (int) geneSets.stream().filter(SigGeneSetDescriptor::isWanted).count();
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		SigGeneSetDescriptor geneSet = geneSets.get(row);
		switch(col) {
			case COL_WANTED:  return geneSet.isWanted();
			case COL_NAME:    return geneSet.getName();
			case COL_GENES:   return geneSet.getGeneCount();
			case COL_OVERLAP: return geneSet.getMostSimilar();
		}
		return null;
	}

	@Override
	public String getColumnName(int col) {
		switch(col) {
			case COL_WANTED: return "Import";
			case COL_NAME: return "Name";
			case COL_GENES: return "Genes";
			case COL_OVERLAP:
				switch(filterType) {
					case HYPERGEOM: return "Hypergeometric";
					case MANN_WHIT_GREATER: return "Mann-Whitney (Greater)";
					case MANN_WHIT_LESS: return "Mann-Whitney (Less)";
					case MANN_WHIT_TWO_SIDED: return "Mann-Whitney (Two-Sided)";
					case NUMBER: return "Overlap # of genes";
					case PERCENT: return "Overlap %";
					case SPECIFIC: return "Overlap % ";
					default: return null;
				}
		}
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
			case COL_WANTED:  return Boolean.class;
			case COL_NAME:    return String.class;
			case COL_GENES:   return Integer.class;
			case COL_OVERLAP: return Double.class;
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
	
	public void setPassedWanted() {
		int rowCount = getRowCount();
		for(int row = 0; row < rowCount; row++) {
			SigGeneSetDescriptor geneSet = geneSets.get(row);
			geneSet.setWanted(geneSet.passes());
		}
		fireTableChanged(new TableModelEvent(this, 0, rowCount-1, COL_WANTED, TableModelEvent.UPDATE));
	}

}
