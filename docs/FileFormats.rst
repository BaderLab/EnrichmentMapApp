File Formats
============

Gene sets file (GMT file)
-------------------------

* Each row of the geneset file represents one geneset and consists of:

``geneset name (--tab--) description (--tab--) a list of tab-delimited genes``

* The geneset names must be unique.
* The gene set file describes the genesets used for the analysis. These files can be obtained...
  
  1. directly downloading our monthly updated gene-set collections from 
     `Baderlab genesets collections`_. Description of sources and methods used to create 
     collection can be found on the :ref:`gene_sets` page.
  2. directly downloading gene-sets collected in the MSigDB_
  3. converting gene annotations / pathways from public databases

.. note:: If you use MSigDB Gene Ontology gene-sets, please consider that they do not include all 
          annotations, as an evidence code filter is applied; if you are interested in achieving 
          maximum coverage, download the original annotations.

.. note:: if you are a R user, Bioconductor offers annotation packages such as ``GO.db``, 
          ``org.Hs.eg.db``, ``KEGG.db``

.. _Baderlab genesets collections: http://download.baderlab.org/EM_Genesets/current_release/
.. _MSigDB: http://software.broadinstitute.org/gsea/msigdb/index.jsp


Expression Data file (GCT, TXT or RNK file) [OPTIONAL]
------------------------------------------------------

* The expression data can be loaded in three different formats: gct (GSEA file type), rnk (GSEA 
  file type) or txt.
* The expression data serves two purposes:

  * Expression data is used by the Heatmap when clicking on nodes and edges in the Enrichment map 
    so the expression of subsets of data can be viewed.
  * Gene sets are filtered based on the genes present in the expression file. For example, if 
    Geneset X contains genes {1,2,3,4,5} but the expression file only contain expression value 
    for genes {1,2,3} Geneset X will be represented as {1,2,3} in the Enrichment Map. 

* Expression data is not required. In the absence of an expression file Enrichment map will create 
  a dummy expression file to associate with the data set. The dummy expression gives an expression 
  value of 1 for all the genes associated with the enriched genesets in the Enrichment map.

.. note:: If you are running a two dataset analysis with no expression files the genes for each 
          dataset is calculated based on the enriched genesets. If a geneset is enriched in one 
          dataset and not the other this could create different subsets of genes associated to each 
          datasets and create multiple edges between genesets. To avoid this, create a fake 
          expression file with the set of genes used for both analyses.

GCT (GSEA file type)
~~~~~~~~~~~~~~~~~~~~

* GCT differs from TXT only because of two additional lines that are required at the top of the 
  file.
* The GCT file contains two additional lines at the top of the file.

  * The first line contains ``#1.2``.
  * The second line contains ``the number of data rows (--tab--) the number of data columns``
  * The third line consists of column headings.

  ``name (--tab--) description (--tab--) sample1 name (--tab--) sample2 name ...``

  * Each line of expression file contains a:

  ``name (--tab--) description (--tab--) list of tab delimited expression values``

.. note:: If the GCT file contains Probeset ID's as primary keys (e.g. as you had GSEA collapse 
          your data file to gene symbols) you need to convert the gct file to use the same 
          primary key as used in the gene sets file (GMT file). You have the following options:

          * Use the GSEA desktop application: GSEA / Tools / Collapse Dataset
          * Run this Python script :ref:`collapse_ExpressionMatrix` using the Chip platform file 
            that was used by GSEA. 

RNK (GSEA file type)
~~~~~~~~~~~~~~~~~~~~

* RNK file is completely different from the GCT or TXT file. It represents a ranked list of genes 
  containing only gene name and a rank or score.
* The first line contains column headings
        
  For example: ``Gene Name (--tab--) Rank Name``
    
* Each line of RNK file contains:
 
  ``name (--tab--) rank OR score``

`Additional Information on GSEA File Formats <http://software.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats>`_

TXT
~~~

* Basic file representing expression values for an experiment. 
* The first line consists of column headings.

  ``name (--tab--) description (--tab--) sample1 name (--tab--) sample2 name ...``
    
* Each line of the expression file contains:

  ``name (--tab--) description (--tab--) list of tab delimited expression values`` 


Enrichment Results Files
------------------------

GSEA result files
~~~~~~~~~~~~~~~~~

* For each analysis GSEA produces two output files. One representing the enriched genesets in
  phenotype A and the other representing the enriched genesets in phenotype B.
* These files are usually named ``gsea_report_for_phenotypeA.Gsea.########.xls`` and 
  ``gsea_report_for_phenotypeB.Gsea.########.xls``
* The files should be loaded in as is and require no pre-processing.
* There is no need to worry about which Enrichment Results Text box to put the two files. The 
  phenotype is specified by the sign of the ES score and is computed internally by the program. 

`Additional Information on GSEA File Formats <http://software.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats>`_

Generic results files
~~~~~~~~~~~~~~~~~~~~~

