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
public class EnablementComboBoxRenderer<T> extends BasicComboBoxRenderer {
	
	private final Set<T> disabledItems = new HashSet<>();
	
	@SafeVarargs
	public final void disableItems(T ... items) {
		for(T item : items)
			disabledItems.add(item);
	}
	
	@SafeVarargs
	public final void enableItems(T ... items) {
		for(T item : items)
			disabledItems.remove(item);
	}
	
	public void enableAll() {
		disabledItems.clear();
	}
	
	@Override
	public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value, 
			                                      int index, boolean isSelected, boolean cellHasFocus) {
		
		Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(disabledItems.contains(value)) {
			component.setForeground(Color.LIGHT_GRAY);
		}
		else {
			component.setForeground(super.getForeground());
		}
		
		return component;
	}
}
