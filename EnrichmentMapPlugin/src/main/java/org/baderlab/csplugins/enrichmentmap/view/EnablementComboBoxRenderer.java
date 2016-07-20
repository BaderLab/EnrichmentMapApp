package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.Color;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * Allows items in a JCombo box to look disabled.
 */
@SuppressWarnings("serial")
public class EnablementComboBoxRenderer extends BasicComboBoxRenderer {
	
	private final Set<Integer> disabledIndicies = new HashSet<>();
	
	public void disableIndex(int index) {
		disabledIndicies.add(index);
	}
	
	public void enableIndex(int index) {
		disabledIndicies.remove(index);
	}
	
	@Override
	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, 
			int index, boolean isSelected, boolean cellHasFocus) {
		
		Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(disabledIndicies.contains(index)) {
			component.setForeground(Color.LIGHT_GRAY);
		}
		else {
			component.setForeground(super.getForeground());
		}
		
		return component;
	}
}
