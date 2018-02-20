package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Desktop;
import java.io.File;
import java.util.Objects;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class OpenPDFViewerTask extends AbstractTask {

	private final File file;
	
	public OpenPDFViewerTask(File file) {
		this.file = Objects.requireNonNull(file);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		try {
			if(Desktop.isDesktopSupported()) {
				Desktop.getDesktop().open(file);
			}
		} catch(Exception e) { 
			// ignore, just make best attempt to open the viewer
		} 
	}

}
