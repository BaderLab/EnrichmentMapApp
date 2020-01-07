package org.baderlab.csplugins.enrichmentmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnrichmentMapBuildProperties {

	private static final String PROPS_FILE_APP   = "app.props";
	
	public static final String APP_VERSION;
	public static final String APP_NAME;
	
	public static final String APP_URL          = "http://www.baderlab.org/Software/EnrichmentMap";
	public static final String HELP_URL_HOME    = "http://enrichmentmap.readthedocs.io/en/latest";
	public static final String HELP_URL_CONTROL = "http://enrichmentmap.readthedocs.io/en/latest/MainPanel.html";
	

	private EnrichmentMapBuildProperties() {}
	
	static {
		Properties props;
		try {
			props = getPropertiesFromClasspath(PROPS_FILE_APP, false);
		} catch (IOException e) {
			e.printStackTrace();
			props = new Properties();
		}

		APP_VERSION = props.getProperty("appVersion", "unknown");
		APP_NAME    = props.getProperty("appName", "EnrichmentMap");
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
