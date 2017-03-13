package org.baderlab.csplugins.enrichmentmap.view.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;

public final class ChartUtil {

	private ChartUtil() {
	}
	
	/**
	 * @return List whose first item is the minimum value of the range, and whose second item is the maximum value.
	 */
	@SuppressWarnings("unchecked")
	public static List<Double> calculateGlobalRange(CyNetwork network, List<CyColumnIdentifier> dataColumns) {
		List<Double> range = new ArrayList<>(2);
		List<CyNode> nodes = network.getNodeList();
		
		if (!nodes.isEmpty()) {
			double min = Double.POSITIVE_INFINITY;
			double max = Double.NEGATIVE_INFINITY;
			
			Collection<CyColumn> columns = network.getDefaultNodeTable().getColumns();
			Map<String, CyColumn> columnMap = columns.stream().collect(Collectors.toMap(CyColumn::getName, c -> c));
			
			for (final CyColumnIdentifier colId : dataColumns) {
				final CyColumn column = columnMap.get(colId);
				
				if (column == null)
					continue;
				
				final Class<?> colType = column.getType();
				final Class<?> colListType = column.getListElementType();
				
				if (Number.class.isAssignableFrom(colType) ||
						(List.class.isAssignableFrom(colType) && Number.class.isAssignableFrom(colListType))) {
					for (final CyNode n : nodes) {
						List<? extends Number> values = null;
						final CyRow row = network.getRow(n);
						
						if (List.class.isAssignableFrom(colType))
							values = (List<? extends Number>) row.getList(column.getName(), colListType);
						else if (row.isSet(column.getName()))
							values = Collections.singletonList((Number)row.get(column.getName(), colType));
						
						double[] mm = minMax(min, max, values);
						min = mm[0];
						max = mm[1];
					}
				}
			}
			
			if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
				range.add(min);
				range.add(max);
			}
		} else {
			range.add(0d);
			range.add(0d);
		}
		
		return range;
	}
	
	private static double[] minMax(double min, double max, final List<? extends Number> values) {
		if (values != null) {
			for (final Number v : values) {
				if (v != null) {
					final double dv = v.doubleValue();
					min = Math.min(min, dv);
					max = Math.max(max, dv);
				}
			}
		}
		
		return new double[]{ min, max };
	}
}
