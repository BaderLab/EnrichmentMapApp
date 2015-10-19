package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyEdge;

/**
 * Basically just a map of edge name to edge, but it ignores the directionality
 * of the edge when looking up by name.
 *
 */
public class EdgeSimilarities {
	
	private final Map<Key,CyEdge> edges = new HashMap<>();
	
	public void addEdge(CyEdge edge, String name1, String interaction, String name2) {
		edges.put(new Key(name1, name2, interaction), edge);
	}
	
	public void addEdge(String fullName, CyEdge edge) {
		edges.put(Key.parse(fullName), edge);
	}
	
	public boolean containsEdge(String name1, String interaction, String name2) {
		Key key = new Key(name1, name2, interaction);
		return edges.containsKey(key) || edges.containsKey(key.flip());
	}
	
	public boolean containsEdge(String name) {
		Key key = Key.parse(name);
		return edges.containsKey(key) || edges.containsKey(key.flip());
	}
	
	public CyEdge getEdge(String name1, String interaction, String name2) {
		return getEdge(new Key(name1, name2, interaction));
	}
	
	public CyEdge getEdge(String name) {
		return getEdge(Key.parse(name));
	}
	
	private CyEdge getEdge(Key key) {
		CyEdge edge = edges.get(key);
		if(edge == null) {
			edge = edges.get(key.flip());
		}
		return edge;
	}
	
	public int size() {
		return edges.size();
	}
	
	public boolean isEmpty() {
		return edges.isEmpty();
	}
	
	private static class Key {
		final String name1;
		final String name2;
		final String interaction;
		
		Key(String name1, String name2, String interaction) {
			this.name1 = name1;
			this.name2 = name2;
			this.interaction = interaction;
		}
		
		Key flip() {
			return new Key(name2, name1, interaction);
		}
		
		static Key parse(String name) {
			int open  = name.indexOf("(");
			int close = name.indexOf(")", open);
			String name1 = name.substring(0, open).trim();
			String name2 = name.substring(close+1, name.length()).trim();
			String interaction = name.substring(open+1, close);
			return new Key(name1, name2, interaction);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((interaction == null) ? 0 : interaction.hashCode());
			result = prime * result + ((name1 == null) ? 0 : name1.hashCode());
			result = prime * result + ((name2 == null) ? 0 : name2.hashCode());
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
			Key other = (Key) obj;
			if (interaction == null) {
				if (other.interaction != null)
					return false;
			} else if (!interaction.equals(other.interaction))
				return false;
			if (name1 == null) {
				if (other.name1 != null)
					return false;
			} else if (!name1.equals(other.name1))
				return false;
			if (name2 == null) {
				if (other.name2 != null)
					return false;
			} else if (!name2.equals(other.name2))
				return false;
			return true;
		}
	}
	
	
	
}
