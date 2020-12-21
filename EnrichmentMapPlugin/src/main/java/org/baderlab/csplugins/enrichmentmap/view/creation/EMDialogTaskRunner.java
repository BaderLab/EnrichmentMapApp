package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JDialog;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentException;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults.ParseGSEAEnrichmentStrategy;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.task.MissingGenesetsException;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.ErrorMessageDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.Message;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.TaskIterator;
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
		runImpl(MissingGenesetStrategy.FAIL_AT_END, ParseGSEAEnrichmentStrategy.FAIL_IMMEDIATELY);
	}
	
	
	private void runImpl(final MissingGenesetStrategy genesetStrategy, final ParseGSEAEnrichmentStrategy gseaStrategy) {
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator(genesetStrategy, gseaStrategy);
		
		// Attempt to run the tasks one time, if it fails because of missing genesets
		// inform the user and prompt if they want to run again ignoring the problems.
		dialogTaskManager.execute(tasks, TaskUtil.onFail(finishStatus -> {
			Exception e = finishStatus.getException();
			if(e instanceof MissingGenesetsException) {
				Collection<String> names = ((MissingGenesetsException)e).getMissingGenesetNames();
				boolean retry = promptForMissingGenesetRetry(names);
				if(retry) {
					runImpl(MissingGenesetStrategy.IGNORE, gseaStrategy);
				}
			} else if(e instanceof ParseGSEAEnrichmentException) {
				boolean retry = promptForGSEAParseRetry();
				if(retry) {
					runImpl(genesetStrategy, ParseGSEAEnrichmentStrategy.REPLACE_WITH_1);
				}
			}
		}));
	}
	
	
	private boolean promptForMissingGenesetRetry(Collection<String> names) {
		int count = names.size();
		int limit = 10;
		
		String title = "There are " + count + " gene sets in the enrichment file that are missing from the GMT file. ";
		if(count > limit)
			title += "The first " + limit + " gene set names are listed below.";
		
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(parent);
		List<Message> messages = names.stream().map(Message::warn).limit(limit).collect(Collectors.toList());
		dialog.addSection(messages, title, IconManager.ICON_WARNING);
		
		String bottomMessage = "<html>It is recommend that you click 'Cancel' and fix the errors in your enrichment and GMT files. <br>"
				+ "However, you may click 'Continue' to create the network without the missing gene sets.</html>";
		dialog.addSection(Collections.emptyList(), bottomMessage, null);
		
		dialog.showDontWarnAgain(false);
		dialog.setFinishButtonText("Continue");
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.shouldContinue();
	}
	
	
	private boolean promptForGSEAParseRetry() {
		String title = "A GSEA enrichment file contained the characters '---' where a numeric value should be. "
				+ "This is a known bug in some versions of GSEA.";
		
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(parent);
		dialog.addSection(Collections.emptyList(), title, IconManager.ICON_WARNING);
		
		String bottomMessage = "<html>Click 'Cancel' to stop the creation of the EnrichmentMap network. You may choose to update "
				+ "your version of GSEA, or manually replace the occurrances of '---' with numeric values. <br>"
				+ "Click 'Continue' to create the network with the current files. All instances of '---' in the enrichment file will be treated as the value 1.</html>";
		dialog.addSection(Collections.emptyList(), bottomMessage, null);
		
		dialog.showDontWarnAgain(false);
		dialog.setFinishButtonText("Continue");
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.shouldContinue();
	}
}
