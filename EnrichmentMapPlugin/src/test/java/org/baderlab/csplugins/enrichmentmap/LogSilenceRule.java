package org.baderlab.csplugins.enrichmentmap;

import org.junit.rules.ExternalResource;
import org.ops4j.pax.logging.internal.DefaultServiceLog;

/**
 * This rule will silence the cytoscape console logger when running a JUnit test.
 */
public class LogSilenceRule extends ExternalResource {
	
	private int logLevelBackup;
	
	@Override
	protected void before() {
		silenceLog();
	}
	
	@Override
	protected void after() {
		restoreLog();
	}
	
	
	private void silenceLog() {
		logLevelBackup = DefaultServiceLog.level;
		DefaultServiceLog.level = DefaultServiceLog.LEVEL_ERROR;
	}
	
	private void restoreLog() {
		DefaultServiceLog.level = logLevelBackup;
	}
	
}
