package org.baderlab.csplugins.enrichmentmap.view.creation;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

import com.google.inject.Inject;

public class DependencyChecker {
	
	public static final String YFILES_APP_STORE_URL = "https://apps.cytoscape.org/apps/yfileslayoutalgorithms";
	public static final String AUTOANNOTATE_APP_STORE_URL = "https://apps.cytoscape.org/apps/autoannotate";
	
	@Inject private AvailableCommands availableCommands;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	
	
	public boolean isYFilesInstalled() {
		return layoutManager.getAllLayouts().stream().anyMatch(layout -> layout.getName().startsWith("yfiles"));
	}
	
	public boolean isAutoAnnotateOpenCommandAvailable() {
		boolean aaInstalled = availableCommands.getNamespaces().contains("autoannotate");
		if(aaInstalled) {
			return availableCommands.getCommands("autoannotate").contains("open");
		}
		return false;
	}
	
}

