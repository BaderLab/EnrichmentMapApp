package org.baderlab.csplugins.enrichmentmap.task;

import java.util.ArrayList;

import org.baderlab.csplugins.enrichmentmap.autoannotate.RunWordCloudObserver;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class RunWordCloudTask implements ObservableTask{

	private CyServiceRegistrar registrar;
	private String clusterColumnName;
	private String nameColumnName;

	public RunWordCloudTask(CyServiceRegistrar registrar, String clusterColumnName, String nameColumnName) {
		this.registrar = registrar;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
	}
	
	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("wordcloud build clusterColumnName=\"" + clusterColumnName
				+ "\" nameColumnName=\"" + nameColumnName + "\"");
		TaskIterator task = executor.createTaskIterator(commands, null);
		RunWordCloudObserver observer = new RunWordCloudObserver();
		registrar.getService(SynchronousTaskManager.class).execute(task);
	}

	@Override
	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
