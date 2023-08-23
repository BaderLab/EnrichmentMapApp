package org.baderlab.csplugins.enrichmentmap.commands.tunables;

import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.work.Tunable;

import com.google.common.base.Splitter;

public class NodeListTunable {
	
	@Tunable(description = "Comma separated list of node SUIDs")
	public String nodes;
	
	public List<Long> getNodeSuids() throws NumberFormatException {
		return Splitter.on(',')
			.trimResults()
			.omitEmptyStrings()
			.splitToStream(nodes)
			.map(Long::parseLong)
			.collect(Collectors.toList());
	}

}
