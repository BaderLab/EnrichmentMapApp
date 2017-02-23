package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Sync;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.task.FilterSignatureGSTask;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.util.ResultTaskObserver;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LoadSignatureSetsActionListener implements ActionListener {

	@Inject private CySwingApplication application;
	@Inject private CyApplicationManager applicationManager;
	@Inject private @Sync TaskManager<?,?> taskManager;
	@Inject private EnrichmentMapManager emManager;
	
	private final File file;
	private final FilterMetric filterMetric;
	
	private Consumer<SetOfGeneSets> geneSetCallback = x -> {};
	private Consumer<Set<String>> loadedSignatureSetsCallback = x -> {};
	
	
	public interface Factory {
		LoadSignatureSetsActionListener create(File file, FilterMetric filterMetric);
	}
	
	@Inject
	public LoadSignatureSetsActionListener(@Assisted File file, @Assisted FilterMetric filterMetric) {
		this.file = file;
		this.filterMetric = filterMetric;
	}
	
	/**
	 * For tests to replace the task manager. 
	 * MKTODO: There may be a better way to do this with Guice/Jukito.
	 */
	public void setTaskManager(TaskManager<?,?> taskManager) {
		this.taskManager = taskManager;
	}
	
	public void setGeneSetCallback(Consumer<SetOfGeneSets> geneSetCallback) {
		this.geneSetCallback = geneSetCallback;
	}
	
	public void setLoadedSignatureSetsCallback(Consumer<Set<String>> loadedSignatureSetsCallback) {
		this.loadedSignatureSetsCallback = loadedSignatureSetsCallback;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// make sure that the minimum information is set in the current set of parameters
		EnrichmentMap currentMap = emManager.getEnrichmentMap(applicationManager.getCurrentNetwork().getSUID());

		if (file.canRead()) {
			// MKTODO warning LoadSignatureGMTFilesTask is side-effecting, it pulls the loaded genes into the EnrichmentMap object
			LoadSignatureGMTFilesTask loadGMTs = new LoadSignatureGMTFilesTask(file, currentMap, filterMetric);

			TaskObserver taskObserver = new ResultTaskObserver() {
				private SetOfGeneSets resultGeneSets;
				private Set<String> loadedSignatureSets;

				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof FilterSignatureGSTask) {
						resultGeneSets = task.getResults(SetOfGeneSets.class);
						loadedSignatureSets = task.getResults(Set.class);
					}
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					geneSetCallback.accept(resultGeneSets);
					loadedSignatureSetsCallback.accept(loadedSignatureSets);
				}
			};
			
			taskManager.execute(loadGMTs.createTaskIterator(), taskObserver);
		} else {
			JOptionPane.showMessageDialog(
					application.getJFrame(),
					"Signature GMT file name not valid.\n",
					"Invalid File",
					JOptionPane.WARNING_MESSAGE
			);
		}
	}
}
