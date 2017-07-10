package org.baderlab.csplugins.enrichmentmap.model.io;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Headless;
import org.baderlab.csplugins.enrichmentmap.CyActivator;
import org.baderlab.csplugins.enrichmentmap.view.control.io.SessionViewIO;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class SessionListener implements SessionLoadedListener, SessionAboutToBeSavedListener, CyShutdownListener {

	@Inject private SessionModelIO modelIO;
	@Inject private SessionViewIO viewIO;
	@Inject private @Headless boolean headless;
	@Inject private Provider<JFrame> frameProvider;
	
	private boolean cytoscapeShuttingDown = false;
	
	@Override
	public void handleEvent(SessionLoadedEvent event) {
		if(modelIO.hasTablesInvalidVersion()) {
			SwingUtilities.invokeLater(() -> {
				String message = "<html>The session file was saved with a different version of " + CyActivator.APP_NAME + " and is "
								   + "<br>not compatible with this version. Please upgrade the " + CyActivator.APP_NAME + " App.</html>";
				JOptionPane.showMessageDialog(frameProvider.get(), message, "EnrichmentMap Session Load Error", JOptionPane.WARNING_MESSAGE);
			});
		}
		restore(event.getLoadedSession());
	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent event) {
		if(sessionIsActuallySaving()) {
			save();
		}
	}
	
	private boolean sessionIsActuallySaving() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for(StackTraceElement frame : stack) {
			String className = frame.getClassName();
			if(className.equals("org.cytoscape.task.internal.session.SaveSessionTask") ||
			   className.equals("org.cytoscape.task.internal.session.SaveSessionAsTask")) {
				return true;
			}
		}
		return false;
	}

	public void save() {
		modelIO.saveModel();
		if(!headless) {
			viewIO.saveView();
		}
	}
	
	public void restore(CySession session) {
		modelIO.restoreModel(session);
		if(!headless) {
			viewIO.restoreView(session);
		}
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
