package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.baderlab.csplugins.enrichmentmap.task.ResultTaskObserver;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

public class LoadSignatureSetsActionListener implements ActionListener {

	private PostAnalysisInputPanel inputPanel;
	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private DialogTaskManager dialog;
	private StreamUtil streamUtil;
	
	private boolean selectAll = false;
	

	public LoadSignatureSetsActionListener(PostAnalysisInputPanel inputPanel,
			CySwingApplication application,
			CyApplicationManager applicationManager, 
			DialogTaskManager dialog,
			StreamUtil streamUtil) {
		this.inputPanel = inputPanel;
		this.application = application;
		this.applicationManager = applicationManager;
		this.dialog = dialog;
		this.streamUtil = streamUtil;
	}

	public void setSelectAll(boolean selectAll) {
		this.selectAll = selectAll;
	}
	
	public void actionPerformed(ActionEvent event) {

		// make sure that the minimum information is set in the current set of parameters
		EnrichmentMap current_map = EnrichmentMapManager.getInstance().getMap(applicationManager.getCurrentNetwork().getSUID());

		final PostAnalysisParameters paParams = inputPanel.getPaParams();
		
		String errors = paParams.checkGMTfiles();
		if(errors.isEmpty()) {

			// make sure to clear out the signature sets first (bug #61)
        	paParams.getSignatureSetNames().clear();
        	paParams.getSelectedSignatureSetNames().clear();
        	paParams.getSignatureGenesets().clear();
        	
        	LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(current_map, paParams, streamUtil, inputPanel);
        	
        	if(selectAll) {
        		TaskObserver taskObserver = new ResultTaskObserver() {
					public void allFinished(FinishStatus finishStatus) {
						// Select all of the signature sets in the file
						DefaultListModel<String> signatureSets = paParams.getSignatureSetNames();
						DefaultListModel<String> selectedSignatureSets = paParams.getSelectedSignatureSetNames();
						selectedSignatureSets.clear();
						for(String name : Collections.list(signatureSets.elements())) {
							selectedSignatureSets.addElement(name);
						}
						signatureSets.clear();
					}
				};
				
				dialog.execute(load_GMTs.createTaskIterator(), taskObserver);
        	}
        	else {
        		dialog.execute(load_GMTs.createTaskIterator());
        	}
			

		} else {
			JOptionPane.showMessageDialog(application.getJFrame(), errors, "Invalid Input", JOptionPane.WARNING_MESSAGE);
		}
	}

}
