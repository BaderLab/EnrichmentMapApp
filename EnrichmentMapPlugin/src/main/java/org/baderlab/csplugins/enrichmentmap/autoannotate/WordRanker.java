package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

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

	public WordRanker(HashMap<Integer, ArrayList<NodeText>> clustersToNodeText) {
		this.clustersToLabels = new HashMap<Integer, String>();
		for (Integer cluster : clustersToNodeText.keySet()) {
			this.clustersToLabels.put(cluster, getLabel(clustersToNodeText.get(cluster)));
		}
	}
	
	public HashMap<Integer, String> getClustersToLabels() {
		return this.clustersToLabels;
	}
	
	public String getLabel(ArrayList<NodeText> nodeTexts) {
		// This is primitive
		TreeMap<String, Integer> wordUnitSet = getWordSet(nodeTexts, 1);
		TreeMap<Integer, ArrayList<String>> wordUnitSetInverse = invert(wordUnitSet);
		TreeMap<String, Integer> wordPairSet = getWordSet(nodeTexts, 2);
		TreeMap<Integer, ArrayList<String>> wordPairSetInverse = invert(wordPairSet);
		TreeMap<String, Integer> wordTripletSet = getWordSet(nodeTexts, 3);
		TreeMap<Integer, ArrayList<String>> wordTripletSetInverse = invert(wordTripletSet);
		TreeMap<String, Integer> wordQuadrupletSet = getWordSet(nodeTexts, 4);
		TreeMap<Integer, ArrayList<String>> wordQuadrupletSetInverse = invert(wordTripletSet);
		return wordPairSetInverse.lastEntry().getValue().get(0);
	}
	
	public TreeMap<String, Integer> getWordSet(ArrayList<NodeText> nodeTextCluster, int wordsPerSplit){
		TreeMap<String, Integer> wordSet = new TreeMap<String, Integer>(); 
		for (NodeText nodeText : nodeTextCluster) {
			String name = nodeText.getName();
			String[] nameText = name.split(" ");
			for (int i=0; i < nameText.length+1-wordsPerSplit ; i++) {
				String word = "";
				int j = 0;
				while (j < wordsPerSplit) {
					word += " " + nameText[i+j];
					j++;
				}
				if (!wordSet.containsKey(word)) {
					wordSet.put(word, 0);
				}
				wordSet.put(word, wordSet.get(word) + 1);
			}
		}
		return wordSet;	
	}
	
	private TreeMap<Integer, ArrayList<String>> invert(TreeMap<String, Integer> map) {
		TreeMap<Integer, ArrayList<String>> inverse_map = new TreeMap<Integer, ArrayList<String>>(); 
		for (String key : map.keySet()) {
			if (!inverse_map.keySet().contains(map.get(key))) {
				inverse_map.put(map.get(key), new ArrayList<String>());
			}
			inverse_map.get(map.get(key)).add(key);
		}
		return inverse_map;
	}
}