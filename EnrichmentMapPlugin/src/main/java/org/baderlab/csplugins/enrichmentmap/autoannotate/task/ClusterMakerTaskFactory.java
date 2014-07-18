package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class ClusterMakerTaskFactory implements TaskFactory {

	private CyNetworkView selectedView;
	private String algorithm;
	private DialogTaskManager dialogTaskManager;
	private CyServiceRegistrar registrar;
	private String edgeAttribute;
	
	public ClusterMakerTaskFactory(CyNetworkView selectedView, String algorithm,
			String edgeAttribute, DialogTaskManager dialogTaskManager, CyServiceRegistrar registrar) {
		
		this.selectedView = selectedView;
		this.algorithm = algorithm;
		this.edgeAttribute = edgeAttribute;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		return new TaskIterator(new ClusterMakerTask(selectedView, algorithm, edgeAttribute, dialogTaskManager, registrar));
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
