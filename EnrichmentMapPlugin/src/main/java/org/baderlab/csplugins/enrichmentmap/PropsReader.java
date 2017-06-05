package org.baderlab.csplugins.enrichmentmap;

import java.util.Properties;

import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;

public class PropsReader extends AbstractConfigDirPropsReader {

	public static final String PROPS_FILE_NAME = "enrichmentmap.props";
	public static final String NAME = "org.baderlab.enrichmentmap";
	
	public PropsReader() {
		super(NAME, PROPS_FILE_NAME, CyProperty.SavePolicy.CONFIG_DIR);
	}

	public static Properties getServiceProps() {
		Properties props = new Properties();
		props.setProperty("cyPropertyName", PROPS_FILE_NAME);
		return props;
	}
}
