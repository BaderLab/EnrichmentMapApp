package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogShowAction;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class OpenEnrichmentMapAction extends AbstractCyAction implements Task {
	
	public static final String NAME = "EnrichmentMap";

	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	@Inject private CreationDialogShowAction masterMapDialogAction;

	public OpenEnrichmentMapAction() {
		super(NAME);
		setPreferredMenu("Apps");
		setMenuGravity(3.1f);
	}

	public synchronized void showPanels() {
		controlPanelMediatorProvider.get().showControlPanel();
		heatMapMediatorProvider.get().showHeatMapPanel();
		masterMapDialogAction.showDialog();
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
