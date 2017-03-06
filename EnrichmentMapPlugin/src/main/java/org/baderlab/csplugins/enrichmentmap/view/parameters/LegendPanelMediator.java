package org.baderlab.csplugins.enrichmentmap.view.parameters;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDTAndWait;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class LegendPanelMediator implements SetCurrentNetworkViewListener {

	@Inject private EnrichmentMapManager emManager;
	@Inject private Provider<LegendPanel> parametersPanelProvider;
	
	@Inject private CySwingApplication swingApplication;
	
	private JDialog dialog;
	
	public void showDialog(EnrichmentMap map) {
		invokeOnEDT(() -> {
			updateUI(map);
			
			if (dialog != null) {
				dialog.pack();
				dialog.setVisible(true);
			}
		});
	}
	
	public void hideDialog() {
		invokeOnEDT(() -> {
			if (dialog != null)
				dialog.setVisible(false);
		});
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView view = e.getNetworkView();
		EnrichmentMap map = view != null ? emManager.getEnrichmentMap(view.getModel().getSUID()) : null;
		
		// TODO Get cutoffs and other params associated with the NetView
		
		invokeOnEDT(() -> {
			if (dialog != null && dialog.isVisible())
				updateUI(map);
		});
	}
	
	@AfterInjection
	@SuppressWarnings("serial")
	private void init() {
		invokeOnEDTAndWait(() -> {
			dialog = new JDialog(swingApplication.getJFrame(), "EnrichmentMap Legend", ModalityType.MODELESS);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setMinimumSize(new Dimension(440, 380));
			
			JButton closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton);
			
			dialog.getContentPane().add(parametersPanelProvider.get(), BorderLayout.CENTER);
			dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), null, closeButton.getAction());
			dialog.getRootPane().setDefaultButton(closeButton);
			dialog.setLocationRelativeTo(swingApplication.getJFrame());
		});
	}

	private void updateUI(EnrichmentMap map) {
		invokeOnEDT(() -> {
			parametersPanelProvider.get().update(map);
		});
	}
}
