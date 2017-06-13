package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ColumnDescriptor<T> {

	private final String name;
	private final Class<T> type;
	
	public ColumnDescriptor(String name, Class<T> type) {
		this.name = name;
		this.type = type;
	}
	
	public String getBaseName() {
		return name;
	}

	public String with(String prefix, AbstractDataSet ds) {
		StringBuilder sb = new StringBuilder();
		if(prefix != null)
			sb.append(prefix);
		sb.append(name);
		if(ds != null) {
			String suffix = " (" + ds.getName() + ")";
			if(ds.getMap().isLegacy()) {
				if(LegacySupport.DATASET1.equals(ds.getName())) {
					suffix = "_dataset1";
				} else if(LegacySupport.DATASET2.equals(ds.getName())) {
					suffix = "_dataset2";
				}
			}
			sb.append(suffix);
		}
		return sb.toString();
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
	
	public void createColumn(CyTable table, String prefix,AbstractDataSet ds) {
		table.createColumn(with(prefix,ds), type, true);
	}
	
	public void createColumn(CyTable table) {
		table.createColumn(name, type, true);
	}
	
	public void createColumnIfAbsent(CyTable table, String prefix, AbstractDataSet ds) {
		if(table.getColumn(with(prefix,ds)) == null)
			createColumn(table, prefix, ds);
	}
	
	public void createColumnIfAbsent(CyTable table) {
		if(table.getColumn(name) == null)
			createColumn(table);
	}
	
	/**
	 * Returns the name.
	 */
	@Override
	public String toString() {
		return name;
	}
}
