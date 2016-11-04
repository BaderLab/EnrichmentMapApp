package org.baderlab.csplugins.enrichmentmap.style;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


public class ColumnDescriptor<T> {

	private final String name;
	private final Class<T> type;
	
	public ColumnDescriptor(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}
	
	public T get(CyRow row) {
		return row.get(name, type);
	}
	
	public T get(CyRow row, String suffix) {
		return row.get(with(suffix), type);
	}
	
	public void set(CyRow row, T value) {
		set(row, null, value);
	}
	
	public void set(CyRow row, String suffix, T value) {
		row.set(with(suffix), value);
	}
	
	public void createColumn(CyTable table) {
		createColumn(table, null);
	}
	
	public void createColumn(CyTable table, String suffix) {
		table.createColumn(with(suffix), type, true);
	}
	
	public String with(String suffix) {
		StringBuilder sb = new StringBuilder(name);
		if(suffix != null)
			sb.append(" (").append(suffix).append(")");
		return sb.toString();
	}
}
