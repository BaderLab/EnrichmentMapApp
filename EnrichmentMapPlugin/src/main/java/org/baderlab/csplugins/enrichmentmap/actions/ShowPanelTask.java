package org.baderlab.csplugins.enrichmentmap.actions;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ShowPanelTask extends AbstractTask {

	private final CySwingApplication swingApplication;

	private CytoPanelComponent panel;
	private CytoPanelName compassPoint = CytoPanelName.EAST;

	public ShowPanelTask(CySwingApplication swingApplication, CytoPanelComponent panel) {
		this.swingApplication = swingApplication;
		this.panel = panel;
	}

	public ShowPanelTask setCompassPoint(CytoPanelName point) {
		this.compassPoint = point;
		return this;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		bringToFront();
	}

	private void bringToFront() {
		if(swingApplication != null && panel != null) {
			CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
			if(cytoPanel != null) {
				int index = cytoPanel.indexOfComponent(panel.getComponent());
				CytoPanelState state = cytoPanel.getState();

				if(state == CytoPanelState.HIDE) {
					cytoPanel.setState(CytoPanelState.DOCK);
				}
				cytoPanel.setSelectedIndex(index);
			}
		}
	}

}
