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

package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;

/**
 * This class builds an Enrichment map from GSEA (Gene set Enrichment analysis)
 * or Generic input. There are two distinct ways to build an enrichment map,
 * from generic input or from GSEA input. GSEA input has specific files that
 * were created by a run of GSEA, including two files specifying the enriched
 * results (one file for phenotype 1 and one file for phenotype 2) - the generic
 * version the enrichment results can be specified in one file. The files also
 * contain additional parameters that would not be available to a generic
 * enrichment analysis including an Enrichment score (ES), normalized Enrichment
 * score(NES).
 * 
 * This command is maintained for backwards compatibility, it has been replaced with the Resolver command.
 */
public class EMBuildCommandTask extends AbstractTask {

	@Tunable(description = "Analysis Type", groups = { "Analysis Type" }, gravity = 1.0)
	public ListSingleSelection<String> analysisType;

	@Tunable(description = "GMT", groups = { "User Input", "Gene Sets" }, gravity = 2.0, dependsOn = "analysisType=" + EnrichmentMapParameters.method_generic, 
			params = "fileCategory=table;input=true", tooltip = "File specifying gene sets.\nFormat: geneset name <tab> description <tab> gene ...")
	public File gmtFile;

	//Dataset 1 Tunables
	@Tunable(description = "Expression", groups = { "User Input", "Datasets", "Dataset 1" }, gravity = 3.0, params = "fileCategory=table;input=true", 
			tooltip = "File with gene expression values.\nFormat: gene <tab> description <tab> expression value <tab> ...")
	public File expressionDataset1;

	@Tunable(description = "Enrichments", groups = { "User Input", "Datasets", "Dataset 1" }, gravity = 4.0, 
			dependsOn = "analysisType=" + EnrichmentMapParameters.method_generic, params = "fileCategory=table;input=true", tooltip = "File specifying enrichment results.\n")
	public File enrichmentsDataset1;

	@Tunable(description = "Enrichments 2", groups = { "User Input", "Datasets", "Dataset 1" }, gravity = 5.0, 
			dependsOn = "analysisType=" + EnrichmentMapParameters.method_GSEA, params = "fileCategory=table;input=true", tooltip = "File specifying enrichment results.\n")
	public File enrichments2Dataset1;

	@Tunable(description = "Ranks", groups = { "User Input", "Datasets", "Dataset 1", "Advanced" }, gravity = 6.0, params = "fileCategory=table;input=true", 
			tooltip = "File specifying ranked genes.\nFormat: gene <tab> score or statistic")
	public File ranksDataset1;

	@Tunable(description = "Classes", groups = { "User Input", "Datasets", "Dataset 1", "Advanced" }, gravity = 7.0, params = "fileCategory=table;input=true", 
			tooltip = "File specifying the classes of each sample in expression file.\nformat: see GSEA website")
	public File classDataset1;

	@Tunable(description = "Phenotype1", groups = { "User Input", "Datasets", "Dataset 1", "Advanced" }, gravity = 8.0, tooltip = "Dataset1 phenotype/class")
	public String phenotype1Dataset1;

	@Tunable(description = "Phenotype2", groups = { "User Input", "Datasets", "Dataset 1", "Advanced" }, gravity = 9.0, tooltip = "Dataset1 phenotype/class")
	public String phenotype2Dataset1;

	//Dataset 2 Tunables
	@Tunable(description = "Expression", groups = { "User Input", "Datasets", "Dataset 2" }, gravity = 10.0, 
			params = "fileCategory=table;input=true;displayState=callapsed", tooltip = "File with gene expression values.\nFormat: gene <tab> description <tab> expression value <tab> ...")
	public File expressionDataset2;

	@Tunable(description = "Enrichments", groups = { "User Input", "Datasets", "Dataset 2" }, gravity = 11.0, 
			dependsOn = "analysisType=" + EnrichmentMapParameters.method_generic, params = "fileCategory=table;input=true;displayState=callapsed", tooltip = "File specifying enrichment results.\n")
	public File enrichmentsDataset2;

