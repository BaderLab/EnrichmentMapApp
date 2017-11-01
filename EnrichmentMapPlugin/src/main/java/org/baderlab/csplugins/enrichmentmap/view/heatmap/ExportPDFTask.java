package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ExportPDFTask extends AbstractTask {

	private static final int MARGIN = 20;
	
	private final File file;
	private final JTable jTable;
	
	public ExportPDFTask(File file, JTable jTable) {
		this.file = file;
		this.jTable = jTable;
	}
	
	private HeatMapTableModel getModel() {
		return (HeatMapTableModel) jTable.getModel();
	}
	
	private HeatMapCellRenderer getCellRenderer() {
		return (HeatMapCellRenderer) jTable.getDefaultRenderer(Double.class);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, DocumentException {
		FileOutputStream out = new FileOutputStream(file);
		
		PdfPTable table = createTable();
		
		table.setTotalWidth(getModel().getColumnCount() * 50);
		table.setLockedWidth(true);
		float width  = table.getTotalWidth() + MARGIN * 2;
		float height = table.getTotalHeight() + MARGIN * 2;
		Rectangle pageSize = new Rectangle(width, height);
		
		Document document = new Document(pageSize);
		PdfWriter.getInstance(document, out);
		document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		document.open();
		document.add(table);
		document.close();
	}
	
	
	private PdfPTable createTable() {
		HeatMapTableModel model = getModel();
		PdfPTable table = new PdfPTable(model.getColumnCount());
		
		for(int row = 0; row < model.getRowCount(); row++) {
			int modelRow = jTable.convertRowIndexToModel(row);
			
			table.addCell(createGeneCell(modelRow));
			table.addCell(createDescriptionCell(modelRow));
			table.addCell(createRankCell(modelRow));
			
			for(int col = HeatMapTableModel.DESC_COL_COUNT; col < model.getColumnCount(); col++) {
				table.addCell(createExpressionCell(modelRow, col));
			}
		}
		return table;
	}
	
	
	private PdfPCell createGeneCell(int modelRow) {
		String text = String.valueOf(getModel().getValueAt(modelRow, HeatMapTableModel.GENE_COL));
		return new PdfPCell(new Phrase(text));
	}
	
	
	private PdfPCell createDescriptionCell(int modelRow) {
		String text = String.valueOf(getModel().getValueAt(modelRow, HeatMapTableModel.DESC_COL));
		return new PdfPCell(new Phrase(text));
	}
	
	
	private PdfPCell createRankCell(int modelRow) {
		PdfPCell cell = new PdfPCell();
		RankValue rankValue = (RankValue) getModel().getValueAt(modelRow, HeatMapTableModel.RANK_COL);
		cell.setPhrase(new Phrase(RankValueRenderer.getRankText(getCellRenderer().getFormat(), rankValue)));
		if(rankValue.isSignificant()) {
			cell.setBackgroundColor(color(RankValueRenderer.SIGNIFICANT_COLOR));
		}
		return cell;
	}
	
	
	private PdfPCell createExpressionCell(int row, int col) {
		double value = (double) getModel().getValueAt(row, col);
		Color color = getCellRenderer().getColor(getModel(), col, value);
		
		PdfPCell cell = new PdfPCell();
		cell.setBackgroundColor(color(color));
		if(Double.isFinite(value)) {
			cell.setPhrase(new Phrase(getCellRenderer().getFormat().format(value)));
		}
		
		return cell;
	}
	
	
	private static BaseColor color(java.awt.Color color) {
		return new BaseColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
	}

}

