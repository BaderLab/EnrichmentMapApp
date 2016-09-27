package org.baderlab.csplugins.enrichmentmap.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;


public class SwingUtil {

	private SwingUtil() {}
	
	/**
	 * recurse up the parents until you find an instance of JFrame or JDialog
	 */
	public static Component getWindowInstance(JPanel panel){
		Component parent = panel.getParent();
		Component current = panel;
		while (parent != null){
			//check to see if parent is an instance of JFrame of JDialog
			if(parent instanceof JFrame || parent instanceof JDialog)
				return parent;
			current = parent;
			parent = current.getParent();
		}
		return current;
	}
	
	
	/**
	 * Call setEnabled(enabled) on the given component and all its children recursively.
	 * Warning: The current enabled state of components is not remembered.
	 */
	public static void recursiveEnable(Component component, boolean enabled) {
    	component.setEnabled(enabled);
    	if(component instanceof Container) {
    		for(Component child : ((Container)component).getComponents()) {
    			recursiveEnable(child, enabled);
    		}
    	}
    }
	
	public static void makeSmall(final JComponent... components) {
		if (components == null || components.length == 0)
			return;
		
		for (JComponent c : components) {
			if (LookAndFeelUtil.isAquaLAF()) {
				c.putClientProperty("JComponent.sizeVariant", "small");
			} else {
				if (c.getFont() != null)
					c.setFont(c.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			}
			
			if (c instanceof JList) {
				((JList<?>) c).setCellRenderer(new DefaultListCellRenderer() {
					@Override
					public Component getListCellRendererComponent(JList<?> list, Object value, int index,
							boolean isSelected, boolean cellHasFocus) {
						super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
						setFont(getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
						
						return this;
					}
				});
			}
		}
	}
	
	public static JButton createOnlineHelpButton(String url, String toolTipText, CyServiceRegistrar serviceRegistrar) {
		JButton btn = new JButton();
		btn.setToolTipText(toolTipText);
		btn.addActionListener((ActionEvent evt) -> {
			serviceRegistrar.getService(OpenBrowser.class).openURL(url);
		});
		
		if (LookAndFeelUtil.isAquaLAF()) {
			btn.putClientProperty("JButton.buttonType", "help");
		} else {
			btn.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(22.0f));
			btn.setText(IconManager.ICON_QUESTION_CIRCLE);
			btn.setBorderPainted(false);
			btn.setContentAreaFilled(false);
			btn.setFocusPainted(false);
			btn.setBorder(BorderFactory.createEmptyBorder());
			btn.setMinimumSize(new Dimension(22, 22));
		}
		
		return btn;
	}
}
