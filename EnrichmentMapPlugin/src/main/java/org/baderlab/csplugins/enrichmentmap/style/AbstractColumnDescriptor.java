package org.baderlab.csplugins.enrichmentmap.style;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.cytoscape.model.CyTable;

public abstract class AbstractColumnDescriptor {

	protected final String name;
	
	
	public AbstractColumnDescriptor(String name) {
		this.name = name;
	}

	public String getBaseName() {
		return name;
	}
	
	public String with(String prefix) {
		return with(prefix, null);
	}
	
	public String with(String prefix, AbstractDataSet ds) {
		if(prefix == null && ds == null)
			return getBaseName();
		
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
	
	public abstract void createColumn(CyTable table, String prefix, AbstractDataSet ds);
	
	public abstract void createColumn(CyTable table);
	
	public void createColumn(CyTable table, String prefix) {
		createColumn(table, prefix, null);
	}
	
	
	public void createColumnIfAbsent(CyTable table, String prefix, AbstractDataSet ds) {
		if(table.getColumn(with(prefix,ds)) == null)
			createColumn(table, prefix, ds);
	}
	
	public void createColumnIfAbsent(CyTable table, String prefix) {
		createColumnIfAbsent(table, prefix);
	}
	
	public void createColumnIfAbsent(CyTable table) {
		if(table.getColumn(name) == null)
			createColumn(table);
	}
	
	public boolean hasColumn(CyTable table) {
		return table.getColumn(name) != null;
	}
	
	public boolean hasColumn(CyTable table, String prefix) {
		return hasColumn(table, prefix, null);
	}
	
	public boolean hasColumn(CyTable table, String prefix, AbstractDataSet ds) {
		return table.getColumn(with(prefix,ds)) != null;
	}
	
	/**
	 * Returns the name.
	 */
	@Override
	public String toString() {
		return name;
	}
}
