package org.baderlab.csplugins.enrichmentmap.view.parameters;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDTAndWait;

import java.awt.Dialog.ModalityType;

import javax.swing.JDialog;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class ParametersPanelMediator implements SetCurrentNetworkViewListener {

	@Inject private EnrichmentMapManager emManager;
	@Inject private Provider<ParametersPanel> parametersPanelProvider;
	
	@Inject private CySwingApplication swingApplication;
	
	private JDialog dialog;
	
	public void showDialog(EnrichmentMap map) {
		invokeOnEDT(() -> {
			updateParameters(map);
			
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
		
		// TODO Get cuttofs and other params associated with the NetView
		
		invokeOnEDT(() -> {
			if (dialog != null && dialog.isVisible())
				updateParameters(map);
		});
	}
	
	@AfterInjection
	private void init() {
		invokeOnEDTAndWait(() -> {
			dialog = new JDialog(swingApplication.getJFrame(), "EnrichmentMap Legend", ModalityType.MODELESS);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.getContentPane().add(parametersPanelProvider.get());
		});
	}

	private void updateParameters(EnrichmentMap map) {
		invokeOnEDT(() -> {
			parametersPanelProvider.get().update(map);
		});
	}
}
