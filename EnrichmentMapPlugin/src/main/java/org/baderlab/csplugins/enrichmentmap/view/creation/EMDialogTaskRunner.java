package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.JDialog;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentException;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults.ParseGSEAEnrichmentStrategy;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksFileReaderTask.UnsortedRanksStrategy;
import org.baderlab.csplugins.enrichmentmap.parsers.RanksUnsortedException;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.task.MissingGenesetsException;
import org.baderlab.csplugins.enrichmentmap.task.TaskErrorStrategies;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.ErrorMessageDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.Message;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


public class EMDialogTaskRunner {

	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private ErrorMessageDialog.Factory errorMessageDialogFactory;
	
	private final JDialog parent;
	private final EMCreationParameters params;
	private final List<DataSetParameters> dataSets;
	
	
	public static interface Factory {
		EMDialogTaskRunner create(JDialog parent, EMCreationParameters params, List<DataSetParameters> dataSets);
	}
	
	
	@AssistedInject
	public EMDialogTaskRunner(@Assisted JDialog parent, @Assisted EMCreationParameters params, @Assisted  List<DataSetParameters> dataSets) {
		this.parent = parent;
		this.params = params;
		this.dataSets = dataSets;
	}
	
	
	public void run() {
		runImpl(TaskErrorStrategies.dialogDefaults());
	}
	
	
	private void runImpl(TaskErrorStrategies strategies) {
		var taskFactory = taskFactoryFactory.create(params, dataSets);
		var tasks = taskFactory.createTaskIterator(strategies);
		
		// Attempt to run the tasks one time, if it fails inform the user and prompt if they want to run again ignoring the problems.
		dialogTaskManager.execute(tasks, TaskUtil.onFail(finishStatus -> {
			Exception e = finishStatus.getException();
			
			if(e instanceof MissingGenesetsException) { // thrown by InitializeGenesetsOfInterestTask
				var names = ((MissingGenesetsException)e).getMissingGenesetNames();
				boolean retry = promptForMissingGenesetRetry(names);
				if(retry) {
					runImpl(strategies.with(MissingGenesetStrategy.IGNORE));
				}
			} 
			else if(e instanceof ParseGSEAEnrichmentException) {
				boolean retry = promptForGSEAParseRetry();
				if(retry) {
					runImpl(strategies.with(ParseGSEAEnrichmentStrategy.REPLACE_WITH_1));
				}
			} 
			else if(e instanceof RanksUnsortedException) {
				var ranksFileName = ((RanksUnsortedException)e).getRanksFileName();
				boolean retry = promptForUnsortedRanksRetry(ranksFileName);
				if(retry) {
					runImpl(strategies.with(UnsortedRanksStrategy.LOG_WARNING));
				}
			}
		}));
	}
	
	
	private boolean prompt(Consumer<ErrorMessageDialog> dialogModifier) {
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(parent);
		
		dialogModifier.accept(dialog);
		
		dialog.showDontWarnAgain(false);
		dialog.setFinishButtonText("Continue");
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.shouldContinue();
	}
	
	
	private boolean promptForMissingGenesetRetry(Collection<String> names) {
		return prompt(dialog -> {
			int count = names.size();
			int limit = 10;
			
			String title = "There are " + count + " gene sets in the enrichment file that are missing from the GMT file. ";
			if(count > limit)
				title += "The first " + limit + " gene set names are listed below.";
			
			List<Message> messages = names.stream().map(Message::warn).limit(limit).collect(Collectors.toList());
			dialog.addSection(messages, title, IconManager.ICON_WARNING);
			
			String bottomMessage = "<html>It is recommend that you click 'Cancel' and fix the errors in your enrichment and GMT files. <br>"
					+ "However, you may click 'Continue' to create the network without the missing gene sets.</html>";
			dialog.addSection(List.of(), bottomMessage, null);
		});
	}
	
	private boolean promptForGSEAParseRetry() {
		return prompt(dialog -> {
			String title = "A GSEA enrichment file contained the characters '---' where a numeric value should be. "
					+ "This is a known bug in some versions of GSEA.";
			
			dialog.addSection(List.of(), title, IconManager.ICON_WARNING);
			
			String bottomMessage = "<html>Click 'Cancel' to stop the creation of the EnrichmentMap network. You may choose to update "
					+ "your version of GSEA, or manually replace the occurrances of '---' with numeric values. <br>"
					+ "Click 'Continue' to create the network with the current files. All instances of '---' in the enrichment file will be treated as the value 1.</html>";
			dialog.addSection(List.of(), bottomMessage, null);
		});
	}
	
	private boolean promptForUnsortedRanksRetry(String ranksFileName) {
		return prompt(dialog -> {
			String title = "One or more rank files are not sorted from greatest to least. This may affect the outcome of certain calculations.";
			
			dialog.addSection(List.of(), title, IconManager.ICON_WARNING);
			
			String bottomMessage = "<html>Click 'Cancel' to stop the creation of the EnrichmentMap network. You may choose to provide "
					+ "sorted rank files instead. <br>"
					+ "Click 'Continue' to create the network with the current files.</html>";
			dialog.addSection(List.of(), bottomMessage, null);
		});
	}
}
