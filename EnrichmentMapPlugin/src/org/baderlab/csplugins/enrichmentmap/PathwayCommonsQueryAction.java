package org.baderlab.csplugins.enrichmentmap;

import giny.model.Node;

import java.awt.event.ActionEvent;
import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.*;

public class PathwayCommonsQueryAction extends CytoscapeAction {
	private Node node;
	boolean queryByNodeName = false;
	public PathwayCommonsQueryAction(Node node, boolean queryByNodeName) {
		super("Query by Node Label...");
		this.queryByNodeName = queryByNodeName;
		this.node = node;
	}
	public PathwayCommonsQueryAction(Node node) {
		super("Query by Gene List...");
		this.node = node;
	}
	public void actionPerformed(ActionEvent evt) {
		final PathwayCommonsQueryTask task = new PathwayCommonsQueryTask(node, queryByNodeName);

		// Configure JTask Dialog Pop-Up Box
		final JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(false);
		jTaskConfig.displayCancelButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(true);
		

		// Execute Task in New Thread; pop open JTask Dialog Box.
		TaskManager.executeTask(task, jTaskConfig);
	}

}

