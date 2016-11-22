package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PostAnalysisPanelMediator implements SetCurrentNetworkViewListener, NetworkAboutToBeDestroyedListener {

	@Inject private EnrichmentMapManager emManager;
	@Inject private Provider<PostAnalysisPanel> postAnalysisPanelProvider;
	
	@Inject private CyServiceRegistrar serviceRegistrar;
	@Inject private CySwingApplication swingApplication;
	
	@SuppressWarnings("serial")
	public void showDialog(EnrichmentMap map) {
		invokeOnEDT(() -> {
			JDialog dialog = new JDialog(swingApplication.getJFrame(), "EnrichmentMap - Advanced Options",
					ModalityType.APPLICATION_MODAL);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.getContentPane().add(postAnalysisPanelProvider.get());
			
			postAnalysisPanelProvider.get().update(map);
			
			JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL,
					"Online Manual...", serviceRegistrar);

			JButton resetButton = new JButton("Reset");
			resetButton.addActionListener(e -> postAnalysisPanelProvider.get().reset(map));

			JButton closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			JButton runButton = new JButton(new AbstractAction("Run") {
				@Override
				public void actionPerformed(ActionEvent e) {
					postAnalysisPanelProvider.get().run(map);
				}
			});

			JPanel buttonPanel =  LookAndFeelUtil.createOkCancelPanel(runButton, closeButton, helpButton, resetButton);
			
			dialog.getContentPane().add(postAnalysisPanelProvider.get(), BorderLayout.CENTER);
			dialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), runButton.getAction(),
					closeButton.getAction());
			dialog.getRootPane().setDefaultButton(runButton);
			
			dialog.pack();
			dialog.setLocationRelativeTo(postAnalysisPanelProvider.get());
			dialog.setVisible(true);
		});
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		// Make sure to clear the panel if there is no network view
		CyNetworkView view = e.getNetworkView();
		EnrichmentMap map = view != null ? emManager.getEnrichmentMap(view.getModel().getSUID()) : null;
		postAnalysisPanelProvider.get().update(map);
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		EnrichmentMap map = emManager.removeEnrichmentMap(e.getNetwork().getSUID());
		
		if (map != null)
			postAnalysisPanelProvider.get().removeEnrichmentMap(map);
	}

	public void updateUI(EnrichmentMap map) {
		postAnalysisPanelProvider.get().update(map);
	}
}
