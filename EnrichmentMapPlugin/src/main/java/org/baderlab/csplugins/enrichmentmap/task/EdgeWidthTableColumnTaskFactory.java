package org.baderlab.csplugins.enrichmentmap.task;

import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.actions.ShowEdgeWidthPanelAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class EdgeWidthTableColumnTaskFactory implements TableColumnTaskFactory {

	private final ShowEdgeWidthPanelAction action;
	
	public EdgeWidthTableColumnTaskFactory(ShowEdgeWidthPanelAction action) {
		this.action = action;
	}

	@Override
	public TaskIterator createTaskIterator(CyColumn column) {
		return new TaskIterator(new Task() {
			public void run(TaskMonitor tm) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						action.actionPerformed(null);
					}
				});
			}
			public void cancel() { }
		});
	}

	@Override
	public boolean isReady(CyColumn column) {
		return column != null 
		    && column.getName() != null
		    && column.getName().endsWith(PostAnalysisVisualStyle.EDGE_WIDTH_FORMULA_COLUMN);
	}


}
