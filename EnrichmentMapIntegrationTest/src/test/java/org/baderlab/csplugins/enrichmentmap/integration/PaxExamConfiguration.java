package org.baderlab.csplugins.enrichmentmap.integration;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

/**
 * Contains the configuration for Pax Exam.
 */
public abstract class PaxExamConfiguration {
	
	/**
	 * Build minimal set of bundles.
	 */
	@Configuration
	public Option[] config() {
		
		// These have to match what's in the pom.xml files.
		final String cyVersion = "3.7.1"; 
		final String emVersion = "3.3.5-SNAPSHOT";
		final String karafVersion = "4.2.1";
		final String groupId   = "org.baderlab.csplugins";
		final String appBundle = "EnrichmentMap";
 
		return options(
			karafDistributionConfiguration()
		       	.frameworkUrl(maven().groupId("org.apache.karaf").artifactId("apache-karaf").type("zip").version(karafVersion))
	            .karafVersion(karafVersion).name("Apache Karaf").useDeployFolder(false),
			systemProperty("org.osgi.framework.system.packages.extra").value("com.sun.xml.internal.bind"),
			junitBundles(),
			vmOption("-Xmx512M"),

			// So that we actually start all of our bundles!
			frameworkStartLevel(50),

			// Specify all of our repositories
			repository("https://nrnb-nexus.ucsd.edu/repository/cytoscape_releases/"),
			repository("https://nrnb-nexus.ucsd.edu/repository/cytoscape_snapshots/"),
			repository("https://nrnb-nexus.ucsd.edu/repository/cytoscape_thirdparty/"),
			
			// Misc. bundles required to run minimal Cytoscape
			mavenBundle().groupId("org.apache.servicemix.specs").artifactId("org.apache.servicemix.specs.jaxb-api-2.1").version("1.2.0").startLevel(3),
			mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.jaxb-impl").version("2.1.6_1").startLevel(3),
			mavenBundle().groupId("javax.activation").artifactId("com.springsource.javax.activation").version("1.1.1").startLevel(3),
			mavenBundle().groupId("javax.xml.stream").artifactId("com.springsource.javax.xml.stream").version("1.0.1").startLevel(3),
			mavenBundle().groupId("commons-io").artifactId("commons-io").version("2.1").startLevel(3),
			mavenBundle().groupId("javax.ws.rs").artifactId("javax.ws.rs-api").version("2.0").startLevel(3),
			mavenBundle().groupId("io.swagger").artifactId("swagger-annotations").version("1.5.7").startLevel(3),
			
			// Third-party bundle
			mavenBundle().groupId("org.cytoscape.distribution").artifactId("third-party").version(cyVersion).startLevel(3),

			// API bundle
			mavenBundle().groupId("org.cytoscape").artifactId("api-bundle").version(cyVersion).startLevel(5),
			
			// Implementation bundles
			mavenBundle().groupId("org.cytoscape").artifactId("property-impl").version(cyVersion).startLevel(7),
			mavenBundle().groupId("org.cytoscape").artifactId("datasource-impl").version(cyVersion).startLevel(9),
			mavenBundle().groupId("org.cytoscape").artifactId("equations-impl").version(cyVersion).startLevel(9),
			mavenBundle().groupId("org.cytoscape").artifactId("event-impl").version(cyVersion).startLevel(9),
			mavenBundle().groupId("org.cytoscape").artifactId("util-impl").version(cyVersion).startLevel(9),
			mavenBundle().groupId("org.cytoscape").artifactId("model-impl").version(cyVersion).startLevel(11),
			mavenBundle().groupId("org.cytoscape").artifactId("work-impl").version(cyVersion).startLevel(11),
			mavenBundle().groupId("org.cytoscape").artifactId("work-headless-impl").version(cyVersion).startLevel(11),
			mavenBundle().groupId("org.cytoscape").artifactId("swing-util-impl").version(cyVersion).startLevel(12),
			mavenBundle().groupId("org.cytoscape").artifactId("presentation-impl").version(cyVersion).startLevel(13),
			mavenBundle().groupId("org.cytoscape").artifactId("viewmodel-impl").version(cyVersion).startLevel(15),
			mavenBundle().groupId("org.cytoscape").artifactId("vizmap-impl").version(cyVersion).startLevel(15),
			mavenBundle().groupId("org.cytoscape.distribution").artifactId("application-metadata-impl").version(cyVersion).startLevel(15).noStart(),
			mavenBundle().groupId("org.cytoscape").artifactId("application-impl").version(cyVersion).startLevel(17),
			mavenBundle().groupId("org.cytoscape").artifactId("layout-impl").version(cyVersion).startLevel(18),
			mavenBundle().groupId("org.cytoscape").artifactId("group-impl").version(cyVersion).startLevel(18),
			mavenBundle().groupId("org.cytoscape").artifactId("session-impl").version(cyVersion).startLevel(19),
			mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-core-impl").version(cyVersion).startLevel(20),
			mavenBundle().groupId("org.cytoscape").artifactId("ding-presentation-impl").version(cyVersion).startLevel(21),
			mavenBundle().groupId("org.cytoscape").artifactId("io-impl").version(cyVersion).startLevel(23),
			mavenBundle().groupId("org.cytoscape").artifactId("core-task-impl").version(cyVersion).startLevel(25),
			//mavenBundle().groupId("org.cytoscape").artifactId("filter2-impl").version(implBundleVersion).startLevel(25)
			//mavenBundle().groupId("org.cytoscape").artifactId("vizmap-gui-impl").version(implBundleVersion).startLevel(27)
			
			mavenBundle().groupId(groupId).artifactId(appBundle).version(emVersion).startLevel(30)
		);
	}
	
	// not working
//	@ProbeBuilder
//	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
//	    System.out.println("TestProbeBuilder gets called");
//	    probe.setHeader(Constants.DYNAMICIMPORT_PACKAGE, "*");
//	    probe.setHeader(Constants.EXPORT_PACKAGE, "org.baderlab.csplugins.enrichmentmap,org.baderlab.csplugins.enrichmentmap.task");
//	    return probe;
//	}
	
	
}
