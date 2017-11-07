package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.awt.Color;
import java.awt.Component;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

import javax.swing.Icon;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jfree.chart.JFreeChart;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class ExportLegendPDFTask extends AbstractTask {

	private static final int MARGIN = 20;
	
	@Inject private RenderingEngineManager engineManager;
	@Inject private VisualMappingManager visualMappingManager;
	
	private final File file;
	private final LegendContent content;
	
	public interface Factory {
		ExportLegendPDFTask create(File file, EMStyleOptions options, Collection<EMDataSet> filteredDataSets);
	}
	
	@Inject
	public ExportLegendPDFTask(LegendContent.Factory legendContentFactory, @Assisted File file, @Assisted EMStyleOptions options, @Assisted Collection<EMDataSet> filteredDataSets) {
		this.file = file;
		this.content = legendContentFactory.create(options, filteredDataSets);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws IOException, DocumentException {
		try {
			FileOutputStream out = new FileOutputStream(file);
			
//			Paragraph nodeShape = createNodeShapeSection();
			
			Document document = new Document(PageSize.NOTE);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.setMargins(MARGIN, MARGIN, MARGIN, MARGIN);
			document.open();

			document.add(new Paragraph(new Chunk("Node Chart")));
			document.add(createChart(writer, content.getChart()));
			
			document.add(new Paragraph(new Chunk("Node Shape")));
			document.add(createNodeShape(writer, content.getGeneSetNodeShape()));
			
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static Image createChart(PdfWriter writer, JFreeChart chart) throws BadElementException {
		final int width = 400, height = 200;
		return drawImage(writer, width, height, graphics -> {
			Rectangle2D area = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(graphics, area);
		});
	}
	
	private static Image createNodeShape(PdfWriter writer, Icon icon) throws BadElementException {
		return drawImage(writer, 30, 30, graphics -> {
			Component component = new Component() {
				public Color getForeground() {
					return Color.BLACK;
				}
			};
			icon.paintIcon(component, graphics, 0, 0);
		});
	}
	
	private static Image drawImage(PdfWriter writer, int width, int height, Consumer<PdfGraphics2D> draw) throws BadElementException {
		PdfContentByte contentByte = writer.getDirectContent();
		PdfTemplate template = contentByte.createTemplate(width, height);
		PdfGraphics2D graphics = new PdfGraphics2D(template, width, height, new DefaultFontMapper());
		draw.accept(graphics);
		graphics.dispose();
		Image image = Image.getInstance(template);
		return image;
	}
	
	
//	private Paragraph createNodeShapeSection() throws IOException {
//		Paragraph p = new Paragraph();
//		p.add("Node Shape");
//		
//		CyNetworkView netView = options.getNetworkView();
//		VisualStyle style = netView != null ? visualMappingManager.getVisualStyle(netView) : null;
//		
//		if(style != null) {
//			NodeShape shape = EMStyleBuilder.getGeneSetNodeShape(style);
//			Icon icon = LegendPanel.getIcon(engineManager, BasicVisualLexicon.NODE_SHAPE, shape, netView);
//			byte[] bytes = getBytes(icon);
//			if(bytes != null) {
//				try {
//					p.add(Image.getInstance(bytes));
//				} catch (BadElementException e) {	 }
//			}
//			
////			if (map.hasSignatureDataSets()) {
////				shape = EMStyleBuilder.getSignatureNodeShape(style);
////				nodeShapeIcon2.setIcon(getIcon(BasicVisualLexicon.NODE_SHAPE, shape, netView));
////			}
//		}
//		
//		return p;
//		
//		
//	}

}
