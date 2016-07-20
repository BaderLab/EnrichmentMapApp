package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExpressionFileReaderTaskFactory implements TaskFactory {
	private DataSet dataset;
	private TaskIterator expressionFileReaderIterator;
	private StreamUtil streamUtil;

	private void initialize() {
		ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset, streamUtil);
		this.expressionFileReaderIterator.append(task);
	}

	public ExpressionFileReaderTaskFactory(DataSet dataset, StreamUtil streamUtil) {
		this.dataset = dataset;
		this.streamUtil = streamUtil;
	}

	public TaskIterator createTaskIterator() {
		expressionFileReaderIterator = new TaskIterator();
		initialize();
		return expressionFileReaderIterator;
	}

	public boolean isReady() {

		return false;
	}

}
