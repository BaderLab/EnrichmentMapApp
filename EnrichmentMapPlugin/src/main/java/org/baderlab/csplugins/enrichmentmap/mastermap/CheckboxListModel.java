package org.baderlab.csplugins.enrichmentmap.mastermap;

import java.util.Iterator;

import javax.swing.DefaultListModel;

@SuppressWarnings("serial")
public class CheckboxListModel extends DefaultListModel<CheckboxData> implements Iterable<CheckboxData> {

	public CheckboxListModel() {
		super();
	}
	
	public Iterator<CheckboxData> iterator() {
		return new Iterator<CheckboxData>() {

			int i = 0;
			int n = getSize();
			
			@Override
			public boolean hasNext() {
				return i < n; 
			}

			@Override
			public CheckboxData next() {
				return get(i++);
			}
		};
	}

}
