package org.baderlab.csplugins.enrichmentmap.parsers;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ClassFileReaderTask extends AbstractTask {

	private final EMDataSet dataset;
	
	public ClassFileReaderTask(EMDataSet dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Parsing class file");
		String classFile = dataset.getDataSetFiles().getClassFile();
		String[] classes = parseClasses(classFile);
		dataset.getEnrichments().setPhenotypes(classes);
	}
	
	
	public static String[] parseClasses(String classFile) {
		if (isNullOrEmpty(classFile))
			return new String[] {"NA_pos", "NA_neg"};

		File f = new File(classFile);
		if(!f.exists())
			return null;

		try {
			List<String> lines = LineReader.readAllLines(classFile, 4);

			/*
			 * GSEA class files will have 3 lines in the following format: 6 2 1
			 * # R9C_8W WT_8W R9C_8W R9C_8W R9C_8W WT_8W WT_8W WT_8W
			 * 
			 * If the file has 3 lines assume it is a GSEA and get the
			 * phenotypes from the third line. If the file only has 1 line
			 * assume that it is a generic class file and get the phenotypes
			 * from the single line
			 * the class file can be split by a space or a tab
			 */
			if(lines.size() >= 3)
				return lines.get(2).split("\\s");
			else if(lines.size() == 1)
				return lines.get(0).split("\\s");
			else
				return null;
			
		} catch (IOException ie) {
			System.err.println("unable to open class file: " + classFile);
			return null;
		}
	}

}
