Attributes
==========

Node Attributes
---------------

For each Enrichment map created the following attributes are created for each node:

	EM#_Name
	  The gene set name.

	EM#_Formatted_name
	  A wrapped version of the gene set name so it is easy to visualize.
	  Note: This is the default label of the node but some users find it easier to 
	  arrange the network when the name is not wrapped. If this is the case in the vizmapper 
	  the user can switch the label mapping from EM#_formatted_name to EM#_name. 

	EM#_GS_DESCR
	  The gene set description (as specified in the second column of the gmt file).

	EM#_Genes
	  The list of genes that are part of this gene set. 

Additionally there are attributes created for each dataset (a different set for each dataset 
if using two dataset mode):

	EM#_pvalue_dataset(1 or 2)
	  Gene set p-value, as specified in GSEA enrichment result file.

	EM#_qvalue_dataset(1 or 2)
	  Gene set q-value, as specified in GSEA enrichment result file.

	EM#_Colouring_dataset(1 or 2)
	  Enrichment map parameter calculated using the formula 1-pvalue multiplied by the sign 
	  of the ES score (if using GSEA mode) or the phenotype (if using the Generic mode)

GSEA specific attributes (these attributes are not populated when creating an enrichment 
map using the generic mode).

	EM#_ES_dataset(1 or 2)
	  Enrichment score, as specified in GSEA enrichment result file.

	EM#_NS_dataset(1 or 2)
	  Normalized Enrichment score, as specified in GSEA enrichment result file.

	EM#_fwer_dataset(1 or 2)
	  Family-wise error score, as specified in GSEA enrichment result file. 


Edge Attributes
---------------

For each Enrichment map created the following attributes are created for each edge:

	EM#_Overlap_size
	  The number of genes associated with the overlap of the two genesets that this edge connects.

	EM#_Overlap_genes
	  The names of the genes that are associated with the overlap of the two genesets that this 
	  edge connects.

	EM#_similarity_coefficient
	  The calculated coefficient for this edge. 