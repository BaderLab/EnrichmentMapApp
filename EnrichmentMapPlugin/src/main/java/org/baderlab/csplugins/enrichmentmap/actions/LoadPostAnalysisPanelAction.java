/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

import com.google.inject.Inject;


@SuppressWarnings("serial")
public class LoadPostAnalysisPanelAction extends AbstractCyAction {

	@Inject private CyServiceRegistrar registrar;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CySwingApplication swingApplication;
	
	private PostAnalysisPanel inputPanel;

	public LoadPostAnalysisPanelAction() {
		super("Load Post Analysis Panel");
	}
	
	public LoadPostAnalysisPanelAction init(PostAnalysisPanel inputPanel) {
		this.inputPanel = inputPanel;
		return this;
	}

	public void actionPerformed(ActionEvent event) {
		CyNetwork network = applicationManager.getCurrentNetwork();
		if(network == null) {
			JOptionPane.showMessageDialog(swingApplication.getJFrame(), "Please select a network first", "EnrichmentMap: Post Analysis", JOptionPane.WARNING_MESSAGE);
			return;
		}

		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);
		
		//if the service has not been registered
		if(cytoPanel.indexOfComponent(inputPanel) == -1) {
			registrar.registerService(inputPanel, CytoPanelComponent.class, new Properties());

			//set the input window in the instance so we can udate the instance window on network focus
			EnrichmentMapManager.getInstance().setAnalysisWindow(inputPanel);

			EnrichmentMap map = EnrichmentMapManager.getInstance().getMap(network.getSUID());
			inputPanel.showPanelFor(map);
		}

		// If the state of the cytoPanelWest is HIDE, show it
		if(cytoPanel.getState() == CytoPanelState.HIDE) {
			cytoPanel.setState(CytoPanelState.DOCK);
		}

		// Select my panel
		int index = cytoPanel.indexOfComponent(inputPanel);
		if(index == -1) {
			return;
		}
		cytoPanel.setSelectedIndex(index);
	}
}
