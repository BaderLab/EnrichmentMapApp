package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class CreateDiseaseSignatureTaskFactory extends AbstractTaskFactory {

	@Inject private EnrichmentMapManager emManager;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CreateDiseaseSignatureTaskParallel.Factory signatureTaskFactory;
	@Inject private Provider<ControlPanelMediator> controlPanelMediatorProvider;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	
	
	private String errors = null;
	
	private final CyNetworkView netView;
	private final PostAnalysisParameters params;
	
	
	public interface Factory {
		CreateDiseaseSignatureTaskFactory create(CyNetworkView netView, PostAnalysisParameters params);
	}
	
	@Inject
	public CreateDiseaseSignatureTaskFactory(@Assisted CyNetworkView netView, @Assisted PostAnalysisParameters params) {
		this.netView = netView;
		this.params = params;
	}
	
	public String getErrors() {
		return errors;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		// Make sure that the minimum information is set in the current set of parameters
		EnrichmentMap map = emManager.getEnrichmentMap(applicationManager.getCurrentNetwork().getSUID());
		
		StringBuilder errorBuilder = new StringBuilder();
		checkMinimalRequirements(errorBuilder, params);
		if (params.getRankTestParameters().getType().isMannWhitney() && map.getAllRanks().isEmpty())
			errorBuilder.append("Mann-Whitney requires ranks. \n");
		
		this.errors = errorBuilder.toString();

		if(errors.isEmpty()) {
			ControlPanelMediator controlPanelMediator = controlPanelMediatorProvider.get();
			EMStyleOptions options = controlPanelMediator.createStyleOptions(netView);
			CyCustomGraphics2<?> chart = controlPanelMediator.createChart(options);
			
			TaskIterator tasks = new TaskIterator();
			tasks.append(signatureTaskFactory.create(params, map));
			tasks.append(applyStyleTaskFactory.create(options, chart, false));
			return tasks;
		} else {
			// MKTODO not entirely sure what to do in this case, just return an empty iterator I guess...
			return new TaskIterator(new AbstractTask() {
				@Override
				public void run(TaskMonitor taskMonitor) throws Exception {
					throw new RuntimeException(errors);
				}
			});
		}
	}
	
	/**
	 * Checks all values of the PostAnalysisInputPanel
	 * 
	 * @return String with error messages (one error per line) or empty String if everything is okay.
	 * @see org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters#checkMinimalRequirements()
	 */
	public void checkMinimalRequirements(StringBuilder errors, PostAnalysisParameters params) {
		errors.append(checkGMTfiles(params));
		if(params.getSelectedGeneSetNames().isEmpty()) {
			errors.append("No Signature Genesets selected \n");
		}
	}
	

	/**
	 * Checks if SignatureGMTFileName is provided and if the file can be read.
	 * 
	 * @return String with error messages (one error per line) or empty String
	 *         if everything is okay.
	 */
	public String checkGMTfiles(PostAnalysisParameters params) {
		String signatureGMTFileName = params.getSignatureGMTFileName();
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
			return "Signature GMT file can not be found \n";
		return "";
	}
}
