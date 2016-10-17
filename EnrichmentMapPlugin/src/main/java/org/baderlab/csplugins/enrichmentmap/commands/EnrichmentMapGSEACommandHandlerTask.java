package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;
import com.google.inject.Provider;


public class EnrichmentMapGSEACommandHandlerTask extends AbstractTask {
	
	@Tunable(description="edbdir")		
	public String edbdir;
	
	@Tunable(description="edbdir2")		
	public String edbdir2;
	
	@Tunable(description="P-value Cutoff", groups={"User Input","Parameters"}, gravity = 17.0, tooltip="P-value between 0 and 1.")
	public Double pvalue = 0.005;
	
	@Tunable(description="FDR Q-value Cutoff", groups={"User Input","Parameters"}, gravity = 18.0, tooltip="FDR Q-value between 0 and 1.")
	public Double qvalue = 0.1;
	
	@Tunable(description="overlap", groups={"User Input","Parameters"}, gravity = 19.0, tooltip="coeffecient between 0 and 1.")
	public Double overlap = 0.25;
	
	@Tunable(description="similaritymetric", groups={"User Input","Parameters"}, gravity = 20.0, tooltip="coeffecient between 0 and 1.")
	public ListSingleSelection<String> similaritymetric;
	
	@Tunable(description="expressionfile")
	public String expressionfile = "expressionfile";
	
	@Tunable(description="expressionfile2")
	public String expressionfile2 = "expressionfile2";

	@Tunable(description="combinedconstant ", groups={"User Input","Parameters"}, gravity = 19.0, tooltip="coeffecient between 0 and 1.")
	public Double combinedconstant ;

	
	@Inject private Provider<EnrichmentMapParameters> emParamsProvider;
	@Inject private EnrichmentMapBuildMapTaskFactory.Factory taskFactoryProvider;
	@Inject private EnrichmentMapManager emManager;
	@Inject private LegacySupport legacySupport;

	
	public EnrichmentMapGSEACommandHandlerTask() {
		similaritymetric = new ListSingleSelection<String>(EnrichmentMapParameters.SM_OVERLAP, EnrichmentMapParameters.SM_JACCARD, EnrichmentMapParameters.SM_COMBINED);
	}

	
	private void buildEnrichmentMap(){
		//Initialize Data create a new params for the new EM and add the dataset files to it
		EnrichmentMapParameters new_params = emParamsProvider.get();
	
	
		//set all files as extracted from the edb directory
		DataSetFiles files = this.InitializeFiles(edbdir, expressionfile);
		new_params.addFiles(LegacySupport.DATASET1, files);
		//only add second dataset if there is a second edb directory.
		if(edbdir2 != null && !edbdir2.equalsIgnoreCase("")){
			new_params.setTwoDatasets(true);
			DataSetFiles files2 = this.InitializeFiles(edbdir2, expressionfile2);
			new_params.addFiles(LegacySupport.DATASET2, files2);
		}
	
		//set the method to gsea
		new_params.setMethod(EnrichmentMapParameters.method_GSEA);
		if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_JACCARD)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_OVERLAP)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
		if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_COMBINED)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
		
		new_params.setSimilarityCutOff(overlap);
		new_params.setPvalue(pvalue);
		new_params.setQvalue(qvalue);
		new_params.setFDR(true);
		new_params.setCombinedConstant(combinedconstant);
	
		String prefix = legacySupport.getNextAttributePrefix();
		new_params.setAttributePrefix(prefix);
		String name = prefix + LegacySupport.EM_NAME;
		EnrichmentMap map = new EnrichmentMap(name, new_params.getCreationParameters());

		EnrichmentMapBuildMapTaskFactory buildmap = taskFactoryProvider.create(map);

		insertTasksAfterCurrentTask(buildmap.createTaskIterator());
		
		emManager.showPanels();
	}
	
	private DataSetFiles InitializeFiles(String edb, String exp){
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String file_sep = System.getProperty("file.separator");
		String testEdbResultsFileName = edb + file_sep + "results.edb";
		String testgmtFileName = edb + file_sep + "gene_sets.gmt";
		
		//the rank file does not have a set name.  We need to figure out the name of the rank file
		String testrnkFileName = "";
		File directory = new File(edb);
		String[] dir_listing = directory.list();
		if(dir_listing.length > 0){
			for(int i = 0 ; i < dir_listing.length;i++){
				if(dir_listing[i].endsWith("rnk") && testrnkFileName.equals(""))
					testrnkFileName = edb + file_sep + dir_listing[i];
				else if(dir_listing[i].endsWith("rnk") && !testrnkFileName.equals(""))
					System.out.println("There are two rnk files in the edb directory.  Using the first one found");
			}
		}
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testEdbResultsFileName);
		files.setGMTFileName(testgmtFileName);
		if(!testrnkFileName.equals(""))
			files.setRankedFile(testrnkFileName);
		if(!exp.equals("")){
			files.setExpressionFileName(exp);
		}
		return files;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		buildEnrichmentMap();
	}

}
