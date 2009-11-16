#!/usr/bin/python
"""
    replace_affy_IDs.py 

    takes a GSEA .gct file and converts the Probe Set ID to a Gene Symbol
    by using a .chip file.

    written 2009 by Oliver Stueker <oliver.stueker@utoronto.ca>
    http://www.baderlab.org/OliverStueker

    $Id$
"""
from __future__ import with_statement
__author__  = '$Author$'[9:-2]
__version__ = '$Revision$'[11:-2]
__date__    = '$Date$'[7:17]

# Revision History:  at the end of File
if __name__ == "__main__":
    
    from optparse import OptionParser
    import re, string, sys, os
    
    parser = OptionParser(usage="%prog [options] -i input.gct -o output.gct -c platform.chip", version="%prog " + __version__)
    parser.add_option(  "-i", "--input", 
                        dest="infile",
    #                   default="input.gct",
                        help="input .gct file\n", 
                        metavar="FILE")
    parser.add_option(  "-o", "--output",
                        dest="outfile",
    #                   default="output.gct",
                        help="output .gct file\n",
                        metavar="FILE")
    parser.add_option(  "-c", "--chip", 
                        dest="chipfile",
                        default=os.environ["HOME"]+"/gsea_home/ChipPlatforms/Mouse430_2.chip",
                        help="Chip File\n", 
                        metavar="FILE")
    
    (options, args) = parser.parse_args()
    print parser.get_usage()
    if not options.infile :
        parser.error("input-file required")
    if not os.path.isfile(options.infile):
        parser.error("input-file does not exist")
    if not options.outfile :
        parser.error("output-file required")
    if not options.chipfile :
        parser.error("chip-file required")
    if not os.path.isfile(options.chipfile):
        parser.error("chip-file does not exist")

    id_symbol_map={}    
    re_chip = re.compile("^(\W+)\w+(\W+)\w+.*")
    re_chip_header = re.compile("^Probe Set ID\tGene Symbol\tGene Title.*")
    passedHeader = 0
    try:
        with file(options.chipfile, "r") as chipfile:
            for line in chipfile:
                if not passedHeader or re_chip_header.match(line):
                    passedHeader = 1
                    continue
                probe = line.split("\t")
                id_symbol_map[probe[0]] = probe[1]


        id_symbol_map_keys = id_symbol_map.keys()
        with file(options.outfile, "w") as outfile:
            with file(options.infile, "r") as infile:
                line_nr = 0
                for line in infile:
                    newline = ""
                    data = line.split("\t")
                    if data[0] in id_symbol_map_keys:
                        data[0] = id_symbol_map[data[0]]
                        newline = "\t".join(data)
                    else:
                        newline = line
                    outfile.write(newline)
    except IOError, text:
        print parser.get_usage()
        print text
        print "exiting"
        sys.exit(1)
