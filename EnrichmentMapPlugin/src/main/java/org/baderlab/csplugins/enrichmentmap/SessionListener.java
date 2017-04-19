package org.baderlab.csplugins.enrichmentmap;

import org.baderlab.csplugins.enrichmentmap.model.io.SessionModelIO;
import org.baderlab.csplugins.enrichmentmap.view.control.io.SessionViewIO;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionListener implements SessionLoadedListener, SessionAboutToBeSavedListener {

	@Inject private SessionModelIO modelIO;
	@Inject private SessionViewIO viewIO;
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		CySession session = event.getLoadedSession();
		restore(session);
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
}
