package org.baderlab.csplugins.enrichmentmap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cytoscape.application.CyApplicationConfiguration;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;



/**
 * Version 2.1.0 of EnrichmentMap on the app store has the wrong Bundle-Name in the manifest file.
 * It was accidentally set to "enrichmentmap" when it should be "EnrichmentMap".
 * That means the cytoscape app manager won't remove version 2.1 when installing version 2.2 or higher.
 * The only fix is to manually remove the 2.1 app file programatically, sigh.
 */
public class Em21Handler {
	
	private static final String baseName = "EnrichmentMap-v2.1.0";
	
	public static void removeVersion21(BundleContext bc, CyApplicationConfiguration appConfig) {
		try {
			if(isEM21Installed(bc)) {
				System.out.println("EnrichmentMap 2.1 is installed");
				
				Path installFolder = getAppInstallFolder(appConfig);
				Path jarFile = installFolder.resolve(baseName + ".jar");
				
				boolean deleted = Files.deleteIfExists(jarFile);
				if(deleted) {
					System.out.println("Deleted EnrichmentMap 2.1 App Jar file: " + jarFile);
				}
				else {
					// We know its installed but the above code didn't delete the file.
					// Sometimes the App manager appends numbers to the end of the file name, lets try that.
					for(int i = 1; i <= 5; i++) {
						jarFile = installFolder.resolve(baseName + "-" + i + ".jar");
						deleted = Files.deleteIfExists(jarFile);
						if(deleted) {
							System.out.println("Deleted App Jar file: " + jarFile);
							break;
						}
					}
					if(!deleted) {
						// For some reason the file couldn't be deleted.
						// Don't pop up a warning dialog, because we've had UI deadocks by throwing up dialogs
						// during cytoscape initialization. Just log it.
						System.out.println("Could not automatically uninstall EnrichmentMap 2.1, please uninstall it using the App Manager");
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static boolean isEM21Installed(BundleContext bc) {
		for(Bundle bundle : bc.getBundles()) {
			String name = bundle.getSymbolicName();
			Version version = bundle.getVersion();
			if(name.equals("org.baderlab.csplugins.enrichmentmap") && version.equals(new Version(2,1,0))) {
				return true;
			}
		}
		return false;
	}
	
	private static Path getAppInstallFolder(CyApplicationConfiguration appConfig) {
		File configFolder = appConfig.getConfigurationDirectoryLocation();
		Path installFolder = configFolder.toPath().resolve("3/apps/installed");
		return installFolder;
	}
}
