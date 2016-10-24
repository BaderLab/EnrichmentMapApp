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
		SimilarityKey k1  = new SimilarityKey("A", "i", "B");
		SimilarityKey k1p = new SimilarityKey("A", "i", "B");
		SimilarityKey k2  = new SimilarityKey("B", "i", "A");
		SimilarityKey k3  = new SimilarityKey("D", "x", "C");
		SimilarityKey k4  = new SimilarityKey("A", "x", "B");
		
		assertEquals(k1, k1);
		assertEquals(k1, k1p);
		assertEquals(k1, k2);
		assertEquals(k2, k1);
		
		assertEquals(k1, k1.swap());
		
		assertNotEquals(k1, k3);
		assertNotEquals(k1, k4);
		
		Set<SimilarityKey> keys = new HashSet<>();
		Collections.addAll(keys, k1, k1p, k2, k3);
		assertEquals(2, keys.size());
	}

}
