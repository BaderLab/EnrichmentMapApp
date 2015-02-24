package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class RunWordCloudForClustersTaskFactory implements TaskFactory{

	private AnnotationSet annotationSet;
	private AutoAnnotationParameters params;
	private CyNetwork network;
	
	private CommandExecutorTaskFactory executor;
	
	
	
	public RunWordCloudForClustersTaskFactory(AnnotationSet annotationSet,
			AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
		this.network = params.getNetwork();
		
		this.executor = AutoAnnotationManager.getInstance().getCommandExecutor();
	}

	
	@Override
	public TaskIterator createTaskIterator() {
		TreeMap<Integer, Cluster> clusterMap = annotationSet.getClusterMap();
		ArrayList<String> commands = new ArrayList<String>();
		for (int clusterNumber : clusterMap.keySet()) {
			
			//ArrayList<String> commands = new ArrayList<String>();
			
			Cluster cluster = clusterMap.get(clusterNumber);

			Set<CyNode> current_nodes = cluster.getNodes();
			String names = "";
			
			for(CyNode node : current_nodes){
				names = names +  "SUID:" + network.getRow(node).get(CyNetwork.SUID,  Long.class) + ",";
			}
			String command = "wordcloud create wordColumnName=\"" + params.getAnnotateColumnName() + "\"" + 
			" cloudName=\"" + params.getName()+ " Cloud " +  clusterNumber + "\""
			+ " cloudGroupTableName=\"" + params.getName() + "\"" + " nodelist=\"" + names + "\"";
			
			commands.add(command);
		}
		
		return executor.createTaskIterator(commands, null);	
	
	}



	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}



	
}
