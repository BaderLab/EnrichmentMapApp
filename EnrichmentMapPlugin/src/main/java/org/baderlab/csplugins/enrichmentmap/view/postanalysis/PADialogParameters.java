package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PADialogParameters implements CardDialogParameters {

	public static final String TITLE = "EnrichmentMap: Add Signature Gene Sets (Post-Analysis)";
	
	@Inject private PADialogPage.Factory pageFactory;
	
	private final EnrichmentMap map;
	private final CyNetworkView view;
	
	public interface Factory {
		PADialogParameters create(EnrichmentMap map, CyNetworkView view);
	}
	
	@Inject
	public PADialogParameters(@Assisted EnrichmentMap map, @Assisted CyNetworkView view) {
		this.map = map;
		this.view = view;
	}
	
	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(
			pageFactory.create(map, view)
		);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(900, 700);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(650, 550);
	}
}
