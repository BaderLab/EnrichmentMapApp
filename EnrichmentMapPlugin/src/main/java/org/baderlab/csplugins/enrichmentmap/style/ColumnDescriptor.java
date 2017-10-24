package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ColumnDescriptor<T> extends AbstractColumnDescriptor {

	private final Class<T> type;
	
	public ColumnDescriptor(String name, Class<T> type) {
		super(name);
		this.type = type;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public T get(CyRow row, String prefix, AbstractDataSet ds) {
		return row.get(with(prefix,ds), type);
	}
	
	public T get(CyRow row, String prefix) {
		return row.get(with(prefix,null), type);
	}
	
	public T get(CyRow row) {
		return row.get(name, type);
	}
	
	public void set(CyRow row, String prefix, AbstractDataSet ds, T value) {
		row.set(with(prefix,ds), value);
	}
	
	public void set(CyRow row, String prefix, T value) {
		row.set(with(prefix,null), value);
	}
	
	public void set(CyRow row, T value) {
		row.set(name, value);
	}
	
	public void createColumn(CyTable table, String prefix, AbstractDataSet ds) {
		table.createColumn(with(prefix,ds), type, true);
	}
	
	@Override
	public void createColumn(CyTable table) {
		table.createColumn(name, type, true);
	}
	
	
}
