package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.io.File;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadSignatureGMTFilesTask implements TaskFactory {

	private final EnrichmentMap map;
	private final File file;
	private final FilterMetric filterMetric;

	/**
	 * @param map Out paramter, this task loads the genes into this object
	 * @param setOfGeneSets Out parameter, this task loads the genes into this object
	 */
	public LoadSignatureGMTFilesTask(File file, EnrichmentMap map, FilterMetric filterMetric) {
		this.file = file;
		this.map = map;
		this.filterMetric = filterMetric;
	}

	public String getTitle() {
		return "Loading Signature Geneset Files...";
	}

	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator tasks = new TaskIterator();
		
		// this is an out-paramter for GMTFileReaderTask and an in-parameter for FilterSignatureGSTask
		// MKTODO: find a better way to pass data from one task to another
		// doesn't this need to be made available to paParams?
		SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
		
		tasks.append(new GMTFileReaderTask(map, file.getAbsolutePath(), setOfGeneSets));
		tasks.append(new FilterSignatureGSTask(map, setOfGeneSets, filterMetric));
		
		return tasks;
	}
	
	@Override
	public boolean isReady() {
		return true;
	}
}
