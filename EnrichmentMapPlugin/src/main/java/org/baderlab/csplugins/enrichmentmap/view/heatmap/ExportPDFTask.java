package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapCellRenderer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValueRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPRow;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ExportPDFTask extends AbstractTask {

	private static final int MARGIN = 20;

	private static final BaseColor HEADER_BACKGROUND = BaseColor.LIGHT_GRAY;
	
	private final File file;
	private final JTable jTable;
	private final RankingOption ranking;
	
	public ExportPDFTask(File file, JTable jTable, RankingOption ranking) {
		this.file = file;
		this.jTable = jTable;
		this.ranking = ranking;
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
		setColumnWidths(table);
		
		float width  = table.getTotalWidth()  + MARGIN * 2;
		float height = table.getTotalHeight() + MARGIN * 2;
		Rectangle pageSize = new Rectangle(width, height);
		
		Document document = new Document(pageSize);
		PdfWriter.getInstance(document, out);
		document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		document.open();
		document.add(table);
		document.close();
	}
	
	
	private void setColumnWidths(PdfPTable table) throws DocumentException {
		int n = table.getNumberOfColumns();
		float[] widths = new float[n];
		
		widths[0] = getPreferredColumnWidth(table, 0);
		widths[1] = getPreferredColumnWidth(table, 1);
		widths[2] = getPreferredColumnWidth(table, 2);
		
		int maxExpression = 0;
		for(int col = 3; col < n; col++) {
			maxExpression = Math.max(maxExpression, getPreferredColumnWidth(table, col));
		}
		
		for(int col = 3; col < n; col++) {
			widths[col] = maxExpression;
		}
		
		table.setTotalWidth(widths);
		table.setLockedWidth(true);
	}
	
	
	private int getPreferredColumnWidth(PdfPTable table, int col) {
		List<PdfPRow> rows = table.getRows();
		int maxSize = 0;
		for(PdfPRow row : rows) {
			PdfPCell cell = row.getCells()[col];
			List<Chunk> chunks = cell.getPhrase().getChunks();
			int size = 0;
			for(Chunk chunk : chunks) {
				size += chunk.getWidthPoint();
			}
			size += cell.getPaddingLeft() + cell.getPaddingRight() + 5;
			maxSize = Math.max(maxSize, size);
		}
		return maxSize;
	}
	
	
	private PdfPTable createTable() {
		HeatMapTableModel model = getModel();
		int colCount = model.getColumnCount();
		
		PdfPTable table = new PdfPTable(colCount);
		
		table.addCell(createGeneHeader());
		table.addCell(createDesciptionHeader());
		table.addCell(createScoreHeader());
		
		for(int col = HeatMapTableModel.DESC_COL_COUNT; col < colCount; col++) {
			table.addCell(createExpressionHeader(col));
		}
		
		for(int row = 0; row < model.getRowCount(); row++) {
			int modelRow = jTable.convertRowIndexToModel(row);
			
			table.addCell(createGeneCell(modelRow));
			table.addCell(createDescriptionCell(modelRow));
			table.addCell(createRankCell(modelRow));
			
			for(int col = HeatMapTableModel.DESC_COL_COUNT; col < colCount; col++) {
				table.addCell(createExpressionCell(modelRow, col));
			}
		}
		return table;
	}
	
	
	private PdfPCell createGeneHeader() {
		PdfPCell cell = new PdfPCell(new Phrase(getModel().getColumnName(HeatMapTableModel.GENE_COL)));
		cell.setBackgroundColor(HEADER_BACKGROUND);
		return cell;
	}
	
	private PdfPCell createDesciptionHeader() {
		PdfPCell cell = new PdfPCell(new Phrase(getModel().getColumnName(HeatMapTableModel.DESC_COL)));
		cell.setBackgroundColor(HEADER_BACKGROUND);
		return cell;
	}
	
	private PdfPCell createScoreHeader() {
		String text = ranking.getPdfHeaderText();
		PdfPCell cell = new PdfPCell(new Phrase(text));
		cell.setBackgroundColor(HEADER_BACKGROUND);
		return cell;
	}
	
	private PdfPCell createExpressionHeader(int col) {
		String text = getModel().getColumnName(col);
		text = SwingUtil.abbreviate(text, 14);
		PdfPCell cell = new PdfPCell(new Phrase(text));
		cell.setRotation(90);
		cell.setBackgroundColor(HEADER_BACKGROUND);
		return cell;
	}
	
	private PdfPCell createGeneCell(int modelRow) {
		String text = String.valueOf(getModel().getValueAt(modelRow, HeatMapTableModel.GENE_COL));
		return new PdfPCell(new Phrase(text));
	}
	
	
	private PdfPCell createDescriptionCell(int modelRow) {
		String text = String.valueOf(getModel().getValueAt(modelRow, HeatMapTableModel.DESC_COL));
		text = SwingUtil.abbreviate(text, 40);
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

