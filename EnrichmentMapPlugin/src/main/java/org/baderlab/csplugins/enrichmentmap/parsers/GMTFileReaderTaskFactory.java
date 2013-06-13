package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class GMTFileReaderTaskFactory implements TaskFactory {
	
	private DataSet dataset;
	private TaskIterator gmtFileReaderIterator;
	private StreamUtil streamUtil;
	
	private void initialize(){
		GMTFileReaderTask task = new GMTFileReaderTask(dataset,streamUtil);
		this.gmtFileReaderIterator.append(task);
	}
	
	public GMTFileReaderTaskFactory(DataSet dataset,StreamUtil streamUtil){
		this.dataset = dataset;
		this.streamUtil = streamUtil;
	}
	
	public TaskIterator createTaskIterator() {
		gmtFileReaderIterator = new TaskIterator();
		initialize();
		return gmtFileReaderIterator;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

}
