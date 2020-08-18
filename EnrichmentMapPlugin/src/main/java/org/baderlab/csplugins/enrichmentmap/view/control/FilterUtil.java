package org.baderlab.csplugins.enrichmentmap.view.control;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class FilterUtil {

	public static Set<String> getColumnNames(Set<String> columnNames, Collection<AbstractDataSet> dataSets) {
		Set<String> filteredNames = new HashSet<>();
		
		for (String name : columnNames) {
			for (AbstractDataSet ds : dataSets) {
				if (ds.getMap().isLegacy()) {
					if(LegacySupport.DATASET1.equals(ds.getName()) && name.endsWith("dataset1")) {
						filteredNames.add(name);
						break;
					}
					if(LegacySupport.DATASET2.equals(ds.getName()) && name.endsWith("dataset2")) {
						filteredNames.add(name);
						break;
					}
				} 
				if (name.endsWith(" (" + ds.getName() + ")")) {
					filteredNames.add(name);
					break;
				}
			}
		}
		
		return filteredNames;
	}
	
	
	public static String getColumnName(Set<String> columnNames, AbstractDataSet dataSet) {
		Set<String> result = getColumnNames(columnNames, Collections.singletonList(dataSet));
		if(result.isEmpty())
			return null;
		return result.iterator().next();
	}
	
	
	public static boolean passesFilter(Set<String> columnNames, CyTable table, CyRow row, Double maxCutoff, Double minCutoff) {
		boolean show = true;
		for (String colName : columnNames) {
			if (table.getColumn(colName) == null)
				continue; // Ignore this column name (maybe the user deleted it)

			Double value = row.get(colName, Double.class);

			// Possible that there isn't value for this interaction
			if (value == null)
				continue;

			if (value >= minCutoff && value <= maxCutoff) {
				show = true;
				break;
			} else {
				show = false;
			}
		}
		return show;
	}
	
	public static boolean passesFilter(String columnName, CyTable table, CyRow row, Double maxCutoff, Double minCutoff) {
		return passesFilter(Collections.singleton(columnName), table, row, maxCutoff, minCutoff);
	}
}
