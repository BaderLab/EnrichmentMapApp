package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.task.ResultTaskObserver;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;

public class LoadSignatureSetsActionListener implements ActionListener {

	private PostAnalysisInputPanel inputPanel;
	private StreamUtil streamUtil;
	private boolean selectAll = false;
	
	private final TaskManager<?,?> taskManager;
	private final CyServiceRegistrar serviceRegistrar;
	
	public LoadSignatureSetsActionListener(
			PostAnalysisInputPanel inputPanel,
			TaskManager<?,?> taskManager,
			StreamUtil streamUtil,
			CyServiceRegistrar serviceRegistrar
	) {
		this.inputPanel = inputPanel;
		this.streamUtil = streamUtil;
		this.taskManager = taskManager;
		this.serviceRegistrar = serviceRegistrar;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		// make sure that the minimum information is set in the current set of parameters
		CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		EnrichmentMap currentMap = EnrichmentMapManager.getInstance().getMap(applicationManager.getCurrentNetwork().getSUID());

		final PostAnalysisParameters paParams = inputPanel.getPaParams();
		String errors = paParams.checkGMTfiles();
		
		if (errors.isEmpty()) {
			// make sure to clear out the signature sets first (bug #61)
			paParams.getSignatureSetNames().clear();
			paParams.getSelectedSignatureSetNames().clear();
			paParams.getSignatureGenesets().clear();

			LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(currentMap, paParams, streamUtil, inputPanel);
        	
			if (selectAll) {
				TaskObserver taskObserver = new ResultTaskObserver() {
					@Override
					public void allFinished(FinishStatus finishStatus) {
						// Select all of the signature sets in the file
						DefaultListModel<String> signatureSets = paParams.getSignatureSetNames();
						DefaultListModel<String> selectedSignatureSets = paParams.getSelectedSignatureSetNames();
						selectedSignatureSets.clear();
						
						for (String name : Collections.list(signatureSets.elements()))
							selectedSignatureSets.addElement(name);
						
						signatureSets.clear();
					}
				};

				taskManager.execute(load_GMTs.createTaskIterator(), taskObserver);
			} else {
				TaskObserver taskObserver = new ResultTaskObserver() {
					@Override
					public void allFinished(FinishStatus finishStatus) {
						SwingUtilities.invokeLater(() -> {
							inputPanel.update();
						});
					}
				};
				
				taskManager.execute(load_GMTs.createTaskIterator(), taskObserver);
			}
		} else {
			CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);
			JOptionPane.showMessageDialog(application.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

}
