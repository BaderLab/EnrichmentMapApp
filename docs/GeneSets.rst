.. _gene_sets:

EnrichmentMap Gene Sets
=======================

EnrichmentMap is a Cytoscape plugin developed in the Baderlab to help visualize, navigate and 
analyze functional enrichment results as generated from programs such as Gene Set Enrichment 
Analysis(GSEA), BiNGO, or David. Some enrichment programs, such as GSEA, allow the user to search 
against their own gene set database. As annotation (gene set) sources are regularly updated as new 
information is discovered we set up an automated system to update our gene set collections so we 
are always using the most up-to-date annotations.

If you use these gene sets, please cite our Enrichment Map paper.

.. note:: **(January 2016)** With the latest build of pathways we have removed KEGG from the 
   main compilation set of pathways. If you would like to include KEGG in your analysis 
   the sets are located in the `misc/` directory and can be appended to your gmt file.

.. note:: **(April 2012)** Genesets files from December 2011, January 2012, Februrary 2012, 
   and March 2012 had an error in the up-propagation of GO. Up-propagation only followed 
   the *is-a* relationship and did not follow the *part-of* relationship which translates 
   into missing annotations. This primarily effects genesets in GO cellular compartment. 

.. _Baderlab genesets collections: http://download.baderlab.org/EM_Genesets/current_release/

Summary
-------

Gene Set Files can be downloaded from: `Baderlab genesets collections`_

Enrichment Map Gene Sets are a set of Gene Set files in GMT format (compatible with GSEA) 
updated monthly from original source locations available with:

* Entrez gene ids
* UniProt accessions
* Gene symbols 


The GMT File format contains one Gene Set per line. Each line contains:

* Name (tab) Description (tab) Gene (tab) Gene (tab) ...
* In our format:

  * Name = Gene Set Name % Gene Set Source % Gene Set Source identifier

    * Example --> ATP-dependent protein binding%GO%GO:0043008 OR arginine biosynthesis IV%HUMANCYC%ARGININE-SYN4-PWY 

  * Description = Gene Set Name

    * Example --> ATP-dependent protein binding OR arginine biosynthesis IV 

  * Gene = identified by one of the three possible identifiers (Entrez gene id, UniProt 
    accession or gene symbols)
  * **IMPORTANT NOTE**: Originally we used the "|" to separate information in the Name 
    field but we came across issues with this separator in GSEA so we changed to "%". 
    The "%" was used as of the December 2011 build. 

In the main directory (current_release/Human/symbol) there are 5 primary files to choose from:

  **Human_GO_AllPathways_with_GO_iea_{Date}_{ID}.gmt**
    Contains genesets from all 3 divisions of GO (biological process, molecular function, 
    cellular component) including annotations that have evidence code IEA (inferred from 
    electronic annotation), ND (no biological data available), and RCA (inferred from 
    reviewed computational analysis) and all pathway resources.

  **Human_GO_AllPathways_no_GO_iea_{Date}_{ID}.gmt**
    Contains genesets from all 3 divisions of GO (biological process, molecular function, 
    cellular component) excluding annotations that have evidence code IEA (inferred from 
    electronic annotation), ND (no biological data available), and RCA (inferred from 
    reviewed computational analysis) and all pathway resources.

  **Human_GOBP_AllPathways_with_GO_iea_{Date}_{ID}.gmt**
    Contains only genesets from GO biological process including annotations that have 
    evidence code IEA (inferred from electronic annotation), ND (no biological data available), 
    and RCA (inferred from reviewed computational analysis) and all pathway resources.

  **Human_GOBP_AllPathways_no_GO_iea_{Date}_{ID}.gmt** (recommended file)
    Contains only genesets from GO biological process excluding annotations that have evidence 
    code IEA (inferred from electronic annotation), ND (no biological data available), and RCA 
    (inferred from reviewed computational analysis) and all pathway resources.

  **Human_AllPathways_{Date}_{ID}.gmt**
    Contains only genesets from all pathways resources. 


Current Stats
-------------

Human
  http://download.baderlab.org/EM_Genesets/current_release/Human/Summary_Geneset_Counts.txt

Mouse
  http://download.baderlab.org/EM_Genesets/current_release/Mouse/Summary_Geneset_Counts.txt


Sources
-------

.. _KEGG: http://www.genome.jp/kegg/
.. _MSigDB: http://software.broadinstitute.org/gsea/msigdb/index.jsp
.. _NCI: http://pid.nci.nih.gov/
.. _Institute of Bioinformatics: http://www.ibioinformatics.org/
.. _NetPath: http://www.netpath.org/browse/
.. _HumanCyc: https://humancyc.org/
.. _Reactome: https://reactome.org/ReactomeGWT/entrypoint.html
.. _GO: https://www.ebi.ac.uk/GO
.. _Panther: http://www.pantherdb.org/pathway/

Human
~~~~~

