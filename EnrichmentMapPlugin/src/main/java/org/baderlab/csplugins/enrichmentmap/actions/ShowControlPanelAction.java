package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.view.controlpanel.ControlPanelMediator;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class ShowControlPanelAction extends AbstractCyAction {
	
	public static final String SHOW_NAME = "Show EnrichmentMap Panel";

	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;

	public ShowControlPanelAction() {
		super(SHOW_NAME);
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		controlPanelMediatorProvider.get().showControlPanel();
	}
}
