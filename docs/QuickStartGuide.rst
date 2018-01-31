Quick Start Guide
=================

Creating an Enrichment Map
--------------------------

You have a few different options:

  * Load GSEA Results
  * Load Generic Results
  * Load David Results
  * Load Bingo Results 

The only difference between the above modes is the structure of the enrichment table(s). In either 
case, to use the plugin you will need the following files:

  * file.gmt: gene-set to gene ID
  * file.txt or .gct: expression matrix [OPTIONAL]
  * file.txt or .xls: enrichment table(s) 

.. note:: GSEA saves the enrichment table as a .xls file; however, these are not true Excel files, 
          they are tab-separated text files with a modified extension; Enrichment Map does not work 
          with "true" Excel .xls files.

If your enrichment results were generated from GSEA, you will just have to pick the right files from
your results folder. If you have generated the enrichment results using another method, you will 
have to go to the Full User Guide, File Format section, and make sure that the file format complies 
with Enrichment Map requirements.

You can use the parameter defaults. For a more careful choice of the parameter settings, please go 
to the |Full User Guide, Tips on Parameter Choice|. 


Graphical Mapping of Enrichment
-------------------------------

* Nodes represent gene-sets.
* Node size represents how many genes are in the gene-set. 
* Edges represent mutual overlap.
* Enrichment significance (p-value) is conveyed as node colour intensity.
* The enriched phenotype is conveyed by node colour hue.

.. note:: In standard two-class designs, where two phenotypes are compared (e.g. treated vs 
          untreated) the colour hue conveys the enriched phenotype; this is equivalent to mapping 
          enrichment in up- and down-regulated genes, if one of the two phenotypes is assumed as 
          reference (e.g. untreated), and the other phenotype is the one of interest; in such a 
          case, enriched in the phenotype of interest means up, and enrichment in the reference 
          phenotype means down. 
    

Exploring the Enrichment Map
----------------------------

* The "Parameters" tab in the "Results Panel" on the right side of the window contains a legend 
  mapping the colours to the phenotypes and displaying the parameters used to create the map 
  (cut-off values and data files).
* The "Network" tab in the "Control Panel" on the left lists all available networks in the current 
  session and at the bottom has a overview of the current network which allows to easily navigate 
  in a network even at higher zoom levels by dragging the blue rectangle (the current view) over 
  the network.
* Clicking on a node (the circle that represents a gene set) will open the "EM Geneset Expression 
  Viewer" tab in the "Data Panel" showing a heatmap of the expression values of all genes in the 
  selected gene set.
* Clicking on an edge (the line between two nodes) will open the "EM Overlap Expression Viewer" 
  tab in the "Data Panel" showing a heatmap of the expression values of all genes both gene sets 
  that are connected by this edge have in common.
* If several nodes and edges are selected (e.g. by dragging a selection box around the desired gene 
  sets) the "EM Geneset Expression Viewer" will show the union of all genes in the selected gene 
  sets and the "EM Overlap Expression Viewer" will show only those genes that all selected gene 
  sets have in common. 


Advanced Tips
-------------

* With large networks and low zoom-levels Cytoscape automatically reduces the details (such as 
  hiding the node labels and not showing the node borders). To override this mechanism click on 
  "View / Show Graphics Details"
* The VizMapper and the Node- and Edge Attribute Browser open up a lot more visualization options 
  like linking the label size to Enrichment Scores or p-values. Refer to the Cytoscape manual at 
  www.cytoscape.org for more information.
* If you have used Genesets from GSEAs MSigDb, you can access additional informations for each gene 
  set, by adding the a new property:

        (Edit / Preferences / Properties... / Add -> enter property name: nodelinkouturl.MSigDb -> 
        enter property value: http://www.broad.mit.edu/gsea/msigdb/cards/%ID%.html -> [ (./) ] Make 
        Current Cytoscape Properties default -> (OK) ). Now you can right-click on a node and choose 
        LinkOut/MSigDb to open the Database entry of the Geneset represented by that node in your 
        Browser. 

* When loading GSEA results there is no need to specify each file. Use the GSEA RPT file to 
  auto-populate all the file fields in the EM interface. Check out: |How to use RPT files|
* You can specify more lax p-value, q-value and coefficient threshold initially and fine tune them 
  after the network is created by adjusting them through the p-value, q-value and coefficient 
  tuners in the results panel. Check out: |How to use Parameters Panel|