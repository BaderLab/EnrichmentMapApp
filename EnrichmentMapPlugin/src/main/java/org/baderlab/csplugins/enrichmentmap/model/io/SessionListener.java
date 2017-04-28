package org.baderlab.csplugins.enrichmentmap.model.io;

import org.baderlab.csplugins.enrichmentmap.view.control.io.SessionViewIO;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionListener implements SessionLoadedListener, SessionAboutToBeSavedListener, CyShutdownListener {

	@Inject private SessionModelIO modelIO;
	@Inject private SessionViewIO viewIO;
	
	private boolean cytoscapeShuttingDown = false;
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		restore(event.getLoadedSession());
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		save();
	}

	public void save() {
		modelIO.saveModel();
		viewIO.saveView();
	}
	
	public void restore(CySession session) {
		modelIO.restoreModel(session);
		viewIO.restoreView(session);
	}
	
	@Override
	public void handleEvent(CyShutdownEvent e) {
		cytoscapeShuttingDown = e.actuallyShutdown();
	}

	public void appShutdown() {
		if(!cytoscapeShuttingDown) {
			// the App is being updated or restarted, we want to save all the data first
			save();
		}
	}
}
