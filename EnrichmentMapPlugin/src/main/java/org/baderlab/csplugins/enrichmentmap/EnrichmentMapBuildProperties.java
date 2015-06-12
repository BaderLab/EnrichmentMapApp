package org.baderlab.csplugins.enrichmentmap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnrichmentMapBuildProperties {

	private static final String PROPS_FILE_APP   = "app.props";
	private static final String PROPS_FILE_BUILD = "revision.props";
	
	
	public static final String APP_VERSION;
	public static final String APP_NAME;
	public static final String APP_URL;
	public static final String USER_MANUAL_URL;
	public static final String BUILD_ID;
	

	private EnrichmentMapBuildProperties() {}
	
	static {
		APP_URL = "http://www.baderlab.org/Software/EnrichmentMap";
		USER_MANUAL_URL = APP_URL + "/UserManual";
		
		Properties plugin_props;
		try {
			plugin_props = getPropertiesFromClasspath(PROPS_FILE_APP, false);
		} catch (IOException e) {
			e.printStackTrace();
			plugin_props = new Properties();
		}

		APP_VERSION = plugin_props.getProperty("appVersion", "unknown");
		APP_NAME    = plugin_props.getProperty("appName", "EnrichmentMap");


		Properties build_props;
		try {
			build_props = getPropertiesFromClasspath(PROPS_FILE_BUILD, false);
		} catch (IOException e) {
			e.printStackTrace();
			build_props = new Properties();
		}
		
		String build_number   = build_props.getProperty("build.number", "0");
		String build_user     = build_props.getProperty("build.user", "user");
		String git_commit_id  = build_props.getProperty("git.commit.id", "0");
		//String git_build_time = build_props.getProperty("git.build.time", "1900/01/01 00:00:00 +0000 (GMT)");

		BUILD_ID = String.format("Build: %s from GIT: %s by: %s", build_number, git_commit_id, build_user);
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
