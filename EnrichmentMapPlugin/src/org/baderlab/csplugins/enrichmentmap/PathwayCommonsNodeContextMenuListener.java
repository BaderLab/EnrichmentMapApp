package org.baderlab.csplugins.enrichmentmap;

import giny.model.Node;
import giny.view.NodeView;

import javax.swing.*;

import ding.view.NodeContextMenuListener;

public class PathwayCommonsNodeContextMenuListener implements
		NodeContextMenuListener {
	public PathwayCommonsNodeContextMenuListener() {}
	/**
	 * @param nodeView The clicked NodeView
	 * @param menu popup menu to add the LinkOut menu
	 */
	public void addNodeContextMenuItems(NodeView nodeView, JPopupMenu menu) {
		if (menu == null) {
			menu = new JPopupMenu();
		}
		Node node = nodeView.getNode();
		JMenu pcmenu = new JMenu("Create PathwayCommons Network");
		pcmenu.add(new JMenuItem(new PathwayCommonsQueryAction(node, true)));
		pcmenu.add(new JMenuItem(new PathwayCommonsQueryAction(node)));
		menu.add(pcmenu);
		menu.add(new JSeparator());

	}
	

}
