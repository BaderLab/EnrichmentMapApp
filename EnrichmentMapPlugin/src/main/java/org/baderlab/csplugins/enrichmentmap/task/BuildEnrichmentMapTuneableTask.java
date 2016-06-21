/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id: BuildEnrichmentMapTask.java 383 2009-10-08 20:06:35Z risserlin $
// $LastChangedDate: 2009-10-08 16:06:35 -0400 (Thu, 08 Oct 2009) $
// $LastChangedRevision: 383 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/BuildEnrichmentMapTask.java $

package org.baderlab.csplugins.enrichmentmap.task;

import java.io.File;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 11:44:46 AM
 * <p>
 * This class builds an Enrichment map from GSEA (Gene set Enrichment analysis) or Generic input.  There are two distinct ways
 * to build an enrichment map, from generic input or from GSEA input.  GSEA input has
 * specific files that were created by a run of GSEA, including two files specifying the enriched
 * results (one file for phenotype 1 and one file for phenotype 2) - the generic version
 * the enrichment results can be specified in one file.  The files also contain
 * additional parameters that would not be available to a generic enrichment analysis including
 * an Enrichment score (ES), normalized Enrichment score(NES).
 */
public class BuildEnrichmentMapTuneableTask extends AbstractTask {
	private EnrichmentMap map;
	private EnrichmentMapParameters params;
	    
	private DataSetFiles dataset1files = new DataSetFiles();
	private DataSetFiles dataset2files = new DataSetFiles();
	
	@Tunable(description="Analysis Type", groups={"Analysis Type"},gravity = 1.0)
	public ListSingleSelection<String> analysisType;
	
	@Tunable(description="GMT", groups={"User Input","Gene Sets"},gravity = 2.0,
			dependsOn="analysisType="+EnrichmentMapParameters.method_generic, params="fileCategory=table;input=true",
			tooltip="File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...")
	public File gmtFile;
	
	
	//Dataset 1 Tunables
		@Tunable(description="Expression", groups={"User Input","Datasets","Dataset 1"}, gravity = 3.0, params="fileCategory=table;input=true",
				tooltip="File with gene expression values.\n" + "Format: gene <tab> description <tab> expression value <tab> ...")
		public File expressionDataset1;   
		
		@Tunable(description="Enrichments", groups={"User Input","Datasets","Dataset 1"}, gravity = 4.0,
				dependsOn="analysisType="+EnrichmentMapParameters.method_generic, params="fileCategory=table;input=true",
				tooltip="File specifying enrichment results.\n")
		public File enrichmentsDataset1; 	
		
		@Tunable(description="Enrichments 2", groups={"User Input","Datasets","Dataset 1"}, gravity = 5.0,
				dependsOn="analysisType="+EnrichmentMapParameters.method_GSEA, params="fileCategory=table;input=true",
				tooltip="File specifying enrichment results.\n")
		public File enrichments2Dataset1; 
		
		@Tunable(description="Ranks", groups={"User Input","Datasets","Dataset 1","Advanced"}, gravity = 6.0, params="fileCategory=table;input=true",
				tooltip="File specifying ranked genes.\n" + "Format: gene <tab> score or statistic")
		public File ranksDataset1;
		
		@Tunable(description="Classes", groups={"User Input","Datasets","Dataset 1","Advanced"}, gravity = 7.0, params="fileCategory=table;input=true",
				tooltip="File specifying the classes of each sample in expression file.\n" + "format: see GSEA website")
		public File classDataset1;
		
		@Tunable(description="Phenotype1", groups={"User Input","Datasets","Dataset 1","Advanced"}, gravity = 8.0, 
				tooltip="Dataset1 phenotype/class")
		public String phenotype1Dataset1;
		
		@Tunable(description="Phenotype2", groups={"User Input","Datasets","Dataset 1","Advanced"}, gravity = 9.0, 
				tooltip="Dataset1 phenotype/class")
		public String phenotype2Dataset1;
		
		//Dataset 2 Tunables
		@Tunable(description="Expression", groups={"User Input","Datasets","Dataset 2"}, gravity = 10.0, params="fileCategory=table;input=true;displayState=callapsed",
				tooltip="File with gene expression values.\n" + "Format: gene <tab> description <tab> expression value <tab> ...")
		public File expressionDataset2;   
		
		@Tunable(description="Enrichments", groups={"User Input","Datasets","Dataset 2"}, gravity = 11.0,
				dependsOn="analysisType="+EnrichmentMapParameters.method_generic, params="fileCategory=table;input=true;displayState=callapsed",
				tooltip="File specifying enrichment results.\n")
		public File enrichmentsDataset2; 	
		
