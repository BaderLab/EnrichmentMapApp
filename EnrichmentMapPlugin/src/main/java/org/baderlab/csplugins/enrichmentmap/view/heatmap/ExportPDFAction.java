package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ExportPDFAction extends AbstractAction {

	@Inject private Provider<JFrame> jframeProvider;
	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final JTable table;
	private final Supplier<RankingOption> rankingSupplier;
	
	public interface Factory {
		ExportPDFAction create(JTable table, Supplier<RankingOption> rankingSupplier);
	}
	
	@Inject
	public ExportPDFAction(@Assisted JTable table, @Assisted Supplier<RankingOption> rankingSupplier) {
		super("Export to PDF");
		this.table = table;
		this.rankingSupplier = rankingSupplier;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<File> file = FileBrowser.promptForPdfExport(fileUtil, jframeProvider.get());
		if(file.isPresent()) {
			ExportPDFTask exportPdfTask = new ExportPDFTask(file.get(), table, rankingSupplier.get());
			Task openPdfViewerTask = getOpenPdfViewerTask(file.get());
			dialogTaskManager.execute(new TaskIterator(exportPdfTask, openPdfViewerTask));
		}
	}
	
	
	private static Task getOpenPdfViewerTask(File file) {
		return new AbstractTask() {
			@Override
			public void run(TaskMonitor taskMonitor) {
				try {
					if(Desktop.isDesktopSupported()) {
						Desktop.getDesktop().open(file);
					}
				} catch(Exception e) { } // ignore, just make best attempt to open the viewer
			}
		};
	}
	
}


