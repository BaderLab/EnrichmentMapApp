/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.baderlab.csplugins.enrichmentmap.model;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


public class HyperLinkOut extends JMenu {
	private static final String TITLE = "Search";
	private final String value;
	private final Map<String, Map<String, String>> structure;

	/**
	 * Creates a new HyperLinkOut object.
	 *
	 * @param value  DOCUMENT ME!
	 * @param menuStructure  DOCUMENT ME!
	 */
	public HyperLinkOut(String value, final Map<String, Map<String, String>> menuStructure) {
		this.value = value;
		this.structure = menuStructure;

		setBackground(Color.white);

		String dispStr = null;

		if (value.length() > 30) {
			dispStr = value.substring(0, 29) + " ... ";
		} else
			dispStr = value;

		setText("<html>Search <strong text=\"#DC143C\">" + dispStr + "</strong> on the web</html>");
		buildLinks();
	}

	private Map<String, java.util.List> getDefaultMenu() {
		Map<String, java.util.List> def = new HashMap<String, java.util.List>();

		java.util.List<String> se = new ArrayList<String>();
		se.add("Google");
		se.add("Ask");
		def.put("Search Engines", se);

		java.util.List<String> bio = new ArrayList<String>();
		bio.add("SGD");
		bio.add("GO");
		bio.add("MGD");
		bio.add("Reactome");
		def.put("Biological Databases", bio);

		return def;
	}

	/**
	 *  DOCUMENT ME!
	 */
	public void search() {
	}

	private void buildLinks() {
		//		Set<String> dbNames = xref.getDBNames();
		String fullName;

		JMenu cat;

		for (final String category : structure.keySet()) {
			cat = new JMenu(category);

			Map<String, String> children = structure.get(category);

			for (final String name : children.keySet()) {
				JMenuItem dbLink = new JMenuItem(name);
				dbLink.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							//TODO: move to mouse click action.
							//openBrowser(structure.get(category).get(name).replace("%ID%", value));
						}
					});
				cat.add(dbLink);
			}

			this.add(cat);
		}
	}
	//TODO:move to mouse click action on the table.
	/*private void openBrowser(String url) {
		// System.out.println("URL ==== " + url);
		OpenBrowser.openURL(url);
	}*/
}