		@Tunable(description="Enrichments 2", groups={"User Input","Datasets","Dataset 2"}, gravity = 12.0,
				dependsOn="analysisType="+EnrichmentMapParameters.method_GSEA, params="fileCategory=table;input=true;displayState=callapsed",
				tooltip="File specifying enrichment results.\n")
		public File enrichments2Dataset2; 
		
		@Tunable(description="Ranks", groups={"User Input","Datasets","Dataset 2","Advanced"}, gravity = 13.0, params="fileCategory=table;input=true;displayState=callapsed",
				tooltip="File specifying ranked genes.\n" + "Format: gene <tab> score or statistic")
		public File ranksDataset2;
		
		@Tunable(description="Classes", groups={"User Input","Datasets","Dataset 2","Advanced"}, gravity = 14.0, params="fileCategory=table;input=true;displayState=callapsed",
				tooltip="File specifying the classes of each sample in expression file.\n" + "format: see GSEA website")
		public File classDataset2;
		
		@Tunable(description="Phenotype1", groups={"User Input","Datasets","Dataset 2","Advanced"}, gravity = 15.0, params="displayState=callapsed",
				tooltip="Dataset2 phenotype/class")
		public String phenotype1Dataset2;
		
		@Tunable(description="Phenotype2", groups={"User Input","Datasets","Dataset 2","Advanced"}, gravity = 16.0, params="displayState=callapsed",
				tooltip="Dataset2 phenotype/class")
		public String phenotype2Dataset2;
		
	
	//Parameter Tuneables
	@Tunable(description="P-value Cutoff", groups={"User Input","Parameters"}, gravity = 17.0,
			tooltip="P-value between 0 and 1.")
	public Double pvalue = 0.005;
	
	@Tunable(description="FDR Q-value Cutoff", groups={"User Input","Parameters"}, gravity = 18.0,
			tooltip="FDR Q-value between 0 and 1.")
	public Double qvalue = 0.1;
	
	@Tunable(description="Similarity Cutoff", groups={"User Input","Parameters"}, gravity = 19.0,
			tooltip="coeffecient between 0 and 1.")
	public Double similaritycutoff = 0.25;
	
	@Tunable(description="Similarity Coeffecient", groups={"User Input","Parameters"}, gravity = 20.0,
			tooltip="coeffecient between 0 and 1.")
	public  ListSingleSelection<String> coeffecients;
	
    //values to track progress
    //TODO - implement usage
    //private int maxValue;
    
	//required services
    private CySessionManager sessionManager;
	private StreamUtil streamUtil;
	
    private CyApplicationManager applicationManager;
    private CySwingApplication swingApplication;
    private CyNetworkManager networkManager;
    private CyNetworkViewManager networkViewManager;
    private CyNetworkViewFactory networkViewFactory;
    private CyNetworkFactory networkFactory;
    private CyTableFactory tableFactory;
    private CyTableManager tableManager;
    
    private VisualMappingManager visualMappingManager;
    private VisualStyleFactory visualStyleFactory;
    
    //we will need all three mappers
    private VisualMappingFunctionFactory vmfFactoryContinuous;
    private VisualMappingFunctionFactory vmfFactoryDiscrete;
    private VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    private CyLayoutAlgorithmManager layoutManager;
    private  MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    //
    private DialogTaskManager dialog;
      
    public BuildEnrichmentMapTuneableTask(CySessionManager sessionManager,
			StreamUtil streamUtil, CyApplicationManager applicationManager,
			CySwingApplication swingApplication,
			CyNetworkManager networkManager,
			CyNetworkViewManager networkViewManager,
			CyNetworkViewFactory networkViewFactory,
			CyNetworkFactory networkFactory, CyTableFactory tableFactory,
			CyTableManager tableManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			VisualMappingFunctionFactory vmfFactoryContinuous,
			VisualMappingFunctionFactory vmfFactoryDiscrete,
			VisualMappingFunctionFactory vmfFactoryPassthrough,
			CyLayoutAlgorithmManager layoutManager,
			MapTableToNetworkTablesTaskFactory mapTableToNetworkTable,
			DialogTaskManager dialog) {
		super();
		this.sessionManager = sessionManager;
		this.streamUtil = streamUtil;
		this.applicationManager = applicationManager;
		this.swingApplication = swingApplication;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.networkViewFactory = networkViewFactory;
		this.networkFactory = networkFactory;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.vmfFactoryDiscrete = vmfFactoryDiscrete;
		this.vmfFactoryPassthrough = vmfFactoryPassthrough;
		this.layoutManager = layoutManager;
		this.mapTableToNetworkTable = mapTableToNetworkTable;
		this.dialog = dialog;
		
		 //create a drop down of the different types of analyses that can be done.
        analysisType = new ListSingleSelection<String>(EnrichmentMapParameters.method_GSEA,EnrichmentMapParameters.method_generic,EnrichmentMapParameters.method_Specialized );
        
        coeffecients = new ListSingleSelection<String>(EnrichmentMapParameters.SM_OVERLAP, EnrichmentMapParameters.SM_JACCARD, EnrichmentMapParameters.SM_COMBINED);

	}



