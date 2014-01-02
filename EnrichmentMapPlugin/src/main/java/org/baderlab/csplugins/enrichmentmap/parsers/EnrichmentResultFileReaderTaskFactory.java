package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class EnrichmentResultFileReaderTaskFactory implements TaskFactory {
	private DataSet dataset;
	private TaskIterator enrichmentResultFileReaderIterator;
	private StreamUtil streamUtil;
	
	private void initialize(){
		EnrichmentResultFileReaderTask task = new EnrichmentResultFileReaderTask(dataset,streamUtil);
		this.enrichmentResultFileReaderIterator.append(task);
	}
	
	public EnrichmentResultFileReaderTaskFactory(DataSet dataset,StreamUtil streamUtil){
		this.dataset = dataset;
		this.streamUtil = streamUtil;
	}
	
	public TaskIterator createTaskIterator() {
		enrichmentResultFileReaderIterator = new TaskIterator();
		initialize();
		return enrichmentResultFileReaderIterator;
	}

	public boolean isReady() {
		
		return false;
	}
}
