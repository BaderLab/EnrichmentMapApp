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

	public Class<?> getType() {
		return type;
	}
	
	public T get(CyRow row, String prefix, String suffix) {
		return row.get(with(prefix,suffix), type);
	}
	
	public T get(CyRow row, String prefix) {
		return row.get(with(prefix,null), type);
	}
	
	public void set(CyRow row, String prefix, String suffix, T value) {
		row.set(with(prefix,suffix), value);
	}
	
	public void set(CyRow row, String prefix, T value) {
		row.set(with(prefix,null), value);
	}
	
	public void createColumn(CyTable table, String prefix, String suffix) {
		table.createColumn(with(prefix,suffix), type, true);
	}
	
	public void createColumnIfAbsent(CyTable table, String prefix, String suffix) {
		if(table.getColumn(with(prefix,suffix)) == null)
			createColumn(table, prefix, suffix);
	}
	
	public String with(String prefix, String suffix) {
		StringBuilder sb = new StringBuilder();
		if(prefix != null)
			sb.append(prefix);
		sb.append(name);
		if(suffix != null)
			sb.append(" (").append(suffix).append(")");
		return sb.toString();
	}
	
	/**
	 * Returns the name.
	 */
	@Override
	public String toString() {
		return name;
	}
}
