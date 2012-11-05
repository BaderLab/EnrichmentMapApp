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

package org.baderlab.csplugins.enrichmentmap.view;
import java.awt.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.baderlab.csplugins.enrichmentmap.Enrichment_Map_Plugin;

import cytoscape.*;

/**
 * @author revilo
 * <p>
 * Date   June 6, 2009<br>
 * Time   6:03:00 PM<br>
 */
public class AboutPanel extends JDialog {
    /**
     * 
     */
    private static final long serialVersionUID = 7233557042420194604L;
    
    String pluginUrl = Enrichment_Map_Plugin.pluginUrl;
    String pluginVersion = Enrichment_Map_Plugin.plugin_props.getProperty("pluginVersion", "0.1");
    String pluginReleaseSuffix = Enrichment_Map_Plugin.plugin_props.getProperty("pluginReleaseSuffix", "");

    public AboutPanel() {
        super(Cytoscape.getDesktop(), "About Enrichment Map", false);
        setResizable(false);

        //main panel for dialog box
        JEditorPane editorPane = new JEditorPane();
        editorPane.setMargin(new Insets(10,10,10,10));
        editorPane.setEditable(false);
        editorPane.setEditorKit(new HTMLEditorKit());
        editorPane.addHyperlinkListener(new HyperlinkAction(editorPane));

        URL logoURL = Enrichment_Map_Plugin.class.getResource("resources/enrichmentmap_logo.png");
        if ( ! pluginReleaseSuffix.contentEquals(""))
            pluginReleaseSuffix = " (" + pluginReleaseSuffix + ")";
        editorPane.setText(
                "<html><body>"+
//                "<div style=\"float:right;\"><img height=\"77\" width=\"125\" src=\""+ logoURL.toString() +"\" ></div>" +
                "<table border='0'><tr>" +
                "<td width='125'></td>"+
                "<td width='200'>"+
                "<p align=center><b>Enrichment Map v" + pluginVersion + pluginReleaseSuffix + "</b><BR>" + 
                "A Cytoscape Plugin<BR>" +
                "<BR></p>" +
                "</td>"+
                "<td width='125'><div align='right'><img height='77' width='125' src=\""+ logoURL.toString() +"\" ></div></td>"+
                "</tr></table>" +
                "<p align=center>Enrichment Map is a network-based method to visualize<BR>"+
                "and interpret gene-set enrichment results.<BR>" +
                "<BR>" +
                "by Gary Bader, Daniele Merico, Ruth Isserlin and Oliver Stueker<BR>" +
                "(<a href='http://www.baderlab.org/'>Bader Lab</a>, University of Toronto)<BR>" +
                "<BR>" +
                "Plugin Homepage:<BR>" +
                "<a href='" + pluginUrl + "'>" + pluginUrl + "</a><BR>" +
                "<BR>" +
                "If you use this plugin in your research, please cite:<BR>" +
                "Merico D, Isserlin R, Stueker O, Emili A, Bader GD<BR>" +
                "Enrichment Map: A Network-Based Method for <BR>" +
                "Gene-Set Enrichment Visualization and Interpretation<BR>" +
                "<i>PLoS One. 2010 Nov 15;5(11)</i><BR>" +
                "<BR>" +
                "<font size='-1'>" + Enrichment_Map_Plugin.buildId + "</font>" +
                "</p></body></html>"
            );
        setContentPane(editorPane);
    }

    private class HyperlinkAction implements HyperlinkListener {
        @SuppressWarnings("unused")
        JEditorPane pane;

        public HyperlinkAction(JEditorPane pane) {
            this.pane = pane;
        }

        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                cytoscape.util.OpenBrowser.openURL(event.getURL().toString());
            }
        }
    }	
}
