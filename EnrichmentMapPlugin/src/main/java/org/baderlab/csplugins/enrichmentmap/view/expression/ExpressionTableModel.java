package org.baderlab.csplugins.enrichmentmap.view.expression;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

@SuppressWarnings("serial")
public class ExpressionTableModel extends AbstractTableModel {

	private final EnrichmentMap map;
	private final List<String> genes;
	
	public ExpressionTableModel(EnrichmentMap map, List<String> genes) {
		this.map = map;
		this.genes = genes;
	}

	@Override
	public int getRowCount() {
		return genes.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}
	
	@Override
	public String getColumnName(int col) {
		if(col == 0)
			return "Gene";
		return null;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return genes.get(rowIndex);
	}

}
