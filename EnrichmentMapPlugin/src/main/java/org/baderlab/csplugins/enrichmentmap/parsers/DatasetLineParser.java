package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.base.Strings;

public abstract class DatasetLineParser extends AbstractTask {

	public static final Double DefaultScoreAtMax = -1000000.0;

	private DataSet dataset;

	public DatasetLineParser(DataSet dataset) {
		this.dataset = dataset;
	}
		
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parse(taskMonitor);
	}

	public void parse(TaskMonitor taskMonitor) throws IOException {
		String enrichmentResultFileName1 = dataset.getEnrichments().getFilename1();
		String enrichmentResultFileName2 = dataset.getEnrichments().getFilename2();
		
		if(!Strings.isNullOrEmpty(enrichmentResultFileName1))
			readFile(enrichmentResultFileName1, taskMonitor);
		if(!Strings.isNullOrEmpty(enrichmentResultFileName2))
			readFile(enrichmentResultFileName2, taskMonitor);
	}
	
	public static List<String> readLines(String fileName, int limit) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			List<String> lines = new ArrayList<>();
			int count = 0;
            for(String line; (line = reader.readLine()) != null;) {
                lines.add(line);
                count++;
                if(count >= limit) {
                	break;
                }
            }
            return lines;
        }
	}
	
	public static List<String> readLines(String fileName) throws IOException {
		try(BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
			List<String> lines = new ArrayList<>();
            for(String line; (line = reader.readLine()) != null;) {
                lines.add(line);
            }
            return lines;
        }
	}
	
	public void readFile(String enrichmentResultFileName, TaskMonitor taskMonitor) throws IOException {
		List<String> lines = readLines(enrichmentResultFileName);
		parseLines(lines, dataset, taskMonitor);
	}
	
	
	public abstract void parseLines(List<String> lines, DataSet dataset, TaskMonitor taskMonitor);
}
