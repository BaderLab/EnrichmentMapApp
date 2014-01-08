package org.baderlab.csplugins.enrichmentmap;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;


import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTask;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;

public class EnrichmentMapUtils {
	
	public Properties build_props = new Properties();
	//by making these static when both gsea and normal plugins are installed, the about box will display only one of them.
    public Properties plugin_props = new Properties();
    public static String buildId ;
    public static String pluginUrl;
    public static String userManualUrl;
    public static String pluginVersion;
    public static String pluginReleaseSuffix;
    
    //the name can not be static
    public String pluginName;
    
    private static boolean overrideHeatmapRevalidation = false;
    
    public EnrichmentMapUtils(String type){
    		//get the plugin properties from the plugin props file. properties available (pluginName, pluginDescription,
		//pluginVersion, cytoscapeVersion,pluginCategory) --> required in the file.
    		if(type.equals("gsea")){
    			try {
    				this.plugin_props = getPropertiesFromClasspath("gsea/plugin.props",false);
    			} catch (IOException e) {
    			// TODO: write Warning "Could not load 'plugin.props' - using default settings"
			
    			}
    		}
    		else{
    			try{
    				this.plugin_props = getPropertiesFromClasspath("plugin.props", false);
    			}
    			catch(IOException ei){
    				System.out.println("Neither of the configuration files could be found");
    			}
    		}

		pluginUrl = this.plugin_props.getProperty("pluginURL", "http://www.baderlab.org/Software/EnrichmentMap");
		userManualUrl = pluginUrl + "/UserManual";
		pluginVersion = this.plugin_props.getProperty("pluginVersion","0.1");
		pluginReleaseSuffix = this.plugin_props.getProperty("pluginReleaseSuffix","");
		pluginName = this.plugin_props.getProperty("pluginName","EnrichmentMap");
		
		// read buildId properties:
        //properties available in revision.txt ( git.branch,git.commit.id, git.build.user.name, 
		//git.build.user.email, git.build.time, git.commit.id,git.commit.id.abbrev
		//, build.user,build.timestamp, build.os, build.java_version, build.number)
        try {
            this.build_props = getPropertiesFromClasspath("revision.txt",true);
        } catch (IOException e) {
            // TODO: write Warning "Could not load 'buildID.props' - using default settings"
            this.build_props.setProperty("build.number", "0");
            this.build_props.setProperty("git.commit.id", "0");
            this.build_props.setProperty("build.user", "user");
            //Enrichment_Map_Plugin.build_props.setProperty("build.host", "host");-->can't access with maven implementaion
            this.build_props.setProperty("git.build.time", "1900/01/01 00:00:00 +0000 (GMT)");
        }

        this.buildId = "Build: " + this.build_props.getProperty("build.number") +
                                        " from GIT: " + this.build_props.getProperty("git.commit.id") +
                                        " by: " + this.build_props.getProperty("build.user")  ;

		
		
    }

    private Properties getPropertiesFromClasspath(String propFileName, boolean inMaindir) throws IOException {
        // loading properties file from the classpath
        Properties props = new Properties();
        InputStream inputStream;
        
        if(inMaindir)
        		inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
        else
        		inputStream = this.getClass().getResourceAsStream(propFileName);

        if (inputStream == null) {
            throw new FileNotFoundException("property file '" + propFileName
                    + "' not found in the classpath");
            
        }

        props.load(inputStream);
        return props;
    }

    public static boolean isOverrideHeatmapRevalidation() {
		return overrideHeatmapRevalidation;
	}

	public static void setOverrideHeatmapRevalidation(
			boolean overrideHeatmapRevalidation) {
		EnrichmentMapUtils.overrideHeatmapRevalidation = overrideHeatmapRevalidation;
	}
   
    public Properties getBuild_props() {
		return build_props;
	}

	public void setBuild_props(Properties build_props) {
		this.build_props = build_props;
	}

	public Properties getPlugin_props() {
		return plugin_props;
	}

	public void setPlugin_props(Properties plugin_props) {
		this.plugin_props = plugin_props;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getPluginUrl() {
		return pluginUrl;
	}

	public void setPluginUrl(String pluginUrl) {
		this.pluginUrl = pluginUrl;
	}

	public String getUserManualUrl() {
		return userManualUrl;
	}

	public void setUserManualUrl(String userManualUrl) {
		this.userManualUrl = userManualUrl;
	}

	public String getPluginName() {
		return pluginName;
	}

	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}

	
	public static Component getWindowInstance(JPanel panel){
		//recurse up the parents until you find an instance of JFrame or JDialog
		Component parent = panel.getParent();
		Component current = panel;
		while (parent != null){
			//check to see if parent is an instance of JFrame of JDialog
			if(parent instanceof JFrame || parent instanceof JDialog)
				return parent;
			current = parent;
			parent = current.getParent();
		}

		return current;

	}
	
    
}



    

