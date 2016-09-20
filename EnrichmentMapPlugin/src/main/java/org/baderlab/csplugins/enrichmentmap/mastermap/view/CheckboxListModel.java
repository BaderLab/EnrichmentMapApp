package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.DefaultListModel;

@SuppressWarnings("serial")
public class CheckboxListModel extends DefaultListModel<CheckboxData> implements Iterable<CheckboxData> {

	public CheckboxListModel() {
		super();
	}
	
	public List<CheckboxData> toList() {
		int n = getSize();
		List<CheckboxData> list = new ArrayList<>(n);
		for(int i = 0; i < n; i++) {
			list.add(get(i));
		}
		return list;
	}
	
	public Stream<CheckboxData> stream() {
		return StreamSupport.stream(spliterator(), false);
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
