package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenPDFViewerTask;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Task;
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
	private final BooleanSupplier showValuesSupplier;
	
	public interface Factory {
		ExportPDFAction create(JTable table, Supplier<RankingOption> rankingSupplier, BooleanSupplier showValuesSupplier);
	}
	
	@Inject
	public ExportPDFAction(@Assisted JTable table, @Assisted Supplier<RankingOption> rankingSupplier, @Assisted BooleanSupplier showValuesSupplier) {
		super("Export to PDF");
		this.table = table;
		this.rankingSupplier = rankingSupplier;
		this.showValuesSupplier = showValuesSupplier;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Optional<File> file = FileBrowser.promptForPdfExport(fileUtil, jframeProvider.get());
		if(file.isPresent()) {
			HeatMapTableModel model = (HeatMapTableModel) table.getModel();
			ExportPDFTask exportPdfTask = new ExportPDFTask(file.get(), model, rankingSupplier.get(), showValuesSupplier.getAsBoolean());
			exportPdfTask.setRowToModelRow(table::convertRowIndexToModel);
			Task openPdfViewerTask = new OpenPDFViewerTask(file.get());
			dialogTaskManager.execute(new TaskIterator(exportPdfTask, openPdfViewerTask));
		}
	}
	
	
}
