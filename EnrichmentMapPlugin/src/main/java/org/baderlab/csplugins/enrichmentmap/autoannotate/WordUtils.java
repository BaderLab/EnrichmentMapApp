package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
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

public class WordUtils{

	public static ArrayList<String[]> parseWordInfo(List<String> wordInfoRaw) {
		ArrayList<String[]> wordInfoParsed = new ArrayList<String[]>();
		for (String wordInfo : wordInfoRaw) {
			wordInfoParsed.add(wordInfo.split(","));
		}
		return wordInfoParsed;
	}
	
	public static String biggestWord(List<String> wordList,
			List<String> sizeList, List<String> clusterList,
			List<String> numberList) {
		String biggestWord = "";
		int biggestSize = -1;
		for (int i = 0; i < wordList.size(); i++) {
			// eventually make use of the other parts of the wordInfo (cluster grouping, word number (?))
			if (Integer.parseInt(sizeList.get(i)) > biggestSize) {
				biggestSize = Integer.parseInt(sizeList.get(i));
				biggestWord = wordList.get(i);
			}
		}
		return biggestWord;
	}
}