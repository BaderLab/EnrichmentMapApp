#!python
# jython 2.1.2
# META INFORMATION:
"""
    disease_hub_2.py
    
    Create new Disease-Signature-Hub node and generate edges to related Gene Sets.
    Requires on tab-seperated signature file in the following format:

    #ID	Target	Overlap_Nab	Overlap_Prc	Overlap_Hpv
    GO:0000018	DH_ColonCancer	 23	0.046	0.001
    GO:0000041	DH_ColonCancer	  1	0.002	1
    GO:0000051	DH_ColonCancer	256	0.512	0.0045
    
    Notes:
      * The ID's in the Source field (Gene Sets) have to match to the Node ID in the Enrichment Map.
        E.g. if you have used the GSEA GO-Genesets those need to be the GO terms (like "CELL_CYCLE").
      * The header line has to start with a # character. All lines beginning with a # character will
        be treatet as comments and ignored by the script.
    
    written 2009 by Oliver Stueker <oliver.stueker@utoronto.ca>
    http://www.baderlab.org/OliverStueker

    $Id: disease_hub_2.py 58 2009-06-03 21:55:36Z revilo $
"""
__author__  = '$Author: revilo $'[9:-2]
__version__ = '$Revision: 58 $'[11:-2]
__date__    = '$Date: 2009-06-03 17:55:36 -0400 (Wed, 03 Jun 2009) $'[7:17]

# IMPORTS
from cytoscape import Cytoscape
import cytoscape.layout.CyLayouts as CyLayouts
import java.lang.Integer as Integer
import java, os

user_home = java.lang.System.getProperty("user.home")

####################################################################################################
#####                                       CONFIGURE ME                                       #####
##### input files :                                                                            #####
#options_sig_file = user_home + '/Disease_hub/DiseaseSignatureScript_Sample_Input.txt'
options_sig_file = 'C:/Disease_hub/DiseaseSignatureScript_Sample_Input.txt'
#####                                                                                          #####
####################################################################################################

# Visial style bypass:
hub_node_shape   = "TRIANGLE"
hub_node_color   = "255,255,0"  # yellow
hub_border_color = "255,255,0"  # yellow
hub_edge_color   = "255,0,200"  # pink

# STATICS
FALSE = 0
TRUE = 1


# Reading data from Hypergeometric Test...
dis_sig = {}
#                Name of Hub       Name of Geneset Overlap_Nab  Overlap_Prc  Overlap_Hpv
# dis_sig = { "DH_ColonCancer" : {  'GO:0000018': [      23,     0.046,      0.001  ],
#                                   'GO:0000059': [      32,     0.064,      0.23   ], 
#                                   'GO:0000070': [       6,     0.012,      0.06   ], 
#                                   'GO:0000077': [       2,     0.004,      1.0    ], 
#                                   'GO:0000075': [       8,     0.016,      0.09   ], 
#                                   'GO:0000051': [     256,     0.512,      0.0045 ], 
#                                   'GO:0000041': [       1,     0.002,      1.0    ], 
#                                   'GO:0000060': [      67,     0.134,      7.0e-06]    }
#               }

dis_sig = {}
dis_sig_file = file(options_sig_file, "r")
for line in dis_sig_file :
    if line[0] == "#" :
        continue
    line = line.split("\t")
    d_hub = line[1]
    g_set = line[0]
    if not d_hub in dis_sig.keys():
        dis_sig[ d_hub ] = {}
    dis_sig[d_hub][g_set] = [int(line[2]), float(line[3]), float(line[4]) ]

dis_sig_file.close()

# now go to Cytoscape
graph = Cytoscape.getCurrentNetwork()
edges = {}

# get all nodes
nodes = {}
for node in graph.nodesList():
    nodes[node.getIdentifier()] = node

# itereate over all hub-nodes
for hub_name in dis_sig.keys():
    # generate new hub node and append to internal list
    if not hub_name in nodes.keys():
        hub_node = Cytoscape.getCyNode(hub_name, True)
        graph.addNode(hub_node)
        nodes[hub_name] = hub_node
    # set Visual Style bypass
    x = graph.setNodeAttributeValue(hub_node, "node.shape", hub_node_shape)
    x = graph.setNodeAttributeValue(hub_node, "node.fillColor", hub_node_color)
    x = graph.setNodeAttributeValue(hub_node, "node.borderColor", hub_border_color)

    for geneset in dis_sig[hub_name].keys():
        # generate new edges between hub-node and Gene sets
        if (geneset in nodes.keys() ) :
            edge = Cytoscape.getCyEdge(nodes[hub_name], nodes[geneset], "interaction", "-", True)
            graph.addEdge(edge)
            edges[edge.getIdentifier()] = edge
            x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_size", dis_sig[hub_name][geneset][0])
            x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_Prc",  dis_sig[hub_name][geneset][1])
            x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_Hpv",  dis_sig[hub_name][geneset][2])
            if dis_sig[hub_name][geneset][2] < 0.0001 :
                x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  1.0 )
                # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 5.0)
            elif dis_sig[hub_name][geneset][2] < 0.05 :
                x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  0.51 )
                # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 1.0)
            else :
                x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  0.0 )
                # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 0.0)
                
            # set Visual Style bypass
            x = graph.setEdgeAttributeValue(edge, "edge.color", hub_edge_color)

Cytoscape.getCurrentNetworkView().redrawGraph(FALSE, TRUE)
