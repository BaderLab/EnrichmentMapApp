package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.view.controlpanel.ControlPanel;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@SuppressWarnings("serial")
@Singleton
public class ShowControlPanelAction extends AbstractCyAction {
	
	public static final String SHOW_NAME = "Show EnrichmentMap Panel";
	public static final String HIDE_NAME = "Hide EnrichmentMap Panel";
	
	@Inject private CyServiceRegistrar registrar;
	@Inject private CySwingApplication swingApplication;
	@Inject private Provider<ControlPanel> controlPanelProvider;

	private ControlPanel panel = null;
	
	
	public ShowControlPanelAction() {
		super(SHOW_NAME);
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		if(panel == null) {
			panel = controlPanelProvider.get();
			registrar.registerAllServices(panel, new Properties());
			setName(HIDE_NAME);
			bringToFront();
		}
		else {
			registrar.unregisterAllServices(panel);
			panel.dispose();
			panel = null;
			setName(SHOW_NAME);
		}
	}
	
	private void bringToFront() {
		CytoPanelName compassPoint = panel.getCytoPanelName();
		CytoPanel cytoPanel = swingApplication.getCytoPanel(compassPoint);
		int index = cytoPanel.indexOfComponent(panel.getIdentifier());
		cytoPanel.setSelectedIndex(index);
	}

}
