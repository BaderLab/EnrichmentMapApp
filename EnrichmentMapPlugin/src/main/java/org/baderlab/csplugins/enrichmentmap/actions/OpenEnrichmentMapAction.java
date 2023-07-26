package org.baderlab.csplugins.enrichmentmap.actions;

import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class OpenEnrichmentMapAction implements TaskFactory {
	
	public static final String NAME = "EnrichmentMap";

	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	@Inject private CreationDialogShowAction masterMapDialogAction;

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new OpenEnrichmentMapTask());
	}

	@Override
	public boolean isReady() {
		return true;
	}
	
	
	private class OpenEnrichmentMapTask extends AbstractTask {

		public synchronized void showPanels() {
			controlPanelMediatorProvider.get().showControlPanel();
			heatMapMediatorProvider.get().showHeatMapPanel();
			masterMapDialogAction.showDialog();
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) {
			showPanels();
		}
		
	}
	
}
