package org.baderlab.csplugins.enrichmentmap.commands.tunables;

import java.awt.Color;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.cytoscape.work.Tunable;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class DatasetColorTunable {
	
	@Tunable(description="Comma separated list of key=value pairs where the key is the "
			+ "name or index of a data set, and the value is an HTML hex color code. "
			+ "Example: \"DataSet1=#224433,DataSet2=#887766\"")
	public String colors = null;
	
	
	public Map<String,Color> getColors() {
		if(Strings.isNullOrEmpty(colors))
			return Collections.emptyMap();

		return 
			Splitter.on(',')
			.omitEmptyStrings()
			.withKeyValueSeparator(
				Splitter.on('=')
				.limit(2)
				.trimResults()
			)  
			.split(colors)
			.entrySet()
			.stream()
			.collect(
				Collectors.toMap(
					Entry::getKey, 
					v -> Color.decode(v.getValue())
				)
			);
	}

}
