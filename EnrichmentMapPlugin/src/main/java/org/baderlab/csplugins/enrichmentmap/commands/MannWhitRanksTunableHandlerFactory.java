package org.baderlab.csplugins.enrichmentmap.commands;

import org.cytoscape.command.StringTunableHandlerFactory;
import org.cytoscape.work.BasicTunableHandlerFactory;

public class MannWhitRanksTunableHandlerFactory 
		extends BasicTunableHandlerFactory<MannWhitRanksTunableHandler>
		implements StringTunableHandlerFactory<MannWhitRanksTunableHandler> {

	public MannWhitRanksTunableHandlerFactory() {
		super(MannWhitRanksTunableHandler.class, MannWhitRanks.class);
	}

}
