package org.baderlab.csplugins.enrichmentmap.parsers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.model.TableParameters;
import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.common.collect.ImmutableSet;

public class LoadEnrichmentsFromTableTask extends AbstractTask {

	private final TableParameters tableParams;
	private final EMDataSet dataset;
	
	
	public LoadEnrichmentsFromTableTask(TableParameters tableParams, EMDataSet dataset) {
		this.tableParams = tableParams;
		this.dataset = dataset;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		CyTable table = tableParams.getTable();
		Collection<CyRow> rows = table.getAllRows();
		
		DiscreteTaskMonitor tm = getDiscreteTaskMonitor(taskMonitor, rows.size());
		
		EnrichmentMap map = dataset.getMap();
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		Map<String,GeneSet> genesets = dataset.getSetOfGeneSets().getGeneSets();
		
		Predicate<CyRow> filter = tableParams.getFilter();
		
		for(CyRow row : table.getAllRows()) {
			if(filter == null || filter.test(row)) {
				List<String> genes = row.getList(tableParams.getGenesColumn(), String.class);
				String name = row.get(tableParams.getNameColumn(), String.class);
				
				Double pvalue = null;
				if(tableParams.getPvalueColumn() != null)
					pvalue = row.get(tableParams.getPvalueColumn(), Double.class);
				if(pvalue == null)
					pvalue = 1.0;
				
				Double qvalue = null;
				if(tableParams.getQvalueColumn() != null)
					qvalue = row.get(tableParams.getQvalueColumn(), Double.class);
				if(qvalue == null)
					qvalue = 1.0;
				
				// Skip row if data is invalid in any way
				if(!(genes == null || genes.isEmpty() || name == null || name.isEmpty())) {
					String description = null;
					if(tableParams.getDescriptionColumn() != null) {
						description = row.get(tableParams.getDescriptionColumn(), String.class);
					}
					
					// Build the GeneSet object
					ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
					
					for(String gene : genes) {
						Integer hash = map.addGene(gene);
						if(hash != null)
							builder.add(hash);
					}
					
					GeneSet gs = new GeneSet(name, description, builder.build());
					int gsSize = gs.getGenes().size();
					genesets.put(name, gs);
					
					GenericResult result = new GenericResult(name, description, pvalue, gsSize, qvalue);
					enrichments.getEnrichments().put(name, result);
				}
			}
			tm.inc();
		}
		
		map.getParams().setFDR(tableParams.getQvalueColumn() != null);
		
		// TODO if we add support for fdr q-value column then make sure to set the following...
		// dataset.getMap().getParams().setFDR(FDR);
	}

	
	private static DiscreteTaskMonitor getDiscreteTaskMonitor(TaskMonitor delegate, int size) {
		DiscreteTaskMonitor tm = new DiscreteTaskMonitor(delegate, size);
		tm.setStatusMessage("Processing table for enrichment data - " + size + " rows");
		tm.setTitle("Loading Enrichment Data");
		return tm;
	}

}
