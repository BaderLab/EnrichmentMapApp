package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;

public class CheckboxData {

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private boolean selected;

	private final String display;
	private final Path path;

	public CheckboxData(String display, Path path) {
		this.display = display;
		this.path = path;
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

	public Path getPath() {
		return path;
	}

	public void addPropertyChangeListener(String propName, PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(propName, listener);
	}

	public void removePropertyChangeListener(String propName, PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(propName, listener);
	}

}
