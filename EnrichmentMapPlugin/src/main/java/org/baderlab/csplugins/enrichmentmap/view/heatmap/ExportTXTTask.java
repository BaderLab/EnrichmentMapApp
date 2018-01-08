package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.RowSorter;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ExportTXTTask extends AbstractTask {

	private final File file;
	private final JTable table;
	private final boolean leadingEdgeOnly;

	public ExportTXTTask(File file, JTable table, boolean leadingEdgeOnly) {
		this.file = file;
		this.table = table;
		this.leadingEdgeOnly = leadingEdgeOnly;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		taskMonitor.setTitle("Export HeatMap to TXT file");
		
		HeatMapTableModel model = (HeatMapTableModel) table.getModel();
		
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			int numCols = model.getColumnCount();
			
			// Print column headers
			out.append(model.getColumnName(HeatMapTableModel.GENE_COL)).append('\t');
			out.append(model.getColumnName(HeatMapTableModel.DESC_COL)).append('\t');
			out.append("Ranks").append('\t');
			
			for(int col = HeatMapTableModel.DESC_COL_COUNT; col < numCols; col++) {
				out.append(model.getColumnName(col));
				out.append(col == numCols-1 ? "\n" : "\t");
			}
			
			RowSorter<?> sorter = table.getRowSorter();
			int numViewRows = sorter.getViewRowCount();
			
			// Print table data
			for(int viewRow = 0; viewRow < numViewRows; viewRow++) { 
				int row = sorter.convertRowIndexToModel(viewRow);
				
				if(leadingEdgeOnly && !model.getRankValue(row).isSignificant())
					continue;
				
				out.append(getGeneText(model, row)).append('\t');
				out.append(getDescriptionText(model, row)).append('\t');
				out.append(getRankText(model, row)).append('\t');
				
				for(int col = HeatMapTableModel.DESC_COL_COUNT; col < numCols; col++) {
					out.append(getExpressionText(model, row, col));
					out.append(col == numCols-1 ? "\n" : "\t");
				}				
			}
		}
	}
	
	
	public static String getGeneText(HeatMapTableModel model, int modelRow) {
		return String.valueOf(model.getValueAt(modelRow, HeatMapTableModel.GENE_COL));
	}
	
	public static String getDescriptionText(HeatMapTableModel model, int modelRow) {
		Object value = model.getValueAt(modelRow, HeatMapTableModel.DESC_COL);
		String text = value == null ? "" : String.valueOf(value);
		text = text.replaceAll("\t", " ");
		return SwingUtil.abbreviate(text, 40);
	}
	
	public static String getRankText(HeatMapTableModel model, int modelRow) {
		RankValue rankValue = (RankValue) model.getValueAt(modelRow, HeatMapTableModel.RANK_COL);
		return getRankText(HeatMapCellRenderer.getFormat(), rankValue);
	}
	
	public static String getRankText(DecimalFormat format, RankValue rankValue) {
		Double score = rankValue.getScore();
		if(score == null) {
			Integer rank = rankValue.getRank();
			if(rank != null) {
				return String.valueOf(rank);
			}
		} else {
			return format.format(score);
		}
		return "";
	}
	
	public static String getExpressionText(HeatMapTableModel model, int modelRow, int col) {
		Number value = (Number) model.getValueAt(modelRow, col);
		if(value == null)
			return "";
		return getExpressionText(value.doubleValue());
	}
	
	public static String getExpressionText(double value) {
		return HeatMapCellRenderer.getText(value);
	}

}
