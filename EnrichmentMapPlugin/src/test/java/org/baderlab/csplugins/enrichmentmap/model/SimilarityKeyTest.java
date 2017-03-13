package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class SimilarityKeyTest {
	
	@Test
	public void testSimilarityKey() {
		SimilarityKey k1  = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k1p = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k2  = new SimilarityKey("B", "A", "i", null);
		SimilarityKey k3  = new SimilarityKey("D", "C", "x", null);
		SimilarityKey k4  = new SimilarityKey("A", "B", "x", null);
		
		assertEquals(k1, k1);
		assertEquals(k1, k1p);
		assertEquals(k1, k2);
		assertEquals(k2, k1);
		
		assertNotEquals(k1, k3);
		assertNotEquals(k1, k4);
		
		Set<SimilarityKey> keys = new HashSet<>();
		Collections.addAll(keys, k1, k1p, k2, k3);
		assertEquals(2, keys.size());
	}
	

	@Test
	public void testSimilarityKeyHashCode() {
		SimilarityKey k1  = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k1p = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k2  = new SimilarityKey("B", "A", "i", null);
		SimilarityKey k3  = new SimilarityKey("D", "C", "x", null);
		SimilarityKey k4  = new SimilarityKey("A", "B", "x", null);
		
		assertEquals(k1.hashCode(), k1p.hashCode());
		assertEquals(k1.hashCode(), k2.hashCode());
		assertEquals(k2.hashCode(), k1.hashCode());
		
		assertNotEquals(k1.hashCode(), k3.hashCode());
		assertNotEquals(k1.hashCode(), k4.hashCode());
	}


	@Test
	public void testSimilarityKeySet() {
		SimilarityKey k1  = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k2  = new SimilarityKey("A", "B", "i", "1");
		SimilarityKey k3  = new SimilarityKey("A", "B", "i", "2");
		
		assertNotEquals(k1, k2);
		assertNotEquals(k2, k1);
		assertNotEquals(k1, k3);
	}
	
	
	@Test
	public void testSimilarityKeyToString() {
		SimilarityKey k1  = new SimilarityKey("A", "B", "i", null);
		SimilarityKey k2  = new SimilarityKey("A", "B", "i", "1");
		SimilarityKey k3  = new SimilarityKey("A", "B", "i", "2");
		
		assertEquals("A (i) B", k1.toString());
		assertEquals("A (i_set1) B", k2.toString());
		assertEquals("A (i_set2) B", k3.toString());
	}

	
}
