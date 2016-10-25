package org.baderlab.csplugins.enrichmentmap.style;

import java.util.List;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class ListColumnDescriptor<T> {

	private final String name;
	private final Class<T> elementType;
	
	public ListColumnDescriptor(String name, Class<T> elementType) {
		this.name = name;
		this.elementType = elementType;
	}

	public String getName() {
		return name;
	}

	public Class<?> getElementType() {
		return elementType;
	}
	
	public List<T> get(CyRow row) {
		return row.getList(name, elementType);
	}
	
	public void set(CyRow row, List<T> value) {
		set(row, null, value);
	}
	
	public void set(CyRow row, String suffix, List<T> value) {
		row.set(nameWith(suffix), value);
	}
	
	public void createColumn(CyTable table) {
		createColumn(table, null);
	}
	
	public void createColumn(CyTable table, String suffix) {
		table.createListColumn(nameWith(suffix), elementType, true);
	}
	
	private String nameWith(String suffix) {
		StringBuilder sb = new StringBuilder(name);
		if(suffix != null)
			sb.append(" (").append(suffix).append(")");
		return sb.toString();
	}
}
