package org.baderlab.csplugins.enrichmentmap;

import com.google.inject.AbstractModule;

/**
 * Guice module
 *
 */
public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(EnrichmentMapManager.class).asEagerSingleton();
	}
	
}

