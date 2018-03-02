package org.baderlab.csplugins.enrichmentmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnrichmentMapBuildProperties {

	private static final String PROPS_FILE_APP   = "app.props";
	
	public static final String APP_VERSION;
	public static final String APP_NAME;
	public static final String APP_URL;
	public static final String USER_MANUAL_URL;
	

	private EnrichmentMapBuildProperties() {}
	
	static {
		APP_URL = "http://www.baderlab.org/Software/EnrichmentMap";
		USER_MANUAL_URL = "http://enrichmentmap.readthedocs.io/en/latest";
		
		Properties plugin_props;
		try {
			plugin_props = getPropertiesFromClasspath(PROPS_FILE_APP, false);
		} catch (IOException e) {
			e.printStackTrace();
			plugin_props = new Properties();
		}

		APP_VERSION = plugin_props.getProperty("appVersion", "unknown");
		APP_NAME    = plugin_props.getProperty("appName", "EnrichmentMap");
	}

	private static Properties getPropertiesFromClasspath(String propFileName, boolean inMaindir) throws IOException {
		InputStream inputStream;
		if(inMaindir)
			inputStream = EnrichmentMapBuildProperties.class.getClassLoader().getResourceAsStream(propFileName);
		else
			inputStream = EnrichmentMapBuildProperties.class.getResourceAsStream(propFileName);

		if (inputStream == null)
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");

		Properties props = new Properties();
		props.load(inputStream);
		return props;
	}
}
