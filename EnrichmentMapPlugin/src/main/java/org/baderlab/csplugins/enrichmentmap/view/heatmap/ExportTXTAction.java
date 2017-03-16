package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ExportTXTAction extends AbstractAction {
	
	@Inject private FileUtil fileUtil;
	@Inject private Provider<JFrame> jframeProvider;
	
	private final JTable table;
	
	public interface Factory {
		ExportTXTAction create(JTable table);
	}
	
	@Inject
	public ExportTXTAction(@Assisted JTable table) {
		super("Export to TXT");
		this.table = table;
	}
	
	private File promptForFile() {
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("txt Files", "txt"));
		File file = fileUtil.getFile(jframeProvider.get(), "Export Heatmap as txt File", FileUtil.SAVE, filter);
		if(file == null)
			return null;
		
		String fileName = file.toString();
		if(!fileName.endsWith(".txt")) {
			fileName += ".txt";
			file = new File(fileName);
		}
		return file;
	}
	
	private boolean promptForLeadingEdgeOnly(HeatMapTableModel tableModel) {
		if(tableModel.hasSignificantRanks()) {
			int response = JOptionPane.showConfirmDialog(jframeProvider.get(), "Would you like to save the leading edge only?");
			if(response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION)
				return true;
		}
		return false;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		File file = promptForFile();
		if(file == null)
			return;

		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		boolean leadingEdgeOnly = promptForLeadingEdgeOnly(tableModel);
		
		try(BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
			int numCols = tableModel.getColumnCount();
			
			// Print column headers
			out.append(tableModel.getColumnName(HeatMapTableModel.GENE_COL));
			out.append("\t");
			// Skip the ranks column
			for(int col = HeatMapTableModel.DESC_COL_COUNT; col < numCols; col++) {
				out.append(tableModel.getColumnName(col));
				out.append(col == numCols-1 ? "\n" : "\t");
			}
			
			RowSorter<?> sorter = table.getRowSorter();
			int numViewRows = sorter.getViewRowCount();
			
			// Print table data
			for(int viewRow = 0; viewRow < numViewRows; viewRow++) { 
				int modelRow = sorter.convertRowIndexToModel(viewRow);
				
				if(leadingEdgeOnly && !tableModel.getRankValue(modelRow).isSignificant())
					continue;
				
				out.append(tableModel.getValueAt(modelRow, HeatMapTableModel.GENE_COL).toString());
				out.append("\t");
				for(int col = HeatMapTableModel.DESC_COL_COUNT; col < numCols; col++) {
					out.append(tableModel.getValueAt(modelRow, col).toString());
					out.append(col == numCols-1 ? "\n" : "\t");
				}				
			}
			
		} catch(IOException e) {
			JOptionPane.showMessageDialog(jframeProvider.get(), e.getMessage(), "Error writing file", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

}