* The generic results file is a tab delimited file with enriched gene-sets and their corresponding 
  p-values (and optionally, FDR corrections)
* The Generic Enrichment Results file needs:
        
  * gene-set ID (must match the gene-set ID in the GMT file)
  * gene-set name or description
  * p-value
  * FDR correction value
  * Phenotype: +1 or -1, to identify enrichment in up- and down-regulation, or, more in general, 
    in either of the two phenotypes being compared in the two-class analysis

    * +1 maps to red
    * -1 maps to blue 

  * gene list separated by commas 

.. note:: Description and FDR columns can have empty or NA values, but the column and the 
          column header must exist.

.. note:: If no value is provided under phenotype, Enrichment Map will assume there is only one 
          phenotype, and will map enrichment p-values to red.

:ref:`examples_of_generic_files`


DAVID Enrichment Result File
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Available only in v1.0 or higher
* The DAVID option expects a file as generated by the DAVID web interface.
* When using DAVID as the analysis type there is no requirement to enter either a gmt file or an 
  expression file. Both are options if the user wishes to add them to the analysis.
* The DAVID Enrichment Result File is a file generated by the DAVID Functional Annotation Chart 
  Report and consists of the following fields: **Important**: Make sure you are using CHART Report 
  and NOT a Clustered Report.

  * Category (DAVID category, i.e. Interpro, sp_pir_keywords, ...)
  * Term - Gene set name
  * Count - number of genes associated with this gene set
  * Percentage (gene associated with this gene set/total number of query genes)
  * P-value - modified Fisher Exact P-value
  * Genes - the list of genes from your query set that are annotated to this gene set.
  * List Total - number of genes in your query list mapped to any gene set in this ontology
  * Pop Hits - number of genes annotated to this gene set on the background list
  * Pop Total - number of genes on the background list mapped to any gene set in this ontology.
  * Fold enrichment
  * Bonferroni
  * Benjamini
  * FDR 
 
.. warning:: In the absence of a gmt gene sets are constructed based on the 
             field Genes in the DAVID output. This only considers the genes entered in your 
             query set and not the genes in your background set. This will drastically affect 
             the amount of overlap you see in the resulting Enrichment Map. 

:ref:`david_tutorial`


BiNGO Enrichment Result File
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* Available only in v1.2 or higher
* The BiNGO option expects a file as generated by the BiNGO Cytsocape Plugin.
* When using BiNGO as the analysis type there is no requirement to enter either a gmt file 
  or an expression file. Both are options if the user wishes to add them to the analysis.
* The BiNGO Enrichment Result File is a file generated by the BiNGO cytoscape plugin 
  and consists of the following fields: **Important**: When running BiNGO make sure to 
  check off "Check Box for saving data"

  * The first 20 lines of BiNGO output file list parameters used for the analysis and are ignored by the Enrichment map plugin
  * GO-ID - Gene set name
  * p-value - hypergeometric or binomial Exact P-value
  * corr p-value - corrected p-value
  * x - number of genes in your query list mapped to this gene-set
  * n - number of genes in the background list mapped to this gene-set
  * X - number of genes annotated to this gene set on the background list
  * N - number of genes on the background list mapped to any gene set in this ontology.
  * Description - gene list description
  * Genes - the list of genes from your query set that are annotated to this gene set. 

.. warning:: In the absence of a gmt gene sets are constructed based on the field Genes in 
             the BiNGO output. This only considers the genes entered in your query set and 
             not the genes in your background set. This will drastically affect the amount 
             of overlap you see in the resulting Enrichment Map. 

:ref:`david_tutorial`


.. _rpt_files:

RPT files
~~~~~~~~~

* A special trick for GSEA results, in any GSEA analysis an rpt file is created that specifies 
  the location of all files (including the gmt, gct, results files, phenotype specification, 
  and rank files).
* Any of the Fields under the dataset tab (Expression, Enrichment Results 1 or Enrichment Results 2) 
  will accept an rpt file and populate GMT, Expression, Enrichment Results 1, Enrichment Results 2, 
  Phenotypes, and Ranks the values for that dataset.
* A second rpt file can be loaded for dataset 2. It will give you a warning if the GMT file 
  specified is different than the one specified in dataset 1. You will have the choice to use 
  the GMT for data set 1, data set 2 or abort the second rpt load.
* An rpt file is a text file with following information (parameters surrounded by " ' ' '" are 
  those that EM uses): 

