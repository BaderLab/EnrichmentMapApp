.. _parameters:

Tips on Parameter Choice
========================

Node (Gene Set inclusion) Parameters
------------------------------------

* Node specific parameters filter the gene sets included in the enrichment map.
* For a gene set to be included in the enrichment map it needs to pass both 
  p-value and q-value thresholds. 

**P-value**

* All gene sets with a p-value with the specified threshold or below are included in the map. 

**FDR Q-value**

* All gene sets with a q-value with the specified threshold or below are included in the map.
* Depending on the type of analysis the FDR Q-value used for filtering genesets by EM is different

  * For GSEA the FDR Q-value used is 8th column in the gsea_results file and is called "FDR q-val".
  * For Generic the FDR Q-value used is 4th column in the generic results file.
  * For David the FDR Q-value used is 12th column in the david results file and is called "Benjamini".
  * For Bingo the FDR Q-value used is 3rd column in the Bingo results file and is called "core p-value" 


Edge (Gene Set relationship) Parameters
---------------------------------------

* An edge represents the degree of gene overlap that exists between two gene sets, A and B.
* Edge specific parameters control the number of edges that are created in the enrichment map.
* Only one coefficient type can be chosen to filter the edges.

**Jaccard Coefficient** 
::

  Jaccard Coefficient = [size of (A intersect B)] / [size of (A union B)]

**Overlap Coefficient**
::

  Overlap Coefficient = [size of (A intersect B)] / [size of (minimum( A , B))]

**Combined Coefficient**

* the combined coefficient is a merged version of the jacquard and overlap coefficients.
* the combined constant allows the user to modulate reciprocally the weights associated 
  with the jacquard and overlap coefficients.
* When k = 0.5 the combined coefficient is the average between the jacquard and overlap. 

::

  Combined Constant = k
  Combined Coefficient = (k * Overlap) + ((1-k) * Jaccard)


Tips on Parameter Choice
------------------------

**P-value and FDR Thresholds**

GSEA can be used with two different significance estimation settings: gene-set permutation 
and phenotype permutation. Gene-set permutation was used for Enrichment Map application 
examples.

*Gene-set Permutation*

Here are different sets of thresholds you may consider for gene-set permutation:

  Very permissive:
    * p-value < 0.05
    * FDR < 0.25 

  Moderately permissive:
    * p-value < 0.01
    * FDR < 0.1 

  Moderately conservative:
    * p-value < 0.005
    * FDR < 0.075 

  Conservative:
    * p-value < 0.001
    * FDR < 0.05 

For high quality, high coverage transcriptomic data, the number of enriched terms at the 
very conservative threshold is usually 100-250 when using gene-set permutation.

*Phenotype Permutation*

  Recommended:
    * p-value < 0.05
    * FDR < 0.25 

In general, we recommend to use permissive thresholds only if your having a hard time finding 
any enriched terms.

**Jaccard vs. Overlap Coefficient**

* The Overlap Coefficient is recommended when relations are expected to occur between 
  large-size and small-size gene-sets, as in the case of the Gene Ontology.
* The Jaccard Coefficient is recommended in the opposite case.
* When the gene-sets are about the same size, Jaccard is about the half of the Overlap 
  Coefficient for gene-set pairs with a small intersection, whereas it is about the same 
  as the Overlap Coefficient for gene-sets with large intersections.
* When using the Overlap Coefficient and the generated map has several large gene-sets 
  excessively connected to many other gene-sets, we recommend switching to the Jaccard 
  Coefficient. 

**Overlap Thresholds**

* 0.5 is moderately conservative, and is recommended for most of the analyses.
* 0.3 is permissive, and might result in a messier map. 

**Jaccard Thresholds**

* 0.5 is very conservative
* 0.25 is moderately conservative 
