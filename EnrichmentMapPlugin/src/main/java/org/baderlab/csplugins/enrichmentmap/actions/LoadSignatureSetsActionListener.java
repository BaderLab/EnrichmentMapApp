package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.function.Consumer;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.task.FilterSignatureGSTask;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.task.ResultTaskObserver;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class LoadSignatureSetsActionListener implements ActionListener {

	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private TaskManager<?,?> taskManager;
	private StreamUtil streamUtil;
	
	private String fileName;
	private FilterMetric filterMetric;
	
	private Consumer<SetOfGeneSets> geneSetCallback = x -> {};
	private Consumer<Set<String>> loadedSignatureSetsCallback = x -> {};
	
	public LoadSignatureSetsActionListener(
			String fileName,
			FilterMetric filterMetric,
			CySwingApplication application,
			CyApplicationManager applicationManager, 
			TaskManager<?,?> taskManager,
			StreamUtil streamUtil) {
		
		this.fileName = fileName;
		this.filterMetric = filterMetric;
		
		this.application = application;
		this.applicationManager = applicationManager;
		this.taskManager = taskManager;
		this.streamUtil = streamUtil;
	}
	
	
	public void setGeneSetCallback(Consumer<SetOfGeneSets> geneSetCallback) {
		this.geneSetCallback = geneSetCallback;
	}
	
	public void setLoadedSignatureSetsCallback(Consumer<Set<String>> loadedSignatureSetsCallback) {
		this.loadedSignatureSetsCallback = loadedSignatureSetsCallback;
	}
	
	
	public void actionPerformed(ActionEvent event) {
		// make sure that the minimum information is set in the current set of parameters
		EnrichmentMap current_map = EnrichmentMapManager.getInstance().getMap(applicationManager.getCurrentNetwork().getSUID());

		String errors = checkGMTfiles(fileName);
		if(errors.isEmpty()) {

			// MKTODO warning LoadSignatureGMTFilesTask is side-effecting, it pulls the loaded genes into the EnrichmentMap object
			LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(fileName, current_map, filterMetric, streamUtil);

			TaskObserver taskObserver = new ResultTaskObserver() {
				private SetOfGeneSets resultGeneSets;
				private Set<String> loadedSignatureSets;
				
				/**
				 * @see FilterSignatureGSTask#getResults
				 */
				public void taskFinished(ObservableTask task) {
					if(task instanceof FilterSignatureGSTask) {
						resultGeneSets = task.getResults(SetOfGeneSets.class);
						loadedSignatureSets = task.getResults(Set.class);
					}
				}
				
				public void allFinished(FinishStatus finishStatus) {
					geneSetCallback.accept(resultGeneSets);
					loadedSignatureSetsCallback.accept(loadedSignatureSets);
				}
			};
			
			taskManager.execute(load_GMTs.createTaskIterator(), taskObserver);

		} else {
			JOptionPane.showMessageDialog(application.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

	private static String checkGMTfiles(String signatureGMTFileName) {
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
			return "Signature GMT file can not be found \n";
		return "";
	}
}
