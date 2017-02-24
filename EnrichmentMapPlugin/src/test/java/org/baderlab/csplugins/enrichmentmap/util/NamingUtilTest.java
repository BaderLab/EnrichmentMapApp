package org.baderlab.csplugins.enrichmentmap.util;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

public class NamingUtilTest {

	@Test
	public void testGetUniqueName() {
		Set<String> names = Sets.newSet(
				"My Name A",
				"My Name B",
				"My Name B(1)",
				"My Name B(3)",
				"My Name C(1)",
				"My Name D_1",
				"My Name E (1)"
		);
		
		assertEquals("My Name", NamingUtil.getUniqueName("My Name", names));
		assertEquals("My Name", NamingUtil.getUniqueName(" My Name  \t", names)); // Always trim!
		assertEquals("My Name a", NamingUtil.getUniqueName("My Name a", names)); // Case sensitive!
		assertEquals("My Name A(1)", NamingUtil.getUniqueName("My Name A", names));
		assertEquals("My Name A(1)", NamingUtil.getUniqueName("My Name A(1)", names));
		assertEquals("My Name A(01)", NamingUtil.getUniqueName("My Name A(01)", names));
		assertEquals("My Name A_1", NamingUtil.getUniqueName("My Name A_1", names));
		assertEquals("My Name B(2)", NamingUtil.getUniqueName("My Name B", names));
		assertEquals("My Name B(4)", NamingUtil.getUniqueName("My Name B(3)", names));
		assertEquals("My Name B(2)", NamingUtil.getUniqueName("My Name B(2)", names));
		assertEquals("My Name C(2)", NamingUtil.getUniqueName("My Name C(1)", names));
		assertEquals("My Name D", NamingUtil.getUniqueName("My Name D", names));
		assertEquals("My Name D(1)", NamingUtil.getUniqueName("My Name D(1)", names));
		assertEquals("My Name E (2)", NamingUtil.getUniqueName("My Name E (1)", names));
	}
}
