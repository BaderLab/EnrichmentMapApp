package org.baderlab.csplugins.enrichmentmap.view.expression;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public class ExportPDFAction extends AbstractAction {

	public ExportPDFAction() {
		super("Export to PDF");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("ExportPDFAction.actionPerformed()");
	}

}