	/**
     * buildEnrichmentMap - parses all GSEA input files and creates an enrichment map
     */
    public void buildEnrichmentMap(){
    		
    		//Initialize Data
    	
    		//create a new params for the new EM and add the dataset files to it
		EnrichmentMapParameters new_params = new EnrichmentMapParameters(sessionManager,streamUtil,applicationManager);
		if(analysisType.getSelectedValue() == EnrichmentMapParameters.method_Specialized)
			new_params.setMethod(EnrichmentMapParameters.method_Specialized);
		if(analysisType.getSelectedValue() == EnrichmentMapParameters.method_GSEA)
			new_params.setMethod(EnrichmentMapParameters.method_GSEA);
		if(analysisType.getSelectedValue() == EnrichmentMapParameters.method_generic)
			new_params.setMethod(EnrichmentMapParameters.method_generic);
		
		//Set Dataset1 Files
		if(gmtFile!=null) dataset1files.setGMTFileName(gmtFile.getAbsolutePath());
		if(expressionDataset1!=null) { 
			dataset1files.setExpressionFileName(expressionDataset1.getAbsolutePath());
			new_params.setData(true);
		}
		if(enrichmentsDataset1!=null) dataset1files.setEnrichmentFileName1(enrichmentsDataset1.getAbsolutePath());
		if(enrichments2Dataset1!=null) dataset1files.setEnrichmentFileName2(enrichments2Dataset1.getAbsolutePath());
		if(ranksDataset1!=null) dataset1files.setRankedFile(ranksDataset1.getAbsolutePath());
		if(classDataset1!=null) dataset1files.setClassFile(classDataset1.getAbsolutePath());
		if(phenotype1Dataset1!= null) dataset1files.setPhenotype1(phenotype1Dataset1);
		if(phenotype2Dataset1!= null) dataset1files.setPhenotype2(phenotype2Dataset1);
		new_params.addFiles(EnrichmentMap.DATASET1, dataset1files);
		
		//Set the parameters
		new_params.setPvalue(pvalue);
		new_params.setQvalue(qvalue);
		new_params.setSimilarityCutOff(similaritycutoff);
		if(coeffecients.getSelectedValue() == EnrichmentMapParameters.SM_JACCARD)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		if(coeffecients.getSelectedValue() == EnrichmentMapParameters.SM_OVERLAP)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
		if(coeffecients.getSelectedValue() == EnrichmentMapParameters.SM_COMBINED)
			new_params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
		
		//Set Dataset2 Files
		if(expressionDataset2!=null) {
			dataset2files.setExpressionFileName(expressionDataset2.getAbsolutePath());
			new_params.setData2(true);
		}
		if(enrichmentsDataset2!=null) dataset2files.setEnrichmentFileName1(enrichmentsDataset2.getAbsolutePath());
		if(enrichments2Dataset2!=null) dataset2files.setEnrichmentFileName2(enrichments2Dataset2.getAbsolutePath());
		if(ranksDataset2!=null) dataset2files.setRankedFile(ranksDataset2.getAbsolutePath());
		if(classDataset2!=null) dataset2files.setClassFile(classDataset2.getAbsolutePath());
		if(phenotype1Dataset2!= null) dataset2files.setPhenotype1(phenotype1Dataset2);
		if(phenotype2Dataset2!= null) dataset2files.setPhenotype2(phenotype2Dataset2);
		
		if(!dataset2files.isEmpty())
			new_params.addFiles(EnrichmentMap.DATASET2, dataset2files);
	
    EnrichmentMap map = new EnrichmentMap(new_params);

                    
   	EnrichmentMapBuildMapTaskFactory buildmap = new EnrichmentMapBuildMapTaskFactory(map,  
        			applicationManager,swingApplication,networkManager,networkViewManager,networkViewFactory,networkFactory,tableFactory,tableManager, 
        			visualMappingManager,visualStyleFactory,
        			vmfFactoryContinuous, vmfFactoryDiscrete,vmfFactoryPassthrough, dialog,  streamUtil,layoutManager,mapTableToNetworkTable);

   	insertTasksAfterCurrentTask(buildmap.createTaskIterator());
   	
	EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
	manager.registerServices();
    	
           
       
    }


 /**
     * Run the Task.
     */
    public void run() {
        buildEnrichmentMap();
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Enrichment Map Tuneable build");
    }


	public boolean isReady() {

		return true;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		 buildEnrichmentMap();
	}
}
