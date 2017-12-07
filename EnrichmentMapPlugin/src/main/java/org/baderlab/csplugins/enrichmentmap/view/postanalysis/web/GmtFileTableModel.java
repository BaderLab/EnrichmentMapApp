package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class GmtFileTableModel extends AbstractTableModel {

	private final List<GmtFile> files;
	
	public GmtFileTableModel(List<GmtFile> files) {
		this.files = files == null ? Collections.emptyList() : files;
	}
	
	@Override
	public int getRowCount() {
		return files.size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public String getColumnName(int col) {
		switch(col) {
			case 0: return "GMT File";
			case 1: return "Size";
		}
		return null;
	}

	@Override
	public Object getValueAt(int row, int col) {
		GmtFile file = files.get(row);
		switch(col) {
			case 0: return file.getFilePath();
			case 1: return file.getSize();
		}
		return null;
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		switch(col) {
			case 0: return String.class;
			case 1: return Integer.class;
		}
		return null;
	}

}
