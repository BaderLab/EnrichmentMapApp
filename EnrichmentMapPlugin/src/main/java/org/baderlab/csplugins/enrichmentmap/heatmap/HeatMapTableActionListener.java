package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.cytoscape.util.swing.OpenBrowser;

/**
 * Created by User: risserlin Date: Nov 4, 2009 Time: 11:35:49 AM
 *
 * Action listener to listen for clicks in the heatmap. If the name(column 1) or
 * description(column 2) is right clicked a linkout menu is brought up. If any
 * other cell in the other columns are right clicked they you can view the value
 * of the cell.
 *
 */
public class HeatMapTableActionListener implements MouseListener {

	private OpenBrowser openBrowser;
	private JTable jtable;
	//need the model in order to get the expression value for the mouse click of cells of the heatmap
	private HeatMapTableModel model;

	// For right-click menu
	private JPopupMenu rightClickPopupMenu;

	//for the linkout properties that are loaded from cytoscape properties.
	private Map<String, Map<String, String>> linkoutProps;

	public HeatMapTableActionListener(JTable jtable, HeatMapTableModel model, JPopupMenu rightClickPopupMenu,
			Map<String, Map<String, String>> linkoutProps, OpenBrowser openBrowser) {
		this.jtable = jtable;
		this.model = model;
		this.linkoutProps = linkoutProps;
		this.rightClickPopupMenu = rightClickPopupMenu;
		this.openBrowser = openBrowser;
	}

	/**
	 * Handle a mouse click event - action is only associated with a right click
	 * 
	 * @param e
	 */
	public void mouseClicked(MouseEvent e) {
		final int column = jtable.getColumnModel().getColumnIndexAtX(e.getX());
		final int row = e.getY() / jtable.getRowHeight();
		final Object value = jtable.getValueAt(row, column);

		// If action is right click, then show edit pop-up menu
		if((SwingUtilities.isRightMouseButton(e)) || (isMacPlatform() && e.isControlDown())) {

			if(value != null) {
				//if there is something in the menu remove the last object in the list which should be the linkouts
				//the assumption made by the attribute browser is that the last in the pop up is linkouts
				if(rightClickPopupMenu.getComponentCount() > 0)
					rightClickPopupMenu.remove(rightClickPopupMenu.getComponentCount() - 1);

				//put in the pop linkout menu if it is the first two columns
				if(column == 0 || column == 1) {
					rightClickPopupMenu.add(new HyperLinkOut(value.toString(), linkoutProps, openBrowser));
				}
				//otherwise put the value in a pop-up
				else {
					//added the right click directly to renderering of the column so don't need this anymore
					//rightClickPopupMenu.add(new JMenuItem(model.getExpValueAt(row,column).toString()));
				}
				rightClickPopupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	} // mouseClicked

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void mouseExited(MouseEvent e) {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void mousePressed(MouseEvent e) {

	}

	/**
	 * String used to compare against os.name System property - to determine if
	 * we are running on Windows platform.
	 */
	static final String MAC_OS_ID = "mac";

	/**
	 * Routine which determines if we are running on mac platform
	 *
	 * @return boolean
	 */
	private boolean isMacPlatform() {
		String os = System.getProperty("os.name");

		return os.regionMatches(true, 0, MAC_OS_ID, 0, MAC_OS_ID.length());
	}

}
