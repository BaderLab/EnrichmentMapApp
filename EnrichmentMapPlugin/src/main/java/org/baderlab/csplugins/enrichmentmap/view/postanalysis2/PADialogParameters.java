package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PADialogParameters implements CardDialogParameters {

	public static final String TITLE = "EnrichmentMap: Add Signature Gene Sets (Post-Analysis)";
	
	@Inject private Provider<PADialogPage> pageProvider;
	
	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(pageProvider.get());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(820, 700);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(650, 550);
	}
}
