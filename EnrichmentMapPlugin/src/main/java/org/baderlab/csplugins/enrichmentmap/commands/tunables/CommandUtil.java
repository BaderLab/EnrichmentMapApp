package org.baderlab.csplugins.enrichmentmap.commands.tunables;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.work.util.ListSingleSelection;

public class CommandUtil {

	public static ListSingleSelection<String> lssFromEnum(Enum<?> ... values) {
		List<String> names = new ArrayList<>(values.length);
		for(Enum<?> value : values) {
			names.add(value.name());
		}
		return new ListSingleSelection<>(names);
	}
	
}
