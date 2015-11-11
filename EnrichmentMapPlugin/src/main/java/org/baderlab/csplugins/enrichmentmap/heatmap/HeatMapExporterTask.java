package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;


public class HeatMapExporterTask extends AbstractTask {

	final JTable jtable1;
	final JTableHeader header;
	final File file;

	
	public HeatMapExporterTask(JTable jtable1, JTableHeader header, File file) {
		this.jtable1 = jtable1;
		this.header = header;
		this.file = file;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Exporting Heat Map");
		
		int headerHeight = header.getHeight();
		Rectangle pageSize = new Rectangle(jtable1.getWidth(), jtable1.getHeight() + (headerHeight * 2));
		Document document = new Document(pageSize);
		
		try(FileOutputStream stream = new FileOutputStream(file)) {
			PdfWriter writer = PdfWriter.getInstance(document, stream);
			try {
				document.open();

				PdfContentByte canvas = writer.getDirectContent();
				float width  = pageSize.getWidth();
				float height = pageSize.getHeight();

				//print the legend
				//PdfTemplate legend = cb.createTemplate(width,height_header);
				//Graphics2D g = legend.createGraphics(width, height_header);
				//double imageScale = width / ((double) legendpanel.getWidth());
				//g.scale(imageScale, imageScale);
				//legendpanel.paint(g);
				//g.dispose();
				//cb.addTemplate(legend, 0, (pageSize.getHeight()-25) );
				
				Graphics2D g1 = canvas.createGraphics(width, headerHeight);
				header.paint(g1);
				g1.dispose();
				
				taskMonitor.setProgress(0.25);
				
				Graphics2D g2 = canvas.createGraphics(width, height);
				jtable1.paint(g2);
				g2.dispose();

				taskMonitor.setProgress(0.5);
				
				Graphics2D g3 = canvas.createGraphics(width, headerHeight);
				header.paint(g3);
				g3.dispose();
				
				taskMonitor.setProgress(0.75);

			} finally {
				document.close();
				writer.close();
				taskMonitor.setProgress(0.99);
			}
			
		} catch (DocumentException exp) {
			throw new IOException(exp);
		}
	}

}
