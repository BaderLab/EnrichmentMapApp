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

// $Id: HeatMapActionListener.java 368 2009-09-25 14:49:59Z risserlin $
// $LastChangedDate: 2009-09-25 10:49:59 -0400 (Fri, 25 Sep 2009) $
// $LastChangedRevision: 368 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/HeatMapActionListener.java $

package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParameters.Transformation;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Heat map action listener
 */
@Deprecated
public class HeatMapTransformActionListener implements ActionListener {

	@Inject private @Nodes Provider<HeatMapPanel> nodesPanelProvider;
	@Inject private @Edges Provider<HeatMapPanel> edgesPanelProvider;
	
	private HeatMapParameters hmParams;
	private EnrichmentMap map;

	
	public interface Factory {
		HeatMapTransformActionListener create(HeatMapParameters hmParams, EnrichmentMap map);
	}
	
	@Inject
	public HeatMapTransformActionListener(@Assisted HeatMapParameters hmParams, @Assisted EnrichmentMap map) {
		this.hmParams = hmParams;
		this.map = map;
	}

	/**
	 * Update heat map according to action selection
	 */
	public void actionPerformed(ActionEvent evt) {
		HeatMapPanel edgeOverlapPanel = edgesPanelProvider.get();
		HeatMapPanel nodeOverlapPanel = nodesPanelProvider.get();
		
		edgeOverlapPanel.clearPanel();
		nodeOverlapPanel.clearPanel();

		JComboBox box = (JComboBox) evt.getSource();
		
		String select = (String) box.getSelectedItem();
		Transformation transformation = Transformation.fromDisplay(select);
		hmParams.setTransformation(transformation);

		hmParams.ResetColorGradient_ds1();

		if(map.getDataset(LegacySupport.DATASET2) != null && map.getDataset(LegacySupport.DATASET2).getExpressionSets() != null
				&& !map.getDataset(LegacySupport.DATASET1).getExpressionSets().getFilename()
						.equalsIgnoreCase(map.getDataset(LegacySupport.DATASET2).getExpressionSets().getFilename()))
			hmParams.ResetColorGradient_ds2();

		edgeOverlapPanel.updatePanel();
		nodeOverlapPanel.updatePanel();
	}
}
