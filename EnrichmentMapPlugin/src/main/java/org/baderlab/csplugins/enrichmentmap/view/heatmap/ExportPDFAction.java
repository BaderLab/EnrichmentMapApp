package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

@SuppressWarnings("serial")
public class ExportPDFAction extends AbstractAction {

	@Inject private Provider<JFrame> jframeProvider;
	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final JTable jtable;
	
	public interface Factory {
		ExportPDFAction create(JTable table);
	}
	
	@Inject
	public ExportPDFAction(@Assisted JTable table) {
		super("Export to PDF");
		this.jtable = table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("pdf Files", "pdf"));
		File file = fileUtil.getFile(jframeProvider.get(), "Export Heatmap as PDF File", FileUtil.SAVE, filter);
		
		if (file != null) {
			String fileName = file.toString();
			if(!fileName.endsWith(".pdf")) {
				fileName += ".pdf";
				file = new File(fileName);
			}
			
			HeatMapExporterTask task = new HeatMapExporterTask(file);
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}
	
	class HeatMapExporterTask extends AbstractTask {

		private final File file;
		
		public HeatMapExporterTask(File file) {
			this.file = file;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setTitle("EnrichmentMap");
			taskMonitor.setStatusMessage("Exporting Heat Map");
			
			JTableHeader header = jtable.getTableHeader();

			int headerHeight = header.getHeight();
			Rectangle pageSize = new Rectangle(jtable.getWidth(), jtable.getHeight() + (headerHeight * 2));
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
					jtable.paint(g2);
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
	
}


