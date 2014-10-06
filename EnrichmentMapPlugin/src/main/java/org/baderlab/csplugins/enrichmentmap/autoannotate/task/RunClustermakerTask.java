package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class RunClustermakerTask extends AbstractTask {
	

	private CyNetwork network;
	private CyNetworkView view;
	private String clusterColumnName;
	private String algorithm;

	
	private TaskMonitor taskMonitor = null;
	
	
	public RunClustermakerTask(AutoAnnotationParameters params) {
		super();
		
		this.network = params.getNetwork();
		this.view = params.getNetworkView();
		this.clusterColumnName = params.getClusterColumnName();
		this.algorithm = params.getAlgorithm();

	}

	
	private void runClusterMaker() {
		// Delete potential existing columns - sometimes clusterMaker doesn't do this
		if (network.getDefaultNodeTable().getColumn(clusterColumnName) != null) {
			network.getDefaultNodeTable().deleteColumn(clusterColumnName);
		}
		
		// Cluster based on similarity coefficient if possible
		String edgeAttribute;
		try {
			edgeAttribute = EnrichmentMapManager.getInstance().getCyNetworkList().get(view.getModel().getSUID()).getParams().getAttributePrefix() + EnrichmentMapVisualStyle.SIMILARITY_COEFFICIENT;
		} catch (NullPointerException e) {
			edgeAttribute = "--None--";
		}
		
		for (View<CyNode> nodeView : view.getNodeViews()) {
			if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) {
				network.getRow(nodeView.getModel()).set(CyNetwork.SELECTED, true);
			}
		}
		
		// Executes the task inside of clusterMaker
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(getCommand(algorithm, edgeAttribute, network.toString()));
		Observer observer = new Observer();
		TaskIterator taskIterator = AutoAnnotationManager.getInstance().getCommandExecutor().createTaskIterator(commands, null);
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(taskIterator, observer);
		while (!observer.isFinished()) {
			// Prevents task from continuing to execute until clusterMaker has finished
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private String getCommand(String algorithm, String edgeAttribute, String networkName) {
		String command = "";
		if (algorithm == "Affinity Propagation Cluster") {
			command = "cluster ap attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Cluster Fuzzifier") {
			command = "cluster fuzzifier attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Community cluster (GLay)") {
			command = "cluster glay clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "ConnectedComponents Cluster") {
			command = "cluster connectedcomponents attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "Fuzzy C-Means Cluster") {
			command = "cluster fcml attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "MCL Cluster") {
			command = "cluster mcl attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		} else if (algorithm == "SCPS Cluster") {
			command = "cluster scps attribute=\"" + edgeAttribute + "\" clusterAttribute=\"" + clusterColumnName + "\" selectedOnly=true";
		}
		return command;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;

		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Clustering nodes...");
		runClusterMaker();
		
				
	}
}