	@Tunable(description = "Enrichments 2", groups = { "User Input", "Datasets", "Dataset 2" }, gravity = 12.0, 
			dependsOn = "analysisType=" + EnrichmentMapParameters.method_GSEA, params = "fileCategory=table;input=true;displayState=callapsed", tooltip = "File specifying enrichment results.\n")
	public File enrichments2Dataset2;

	@Tunable(description = "Ranks", groups = { "User Input", "Datasets", "Dataset 2", "Advanced" }, gravity = 13.0, 
			params = "fileCategory=table;input=true;displayState=callapsed", tooltip = "File specifying ranked genes.\nFormat: gene <tab> score or statistic")
	public File ranksDataset2;

	@Tunable(description = "Classes", groups = { "User Input", "Datasets", "Dataset 2", "Advanced" }, gravity = 14.0, 
			params = "fileCategory=table;input=true;displayState=callapsed", tooltip = "File specifying the classes of each sample in expression file.\nformat: see GSEA website")
	public File classDataset2;

	@Tunable(description = "Phenotype1", groups = { "User Input", "Datasets", "Dataset 2", "Advanced" }, gravity = 15.0, params = "displayState=callapsed", tooltip = "Dataset2 phenotype/class")
	public String phenotype1Dataset2;

	@Tunable(description = "Phenotype2", groups = { "User Input", "Datasets", "Dataset 2", "Advanced" }, gravity = 16.0, params = "displayState=callapsed", tooltip = "Dataset2 phenotype/class")
	public String phenotype2Dataset2;

	//Parameter Tuneables
	@Tunable(description = "P-value Cutoff", groups = { "User Input", "Parameters" }, gravity = 17.0, tooltip = "P-value between 0 and 1.")
	public Double pvalue = 0.005;

	@Tunable(description = "FDR Q-value Cutoff", groups = { "User Input", "Parameters" }, gravity = 18.0, tooltip = "FDR Q-value between 0 and 1.")
	public Double qvalue = 0.1;

	@Tunable(description = "Similarity Cutoff", groups = { "User Input", "Parameters" }, gravity = 19.0, tooltip = "coeffecient between 0 and 1.")
	public Double similaritycutoff = 0.25;

	@Tunable(description = "Similarity Coeffecient", groups = { "User Input", "Parameters" }, gravity = 20.0, tooltip = "coeffecient between 0 and 1.")
	public ListSingleSelection<String> coeffecients;

	@Tunable
	public double combinedConstant = LegacySupport.combinedConstant_default;
	
	@Tunable
	public ListSingleSelection<String> edgeStrategy;
	
	@Tunable
	public Integer minExperiments = null;
	
	@Tunable
	public ListSingleSelection<String> nesFilter;
	
	@Tunable
	public boolean filterByExpressions = true;
	
	@Tunable
	public String networkName = null;
	
	

	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private LegacySupport legacySupport;
	

