EnrichmentMap Cytoscape App 2.2
===============================

The Enrichment Map Cytoscape Plugin allows you to visualize the results of gene-set enrichment as a 
network. It will operate on any generic enrichment results as well as specifically on Gene Set 
Enrichment Analysis (GSEA) results. Nodes represent gene-sets and edges represent mutual overlap; 
in this way, highly redundant gene-sets are grouped together as clusters, dramatically improving 
the capability to navigate and interpret enrichment results.

Gene-set enrichment is a data analysis technique taking as input:

1. A (ranked) gene list, from a genomic experiment
2. gene-sets, grouping genes on the basis of a-priori knowledge (e.g. Gene Ontology) or experimental
   data (e.g. co-expression modules) 

and generating as output the list of enriched gene-sets, i.e. best sets that summarizing the 
gene-list. It is common to refer to gene-set enrichment as functional enrichment because functional 
categories (e.g. Gene Ontology) are commonly used as gene-sets. 

.. image:: images/EM_example_2.png


.. toctree::
   :maxdepth: 3
   :caption: User Guide

   Installing
   QuickStartGuide
   FileFormats
   Parameters
   Interfaces
   Attributes
   AdditionalFeatures
   GeneSets
   ExamplesOfGenericEnrichmentResultFiles
  

.. toctree::
   :maxdepth: 2
   :caption: Links
   
   Baderlab.org <http://baderlab.org>
   Baderlab genesets collections <http://download.baderlab.org/EM_Genesets/current_release>
   Cytoscape.org <http://cytoscape.org>
   Cytoscape App Store <http://apps.cytoscape.org/apps/enrichmentmap>
   GitHub <https://github.com/BaderLab/EnrichmentMapApp>