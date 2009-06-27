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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import giny.model.Node;
import giny.model.Edge;

/**
 * Created by
 * User: risserlin
 * Date: Feb 6, 2009
 * Time: 1:38:22 PM
 */
public class SummaryPanel extends JPanel {

    private JTextPane textPane;


    public SummaryPanel() {
        this.setLayout(new java.awt.BorderLayout());


        //information about the current selected edges
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBorder(new EmptyBorder(5,5,5,5));
        textPane.setContentType("text/html");
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane jScrollPane = new javax.swing.JScrollPane(textPane);

        this.add(jScrollPane);

    }

    public void updateNodeInfo(Object[] nodes){

        String genesets = "<html> <h1>Genesets:\n</h1>";

        for(int i = 0; i<nodes.length;i++){
              genesets = genesets  + (i+1) +". " + ((Node)nodes[i]).getIdentifier() + "<br>";
        }

        genesets = genesets + "</html>";
        textPane.setText(genesets);

        this.revalidate();
    }

    public void updateEdgeInfo(Object[] edges){

        String genesets = "<html> <h1>Genesets Overlaps:\n</h1>";

        for(int i = 0; i<edges.length;i++){
            genesets = genesets +  (i+1) +". " + ((Edge)edges[i]).getIdentifier() + "\n<br>";

        }

        genesets = genesets + "</html>";
        textPane.setText(genesets);

        this.revalidate();
    }

    public void clearInfo(){
        textPane.setText("");
        this.revalidate();
    }
}