+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| Source                         | File Type | ID extracted | Frequency source is updated  | Number of pathways                      | File Origin                         |
+================================+===========+==============+==============================+=========================================+=====================================+
| KEGG_                          | GMT       | Symbol       | static as of July 1, 2011    | 236                                     | KEGG ftp site (July 2011)           |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| MSigDB_ - C2                   | GMT       | Entrez gene  | sporadically                 | Biocarta - 217,                         | manual download                     |
|                                |           |              |                              | Other - 47                              |                                     |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| NCI_                           | BioPAX    | Entrez gene  | sporadically                 | 219 pathways                            | scripted download of zipped release |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| `Institute of Bioinformatics`_ | BioPAX    | Entrez gene  | sporadically                 | 35 pathways - 10                        | received directly from IOB          |
| (IOB)                          |           |              |                              | are the same as CellMap,                | - static (July 2011)                |
|                                |           |              |                              | 1 is the same as NetPath                |                                     |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| NetPath_  (IOB)                | BioPAX    | Entrez gene  | static                       | 25 pathways -                           | scripted download of files          |
|                                |           |              |                              | 12 are cancer pathways (10 are CellMap),| numbered 1-25                       |
|                                |           |              |                              | 13 are immunity pathways                |                                     |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| HumanCyc_                      | BioPAX    | UniProt      | updated periodically         | 249 Pathways                            | scripted download of zipped release |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| Reactome_                      | BioPAX    | UniProt      | updated release              | 1117 pathways (release 37)              | scripted download of zipped release |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| GO_                            | GAF       | Uniprot      | released once a month        | 13034 no GO IEA, 15181 with GO IEA      | scripted download from EBI ftp site |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| MSigDB_ - C3                   | GMT       | Entrez gene  | sporadically                 | 221 miRs, 616 TFs                       | manual download                     |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+
| Panther_                       | BioPAX    | UniProt      | updated periodically         | 307 Pathways                            | scripted download of biopax archive |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+-------------------------------------+

    
Mouse
~~~~~

+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| Source                         | File Type | ID extracted | Frequency source is updated  | Number of pathways                      | File Origin                            |
+================================+===========+==============+==============================+=========================================+========================================+
| Reactome_                      | BioPAX    | UniProt      | updated release              | 946 pathways (release 37)               | scripted download of zipped release    |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| GO_                            | GAF       | MGI          | released once a month        | 14563 no GO IEA, 15041 with GO IEA      | scripted download from MGI ftp site    |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| KEGG_                          | GMT       | Entrez gene  | static as of July 1, 2011    | 236                                     | translated from Human using Homologene |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| MSigDB_ - C2                   | GMT       | Entrez gene  | sporadically                 | total 880: Kegg - 186, Reactome - 430,  | translated from Human using Homologene |
|                                |           |              |                              | Biocarta - 217, Other - 47              |                                        |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| NCI_                           | GMT       | Entrez gene  | sporadically                 | 219 pathways                            | translated from Human using Homologene |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| `Institute of Bioinformatics`_ | GMT       | Entrez gene  | sporadically                 | 35 pathways -                           | translated from Human using Homologene |
| (IOB)                          |           |              |                              | 10 are the same as CellMap,             |                                        | 
|                                |           |              |                              | 1 is the same as NetPath                |                                        |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| NetPath_  (IOB)                | GMT       | Entrez gene  | static                       | 25 pathways -                           | translated from Human using Homologene |
|                                |           |              |                              | 12 are cancer pathways (10 are CellMap),|                                        |
|                                |           |              |                              | 13 are immunity pathways                |                                        |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| HumanCyc_                      | GMT       | Entrez gene  | updated periodically         | 249 Pathways                            | translated from Human using Homologene |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+
| Panther_                       | BioPAX    | UniProt      | updated periodically         | 307 Pathways                            | translated from Human using Homologene |
+--------------------------------+-----------+--------------+------------------------------+-----------------------------------------+----------------------------------------+


Specialty Gene Sets
-------------------

The bulk of our genesets are groupings from similar biological processes, pathways and functional 
annotations but there are a few additional collections of sets that we don't group with them. 
They include:

miRs
  * Sets consisting of all the targets for a given microRNA.
  * miR genesets are retrieved from Msigdb c3 collection. 

Transcription Factors
 * Sets consisting of all the targets for a given transcription factor.
 * TF genesets are retrieved from Msigdb c3 collection. 

Disease Phenotype
 * Sets consisting of all known proteins associated with the given disease.
 * Disease phenotype genesets are retrieved from the Human phenotype ontology. 
   Genes associated with a particular disease are annotated to it. In addition, 
   in the same style as the Gene Ontology, the relationship between each disease 
   is stored creating an ontology of diseases. Annotations are up-propagated to 
   related disease terms. 

Drugs Targets
 * Sets consisting of all the known or predicted targets for a given drug.
 * Drug target information is retrieved from drugbank. Drugbank is a resource containing 
   6711 drug entries including 1447 FDA-approved small molecule drugs, 131 FDA-approved 
   biotech (protein/peptide) drugs, 85 nutraceuticals and 5080 experimental drugs. In 
   addition to the compilation of all drugs contained in drugbank geneset files are also 
   created for each of the defined drug categories including approved, experimental, 
   illicit, nutraceutical, and small molecule. 


