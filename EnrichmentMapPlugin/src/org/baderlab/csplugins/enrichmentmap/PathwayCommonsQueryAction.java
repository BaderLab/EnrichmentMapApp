/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Carl Song
 ** Authors: Carl Song, Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
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

package org.baderlab.csplugins.enrichmentmap;

import giny.model.Node;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpException;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.*;

/**
 * @author carlsong
 *
 */
public class PathwayCommonsQueryAction extends CytoscapeAction {
	private Node node;
	private boolean biopax;
	private CyAttributes cyAttributes = Cytoscape.getNodeAttributes();
	private PathwayCommonsImportTask task;
	private String cpath_id;
	private	String queryString = "version=3.0&q=PATHWAY_NAME&format=html&cmd=get_by_keyword&snapshot_id=GLOBAL_FILTER_SETTINGS&record_type=PATHWAY";
	
	/**
	 * @param node
	 * @param biopax (true: create BioPAX view; false: create Binary SIF view)
	 * @param label (Text for menu item)
	 */
	public PathwayCommonsQueryAction(Node node, boolean biopax, String label) {
		super(label);
		this.node = node;
		this.biopax = biopax;
	}

	/**
	 * @param nodeLabel
	 * @return replaces all spaces to underscores
	 */
	private String clean(String nodeLabel){
		return nodeLabel.replaceAll("_", " ");
	}
	/**
	 * @return Retrieve top matching cpath_id by node label. The matching algorithm may need to be updated frequently
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws InterruptedException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String queryByNodeName() throws IOException, InterruptedException {
		queryString = queryString.replace("PATHWAY_NAME", clean(node.getIdentifier()));
		PathwayCommonsWebAPI webAPI = new PathwayCommonsWebAPI();
        String result = webAPI.query(queryString);
        Pattern pattern = Pattern.compile("id=(\\d+)\">Pathway:");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find())
        	return matcher.group(1);
        else
        	throw new InterruptedException("Pathway not found!");
	}
	
	public void actionPerformed(ActionEvent evt) {
		try {
			cpath_id = cyAttributes.getAttribute(node.getIdentifier(), "cpath_id").toString();
		} catch (NullPointerException e) {
			String[] options = new String[]{"OK", "Cancel"};
			int choice = JOptionPane.showOptionDialog(Cytoscape.getDesktop(), "This node does not have a cpath_id associated with it.\n" +
					"Proceed query by node name?", 
					"cpath_id not found!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, 
					null, options, options[0]);
			if (choice == 0) {
				try {	
					cpath_id = queryByNodeName();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(Cytoscape.getDesktop(), e.getMessage());
				}
			}			
		}

		// Configure JTask Dialog Pop-Up Box
		final JTaskConfig jTaskConfig = new JTaskConfig();
		jTaskConfig.setOwner(Cytoscape.getDesktop());
		jTaskConfig.displayCloseButton(false);
		jTaskConfig.displayCancelButton(true);
		jTaskConfig.displayStatus(true);
		jTaskConfig.setAutoDispose(true);
		

		// Execute Task in New Thread; pop open JTask Dialog Box.
		if (cpath_id != null) {
			task = new PathwayCommonsImportTask(cpath_id, biopax);
			TaskManager.executeTask(task, jTaskConfig);
		}
	}
}