	public EMBuildCommandTask() {
		analysisType = new ListSingleSelection<String>(EnrichmentMapParameters.method_GSEA,
				EnrichmentMapParameters.method_generic, EnrichmentMapParameters.method_Specialized);

		coeffecients = new ListSingleSelection<String>(EnrichmentMapParameters.SM_OVERLAP,
				EnrichmentMapParameters.SM_JACCARD, EnrichmentMapParameters.SM_COMBINED);
		
		edgeStrategy = ResolverCommandTask.enumNames(EdgeStrategy.values());
		edgeStrategy.setSelectedValue(EdgeStrategy.AUTOMATIC.name());
		
		nesFilter = ResolverCommandTask.enumNames(NESFilter.values());
		nesFilter.setSelectedValue(NESFilter.ALL.name());
	}

	
	/**
	 * buildEnrichmentMap - parses all GSEA input files and creates an enrichment map
	 */
	public void buildEnrichmentMap() {
		// Note we must continue to use the old constants from EnrichmentMapParameters for backwards compatibility
		Method method = EnrichmentMapParameters.stringToMethod(analysisType.getSelectedValue());
		SimilarityMetric metric = EnrichmentMapParameters.stringToSimilarityMetric(coeffecients.getSelectedValue());
		
		//Set Dataset1 Files
		DataSetFiles dataset1files = new DataSetFiles();
		if(gmtFile != null)
			dataset1files.setGMTFileName(gmtFile.getAbsolutePath());
		if(expressionDataset1!=null)
			dataset1files.setExpressionFileName(expressionDataset1.getAbsolutePath());
		if(enrichmentsDataset1 != null)
			dataset1files.setEnrichmentFileName1(enrichmentsDataset1.getAbsolutePath());
		if(enrichments2Dataset1 != null)
			dataset1files.setEnrichmentFileName2(enrichments2Dataset1.getAbsolutePath());
		if(ranksDataset1 != null)
			dataset1files.setRankedFile(ranksDataset1.getAbsolutePath());
		if(classDataset1 != null)
			dataset1files.setClassFile(classDataset1.getAbsolutePath());
		if(phenotype1Dataset1 != null)
			dataset1files.setPhenotype1(phenotype1Dataset1);
		if(phenotype2Dataset1 != null)
			dataset1files.setPhenotype2(phenotype2Dataset1);

		//Set Dataset2 Files
		DataSetFiles dataset2files = new DataSetFiles();
		if(expressionDataset2!=null)
			dataset2files.setExpressionFileName(expressionDataset2.getAbsolutePath());
		if(enrichmentsDataset2 != null)
			dataset2files.setEnrichmentFileName1(enrichmentsDataset2.getAbsolutePath());
		if(enrichments2Dataset2 != null)
			dataset2files.setEnrichmentFileName2(enrichments2Dataset2.getAbsolutePath());
		if(ranksDataset2 != null)
			dataset2files.setRankedFile(ranksDataset2.getAbsolutePath());
		if(classDataset2 != null)
			dataset2files.setClassFile(classDataset2.getAbsolutePath());
		if(phenotype1Dataset2 != null)
			dataset2files.setPhenotype1(phenotype1Dataset2);
		if(phenotype2Dataset2 != null)
			dataset2files.setPhenotype2(phenotype2Dataset2);

		NESFilter nesf;
		try {
			nesf = NESFilter.valueOf(nesFilter.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("nesFilter is invalid: '" + nesFilter.getSelectedValue() + "'");
		}
		
		List<DataSetParameters> dataSets = new ArrayList<>(2);
		dataSets.add(new DataSetParameters(LegacySupport.DATASET1, method, dataset1files));
		if(!dataset2files.isEmpty()) {
			dataSets.add(new DataSetParameters(LegacySupport.DATASET2, method, dataset2files));
		}
		
		EdgeStrategy strategy;
		try {
			strategy = EdgeStrategy.valueOf(edgeStrategy.getSelectedValue().toUpperCase());
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("edgeStrategy is invalid: '" + edgeStrategy.getSelectedValue() + "'");
		}
		
		String prefix = legacySupport.getNextAttributePrefix();
		EMCreationParameters creationParams = 
				new EMCreationParameters(prefix, pvalue, qvalue, nesf, Optional.ofNullable(minExperiments), filterByExpressions,
						metric, similaritycutoff, combinedConstant, strategy);
		
		if(networkName != null && !networkName.trim().isEmpty())
			creationParams.setNetworkName(networkName);
		//System.out.println(creationParams);
		//System.out.println(dataSets);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}


	public String getTitle() {
		return "Enrichment Map Tuneable build";
	}

	public boolean isReady() {
		return true;
	}

	@Override
	public void run(TaskMonitor arg0) {
		buildEnrichmentMap();
	}
}
