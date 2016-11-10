package org.baderlab.csplugins.enrichmentmap.view.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.DefaultListModel;

@SuppressWarnings("serial")
public class CheckboxListModel<T> extends DefaultListModel<CheckboxData<T>> implements Iterable<CheckboxData<T>> {

	public CheckboxListModel() {
		super();
	}
	
	public void addElements(Collection<CheckboxData<T>> elements) {
		elements.forEach(this::addElement);
	}
	
	public List<CheckboxData<T>> toList() {
		int n = getSize();
		List<CheckboxData<T>> list = new ArrayList<>(n);
		for(int i = 0; i < n; i++) {
			list.add(get(i));
		}
		return list;
	}
	
	public Stream<CheckboxData<T>> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	public Iterator<CheckboxData<T>> iterator() {
		return new Iterator<CheckboxData<T>>() {

			int i = 0;
			int n = getSize();
			
			@Override
			public boolean hasNext() {
				return i < n; 
			}

			@Override
			public CheckboxData<T> next() {
				return get(i++);
			}
		};
	}

}
