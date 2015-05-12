package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.WordInfo;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.WordInfoNumberComparator;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateClusterLabelTask extends AbstractTask{
	
	private Cluster cluster;
	private CyTable clusterSetTable;
	
	private TaskMonitor taskMonitor = null;
	
	public UpdateClusterLabelTask(Cluster cluster, CyTable clusterSetTable) {
		super();
		this.cluster = cluster;
		this.clusterSetTable = clusterSetTable;
	}

	public void updateClusterLabel() {
		AnnotationSet parent = cluster.getParent();
		CyNetwork network =  parent.getView().getModel();
		String nameColumnName = parent.getNameColumnName();
		double sameClusterBonus = parent.getSameClusterBonus();
		double centralityBonus = parent.getCentralityBonus();
		
		//add a column to the clusterSetTable to store the computed label if it doesn't already exist
		if(clusterSetTable.getColumn("WC_ComputedLabel") == null)
			clusterSetTable.createColumn("WC_ComputedLabel", String.class, false);
	
		
		String mostCentralNodeLabel = cluster.getMostCentralNodeLabel();
		if (cluster.getSize() == 1) {
			String oldLabel = cluster.getLabel();
			String newLabel = network.getRow(cluster.getNodesToCoordinates().keySet().iterator().next()).
					get(nameColumnName, String.class);
			if (!newLabel.equals(oldLabel)) {
				cluster.setLabel(newLabel);
			}
		} else {
			// Look up the WordCloud info of this cluster in its table
			CyRow clusterRow = clusterSetTable.getRow(cluster.getCloudName());
			// Get each piece of the WordCloud info
			List<String> wordList = clusterRow.get("WC_Word", List.class);
			List<String> sizeList = clusterRow.get("WC_FontSize", List.class);
			List<String> clusterList = clusterRow.get("WC_Cluster", List.class);
			List<String> numberList = clusterRow.get("WC_Number", List.class);
			
			ArrayList<WordInfo> wordInfos = new ArrayList<WordInfo>();
			if(wordList != null){
				for (int i=0; i < wordList.size(); i++) {
					wordInfos.add(new WordInfo(wordList.get(i), 
										Integer.parseInt(sizeList.get(i)),
										Integer.parseInt(clusterList.get(i)),
										Integer.parseInt(numberList.get(i))));
				}
			}
			// Only update the labels if the wordCloud has changed
			AnnotationSet annotationSet = cluster.getParent();
			if (wordInfos != null && wordInfos.size() != cluster.getWordInfos().size()) {
				// WordCloud table entry for this cluster has changed
				cluster.setWordInfos(wordInfos);
				cluster.setLabel(cluster.makeLabel(wordInfos, mostCentralNodeLabel, sameClusterBonus, centralityBonus,
						annotationSet.getWordSizeThresholds(), annotationSet.getMaxWords()));
			} else {
				for (int infoIndex = 0; infoIndex < cluster.getWordInfos().size(); infoIndex++) {
					if (!wordInfos.get(infoIndex).equals(cluster.getWordInfos().get(infoIndex))) {
						// WordCloud table entry for this cluster has changed
						cluster.setWordInfos(wordInfos);
						cluster.setLabel(cluster.makeLabel(wordInfos, mostCentralNodeLabel, sameClusterBonus, centralityBonus,
								annotationSet.getWordSizeThresholds(), annotationSet.getMaxWords()));
						if (cluster.getTextAnnotation() != null) {
							cluster.getTextAnnotation().setText(cluster.getLabel());
						}
						return;
					}
				}
			}
			
			//set the label in the wordcloud table
			clusterRow.set("WC_ComputedLabel", cluster.getLabel());
		}
	}
	
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			this.taskMonitor = taskMonitor;
			this.taskMonitor.setTitle("Updating cluster label");
			
			updateClusterLabel();
			
		}

}
