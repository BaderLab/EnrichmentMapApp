package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.HashSet;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateGroupsTask extends AbstractTask{

	private AnnotationSet annotationSet;
	private AutoAnnotationParameters params;
	
	private TaskMonitor taskMonitor;
	
	public CreateGroupsTask(AnnotationSet annotationSet,
			AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
	}


	private void createGroups(){
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		CyGroupManager groupManager = autoAnnotationManager.getGroupManager();
		CyGroupFactory groupFactory =autoAnnotationManager.getGroupFactory();		
		
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
						
				//Create a Node with the Annotation Label to represent the group
				CyNode groupNode = this.params.getNetwork().addNode();
				this.params.getNetwork().getRow(groupNode).set(CyNetwork.NAME, cluster.getLabel());
				autoAnnotationManager.flushPayloadEvents();
				
				CyGroup group = groupFactory.createGroup( this.params.getNetwork(), groupNode,new ArrayList<CyNode>(cluster.getNodes()),null, true);							
				cluster.setGroup(group);
				
				//on suggestion from Scooter, remove the group node after the group has been created.
				HashSet<CyNode> removeGroupNode = new HashSet<CyNode>();
				removeGroupNode.add(groupNode);
				this.params.getNetwork().removeNodes(removeGroupNode);
			}
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		this.taskMonitor.setTitle("Creating Groups");
		
		createGroups();
		
	}
	
}
