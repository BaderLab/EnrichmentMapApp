package org.baderlab.csplugins.enrichmentmap.view.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CheckboxData<T> {

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private boolean selected;

	private final String display;
	private final T data;

	public CheckboxData(String display, T data) {
		this.display = display;
		this.data = data;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if(this.selected == selected)
			return;
		boolean oldValue = this.selected;
		this.selected = selected;
		pcs.firePropertyChange("selected", oldValue, selected);
	}

	public String getDisplay() {
		return display;
	}

	public T getData() {
		return data;
	}

	public void addPropertyChangeListener(String propName, PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(propName, listener);
	}

	public void removePropertyChangeListener(String propName, PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(propName, listener);
	}

}
