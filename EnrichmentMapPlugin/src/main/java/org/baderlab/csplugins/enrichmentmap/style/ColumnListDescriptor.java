package org.baderlab.csplugins.enrichmentmap.style;

import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ColumnListDescriptor<T> {

	private final String name;
	private final Class<T> elementType;
	
	public ColumnListDescriptor(String name, Class<T> elementType) {
		this.name = name;
		this.elementType = elementType;
	}

	public String getBaseName() {
		return name;
	}

	public Class<?> getElementType() {
		return elementType;
	}
	
	public List<T> get(CyRow row, String prefix, String suffix) {
		return row.getList(with(prefix,suffix), elementType);
	}
	
	public void set(CyRow row, String prefix, String suffix, List<T> value) {
		row.set(with(prefix,suffix), value);
	}
	
	public void createColumn(CyTable table, String prefix, String suffix) {
		table.createListColumn(with(prefix,suffix), elementType, true);
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
}
