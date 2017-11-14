package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class SigGeneSetTableModel extends AbstractTableModel {

	private List<SigGeneSetDescriptor> geneSets;
	
	public SigGeneSetTableModel(List<SigGeneSetDescriptor> geneSets) {
		this.geneSets = geneSets;
	}
	public SigGeneSetTableModel() {
		this(Collections.emptyList());
	}
	

	@Override
	public int getRowCount() {
		return geneSets.size();
	}

	@Override
	public int getColumnCount() {
		return 3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SigGeneSetDescriptor geneSet = geneSets.get(rowIndex);
		switch(columnIndex) {
			case 0: return geneSet.getName();
			case 1: return geneSet.getGeneCount();
			case 2: return geneSet.getMaxOverlap();
		}
		return null;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
			case 0: return "Name";
			case 1: return "Genes";
			case 2: return "Max Overlap";
		}
		return null;
	}
	

}
