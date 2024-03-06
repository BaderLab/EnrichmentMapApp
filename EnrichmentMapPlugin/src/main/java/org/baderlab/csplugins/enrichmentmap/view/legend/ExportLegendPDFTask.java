package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.awt.Color;
import java.awt.Component;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.Icon;

import org.baderlab.csplugins.enrichmentmap.style.ChartData;
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
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
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

		addTo(document, createNodeColorSection(writer));
		addTo(document, createNodeShapeSection(writer));
		addTo(document, createChartSection(writer));
		addTo(document, createChartColorSection(writer));
		addTo(document, createNodeDataSetColorSection(writer));
		addTo(document, createEdgeColorSection(writer));
	
		document.close();
	}
	
	
	private List<Element> createNodeColorSection(PdfWriter writer) throws DocumentException {
//		ColorLegendPanel colorPanel = content.getNodeColorLegend();
//		if(colorPanel == null)
//			return null;
//		Paragraph title = new Paragraph(new Chunk(LegendContent.NODE_COLOR_HEADER));
//		Paragraph body = new Paragraph();
//		body.add(createNodeColor(writer, colorPanel));
//		return Arrays.asList(title, body);
		return null;
	}
	
	private static Image createNodeColor(PdfWriter writer, ColorLegendPanel colorPanel) throws DocumentException {
		colorPanel.setSize(colorPanel.getPreferredSize());
		return drawImage(writer, colorPanel.getWidth(), colorPanel.getHeight(), colorPanel::paint);
	}
	
	
	
	private List<Element> createNodeShapeSection(PdfWriter writer) throws DocumentException {
		Icon gsShape = content.getGeneSetNodeShape();
		Icon sigShape = content.getSignatureNodeShape();
		if(gsShape == null && sigShape == null)
			return null;
		Paragraph title = new Paragraph(new Phrase(LegendContent.NODE_SHAPE_HEADER));
		PdfPTable table = new PdfPTable(new float[] {1,1.2f});
		
		if(gsShape != null) {
			Image image = createNodeShape(writer, gsShape);
			addShapeRow(writer, table, image, "Gene Set");
		}
		if(sigShape != null) {
			Image image = createNodeShape(writer, sigShape);
			addShapeRow(writer, table, image, "Signature Gene Set");
		}
		return Arrays.asList(title, table);
	}
	
	private static void addShapeRow(PdfWriter writer, PdfPTable table, Image image, String text) throws DocumentException {
		image.setAlignment(Image.ALIGN_RIGHT);
		PdfPCell shape = new PdfPCell(image);
		shape.setBorder(PdfPCell.NO_BORDER);
		shape.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		shape.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
		table.addCell(shape);
		PdfPCell name = new PdfPCell(new Phrase(text));
		name.setBorder(PdfPCell.NO_BORDER);
		name.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
		name.setVerticalAlignment(PdfPCell.ALIGN_CENTER);
		table.addCell(name);
	}
	
	private static Image createNodeShape(PdfWriter writer, Icon icon) throws DocumentException {
		return drawImage(writer, 35, 35, graphics -> {
			@SuppressWarnings("serial")
			Component component = new Component() {
				@Override public Color getForeground() { return Color.BLACK; }
			};
			icon.paintIcon(component, graphics, 2, 2);
		});
	}
	
	
	
	private List<Element> createChartSection(PdfWriter writer) throws DocumentException {
		JFreeChart chart = content.getChart();
		if(chart == null)
			return null;
		Paragraph title = new Paragraph();
		title.add(new Phrase(LegendContent.NODE_CHART_HEADER + ": " + content.getChartLabel()));
		Paragraph body = new Paragraph();
		body.add(createChart(writer, chart));
		return Arrays.asList(title, body);
	}
	
	private static Image createChart(PdfWriter writer, JFreeChart chart) throws DocumentException {
		final int width = 400, height = 200;
		return drawImage(writer, width, height, graphics -> {
			Rectangle2D area = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(graphics, area);
		});
	}
	
	
	private List<Element> createChartColorSection(PdfWriter writer) throws DocumentException {
		ColorLegendPanel posLegend = content.getChartPosLegend();
		ColorLegendPanel negLegend = content.getChartNegLegend();
		if(posLegend == null && negLegend == null)
			return null;
		Paragraph title = new Paragraph(new Chunk(LegendContent.NODE_CHART_COLOR_HEADER));
		Paragraph body = new Paragraph();
		body.setAlignment(Paragraph.ALIGN_CENTER);
		if(posLegend != null) {
			body.add(new Phrase("Positive"));
			body.add(Chunk.NEWLINE);
			body.add(createNodeColor(writer, posLegend));
			body.add(Chunk.NEWLINE);
		}
		if(negLegend != null) {
			body.add(new Phrase("Negative"));
			body.add(Chunk.NEWLINE);
			body.add(createNodeColor(writer, negLegend));
			body.add(Chunk.NEWLINE);
		}
		return Arrays.asList(title, body);
	}
	
	
	private List<Element> createEdgeColorSection(PdfWriter writer) throws DocumentException {
		Map<Object,Paint> edgeColors = content.getEdgeColors();
		if(edgeColors == null || edgeColors.isEmpty())
			return null;
		return createColorsSection(writer, edgeColors, LegendContent.EDGE_COLOR_HEADER);
	}
	
	private List<Element> createNodeDataSetColorSection(PdfWriter writer) throws DocumentException {
		ChartData data = content.getOptions().getChartOptions().getData();
		if(data != ChartData.DATA_SET	)
			return null;
		Map<Object,Paint> dataSetColors = content.getDataSetColors();
		if(dataSetColors == null || dataSetColors.isEmpty())
			return null;
		return createColorsSection(writer, dataSetColors, LegendContent.NODE_DATA_SET_COLOR_HEADER);
	}
	
	private List<Element> createColorsSection(PdfWriter writer, Map<Object,Paint> colors, String titleText) throws DocumentException {
		Paragraph title = new Paragraph(new Chunk(titleText));
		PdfPTable table = new PdfPTable(new float[] {1f,10f});
		final int width = 40, height = 20;
		for(Map.Entry<Object,Paint> entry : colors.entrySet()) {
			Image colorRect = drawImage(writer, width, height, graphics -> {
				graphics.setPaint(entry.getValue());
				graphics.fillRect(0, 0, width, height);
			});
			addShapeRow(writer, table, colorRect, "  " + entry.getKey().toString());
		}
		return Arrays.asList(title, table);
	}
	
	private static Image drawImage(PdfWriter writer, int width, int height, Consumer<PdfGraphics2D> draw) throws DocumentException {
		PdfContentByte contentByte = writer.getDirectContent();
		PdfTemplate template = contentByte.createTemplate(width, height);
		PdfGraphics2D graphics = new PdfGraphics2D(template, width, height, new DefaultFontMapper());
		draw.accept(graphics);
		graphics.dispose();
		Image image = Image.getInstance(template);
		image.setAlignment(Image.ALIGN_CENTER);
		return image;
	}
	

	@SuppressWarnings("unused")
	private static void addTo(Document document, Element element) throws DocumentException {
		if(element != null)
			document.add(element);
	}
	
	private static void addTo(Document document, List<Element> elements) throws DocumentException {
		if(elements != null) {
			for(Element element : elements) {
				document.add(element);
			}
		}
	}
	
}
