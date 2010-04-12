                             Enrichment Map
                           A Cytoscape Plugin
 
        Enrichment Map is a network-based method to visualize 
        and interpret gene-set enrichment results.  

        by Gary Bader, Daniele Merico, Ruth Isserlin and Oliver Stueker 
        (Bader Lab, University of Toronto)  

        Plugin Homepage: http://baderlab.org/Software/EnrichmentMap

Licence:
========
Enrichment Map is free software; you can redistribute it and/or modify it under 
the terms of the GNU Lesser General Public License as published by the Free 
Software Foundation; either version 2.1 of the License, or (at your option) 
any later version.

Enrichment Map is distributed in the hope that it will be useful, but WITHOUT 
ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS 
FOR A PARTICULAR PURPOSE.  The software and documentation provided hereunder is 
on an "as is" basis, and University of Toronto has no obligations to provide 
maintenance, support, updates, enhancements or modifications. In no event shall
the University of Toronto be liable to any party for direct, indirect, special,
incidental or consequential damages, including lost profits, arising out of the 
use of this software and its documentation, even if University of Toronto has 
been advised of the possibility of such damage.  
See the GNU Lesser General Public License (LICENSE.TXT) for more details.

You should have received a copy of the GNU Lesser General Public License
along with this library; if not, write to the Free Software Foundation,
Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.


System requirements:
====================
Enrichment Map requires a Computer with installed Cytoscape 2.6.x.
Please refer to the Cytoscape Manual (available on http://cytoscape.org/)
for Cytoscape's system requirements.


How to install Enrichment Map:
===============================
Manual install:
---------------
1) Download the latest Version of Enrichment Map from 
   http://baderlab.org/Software/EnrichmentMap

2) Locate the folder in which you have installed Cytoscape 2.6
   (e.g. C:\Program Files\Cytoscape-v2.6 or /Applications/Cytoscape_v2.6)
   and in that the folder called "plugins"
   
3) Place the file EnrichmentMap.jar in the Cytoscape-v2.6/plugins folder

4) Start Cytoscape and find the "Enrichment Map" entry in the Plugins menue.

Using the Cytoscape Plugin Manager:
-----------------------------------
1) Start Cytoscape and open the Plugin Manager (Plugins/Manage Plugins)

2) Locate and install the latest Enrichment Map plugin in the "Analysis" 
   Section of "Available for Install".


How to compile Enrichment Map:
==============================
Requirements:
------------
 * installed Cytoscape 2.6.x                         (from http://cytoscape.org)
 * Sun Java Development Kit (JDK/SDK) Version 1.5.x   (from http://java.sun.com)
 * Apache Ant 1.7 or newer                          (from http://ant.apache.org)
 * Enrichment Map source code  (from http://baderlab.org/Software/EnrichmentMap)

Configuration:
---------------
* Make sure that Java and Ant are properly installed and can be run from the 
  command line. (Test by running "java -version", "javac -version" and 
  "ant -version" from the command line)
* either:
  - Set an environment variable called CYTOSCAPE_HOME 
    pointing to your Cytoscape 2.6 installation
  or:
  - place a file called "enrichment_map.properties" in the directory of the 
    unpacked source code distribution of Enrichment Map (the same directory that 
    contains the file build.xml)
	containing a line like this (choose one depending on your platform and 
	adjust to the actual installation directory and the version you are using):

    Apple:
        CYTOSCAPE_HOME=/Applications/Cytoscape_v2.6.x
    Linux:
		CYTOSCAPE_HOME=/usr/local/Cytoscape_v2.6.x
	Windows:
		CYTOSCAPE_HOME=C:/Program Files/Cytoscape-v2.6.x

Compile:
--------
* open a new Command line (Shell) and run one of the following commands:
  - to compile Enrichment Map and pack it into a JAR archive:
      ant jars
    The EnrichmentMap.jar will appear in a folder named "jars".

  - to just compile Enrichment Map (without making a jar):
      ant compile
    The compiled classes will appear in a folder named "classes".

  - to pack a source code release:
      ant release_source
    This will create EnrichmentMapPlugin_v${Version}_src.tar.gz and
    EnrichmentMapPlugin_v${Version}_src.zip in the current directory.
      
  - to pack a binary release:
      ant release_binary
    This will generate a file EnrichmentMapPlugin_v${Version}.zip
    that contains the EnrichmentMap.jar, README.TXT and LICENSE.TXT
      