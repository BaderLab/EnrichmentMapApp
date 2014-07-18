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

	private String algorithm;
	private String edgeAttribute;
	private DialogTaskManager dialogTaskManager;
	private CyServiceRegistrar registrar;
	private String networkName;
	
	public ClusterMakerTask(CyNetworkView selectedView, String algorithm, 
			String edgeAttribute, DialogTaskManager dialogTaskManager, CyServiceRegistrar registrar) {
		this.networkName = selectedView.getModel().toString();
		this.algorithm = algorithm;
		this.edgeAttribute = edgeAttribute;
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
			command = "cluster ap adjustLoops=true attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__APCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Cluster Fuzzifier") {
			command = "cluster fuzzifier adjustLoops=false attribute=\"" + edgeAttribute + "\" "
					+ "clusterAttribute=\"__fuzzifierCluster\" createGroups=false "
					+ "network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Community cluster (GLay)") {
			command = "cluster glay clusterAttribute=\"__glayCluster\" createGroups=false network=\""
					+ networkName + "\" restoreEdges=false selectedOnly=false "
					+ "showUI=false undirectedEdges=true";
		} else if (algorithm == "ConnectedComponents Cluster") {
			command = "cluster connectedcomponents adjustLoops=true attribute=\"" + edgeAttribute + "\" clusterAttribute=\""
					+ "__ccCluster\" createGroups=false network=\"" + networkName
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "Fuzzy C-Means Cluster") {
			command = "cluster fcml adjustLoops=false attribute=\"" + edgeAttribute + "\" "
					+ "clusterAttribute=\"__fcmCluster\" createGroups=false "
					+ "estimateClusterNumber=true network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "MCL Cluster") {
			command = "cluster mcl adjustLoops=false attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__mclCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" "
					+ "restoreEdges=false selectedOnly=false showUI=false undirectedEdges=true";
		} else if (algorithm == "SCPS Cluster") {
			command = "cluster scps adjustLoops=false attribute=\"" + edgeAttribute + "\" clusterAttribute=\"__scpsCluster\" "
					+ "createGroups=false network=\"" + networkName + "\" restoreEdges=false "
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
