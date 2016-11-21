package org.baderlab.csplugins.enrichmentmap.view.control;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class SortedListModel<E> extends AbstractListModel<E> {

	private final SortedSet<E> elements;
	
	public SortedListModel(Comparator<E> comparator) {
		elements = new TreeSet<>(comparator);
	}
	
	public SortedListModel() {
		elements = new TreeSet<>();
	}
	
	@Override
	public int getSize() {
		return elements.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public E getElementAt(int index) {
		return (E)elements.toArray()[index];
	}
	
	public void add(E e) {
		if(elements.add(e)) {
			fireContentsChanged(this, 0, elements.size());
		}
	}
	
	public void remove(E e) {
		if(elements.remove(e)) {
			fireContentsChanged(this, 0, elements.size());
		}
	}

	public void clear() {
		if(!elements.isEmpty()) {
			elements.clear();
			fireContentsChanged(this, 0, elements.size());
		}
	}
	
	public boolean contains(E e) {
		return elements.contains(e);
	}

	public int indexOf(E e) {
		return new ArrayList<>(elements).indexOf(e);
	}

}
