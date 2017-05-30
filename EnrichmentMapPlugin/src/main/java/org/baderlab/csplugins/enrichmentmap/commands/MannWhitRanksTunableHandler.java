package org.baderlab.csplugins.enrichmentmap.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.command.AbstractStringTunableHandler;
import org.cytoscape.work.Tunable;

public class MannWhitRanksTunableHandler extends AbstractStringTunableHandler {

	public MannWhitRanksTunableHandler(Field f, Object o, Tunable t) {
		super(f, o, t);
	}

	public MannWhitRanksTunableHandler(Method get, Method set, Object o, Tunable t) {
		super(get, set, o, t);
	}

	@Override
	public MannWhitRanks processArg(String arg) {
		return new MannWhitRanks(parseArg(arg));
	}
	
	
	private static Map<String,String> parseArg(String arg) {
		Map<String,String> dataSetToRank = new HashMap<>();

		String[] split = arg.split("(?<!\\\\),");
		for (String token: split) {
			token = token.replaceAll("\\\\,", ",");
			String[] t = token.trim().split("(?<!\\\\):");			
			if (t.length >= 2) {
				String attribute = t[0];
				String[] slice = Arrays.copyOfRange(t, 1, t.length);
				String value = String.join(":", slice);
				attribute = attribute.replaceAll("\\\\:", ":");
				value = value.replaceAll("\\\\:", ":");
				
				dataSetToRank.put(attribute, value);
			}
		}
		return dataSetToRank;
	}

}
