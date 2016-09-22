package org.baderlab.csplugins.enrichmentmap.util;


public class ComboItem<V> {
	
	private final V value;
	private final String label;
	
	
	public ComboItem(V value, String label) {
		this.value = value;
		this.label = label;
	}
	
	public ComboItem(V value) {
		this(value, String.valueOf(value));
	}
	
	public V getValue() {
		return value;
	}
	
	public String getLabel() {
		return label;
	}
	
	@Override
	public String toString() {
		return String.valueOf(label);
	}

	public static <V> ComboItem<V> of(V value) {
		return new ComboItem<>(value);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComboItem<?> other = (ComboItem<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
