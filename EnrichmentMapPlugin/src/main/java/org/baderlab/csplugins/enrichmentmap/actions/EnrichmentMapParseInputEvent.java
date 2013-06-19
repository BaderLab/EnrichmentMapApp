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



import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildGMTEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.LoadInputDataTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.VisualizeEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 1:35:40 PM
 * <p>
 * Action associated with the Build map button on the input panel
 * Checks minimum information is supplied before launching main build Enrichment map task
 */
public class EnrichmentMapParseInputEvent {
    
    private EnrichmentMap map;
    private EnrichmentMapInputPanel inputPanel;
	//services required
	private StreamUtil streamUtil;   
    private DialogTaskManager dialog;

	


    /**
     * @param panel - Enrichment map input panel
     */
    public EnrichmentMapParseInputEvent (EnrichmentMapInputPanel panel, EnrichmentMap map,
    		DialogTaskManager dialog, StreamUtil streamUtil) {
        this.map = map;
        this.inputPanel = panel;
        
       this.streamUtil = streamUtil;
       this.dialog = dialog;

    }

    /**
     * Creates a new task, checks the info in the parameters for the minimum amount of information
     *
     * @param event
     */
    public void build() {
       
       
       //add the two datasets from the panel to the parameters
       map.getParams().addFiles(EnrichmentMap.DATASET1, inputPanel.getDataset1files());
       if(!inputPanel.getDataset2files().isEmpty())
    	   map.getParams().addFiles(EnrichmentMap.DATASET2, inputPanel.getDataset2files());
      
     //load the data
  		LoadInputDataTaskFactory loadData = new LoadInputDataTaskFactory(map,streamUtil);
  		
  		dialog.execute(loadData.createTaskIterator());

  		
  		
    }
	
}
