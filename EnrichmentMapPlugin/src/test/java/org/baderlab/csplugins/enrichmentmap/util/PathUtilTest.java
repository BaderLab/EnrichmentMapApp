package org.baderlab.csplugins.enrichmentmap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class PathUtilTest {

	@Test
	public void testPathUtil() {
		{
			Path c = PathUtil.commonRoot(plist("/a/b/c", "/a/b/d"));
			assertEquals(Paths.get("/a/b"), c);
		}
		{
			Path c = PathUtil.commonRoot(plist("/a/b/c/d/e", "/a/b/c/x/y", "/a/b/q/w"));
			assertEquals(Paths.get("/a/b"), c);
		}
		{
			Path c = PathUtil.commonRoot(plist("/x/y/z", "/a/b/d"));
			assertEquals(Paths.get("/"), c);
		}
		{
			Path c = PathUtil.commonRoot(plist("/", "/a/b/d"));
			assertEquals(Paths.get("/"), c);
		}
		{
			Path c = PathUtil.commonRoot(plist("/x/y/z", "a/b/d"));
			assertNull(c);
		}
		{
			Path c = PathUtil.commonRoot(plist("/x/y/z", null));
			assertNull(c);
		}
	}
	
	
	private static List<Path> plist(String ... ps) {
		return Arrays.stream(ps)
				.map(p -> p == null ? null : Paths.get(p))
				.collect(Collectors.toList());
	}
}
