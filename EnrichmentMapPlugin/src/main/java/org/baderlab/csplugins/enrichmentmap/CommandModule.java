package org.baderlab.csplugins.enrichmentmap;

import org.baderlab.csplugins.enrichmentmap.actions.OpenEnrichmentMapAction;
import org.baderlab.csplugins.enrichmentmap.commands.ChartCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.DatasetShowCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.EMBuildCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.EMGseaCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.ExportModelJsonCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.ExportNetworkImageCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.ExportPDFCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.MastermapCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.MastermapListCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.PAKnownSignatureCommandTask;
import org.baderlab.csplugins.enrichmentmap.commands.TableCommandTask;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.multibindings.MultibindingsScanner;
import com.google.inject.multibindings.ProvidesIntoSet;

public class CommandModule extends AbstractModule {

	@Override
	protected void configure() { 
		install(MultibindingsScanner.asModule());
	}
	
	
	@ProvidesIntoSet
	public CommandTaskFactory provideBuild(Provider<EMBuildCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		String desc = "Creates an EnrichmentMap network containing one or two data sets.";
		return CommandTaskFactory.create("build", desc, null, taskProvider, showTask);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideGSEA(Provider<EMGseaCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		String desc = "Creates an EnrichmetMap network from one or two GSEA results. (Deprecated, use 'build' or 'mastermap' command instead.)";
		return CommandTaskFactory.create("gseabuild", desc, null, taskProvider, showTask);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideMastermap(Provider<MastermapCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		String desc = "Creates an EnrichmentMap network containing any number of data sets by scanning files in a folder.";
		String longDesc = "Uses the same algorithm as the Create EnrichmentMap Dialog to scan the files in a folder and "
				+ "automatically group them into data sets. Sub-folders will be scanned up to one level deep, allowing you to "
				+ "organize your data sets into sub-folders under the root folder. Please see the EnrichmentMap documentation "
				+ "for more details on how files are chosen for each data set.";
		return CommandTaskFactory.create("mastermap", desc, longDesc, taskProvider, showTask);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideMastermapList(Provider<MastermapListCommandTask> taskProvider) {
		String desc = "Scans files in a folder and prints out how they would be grouped into data sets, but does not create a network. "
				+ "This command is intended to help debug the 'mastermap' command by showing how the files will be grouped into data sets "
				+ "without actually creating the network.";
		return CommandTaskFactory.create("mastermap list", desc, null, taskProvider);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory providePA(Provider<PAKnownSignatureCommandTask> taskProvider) {
		String desc = "Adds more gene sets to an existing network. This is done by calculating the overlap between gene sets of the "
				+ "current EnrichmentMap network and all the gene sets contained in the provided signature gene set file.";
		return CommandTaskFactory.create("pa", desc, null, taskProvider);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideJson(Provider<ExportModelJsonCommandTask> taskProvider) {
		String desc = "Exports the EnrichmentMap data model to a file. Intended mainly for debugging.";
		return CommandTaskFactory.create("export model", desc, null, taskProvider);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideBuildTable(Provider<TableCommandTask> taskProvider, OpenEnrichmentMapAction showTask) {
		String desc = "Creates an EnrichmentMap network from values in a table.";
		String longDesc = "Intended mainly for other Apps to programatically create an EnrichmentMap network.";
		return CommandTaskFactory.create("build-table", desc, longDesc, taskProvider, showTask);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideDatasetShow(DatasetShowCommandTask.Factory taskFactory) {
		String desc = "Allows to select the data sets to show in an EnrichmentMap network.";
		String longDesc = "This command is basically the same as clicking the checkboxes next to the data sets in the main EnrichmentMap panel.";
		return CommandTaskFactory.create("dataset show", desc, longDesc, () -> taskFactory.create(true));
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideDatasetHide(DatasetShowCommandTask.Factory taskFactory) {
		String desc = "Allows to de-select the data sets to show in an EnrichmentMap network.";
		String longDesc = "This command is basically the same as clicking the checkboxes next to the data sets in the main EnrichmentMap panel.";
		return CommandTaskFactory.create("dataset hide", desc, longDesc, () -> taskFactory.create(false));
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideChart(Provider<ChartCommandTask> taskFactory) {
		String desc = "Sets the chart options for an EnrichmentMap network.";
		String longDesc = "This command is basically the same as setting the chart options in the main EnrichmentMap panel.";
		return CommandTaskFactory.create("chart", desc, longDesc, taskFactory);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideExportPDF(Provider<ExportPDFCommandTask> taskProvider) {
		String desc = "Exports the contents of the Heat Map panel to a PDF file.";
		return CommandTaskFactory.create("export pdf", desc, null, taskProvider);
	}
	
	@ProvidesIntoSet
	public CommandTaskFactory provideExportImage(Provider<ExportNetworkImageCommandTask> taskProvider) {
		String desc = "Exports the network view to an image file in the users home directory.";
		return CommandTaskFactory.create("export png", desc, null, taskProvider);
	}

}