File Structure
--------------

< > denotes directory

* <Release> - directory is named according to date sets were updated.

  * <Species>

    * <Identifier> - (either Entrez gene, UniProt, Gene symbol)
 
      * <GO>
        
        * BP = biological process
        * MF = molecular function
        * CC = Cellular component
        * All = BP + MF + CC
        * no_GO_IEA - indicates that the file excludes GO annotations with evidence codes - 
          'IEA' (inferred from electronic annotation), 'ND' (No biological data available), 
          'RCA' (inferred from reviewed computational analysis)
        * with_GO_IEA - indicates that the file includes GO annotations with evidence codes - 
          'IEA' (inferred from electronic annotation), 'ND' (No biological data available), 
          'RCA' (inferred from reviewed computational analysis) 

      * <Pathways>
      * <miRs>
      * <TF>
      * <Disease phenotypes> 

* In each <identifier> directory There are amalgamated Gene Set files:

  * AllPathways - contains all pathway sources in the Pathways directory
  * GOPathways - contains all GO (MF, BP, CC) and all Pathway sources in the Pathways directory. 


Creating customized Gene Sets
-----------------------------

Download the desired gene set files you would like to use in your customized set and concatenate 
the files.

For example, to combine Human_IOB_Entrezgene.gmt Human_NetPath_Entrezgene.gmt, you can use the 
following linux command: ::

  cat Human_IOB_Entrezgene.gmt Human_NetPath_Entrezgene.gmt > MyCustomizedSet.gmt


References
----------

1. | Kanehisa M, Goto S, Sato Y, Furumichi M, Tanabe M. 
   | **KEGG for integration and interpretation of large-scale molecular data sets.** 
   | Nucleic Acids Res. 2011 Nov 10. PMID: 22080510
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/22080510>`_.
   |

2. | Subramanian A, Tamayo P, Mootha VK, Mukherjee S, Ebert BL, Gillette MA, Paulovich A, Pomeroy SL, Golub TR, Lander ES, Mesirov JP. 
   | **Gene set enrichment analysis: a knowledge-based approach for interpreting genome-wide expression profiles.**
   | Proc Natl Acad Sci U S A. 2005 Oct 25;102(43):15545-50. PMID: 16199517
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/16199517>`_.
   |

3. | Schaefer CF, Anthony K, Krupa S, Buchoff J, Day M, Hannay T, Buetow KH. 
   | **PID: the Pathway Interaction Database.**
   | Nucleic Acids Res. 2009 Jan;37(Database issue):D674-9. PMID: 18832364
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/18832364>`_.
   |

4. | Kandasamy K, et al 
   | **NetPath: a public resource of curated signal transduction pathways.** 
   | Genome Biol. 2010 Jan 12;11(1):R3. PMID: 20067622
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/20067622>`_.
   |

5. | Romero P, Wagg J, Green ML, Kaiser D, Krummenacker M, Karp PD. 
   | **Computational prediction of human metabolic pathways from the complete human genome.** 
   | Genome Biol. 2005;6(1):R2. Epub 2004 Dec 22. PMID: 15642094
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/15642094>`_.
   |

6. | Croft D, O'Kelly G, Wu G, Haw R, Gillespie M, Matthews L, Caudy M, Garapati P, Gopinath G, Jassal B, Jupe S, Kalatskaya I, Mahajan S, May B, Ndegwa N, Schmidt E, Shamovsky V, Yung C, Birney E, Hermjakob H, D'Eustachio P, Stein L. 
   | **Reactome: a database of reactions, pathways and biological processes**
   | Nucleic Acids Res. 2011 Jan;39(Database issue):D691-7. PMID: 21067998
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/21067998>`_.
   |

7. | Ashburner M, Ball CA, Blake JA, Botstein D, Butler H, Cherry JM, Davis AP, Dolinski K, Dwight SS, Eppig JT, Harris MA, Hill DP, Issel-Tarver L, Kasarskis A, Lewis S, Matese JC, Richardson JE, Ringwald M, Rubin GM, Sherlock G. 
   | **Gene ontology: tool for the unification of biology. The Gene Ontology Consortium.**
   | Nat Genet. 2000 May;25(1):25-9. PMID: 10802651
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/10802651>`_.
   |

8. | Mi H, Lazareva-Ulitsky B, Loo R, Kejariwal A, Vandergriff J, Rabkin S, Guo N, Muruganujan A, Doremieux O, Campbell MJ, Kitano H, Thomas PD. 
   | **The PANTHER database of protein families, subfamilies, functions and pathways.**
   | Nucleic Acids Res. 2005 Jan 1;33(Database issue):D284-8. PubMed PMID: 15608197
   | `Pubmed <https://www.ncbi.nlm.nih.gov/pubmed/15608197>`_.
   |
