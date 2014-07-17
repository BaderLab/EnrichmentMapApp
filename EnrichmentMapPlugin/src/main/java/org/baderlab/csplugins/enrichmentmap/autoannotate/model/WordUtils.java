package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 16, 2014<br>
 * Time   01:36 PM<br>
 * <p>
 * Class to store the text attributes of a Node
 */

public class WordUtils{

	public static ArrayList<String[]> parseWordInfo(List<String> wordInfoRaw) {
		ArrayList<String[]> wordInfoParsed = new ArrayList<String[]>();
		for (String wordInfo : wordInfoRaw) {
			wordInfoParsed.add(wordInfo.split(","));
		}
		return wordInfoParsed;
	}
	
	public static String makeLabel(List<String> wordList,
			List<String> sizeList, List<String> clusterList,
			List<String> numberList) {
		String label = "";
		int biggestSize = -1;
		String biggestWord = "";
		String biggestWordCluster = null;
		String secondBiggestWord = "";
		for (int i = 0; i < wordList.size(); i++) {
			if (Integer.parseInt(sizeList.get(i)) > biggestSize) {
				biggestSize = Integer.parseInt(sizeList.get(i));
				biggestWord = wordList.get(i);
				biggestWordCluster = clusterList.get(i);
			}
		}
		biggestSize = -1;
		HashSet<String> uniqueClusters = new HashSet<String>(clusterList);
		if (uniqueClusters.size() > 1) {
			for (int i = 0; i < wordList.size(); i++) {
				if (Integer.parseInt(sizeList.get(i)) > biggestSize && wordList.get(i) != biggestWord && clusterList.get(i) != biggestWordCluster) {
					biggestSize = Integer.parseInt(sizeList.get(i));
					secondBiggestWord = wordList.get(i);
				}
			}
		} else {
			for (int i = 0; i < wordList.size(); i++) {
				if (Integer.parseInt(sizeList.get(i)) > biggestSize && wordList.get(i) != biggestWord) {
					biggestSize = Integer.parseInt(sizeList.get(i));
					secondBiggestWord = wordList.get(i);
				}
			}
		}
		label = secondBiggestWord + " " + biggestWord;
		return label;
	}
}