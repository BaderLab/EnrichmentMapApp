package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 16, 2014<br>
 * Time   01:36 PM<br>
 * <p>
 * Class to store the text attributes of a Node
 */

public final class WordRanker {
	
	public HashMap<Integer, String> clustersToLabels;
	private String clusterColumnName;
	private String nameColumnName;
	private CyServiceRegistrar registrar;

	public WordRanker(CyNetwork network, ArrayList<Cluster> clusters, String clusterColumnName, String nameColumnName, CyServiceRegistrar registrar) {
		this.clustersToLabels = new HashMap<Integer, String>();
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.registrar = registrar;
		createWordClouds();

		// blah blah blah
		
		for (Cluster cluster : clusters) {
			cluster.setLabel(getLabel(cluster, network.getDefaultNodeTable().getAllRows()));
		}
	}
	
	private void createWordClouds() {
		CommandExecutorTaskFactory executor = registrar.getService(CommandExecutorTaskFactory.class);
		ArrayList<String> commands = new ArrayList<String>();
		commands.add("wordcloud build clusterColumnName=\"" + clusterColumnName
				+ "\" nameColumnName=\"" + nameColumnName + "\"");
		TaskIterator task = executor.createTaskIterator(commands, null);
		registrar.getService(DialogTaskManager.class).execute(task); 
	}

	private ArrayList<String[]> parseWordInfo(List<String> wordInfoRaw) {
		ArrayList<String[]> wordInfoParsed = new ArrayList<String[]>();
		for (String wordInfo : wordInfoRaw) {
			wordInfoParsed.add(wordInfo.split(","));
		}
		return wordInfoParsed;
	}
	
	private String biggestWord(ArrayList<String[]> wordInfoParsed) {
		String biggestWord = "";
		int biggestSize = -1;
		for (String[] wordInfo : wordInfoParsed) {
			// eventually make use of the other parts of the wordInfo (cluster grouping, word number (?))
			if (Integer.parseInt(wordInfo[1]) > biggestSize) {
				biggestSize = Integer.parseInt(wordInfo[1]);
				biggestWord = wordInfo[0];
			}
		}
		return biggestWord;
	}
	
	public HashMap<Integer, String> getClustersToLabels() {
		return this.clustersToLabels;
	}
	
	public String getLabel(Cluster cluster, List<CyRow> nodeTable) {
		// Dummy algorithm just to work on showing text on canvas
		int clusterNumber = cluster.getClusterNumber();
		for (CyRow row : nodeTable) {
			Integer rowClusterNumber = row.get(clusterColumnName, Integer.class);
			if (rowClusterNumber != null && rowClusterNumber == clusterNumber) {
				List<String> wordInfoRaw = row.get("Word Info", List.class);
				String label = biggestWord(parseWordInfo(wordInfoRaw));
				return label;
			}
		}
		return "";
	}
	
//	private TreeMap<Integer, ArrayList<String>> invert(TreeMap<String, Integer> map) {
//		TreeMap<Integer, ArrayList<String>> inverse_map = new TreeMap<Integer, ArrayList<String>>(); 
//		for (String key : map.keySet()) {
//			if (!inverse_map.keySet().contains(map.get(key))) {
//				inverse_map.put(map.get(key), new ArrayList<String>());
//			}
//			inverse_map.get(map.get(key)).add(key);
//		}
//		return inverse_map;
//	}
}