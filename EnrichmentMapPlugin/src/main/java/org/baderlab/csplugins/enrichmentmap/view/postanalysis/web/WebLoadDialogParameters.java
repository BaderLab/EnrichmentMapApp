package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

public class WebLoadDialogParameters implements CardDialogParameters<SetOfGeneSets> {

	public static final String TITLE = "Signature Gene Sets";
	

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<CardDialogPage<SetOfGeneSets>> getPages() {
		return Arrays.asList(
			new BaderlabDialogPage()
		);
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(800, 500);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(400, 400);
	}

}
