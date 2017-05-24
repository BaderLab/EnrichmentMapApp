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
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterSignatureGSTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.LoadSignatureGMTFilesTask;
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
	private final EnrichmentMap map;
	
	private Consumer<SetOfGeneSets> geneSetCallback = x -> {};
	private Consumer<Set<String>> filteredSignatureSetsCallback = x -> {};
	
	
	public interface Factory {
		LoadSignatureSetsActionListener create(File file, FilterMetric filterMetric, EnrichmentMap map);
	}
	
	@Inject
	public LoadSignatureSetsActionListener(@Assisted File file, @Assisted FilterMetric filterMetric, @Assisted EnrichmentMap map) {
		this.file = file;
		this.filterMetric = filterMetric;
		this.map = map;
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
	
	public void setFilteredSignatureSetsCallback(Consumer<Set<String>> filteredSignatureSetsCallback) {
		this.filteredSignatureSetsCallback = filteredSignatureSetsCallback;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// make sure that the minimum information is set in the current set of parameters
		if (file.canRead()) {
			// MKTODO warning LoadSignatureGMTFilesTask is side-effecting, it pulls the loaded genes into the EnrichmentMap object
			LoadSignatureGMTFilesTask loadGMTs = new LoadSignatureGMTFilesTask(file, map, filterMetric);

			TaskObserver taskObserver = new ResultTaskObserver() {
				private SetOfGeneSets resultGeneSets;
				private Set<String> filteredSignatureSets;

				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof FilterSignatureGSTask) {
						resultGeneSets = task.getResults(SetOfGeneSets.class);
						filteredSignatureSets = task.getResults(Set.class);
					}
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					geneSetCallback.accept(resultGeneSets);
					filteredSignatureSetsCallback.accept(filteredSignatureSets);
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
