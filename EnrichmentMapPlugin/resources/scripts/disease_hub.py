#!python
# jython 2.1.2
# META INFORMATION:
"""
	disease_hub.py
	
    Create new Disease-Signature-Hub node and generate edges to related Gene Sets.
    Requires two Geneset files in the GSEA-gmt format [1].

    - "options_hub_file"  defines the Disease-Signature-Hub nodes by it's genes
    - "options_gmt_file"  should be the same geneset file as used for generation of the enrichment map
    
    An optional theshold of the minimum number of common genes for a hub -> geneset connection
    can be defined as "overlap_threshold".
    
    [1] http://www.broad.mit.edu/cancer/software/gsea/wiki/index.php/Data_formats#GMT:_Gene_Matrix_Transposed_file_format_.28.2A.gmt.29
    
	written 2009 by Oliver Stueker <oliver.stueker@utoronto.ca>
	http://www.baderlab.org/OliverStueker

    $Id$
"""
__author__  = '$Author$'[9:-2]
__version__ = '$Revision$'[11:-2]
__date__    = '$Date$'[7:17]

# IMPORTS
from cytoscape import Cytoscape
import cytoscape.layout.CyLayouts as CyLayouts
import java.lang.Integer as Integer
import java, os

user_home = java.lang.System.getProperty("user.home")

####################################################################################################
#####                                       CONFIGURE ME                                       #####
##### input files :                                                                            #####
options_hub_file = user_home + '/StemCellProject/Disease_hub_R/leukemia_disease_hub.gmt'       #####
options_gmt_file = user_home + '/StemCellProject/Disease_hub_R/c5.all.v2.5.symbols.gmt'        #####
#####                                                                                          #####
##### only edges with no of common genes >= overlap_threshold will be created:                 #####
overlap_threshold = 1                                                                          #####
####################################################################################################

# Visial style bypass:
hub_node_shape   = "TRIANGLE"
hub_node_color   = "255,255,0"  # yellow
hub_border_color = "255,255,0"  # yellow
hub_edge_color   = "255,0,200"  # pink

# STATICS
FALSE = 0
TRUE = 1

# Reading HUB file...
hubs = {}
hub_file = file(options_hub_file, "r")
for line in hub_file:
	line = line.split("\t")
	hubs[line[0]] = line[2:]
	if '' in hubs[line[0]]:
		hubs[line[0]].remove('')
hub_file.close()

# Reading GMT file...
genesets = {}
gmt_file = file(options_gmt_file, "r")
for line in gmt_file:
	line = line.split("\t")
	genesets[line[0]] = line[2:]
	if '' in genesets[line[0]]:
		genesets[line[0]].remove('')
gmt_file.close()

# calculate connections
connections = {}
for key in hubs.keys():
	connections[key] = {}
	for gene in hubs[key]:
		for geneset in genesets.keys():
			if gene in genesets[geneset] :
				if not connections[key].has_key(geneset):
					connections[key][geneset] = 1
				else:
					connections[key][geneset] += 1

# now go to Cytoscape
graph = Cytoscape.getCurrentNetwork()
edges = {}

# get all nodes
nodes = {}
for node in graph.nodesList():
	nodes[node.getIdentifier()] = node

# itereate over all hub-nodes
for hub_name in connections.keys():
    # generate new hub node and append to internal list
	if not hub_name in nodes.keys():
		hub_node = Cytoscape.getCyNode(hub_name, True)
		graph.addNode(hub_node)
		nodes[hub_name] = hub_node
	# set Visual Style bypass
	x = graph.setNodeAttributeValue(hub_node, "node.shape", hub_node_shape)
	x = graph.setNodeAttributeValue(hub_node, "node.fillColor", hub_node_color)
	x = graph.setNodeAttributeValue(hub_node, "node.borderColor", hub_border_color)

	for geneset in connections[hub_name].keys():
		# generate new edges between hub-node and Gene sets
		if (geneset in nodes.keys() and connections[hub_name][geneset] >= overlap_threshold ) :
			edge = Cytoscape.getCyEdge(nodes[hub_name], nodes[geneset], "interaction", "-", True)
			graph.addEdge(edge)
			edges[edge.getIdentifier()] = edge
			x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_size", connections[hub_name][geneset])
			# set Visual Style bypass
			x = graph.setEdgeAttributeValue(edge, "edge.color", hub_edge_color)

Cytoscape.getCurrentNetworkView().redrawGraph(FALSE, TRUE)
