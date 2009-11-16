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

package org.baderlab.csplugins.enrichmentmap;

import java.awt.event.*;
import javax.swing.*;
import cytoscape.Cytoscape;
import cytoscape.util.*;

/**
 * Created by:
 * @author revilo
 * <p>
 * Date   Jun 12, 2009<br>
 * Time   5:53:54 PM<br>
 */
public class ShowAboutPanelAction extends CytoscapeAction {

	/**
     * 
     */
    private static final long serialVersionUID = 1341062331014243704L;

    public ShowAboutPanelAction() {
		super("Show About Box");
	}
	
	public void actionPerformed(ActionEvent event) {

		// open new dialog
		AboutPanel aboutPanel = new AboutPanel( );
		aboutPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutPanel.pack();
		aboutPanel.setLocationRelativeTo(Cytoscape.getDesktop());
		aboutPanel.setVisible(true);

	}
}
