package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class OpenEnrichmentMapPanelsAction extends AbstractCyAction implements Task {
	
	public static final String NAME = "EnrichmentMap";

	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;

	public OpenEnrichmentMapPanelsAction() {
		super(NAME);
		setPreferredMenu("Apps");
	}

	public synchronized void showPanels() {
		controlPanelMediatorProvider.get().showControlPanel();
		heatMapMediatorProvider.get().showHeatMapPanel();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		showPanels();
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		showPanels();
	}
	
	@Override
	public void cancel() {
	}
}
