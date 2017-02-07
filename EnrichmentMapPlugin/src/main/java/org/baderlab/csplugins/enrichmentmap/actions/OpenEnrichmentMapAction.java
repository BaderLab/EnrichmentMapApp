package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.expression.ExpressionViewerMediator;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class OpenEnrichmentMapAction extends AbstractCyAction {
	
	public static final String NAME = "EnrichmentMap";

	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private Provider<ExpressionViewerMediator> expressionViewerMediatorProvider;

	public OpenEnrichmentMapAction() {
		super(NAME);
		setPreferredMenu("Apps");
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		controlPanelMediatorProvider.get().showControlPanel();
		expressionViewerMediatorProvider.get().showExpressionViewerPanel();
	}
}
