package org.baderlab.csplugins.enrichmentmap.style;

import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

/**
 * MKTODO Replace the "String suffix" parameters with "AbstractDataSet" like in ColumnDescriptor.
 */
public class ColumnListDescriptor<T> extends AbstractColumnDescriptor {

	private final Class<T> elementType;
	
	public ColumnListDescriptor(String name, Class<T> elementType) {
		super(name);
		this.elementType = elementType;
	}

	public Class<?> getElementType() {
		return elementType;
	}

	public List<T> get(CyRow row, String prefix, AbstractDataSet ds) {
		return row.getList(with(prefix,ds), elementType);
	}
	
	public List<T> get(CyRow row, String prefix) {
		return row.getList(with(prefix,null), elementType);
	}
	
	public List<T> get(CyRow row) {
		return row.getList(name, elementType);
	}
	
	public void set(CyRow row, String prefix, AbstractDataSet ds, List<T> value) {
		row.set(with(prefix,ds), value);
	}
	
	public void set(CyRow row, String prefix, List<T> value) {
		row.set(with(prefix,null), value);
	}
	
	public void set(CyRow row, List<T> value) {
		row.set(name, value);
	}
	
	@Override
	public void createColumn(CyTable table, String prefix, AbstractDataSet ds) {
		table.createListColumn(with(prefix,ds), elementType, true);
	}

	@Override
	public void createColumn(CyTable table) {
		table.createListColumn(name, elementType, true);
	}
	
	
	
}
