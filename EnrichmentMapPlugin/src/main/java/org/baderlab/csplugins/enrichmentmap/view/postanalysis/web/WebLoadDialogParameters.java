package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

public class WebLoadDialogParameters implements CardDialogParameters {

	public static final String TITLE = "Signature Gene Sets";
	
	private final PADialogPage parent;
	
	
	public WebLoadDialogParameters(PADialogPage parent) {
		this.parent = parent;
	}
	
	
	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(
			new BaderlabDialogPage(parent)
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
