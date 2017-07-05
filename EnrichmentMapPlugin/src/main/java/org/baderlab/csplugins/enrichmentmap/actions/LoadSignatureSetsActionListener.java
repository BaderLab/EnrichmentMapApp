package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Sync;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterSignatureGSTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.util.NamingUtil;
import org.baderlab.csplugins.enrichmentmap.util.ResultTaskObserver;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class LoadSignatureSetsActionListener implements ActionListener {

	private static final String FILE_EXT = ".gmt";
	
	@Inject private CySwingApplication application;
	@Inject private @Sync TaskManager<?,?> taskManager;
	
	private final File file;
	private final FilterMetric filterMetric;
	private final EnrichmentMap map;
	
	private SetOfGeneSets resultGeneSets;
	private Set<String> filteredSignatureSets;
	private String autoName;
	
	
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
	 * @noreference
	 */
	public void setTaskManager(TaskManager<?,?> taskManager) {
		this.taskManager = taskManager;
	}
	
	public SetOfGeneSets getResultGeneSets() {
		return resultGeneSets;
	}
	
	public Set<String> getFilteredSignatureSets() {
		return filteredSignatureSets;
	}
	
	public String getAutoName() {
		return autoName;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// make sure that the minimum information is set in the current set of parameters
		if (file.canRead()) {
			// MKTODO warning LoadSignatureGMTFilesTask is side-effecting, it pulls the loaded genes into the EnrichmentMap object
			LoadSignatureGMTFilesTask loadGMTs = new LoadSignatureGMTFilesTask(file, map, filterMetric);
			
			TaskObserver taskObserver = new ResultTaskObserver() {
				@SuppressWarnings("unchecked")
				@Override
				public void taskFinished(ObservableTask task) {
					if (task instanceof FilterSignatureGSTask) {
						resultGeneSets = task.getResults(SetOfGeneSets.class);
						filteredSignatureSets = task.getResults(Set.class);
						
						String name = file.getName();
						if(name.toLowerCase().endsWith(FILE_EXT))
							name = name.substring(0, name.length() - FILE_EXT.length()).trim();
						autoName = NamingUtil.getUniqueName(name, map.getSignatureDataSets().keySet());
					}
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
