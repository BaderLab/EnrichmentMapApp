package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PADialogParameters implements CardDialogParameters {

	public static final String TITLE = "EnrichmentMap: Add Signature Gene Sets (Post-Analysis)";
	
	@Inject private PADialogPage.Factory pageFactory;
	
	private final EnrichmentMap map;
	
	public interface Factory {
		PADialogParameters create(EnrichmentMap map);
	}
	
	@Inject
	public PADialogParameters(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(pageFactory.create(map));
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
