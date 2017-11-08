package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.Icon;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jfree.chart.JFreeChart;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;


public class ExportLegendPDFTask extends AbstractTask {

	private static final int MARGIN = 20;
	
	private final File file;
	private final LegendContent content;
	
	public ExportLegendPDFTask(File file, LegendContent legendContent) {
		this.file = file;
		this.content = legendContent;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, DocumentException {
		FileOutputStream out = new FileOutputStream(file);
		
		Document document = new Document(PageSize.NOTE);
		PdfWriter writer = PdfWriter.getInstance(document, out);
		document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
		
		document.open();

		Element nodeColorSection = createNodeColorSection(writer);
		if(nodeColorSection != null)
			document.add(nodeColorSection);
	
		Element nodeShapeSection = createNodeShapeSection(writer);
		if(nodeShapeSection != null)
			document.add(nodeShapeSection);
		
		Element chartSection = createChartSection(writer);
		if(chartSection != null)
			document.add(chartSection);
		
		document.close();
	}
	
	
	
	private Element createNodeColorSection(PdfWriter writer) throws DocumentException {
		ColorLegendPanel colorPanel = content.getNodeColorPanel();
		if(colorPanel == null)
			return null;
		Paragraph p = new Paragraph(new Chunk(LegendContent.NODE_COLOR_HEADER));
		p.add(createNodeColor(writer, colorPanel));
		return p;
	}
	
	private static Image createNodeColor(PdfWriter writer, ColorLegendPanel colorPanel) throws DocumentException {
		colorPanel.setSize(colorPanel.getPreferredSize());
		return drawImage(writer, colorPanel.getWidth(), colorPanel.getHeight(), colorPanel::paint);
	}
	
	
	
	private Element createNodeShapeSection(PdfWriter writer) throws DocumentException {
		Icon gsShape = content.getGeneSetNodeShape();
		Icon sigShape = content.getSignatureNodeShape();
		if(gsShape == null && sigShape == null)
			return null;
		Paragraph p = new Paragraph(new Chunk(LegendContent.NODE_SHAPE_HEADER));
		if(gsShape != null) {
			p.add(createNodeShape(writer, gsShape));
			p.add(new Chunk("Gene Set"));
			p.add(Chunk.NEWLINE);
		}
		if(sigShape != null) {
			p.add(createNodeShape(writer, sigShape));
			p.add(new Chunk("Signature Gene Set"));
		}
		return p;
	}
	
	private static Image createNodeShape(PdfWriter writer, Icon icon) throws DocumentException {
		return drawImage(writer, 30, 30, graphics -> {
			@SuppressWarnings("serial")
			Component component = new Component() {
				@Override public Color getForeground() { return Color.BLACK; }
			};
			icon.paintIcon(component, graphics, 0, 0);
		});
	}
	
	
	
	private Element createChartSection(PdfWriter writer) throws DocumentException {
		JFreeChart chart = content.getChart();
		if(chart == null)
			return null;
		Paragraph p = new Paragraph();
		p.add(new Chunk(LegendContent.NODE_CHART_HEADER));
		p.add(createChart(writer, chart));
		p.add(new Chunk(content.getChartLabel()));
		return p;
	}
	
	private static Image createChart(PdfWriter writer, JFreeChart chart) throws DocumentException {
		final int width = 400, height = 200;
		return drawImage(writer, width, height, graphics -> {
			Rectangle2D area = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(graphics, area);
		});
	}
	
	
	
	private static Image drawImage(PdfWriter writer, int width, int height, Consumer<PdfGraphics2D> draw) throws DocumentException {
		PdfContentByte contentByte = writer.getDirectContent();
		PdfTemplate template = contentByte.createTemplate(width, height);
		PdfGraphics2D graphics = new PdfGraphics2D(template, width, height, new DefaultFontMapper());
		draw.accept(graphics);
		graphics.dispose();
		Image image = Image.getInstance(template);
		return image;
	}
	


}
