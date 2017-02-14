package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ExportTXTAction extends AbstractAction {
	
	public ExportTXTAction() {
		super("Export to TXT");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("ExportTXTAction.actionPerformed()");
	}

}
