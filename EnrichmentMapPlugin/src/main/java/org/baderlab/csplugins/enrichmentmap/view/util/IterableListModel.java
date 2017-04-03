package org.baderlab.csplugins.enrichmentmap.view.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.DefaultListModel;

@SuppressWarnings("serial")
public class IterableListModel<T> extends DefaultListModel<T> implements Iterable<T> {

	public IterableListModel() {
		super();
	}
	
	public void addElements(Collection<T> elements) {
		elements.forEach(this::addElement);
	}
	
	public List<T> toList() {
		int n = getSize();
		List<T> list = new ArrayList<>(n);
		for(int i = 0; i < n; i++) {
			list.add(get(i));
		}
		return list;
	}
	
	public Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false);
	}
	
	public void update() {
		fireContentsChanged(this, 0, getSize());
	}
	
	public Iterator<T> iterator() {
		return new Iterator<T>() {

			int i = 0;
			int n = getSize();
			
			@Override
			public boolean hasNext() {
				return i < n; 
			}

			@Override
			public T next() {
				return get(i++);
			}
		};
	}

}
