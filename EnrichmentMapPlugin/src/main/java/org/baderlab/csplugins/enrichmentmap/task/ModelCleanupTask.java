package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableList;


/**
 * Notes on model optimization.
 * 
 * Expressions and unflitered SetOfGeneSets are stored in the EnrichmentMap object 
 * and are shared between DataSets. (Gene sets of interest are not shared because
 * we assume they will be filtered differently for each data set). This data
 * should be treated as read-only.
 * 
 * Expression sharing is done during load by the ExpressionFileReaderTask.
 * This has the advantage of avoiding parsing the same expression file twice.
 * Expression files are shared based on the file name, if two or more data sets
 * use the same expression file they will be shared. This task just cleans
 * up the keys because file names are initially used as keys for convenience.
 * 
 * Gene set sharing is done after loading by this task. This is because
 * gene set loading is more complicated. Gene sets can come from different
 * files but have the same contents (typical for GSEA). Also gene sets can 
 * come from GMT files or from enrichment files. Its much less complicated
 * to just do the loading the normal way and then determine which gene sets
 * can be shared as a "cleanup" step at the end (which is what this task does).
 * 
 * Why not just throw away the unfiltered gene sets? Because there are future
 * features planed that will likely make use of that information (ie dark matter).
 * 
 * Expressions values are truncated to have fewer significant digits.
 * If the original expression values have a high level of precision this can
 * save a significant amount of space in the session file. However, so that
 * the values are consistent between loading/saving of the session the truncation
 * is done during the initial load by the ExpressionFileReaderTask.
 * 
 */
public class ModelCleanupTask extends AbstractTask {

	private final EnrichmentMap map;
	
	public ModelCleanupTask(EnrichmentMap map) {
		this.map = map;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		cleanUpExpressionMatrixKeys();
		cleanUpGeneSets();
	}
	
	
	private static Iterator<String> createKeyGenerator() {
		return Stream.iterate(0, x -> x + 1).map(String::valueOf).iterator();
	}
	
	
	private void cleanUpExpressionMatrixKeys() {
		Iterator<String> keyGen = createKeyGenerator();
		
		for(String key : ImmutableList.copyOf(map.getExpressionMatrixKeys())) {
			String newKey = keyGen.next();
			
			GeneExpressionMatrix matrix = map.removeExpressionMatrix(key);
			map.putExpressionMatrix(newKey, matrix);
			
			for(EMDataSet ds : map.getDataSetList()) {
				if(ds.getExpressionKey().equals(key)) {
					ds.setExpressionKey(newKey);
				}
			}
		}
	}
	
	
	private void cleanUpGeneSets() {
		Iterator<String> keyGen = createKeyGenerator();
		
		Set<EMDataSet> dataSetsToProcess = Collections.newSetFromMap(new IdentityHashMap<>());
		dataSetsToProcess.addAll(map.getDataSetList());
		
		for(String oldKey : ImmutableList.copyOf(map.getGeneSetsKeys())) {
			SetOfGeneSets mapGeneSets = map.getGeneSets(oldKey);
			String newKey = keyGen.next();
			
			boolean match = false;
			
			Iterator<EMDataSet> dataSetIter = dataSetsToProcess.iterator();
			while(dataSetIter.hasNext()) {
				EMDataSet dataSet = dataSetIter.next();
				
				if(mapGeneSets.equals(dataSet.getSetOfGeneSets())) {
					match = true;
					dataSet.setGeneSetsKey(newKey);
					dataSetIter.remove();
				}
			}
			
			map.removeGeneSets(oldKey);
			if(match) {
				map.putGeneSets(newKey, mapGeneSets);
			}
		}
	}

}
