package org.baderlab.csplugins.enrichmentmap.util;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NamingUtil {

	private static final Pattern PATTERN = Pattern.compile(".*\\((\\d*)\\)$"); // capture just the digits
	
	public static String getUniqueName(final String suggestedName, final Set<String> existingNames) {
		String name = suggestedName != null ? suggestedName.trim() : "";
		
		if (name.isEmpty())
			name = "UNDEFINED";
		
		if (existingNames != null && !existingNames.isEmpty()) {
			if (!isNameTaken(name, existingNames))
				return name;
			
			Matcher m = PATTERN.matcher(name);
			int start = 0;
			
			if (m.matches()) {
				name = name.substring(0, m.start(1) - 1);
				start = Integer.decode(m.group(1));
			}
			
			for (int i = start; true; i++) {
				final String numberedName = name + "(" + (i + 1) + ")";

				if (!isNameTaken(numberedName, existingNames))
					return numberedName;
			}
		}
		
		return name;
	}
	
	private static boolean isNameTaken(final String name, final Set<String> existingNames) {
		if (name != null && existingNames.contains(name))
			return true;

		return false;
	}
	
	private NamingUtil() {
	}
}
