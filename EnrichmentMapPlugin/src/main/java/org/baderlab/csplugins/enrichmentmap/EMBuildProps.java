package org.baderlab.csplugins.enrichmentmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EMBuildProps {

	private static final String PROPS_FILE_APP = "app.props";
	
	public static final String APP_VERSION;
	public static final String APP_NAME;
	
	
	private static final String DOC_BASE_URL = "http://enrichmentmap.readthedocs.io/en/latest/";
	
	public static final String APP_URL           = "http://www.baderlab.org/Software/EnrichmentMap";
	public static final String HELP_URL_HOME     = DOC_BASE_URL;
	public static final String HELP_URL_CREATE   = DOC_BASE_URL + "CreatingNetwork.html#create-enrichmentmap-dialog";
	public static final String HELP_URL_TUTORIAL = DOC_BASE_URL + "QuickTour.html";
	public static final String HELP_URL_PROTOCOL = DOC_BASE_URL + "Protocol.html";
	public static final String HELP_URL_CONTROL  = DOC_BASE_URL + "MainPanel.html";
	public static final String HELP_URL_HEATMAP  = DOC_BASE_URL + "HeatMapPanel.html";
	public static final String HELP_URL_LEGEND   = DOC_BASE_URL + "LegendDialog.html";
	

	private EMBuildProps() {}
	
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
			inputStream = EMBuildProps.class.getClassLoader().getResourceAsStream(propFileName);
		else
			inputStream = EMBuildProps.class.getResourceAsStream(propFileName);

		if (inputStream == null)
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");

		Properties props = new Properties();
		props.load(inputStream);
		return props;
	}
}