::

  '''producer_class'''    xtools.gsea.Gsea
  '''producer_timestamp'''        1367261057110
  param   collapse        false
  param   '''cls'''       WHOLE_PATH_TO_FILE/EM_EstrogenMCF7_TestData/ES_NT.cls#ES24_versus_NT24
  param   plot_top_x      20
  param   norm    meandiv
  param   save_rnd_lists  false
  param   median  false
  param   num     100
  param   scoring_scheme  weighted
  param   make_sets       true
  param   mode    Max_probe
  param   '''gmx'''       WHOLE_PATH_TO_FILE/EM_EstrogenMCF7_TestData/Human_GO_AllPathways_no_GO_iea_April_15_2013_symbol.gmt
  param   gui     false
  param   metric  Signal2Noise
  param   '''rpt_label''' ES24vsNT24
  param   help    false
  param   order   descending
  param   '''out'''       WHOLE_PATH_TO_FILE/EM_EstrogenMCF7_TestData
  param   permute gene_set
  param   rnd_type        no_balance
  param   set_min 15
  param   include_only_symbols    true
  param   sort    real
  param   rnd_seed        timestamp
  param   nperm   1000
  param   zip_report      false
  param   set_max 500
  param   '''res'''       WHOLE_PATH_TO_FILE/EM_EstrogenMCF7_TestData/MCF7_ExprMx_v2_names.gct

  file    WHOLE_PATH_TO_FILE/EM_EstrogenMCF7_TestData/ES24vsNT24.Gsea.1367261057110/index.html

Parameters used by EM and their meaning:

1. producer_class - can be xtools.gsea.Gsea or xtools.gsea.GseaPreranked

  * if xtools.gsea.Gsea:

    * get expression file from res parameter in rpt
    * get phenotype information from cls parameter in rot

  * if xtools.gsea.GseaPreranked:

    * No expression file
    * use rnk as the expression file from rnk parameter in rot
    * set phenotypes to na_pos and na_neg.
    * NOTE: if you want to make using an rpt file easier for GSEAPreranked there are two 
      additional parameters you can add to your rpt file manually that the rpt function 
      will recognize.
    * To do less manual work while creating Enrichment Maps from pre-ranked GSEA, add the 
      following optional parameters to your rpt file::

        param(--tab--)phenotypes(--tab--){phenotype1}_versus_{phenotype2}
        param(--tab--)expressionMatrix(--tab--){path_to_GCT_or_TXT_formated_expression_matrix} 

2. producer_timestamp - needed to find the directory with the results files
3. cls - path to class/phenotype file with information regarding the phenotypes:

 * path/classfilename.cls#phenotype1_versus_phenotype2
 * EM get the path to the class file and also pulls the phenotype1 and phenotype2 
   from the above field 

4. gmx - path to gmt file
5. rpt_label - name of analysis and name of directory that GSEA creates to hold the results. 
   Used when constructing the path to the results directory.
6. out - path to directory where GSEA will put the output directory. Used when constructing 
   the path to the results directory.
7. res - path to expression file. 

rpt Searches for the following results files: 

::

  Enrichment File 1 --> {out}(--File.separator--){rpt_label} + "." + {producer_class} + "." + {producer_timestamp}(--File.separator--) "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls"
  Enrichment File 2 --> {out}(--File.separator--){rpt_label} + "." + {producer_class} + "." + {producer_timestamp}(--File.separator--) "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls"
  Ranks File --> {out}(--File.separator--){rpt_label} + "." + {producer_class} + "." + {producer_timestamp}(--File.separator--) "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";      

* If the enrichments and rank files are not found in the above path then EM replaces the 
  out directory with the path to the given rpt file and tries again.
* If you would like to create your own rpt file for your own analysis pipeline you can put 
  your own values for the above used parameters.
* If your analysis only creates one enrichment file you can make a copy of enrichment file 
  1 in the path of enrichment file 2 with no consequences for EM running. 


EDB File (GSEA file type)
~~~~~~~~~~~~~~~~~~~~~~~~~

* Contained in the GSEA results folder is an edb folder. In the edb folder there are the 
  following files:

  * results.edb
  * gene_sets.gmt
  * classfile.cls [Only in a GSEA analysis. Not in a GSEAPreranked analysis]
  * rankfile.rnk 

* If you specify the results.edb file in any of the Fields under the dataset tab 
  (Expression, Enrichment Results 1 or Enrichment Results 2) the gmt and enrichment 
  files fields will be automatically populated.

* If you want to associate an expression file with the analysis it needs to be loaded 
  manually as described here. 

.. note:: The gene_sets.gmt file contained in the edb directory is filtered according 
          to the expression file.  If you are doing a two dataset analysis where the 
          expression files are from different platforms or contain different sets of 
          genes the edb gene_sets.gmt file can not be used as genes found in one analysis 
          might be lacking in the other.  In this case use the original gmt file (prior 
          to GSEA filtering) and EM will filter each the gene sets separately according 
          to each dataset.

Advanced Settings - Additional Files
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* For each dataset there are additional parameters that the user can set but are not required.
  The advanced parameters include:

  * Ranks file - file specifying the ranks of the genes in the analysis

    * This file has the format specified in the above section - gene (--tab--) rank or score. 
      See `RNK (GSEA file type)`_ for details. 

  * Phenotypes (phenotype1 versus phenotype2)

    * By default the phenotypes are set to Up and Down but in the advanced setting mode 
      the user can change these to any desired text. 

* All of these fields are populated when the user loads the input files using the rpt option.
