package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.util.WeakHashMap;

import javax.swing.JFrame;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class PADialogMediator  {

	public static final String NAME = "Add Signature Gene Sets...";
	
	@Inject private EnrichmentMapManager emManager;
	@Inject private PADialogParameters.Factory paDialogParametersFactory;
	@Inject private Provider<JFrame> jFrameProvider;
	
	
	private WeakHashMap<EnrichmentMap,CardDialog> dialogs = new WeakHashMap<>();
	

	public void showDialog(CyNetworkView netView) {
		EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
		if(map == null)
			return;
		
		CardDialog dialog = dialogs.computeIfAbsent(map, k -> {
			CardDialogParameters params = paDialogParametersFactory.create(map);
			return new CardDialog(jFrameProvider.get(), params);
		});
		
		dialog.open();
	}
	
	public void removeEnrichmentMap(EnrichmentMap map) {
		dialogs.remove(map);
	}
}
