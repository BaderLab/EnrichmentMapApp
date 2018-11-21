package org.baderlab.csplugins.enrichmentmap.view;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.cytoscape.application.swing.CyColumnPresentation;

public class EMColumnPresentation implements CyColumnPresentation {

	private final Icon icon; 
	
	public EMColumnPresentation() {
		URL logoURL = getClass().getClassLoader().getResource("images/enrichmentmap_logo_16.png");
		icon = new ImageIcon(logoURL);
	}
	
	@Override
	public Icon getNamespaceIcon() {
		return icon;
	}

	@Override
	public String getNamespaceDescription() {
		return null;
	}

}
