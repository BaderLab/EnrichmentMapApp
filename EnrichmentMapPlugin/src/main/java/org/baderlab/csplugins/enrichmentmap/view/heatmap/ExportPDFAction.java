package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskIterator;
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
		File file = promptForFile();
		
		if(file != null) {
			ExportPDFTask task = new ExportPDFTask(file, table, rankingSupplier.get());
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}
	
	private File promptForFile() {
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("pdf Files", "pdf"));
		File file = fileUtil.getFile(jframeProvider.get(), "Export Heatmap as PDF File", FileUtil.SAVE, filter);
		if(file != null) {
			String fileName = file.toString();
			if(!fileName.endsWith(".pdf")) {
				file = new File(fileName + ".pdf");
			}
		}
		return file;
	}
}


