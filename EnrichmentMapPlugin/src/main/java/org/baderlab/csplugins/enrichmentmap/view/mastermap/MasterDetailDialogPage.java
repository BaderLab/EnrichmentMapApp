package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback.Message;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;

public class MasterDetailDialogPage implements CardDialogPage {

	private CardDialogCallback callback;
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getPageComboText() {
		return "Master/Detail - Experimental";
	}
	
	@Override
	public void finish() {
	}
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		callback.setMessage(Message.ERROR, "BLAH");
		return new JPanel();
	}

	

}
