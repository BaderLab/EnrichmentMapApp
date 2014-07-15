package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class ClusterMakerTask implements Task {

	private CyNetworkView selectedView;
	private String algorithm;
	private DialogTaskManager dialogTaskManager;
	private CyServiceRegistrar registrar;
	private CyNetwork network;
	
	public ClusterMakerTask(CyNetworkView selectedView, String algorithm, 
			DialogTaskManager dialogTaskManager, CyServiceRegistrar registrar) {
		this.selectedView = selectedView;
		this.network = selectedView.getModel();
		this.algorithm = algorithm;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
	}

	@Override
	public void cancel() {
		return;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		String command = null;
		if (algorithm == "Affinity Propagation Cluster") {
			command = "cluster ap adjustLoops=true attribute=\"--None--\" clusterAttribute=\"__APCluster\" "
					+ "createGroups=false edgeWeighter=\"None\" lambda=0.5 network=\""
					+ network.toString() + "\" preference=-1.0 rNumber=8 restoreEdges=false selectedOnly=false "
					+ "showUI=false undirectedEdges=true";
		} else if (algorithm == "Community cluster (GLay)") {
			command = "cluster glay clusterAttribute=\"__glayCluster\" createGroups=false network=\""
					+ network.toString() + "\" restoreEdges=false selectedOnly=false "
					+ "showUI=false undirectedEdges=true";
		} else if (algorithm == "ConnectedComponents Cluster") {
			command = "cluster connectedcomponents adjustLoops=true attribute=\"--None--\" clusterAttribute=\""
					+ "__ccCluster\" createGroups=false edgeWeighter=\"None\" network=\"" + network.toString()
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "MCL Cluster") {
			command = "cluster mcl adjustLoops=false attribute=\"--None--\" clusterAttribute=\"__mclCluster\" "
					+ "createGroups=false edgeWeighter=\"None\" network=\"" + network.toString() + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "SCPS Cluster") {
			command = "cluster scps adjustLoops=false attribute=\"--None--\" clusterAttribute=\"__scpsCluster\" "
					+ "createGroups=false edgeWeighter=\"None\" network=\"EM1_Enrichment Map\" restoreEdges=false "
					+ "selectedOnly=false showUI=false undirectedEdges=true";
		}
		commands.add(command);
		TaskIterator task = executor.createTaskIterator(commands, null);
		try {
			dialogTaskManager.execute(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
