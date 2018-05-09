package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import static org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel.*;

import java.util.Comparator;
import java.util.List;

import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

/**
 * <p>
 * We want empty ranks and expressions to always sort to the bottom of the table regardless of the
 * sort order of the column (bug #250, bug #300).
 * The DefaultRowSorter.compare() method makes this difficult because it always sorts ascending 
 * then reverses the results if the user wants descending, which causes nulls to sort to the top.
 * The hackey solution here is to put then nulls where we want them based
 * on the actual sort order of the column.
 * </p>
 * 
 * <p>
 * Note: The DefaultRowSorter.compare() method handles nulls itself before calling
 * Comparator.compare(). So for this to work HeatMapTableModel.getValue() can't return null.
 * For the rank column it returns RankValue.EMPTY and for expression columns it 
 * returns Double.NaN.
 * </p>
 */
public class HeatMapRowSorter extends TableRowSorter<HeatMapTableModel> {
	
	public HeatMapRowSorter(HeatMapTableModel model) {
		super(model);
	}
	
	
	@Override
	public Comparator<?> getComparator(int column) {
		if(column == RANK_COL)
			return createRankValueComparator(column);
		if(column >= DESC_COL_COUNT)
			return createExpressionValueComparator(column);
		return super.getComparator(column);
	}
	
	@Override
	protected boolean useToString(int column) {
		return column == GENE_COL || column == DESC_COL;
	}

	private SortOrder getSortOrder(int column) {
		List<? extends SortKey> sortKeys = getSortKeys();
		if(sortKeys != null) {
			for(SortKey sortKey : sortKeys) {
				if(sortKey.getColumn() == column) {
					 return sortKey.getSortOrder();
				}
			}
		}
		return SortOrder.UNSORTED;
	}
	
	
	private Comparator<RankValue> createRankValueComparator(int column) {
		return new Comparator<RankValue>() {
			Comparator<Integer> ascending  = Comparator.nullsLast(Comparator.naturalOrder());
			Comparator<Integer> descending = Comparator.nullsFirst(Comparator.naturalOrder());
			
			@Override
			public int compare(RankValue rv1, RankValue rv2) {
				Comparator<Integer> comparator = (getSortOrder(column) == SortOrder.DESCENDING) ? descending : ascending;
				return comparator.compare(rv1.getRank(), rv2.getRank());
			}
		};
	}
	
	/**
	 * Its important that HeatMapTableModel doesn't return null for missing expression values, 
	 * it must return NaN instead.
	 */
	private Comparator<Double> createExpressionValueComparator(int column) {
		return new Comparator<Double>() {
			
			@Override
			public int compare(Double d1, Double d2) {
				boolean nanFirst = getSortOrder(column) == SortOrder.DESCENDING;
				
				if(d1.isNaN() && d2.isNaN())
					return 0;
				if(d1.isNaN())
					return nanFirst ? -1 : 1;
				if(d2.isNaN())
					return nanFirst ? 1 : -1;
				
				return d1.compareTo(d2);
			}
		};
	}
	
	
}
