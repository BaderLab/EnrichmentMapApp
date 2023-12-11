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

import static org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters.method_GSEA;
import static org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters.method_Specialized;
import static org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters.method_generic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.FilterTunablesLegacy;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
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

	@Tunable(description = "Analysis Type")
	public ListSingleSelection<String> analysisType = new ListSingleSelection<String>(method_GSEA, method_generic, method_Specialized);

	@Tunable(description = "Path to GMT File specifying gene sets. Format: geneset name <tab> description <tab> gene ...")
	public File gmtFile;

	//Dataset 1 Tunables
	@Tunable(description = "Path to Expression File for Dataset 1 with gene expression values. Format: gene <tab> description <tab> expression value <tab> ...")
	public File expressionDataset1;

	@Tunable(description = "Path to Enrichments File for Dataset 1 specifying enrichment results.")
	public File enrichmentsDataset1;

	@Tunable(description = "Path to a second Enrichments File for Dataset 1 specifying enrichment results.")
	public File enrichments2Dataset1;

	@Tunable(description = "Path to a Ranks File for Dataset 1 specifying ranked genes. Format: gene <tab> score or statistic")
	public File ranksDataset1;

	@Tunable(description = "Path to Classes File for Dataset 1 specifying the classes of each sample in expression file. Format: see GSEA website")
	public File classDataset1;

	@Tunable(description = "Phenotype 1 for Dataset 1")
	public String phenotype1Dataset1;

	@Tunable(description = "Phenotype 2 for Dataset 1")
	public String phenotype2Dataset1;

	//Dataset 2 Tunables
	@Tunable(description = "Path to Expression File for Dataset 2 with gene expression values. Format: gene <tab> description <tab> expression value <tab> ...")
	public File expressionDataset2;

	@Tunable(description = "Path to Enrichments File for Dataset 2 specifying enrichment results.")
	public File enrichmentsDataset2;

	@Tunable(description = "Path to a second Enrichments File for Dataset 2 specifying enrichment results.")
	public File enrichments2Dataset2;

	@Tunable(description = "Path to Ranks File for Dataset 2 specifying ranked genes. Format: gene <tab> score or statistic")
	public File ranksDataset2;

	@Tunable(description = "Path to Classes File for Dataset 2 specifying the classes of each sample in expression file. format: see GSEA website")
	public File classDataset2;

	@Tunable(description = "Phenotype 1 for Dataset 2")
	public String phenotype1Dataset2;

	@Tunable(description = "Phenotype 2 for Dataset 2")
	public String phenotype2Dataset2;

	
	@ContainsTunables
	@Inject
	public FilterTunablesLegacy filterArgs;
	

	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	
	
	private DataSetFiles getDataSet1Files() {
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
		return dataset1files;
	}

	private DataSetFiles getDataSet2Files() {
		DataSetFiles dataset2files = new DataSetFiles();
		if(gmtFile != null)
			dataset2files.setGMTFileName(gmtFile.getAbsolutePath());
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
		return dataset2files;
	}
	
	
	private boolean is2DataSet() {
		return expressionDataset2 != null || enrichmentsDataset2 != null || enrichments2Dataset2 != null;
	}
	

	@Override
	public void run(TaskMonitor tm) {
		List<DataSetParameters> dataSets = new ArrayList<>(2);
		Method method = EnrichmentMapParameters.stringToMethod(analysisType.getSelectedValue());
		
		DataSetFiles dataset1files = getDataSet1Files();
		dataSets.add(new DataSetParameters(LegacySupport.DATASET1, method, dataset1files));
		
		if(is2DataSet()) {
			DataSetFiles dataset2files = getDataSet2Files();
			dataSets.add(new DataSetParameters(LegacySupport.DATASET2, method, dataset2files));
		}
		
		EMCreationParameters creationParams = filterArgs.getCreationParameters();
		
		if(filterArgs.networkName != null && !filterArgs.networkName.trim().isEmpty())
			creationParams.setNetworkName(filterArgs.networkName);
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
	}

}
