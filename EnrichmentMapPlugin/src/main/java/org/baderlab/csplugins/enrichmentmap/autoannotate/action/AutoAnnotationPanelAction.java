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

// $Id: LoadEnrichmentsPanelAction.java 286 2009-07-07 15:27:16Z risserlin $
// $LastChangedDate: 2009-07-07 11:27:16 -0400 (Tue, 07 Jul 2009) $
// $LastChangedRevision: 286 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/LoadEnrichmentsPanelAction.java $

package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.DisplayOptionsPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Properties;

import javax.swing.JOptionPane;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 8:46 AM
 *
 * Click on Annotate Clusters Action handler
 */
public class AutoAnnotationPanelAction extends AbstractCyAction {

	private static final long serialVersionUID = 3764130543697594367L;


	// track initialization of panel (so only one panel is ever created)
	private boolean initialized = false;
	private final CytoPanel cytoPanelWest;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private AutoAnnotationPanel annotationPanel;
	// used to register the panel
	private CyServiceRegistrar registrar;

	public AutoAnnotationPanelAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
			CyNetworkViewManager networkViewManager, CySwingApplication application, AnnotationManager annotationManager, 
			CyServiceRegistrar registrar){

		super( configProps,  applicationManager,  networkViewManager);

		putValue(NAME, "Annotate Clusters");		

		this.cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
		this.applicationManager = applicationManager;
		this.application = application;
		this.registrar = registrar;
	}

	public void actionPerformed(ActionEvent event) {
		if (applicationManager.getCurrentNetworkView() != null) {
			// Only registers one panel
			if(!initialized) {
				AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
				autoAnnotationManager.getDisplayOptionsPanelAction().actionPerformed(new ActionEvent("",0,""));
				annotationPanel = new AutoAnnotationPanel(application, autoAnnotationManager.getDisplayOptionsPanel());
				autoAnnotationManager.setAnnotationPanel(annotationPanel);
				registrar.registerService(annotationPanel, CytoPanelComponent.class, new Properties());
				initialized = true;
			}
			// Update the selected view for the panel (necessary for loading sessions)
			annotationPanel.updateSelectedView(applicationManager.getCurrentNetworkView());
			// If the state of the cytoPanelWest is HIDE, show it
			if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
				cytoPanelWest.setState(CytoPanelState.DOCK);
			}

			// Select my panel
			int inputIndex = cytoPanelWest.indexOfComponent(annotationPanel);
			if (inputIndex != -1) cytoPanelWest.setSelectedIndex(inputIndex);
		} else {
			// Don't create the panel if it can't be used
			JOptionPane.showMessageDialog(application.getJFrame(), "Please load an Enrichment Map.");
		}
	}
}