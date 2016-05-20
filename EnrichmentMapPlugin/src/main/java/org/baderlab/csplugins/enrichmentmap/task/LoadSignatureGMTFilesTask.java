package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadSignatureGMTFilesTask implements TaskFactory {

	private final EnrichmentMap map;
	private final StreamUtil streamUtil;
	private final String fileName;
	private final FilterMetric filterMetric;

	/**
	 * @param map Out paramter, this task loads the genes into this object
	 * @param setOfGeneSets Out parameter, this task loads the genes into this object
	 */
	public LoadSignatureGMTFilesTask(String fileName, EnrichmentMap map, FilterMetric filterMetric, StreamUtil streamUtil) {
		this.fileName = fileName;
		this.map = map;
		this.streamUtil = streamUtil;
		this.filterMetric = filterMetric;
	}

	public String getTitle() {
		return "Loading Signature Geneset Files...";
	}


	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator taskIterator = new TaskIterator();
		
		// this is an out-paramter for GMTFileReaderTask and an in-parameter for FilterSignatureGSTask
		// MKTODO: find a better way to pass data from one task to another
		// doesn't this need to be made available to paParams?
		SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
		
		taskIterator.append(new GMTFileReaderTask(map, fileName, setOfGeneSets, GMTFileReaderTask.SIGNATURE_GMT, streamUtil));
		taskIterator.append(new FilterSignatureGSTask(map, setOfGeneSets, filterMetric));
		
		return taskIterator;
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
