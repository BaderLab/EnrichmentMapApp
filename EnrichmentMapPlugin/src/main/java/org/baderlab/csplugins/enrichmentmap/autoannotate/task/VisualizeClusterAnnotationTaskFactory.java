package org.baderlab.csplugins.enrichmentmap.autoannotate.task;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DrawClusterEllipseTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DrawClusterLabelTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;


public class VisualizeClusterAnnotationTaskFactory implements TaskFactory{
	
	private Cluster cluster;
			
	public VisualizeClusterAnnotationTaskFactory(Cluster cluster) {
		super();
		this.cluster = cluster;
	}
			
	@Override
	public TaskIterator createTaskIterator() {
		
		TaskIterator currentTasks = new TaskIterator();
		if(cluster != null){
			DrawClusterEllipseTask drawEllipseTask = new DrawClusterEllipseTask(cluster);
			currentTasks.append(drawEllipseTask);
			DrawClusterLabelTask drawLabelTask = new DrawClusterLabelTask(cluster);
			currentTasks.append(drawLabelTask);
 		}
		
		return currentTasks;
	}

	@Override
	public boolean isReady() {
		if(cluster != null)
			return true;
		return false;
	}
}
