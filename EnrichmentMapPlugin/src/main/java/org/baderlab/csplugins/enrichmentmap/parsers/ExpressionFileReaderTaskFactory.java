package org.baderlab.csplugins.enrichmentmap.parsers;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ExpressionFileReaderTaskFactory implements TaskFactory {
	private DataSet dataset;
	private TaskIterator expressionFileReaderIterator;

	private void initialize() {
		ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset);
		this.expressionFileReaderIterator.append(task);
	}

	public ExpressionFileReaderTaskFactory(DataSet dataset) {
		this.dataset = dataset;
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
