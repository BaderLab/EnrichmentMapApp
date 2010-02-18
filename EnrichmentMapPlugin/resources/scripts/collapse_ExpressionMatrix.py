#!/usr/bin/python
"""
    takes a GSEA expression matrix (*.gct or *.txt format) or ranked gene list (*.rnk format) and 
     - converts the Probe Set ID to a Gene Symbol by using a .chip file (if supplied by -chip option)
     - collapses multiple probe sets for the same gene symbol using "max_probe" mode. 

    use cases:
    1)  replace probeSet IDs of GCT-file by corresponding gene symbols and collapse multiple genes (max_probe):
        %prog -i input.gct -o output.gct -c Mouse430_2.chip --collapse
    
    2)  replace probeSet IDs of GCT-file by corresponding gene symbols:
        %prog -i input.gct -o output.gct -c Mouse430_2.chip --no-collapse
        
    3)  collapse multiple Genes (max_probe) in GCT-file without replacing IDs:
        %prog -i input.gct -o output.gct                    --collapse
    
    4)  replace probeSet IDs of RNK-file by corresponding gene symbols and collapse multiple genes (max_probe):
        %prog -i input.rnk -o output.rnk -c Mouse430_2.chip --collapse    --rnk
        
    5)  replace probeSet IDs of RNK-file by corresponding gene symbols:
        %prog -i input.rnk -o output.rnk -c Mouse430_2.chip --no-collapse --rnk
    
    
    x)  fix Cytoscape Session  - this is a very special case (that's why the --cys option is suppressed in the help page)
        Situation:  You have created an Enrichment Map with a non-collapsed expression file.
                    Now your expression table in the session has been filtered to only contain genes that are present
                    in one of the significant genesets ("genes_of_interest"), while just throwing out duplicates
                    originating from multiple probe sets per gene.
                    
        Solution:  * determine the list of "genes_of_interest" from filtered expression table (-o, from session file)
                   * backup the original filtered expression table (from session file)
                   * collapse the full expression table (-i, GCT or TXT format) by "max_probe" (or other available modes)
                   * filter the collapsed expression table to only contain genes_of_interest
                   * write out the collapsed and filtered expression table to the same path as the original
                     filtered expression table. (in shorter TXT format)
                   
                   * optional: replace Probe-Set-ID's by Symbols before collapsing 


    written 2009/2010 by Oliver Stueker <oliver.stueker@utoronto.ca>

    Copyright 2009-2010 by the Baderlab (Research Group of Gary D. Bader)
                        Donnelly Centre for Cellular and Biomolecular Research
                        University of Toronto
                        Toronto, Ontario
                        CANDADA
                        http://baderlab.org

    $Id$
"""
#from __future__ import with_statement
__author__ = '$Author$'[9:-2]
__version__ = '0.' + '$Revision$'[11:-2]
__date__ = '$Date$'[7:17]
verbose = False
fix_session = False

from Tkinter import *
import tkFileDialog, tkSimpleDialog, tkMessageBox
class ReplaceCollapseGui(Frame):
    
    def __init__(self, master=None):
        Frame.__init__(self, master)
        self.master.title("Replace Probeset IDs")
        self.grid(sticky=N + S + E + W)
        self.createWidgets()
        
    def createWidgets(self):
        self.inputFileName = StringVar()
        self.outputFileName = StringVar()
        self.chipFileName = StringVar()
        self.doCollapse = IntVar()
        self.doIdReplace = IntVar()
        self.debug = False
        
        top = self.winfo_toplevel()
        top.rowconfigure(0, weight=2)
        top.columnconfigure(0, weight=2)
#        top.minsize(width=400, height=250)
        
        self.columnconfigure(0, weight=0)
        self.columnconfigure(1, weight=6)
        self.columnconfigure(2, weight=0)
        self.grid(ipadx=2, ipady=1)

        self.config(relief=GROOVE)

        # File selector: Input File
        currentRow = 0
        infileLabel = Label(self, text='Input File:')
        infileLabel.grid(row=currentRow, column=0, sticky=W)
        
        infileBox = Entry(self, textvariable=self.inputFileName, width=40)
        infileBox.grid(row=currentRow, column=1, sticky=E + W)

        infileButton = Button(self, text="Browse", command=self.chooseInputFile)
        infileButton.grid(row=currentRow, column=2, sticky=E)

        # File selector: Output File
        currentRow += 1
        outfileLabel = Label(self, text='Output File:')
        outfileLabel.grid(row=currentRow, column=0, sticky=W)
        
        outfileBox = Entry(self, textvariable=self.outputFileName, width=40)
        outfileBox.grid(row=currentRow, column=1, sticky=E + W)

        outfileButton = Button(self, text="Browse", command=self.chooseOutputFile)
        outfileButton.grid(row=currentRow, column=2, sticky=E)

        #The Checkbuttons
        currentRow += 1
        checkbuttonFrame = Frame(self)
        checkbuttonFrame.grid(row=currentRow, columnspan=3, sticky=E + W)
        doCollapseCheck = Checkbutton(checkbuttonFrame, text='Collapse Probesets', variable=self.doCollapse)
        doCollapseCheck.grid(row=0, column=0, sticky=E)

        doReplaceCheck = Checkbutton(checkbuttonFrame, text='Translate IDs', variable=self.doIdReplace, command=self.doReplaceButtonPressed)
        doReplaceCheck.grid(row=0, column=1, sticky=W)
        

        # File selector: Chip-Annotation File
        currentRow += 1
        chipfileLabel = Label(self, text='Chip File:')
        chipfileLabel.grid(row=currentRow, column=0, sticky=W)
        
        self.chipfileBox = Entry(self, textvariable=self.chipFileName, width=40, disabledbackground='#CCCCCC', state=DISABLED)
        self.chipfileBox.grid(row=currentRow, column=1, sticky=E + W)

        self.chipfileButton = Button(self, text="Browse", command=self.chooseChipFile, state=DISABLED)
        self.chipfileButton.grid(row=currentRow, column=2, sticky=E)

        # Control Buttons
        currentRow += 1
        controlButonFrame = Frame(self)
        controlButonFrame.grid(row=currentRow, columnspan=3, sticky=E + W)
        self.quitButton = Button (controlButonFrame, text='Quit', command=self.quit)
        self.quitButton.grid(row=0, column=0, padx=10, sticky=W)

        self.clearButton = Button (controlButonFrame, text='Clear', command=self.clear)
        self.clearButton.grid(row=0, column=1, padx=10)


        self.runButton = Button(controlButonFrame, text="Run", command=self.run)
        self.runButton.grid(row=0, column=2, padx=10, sticky=E)

    def center_window(self, w=450, h=200):
        root = self.winfo_toplevel()
        
        # get screen width and height
        ws = root.winfo_screenwidth()
        hs = root.winfo_screenheight()
        # calculate position x, y
        x = (ws / 2) - (w / 2)
        y = (hs / 2) - (h / 2)
        root.geometry('%dx%d+%d+%d' % (w, h, x, y))
        
    def clear(self):
        self.inputFileName.set("")
        self.outputFileName.set("")
        self.chipFileName.set("")
        
    def chooseInputFile(self):
        filename = tkFileDialog.askopenfilename(title="Choose Input Expression Matrix or Rank file",
                                                filetypes=[("Supported Files (GCT, TXT, RNK)", "*.gct"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.txt"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.rnk"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.GCT"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.TXT"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.RNK")])
        self.inputFileName.set(filename)
        
        if self.debug:
            print "selected Input File Name: %s " % filename

    def chooseOutputFile(self):
        def_file = ""
        def_ext = ".TXT"
        if not self.inputFileName.get() == "":
            (inputFileDir, inputFileName) = os.path.split(self.inputFileName.get())
            tokens = inputFileName.rsplit(".", 1)
            def_ext = "." + tokens[-1]
            def_file = tokens[0] + "_collapsed" + "." + tokens[-1]
        
        filename = tkFileDialog.asksaveasfilename(title="Choose Output File",
                                                  filetypes=[("Supported Files (GCT, TXT, RNK)", "*.gct"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.txt"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.rnk"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.GCT"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.TXT"),
                                                           ("Supported Files (GCT, TXT, RNK)", "*.RNK")],
                                                  defaultextension=def_ext,
                                                  initialfile=def_file,
                                                  initialdir=inputFileDir)
        self.outputFileName.set(filename)
        if self.debug:
            print "selected Output File Name: %s " % filename

    def chooseChipFile(self):
        filename = tkFileDialog.askopenfilename(title="Choose Chip Annotation file",
                                                filetypes=[("Chip Annotation file (CHIP)", "*.chip"),
                                                           ("Chip Annotation file (CHIP)", "*.CHIP")])
        self.chipFileName.set(filename)
        if self.debug:
            print "selected Output File Name: %s " % filename

    def doReplaceButtonPressed(self):
        if self.doIdReplace.get() == 1:
            self.chipfileBox.configure(state=NORMAL)
            self.chipfileButton.configure(state=NORMAL)
        else:
            self.chipfileBox.configure(state=DISABLED)
            self.chipfileButton.configure(state=DISABLED)

    def checkInput(self):
        self.inputOK = True
        if self.inputFileName.get() == "":
            inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Input-file required", icon=tkMessageBox.ERROR)
        elif not os.path.isfile(self.inputFileName.get()):
            inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Input-file does not exist", icon=tkMessageBox.ERROR)
        else:
            inputFileName = self.inputFileName.get() 

        if self.outputFileName.get() == "":
            inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Output-file required", icon=tkMessageBox.ERROR)
        else:
            outputFileName = self.outputFileName.get()

        if self.doIdReplace == 1:
            if self.chipFileName.get() == "":
                inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Chip-file required", icon=tkMessageBox.ERROR)
            elif not os.path.isfile(chipFileName.get()):
                inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Chip-file does not exist", icon=tkMessageBox.ERROR)
            else:
                chipFileName = self.chipFileName.get()

        return self.inputOK

    def run(self):
        if self.checkInput():
            if self.debug:
                print "Running..."
                print "Input File:  %s" % self.inputFileName.get()
                print "Output File: %s" % self.outputFileName.get()
                print "Chip File:   %s" % self.chipFileName.get()
                print "Do Collapse: %i" % self.doCollapse.get()
                print "Do Replace:  %i" % self.doIdReplace.get()
            main(inputFileName=self.inputFileName.get(),
                 outputFileName=self.outputFileName.get(),
                 chipFileName=self.chipFileName.get(),
                 doCollapse=(self.doCollapse.get() == 1),
                 collapseMode='max_probe',
                 verbose=True,
                 fix_session=False)
            
            self.quit()


def read_chipfile(chipfileName):
    """
    reads a GSEA chip annotation file (CHIP) 
    and returns a dict that maps the probeset ID's to their corresponding gene symbols.
    
    Format:
    see    http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats#CHIP:_Chip_file_format_.28.2A.chip.29
    
    @return idSymbolMap
    """
    import re
    
    id_symbol_map = {}
    re_chip = re.compile("^(\W+)\w+(\W+)\w+.*")
    re_chip_header = re.compile("^Probe Set ID\tGene Symbol\tGene Title.*")
    passedHeader = 0
    
    # read chip file into dict (mapping object)
    if verbose :
        print "reading Chip file..."
    
    try:
        chipfile = file(chipfileName, "r")
        for line in chipfile:
            if not passedHeader or re_chip_header.match(line):
                passedHeader = 1
                continue
            probe = line.split("\t")
            id_symbol_map[probe[0]] = probe[1]
    except IOError, text:
            raise IOError, text        
    finally:
        chipfile.close()

    return id_symbol_map


def read_inputFile(inputFileName):
    """
    Reads an input file and determines the type.
    
    Supported file Types:
        - Expression file "GCT"   - three header lines, first line is always: "#1.2"
        - Expression file "TXT"   - one header line, Header always starts with "NAME\tDESCRIPTION\t"
        - Ranked gene list "RNK"  - two column: ID{tab}SCORE, score being numerical, 
                                    comment lines (starting with #) are ignored. 
                                    
    @return: (inputFileLines, type)
    """
    import re
    type = ''
    try:
        # read expression data
        if verbose :
            print "reading expression file..."

        infile = file(inputFileName, "r")
        inputFileLines = infile.readlines()
        
        ## Guess the type of file:
        if re.search("^#1.2\s*", inputFileLines[0]):
            type = "GCT"
            if verbose :
                print "...think it's GCT"
        elif re.search("^NAME\tDESCRIPTION\t", inputFileLines[0], re.IGNORECASE):
            type = "TXT"
            if verbose :
                print "...think it's TXT"
        else:
            invalid = False
            re_comment = re.compile("^#")
            re_ranks = re.compile("^[^\t]+\t-?\d*\.?\d+")
            for i in range(len(inputFileLines)):
                if not (re_ranks.search(inputFileLines[i]) or re_comment.search(inputFileLines[i])):
                    invalid = True
                    break
            if not invalid:
                type = "RNK"
                if verbose :
                    print "...think it's RNK"
            else:
                error_text = "Error in line %i\n" % i
                error_text += "Invalid Input File: '%s' \n" % inputFileName
                error_text += "\tIt seems it's neither an expression file (GCT or TXT) or Ranked Gene list\n"
                error_text += "\tRefer to http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats for specifications\n"
                raise IOError, error_text
                
    except IOError, text:
        raise IOError, text
    finally:
        infile.close()
    
    return inputFileLines, type


def replace_IDs(dataLines, id_symbol_map):
    """
    replaces the IDs in the first column by symbols (based on idSymbolMap)
    
    @return: dataLines
    """
    # replace Probeset IDs with Gene Symbols
    if verbose :
        print "replacing Probeset IDs with Gene Symbols..."

    id_symbol_map_keys = id_symbol_map.keys()
   
    for line_nr in range(len(dataLines)):
        line = dataLines[line_nr]
        newline = ""
        tokens = line.split("\t", 1)
        if tokens[0] in id_symbol_map_keys:
            tokens[0] = id_symbol_map[tokens[0]]
            newline = "\t".join(tokens)
        else:
            newline = line
        dataLines[line_nr] = newline
    return dataLines


def collapse_data(data_lines, type, mode='max_probe'):
    """
    collapses multiple expression values (or scores) per gene symbol
    
    Modes:
      - max_probe (default): for each sample, use the maximum expression value for the probe set. 
        For example:

        Probeset_A         10     20     15    200
        Probeset_B        100    105    110     95
        ------------------------------------------
        gene_symbol_AB    100    105    110    200
         
    @return: data_lines - list with complete expression table or ranked gene list
    """
    import re
    re_comment = re.compile("^#")
    
    if verbose :
        print "collapsing Probe-Sets..."
    
    # save header
    if type == "GCT":
        data_header = data_lines[:3]
        data_lines = data_lines[3:]
    elif type == "TXT":
        data_header = data_lines[:1]
        data_lines = data_lines[1:]
    else:
        data_header = []

    # collapse
    data_map = {}
    for line in data_lines:
        if type == "RNK" and re_comment.search(line):
            # don't process comment lines in RANK files
            # but rather append them to the header
            data_header.append(line)
            continue
        
        tokens = line.split("\t")
        
        ##### BEGIN FIX SESSION CODE #####
        if fix_session and (tokens[0] not in genes_of_interest) :
            # restrict to genes_of_interest
            continue
        ##### END   FIX SESSION CODE #####
        
        if not data_map.has_key(tokens[0]): 
            # if we hadn't had this gene before: take it
            data_map[tokens[0]] = tokens[1:]
        else:
            # if we had:
            if type == "GCT" or type == "TXT":
                # case expression Table:    take highest value!
                for i in range(len(tokens[2:])):
                    if tokens[i + 2] > data_map[tokens[0]][i + 1]:
                        data_map[tokens[0]][i + 1] = tokens[i + 2]
                data_map[tokens[0]][0] = data_map[tokens[0]][0] + " " + tokens[1]
            else:
                # case rank file:            take value (Score) with highest magnitude
                if abs(float(tokens[1])) > abs(float(data_map[tokens[0]][0])):
                    data_map[tokens[0]][0] = tokens[1]
                    
    # assemble new output data
    data_lines = data_header
    
    # restore header
    if type == "GCT":
        # calculate new dimensions of collapsed expression table
        data_lines[1] = "\t".join([str(len(data_map.keys())), data_lines[1].split("\t")[1] ])

    # restore expression table / ranked gene list
    for gene in data_map.keys():
        newline = gene + "\t" + "\t".join(data_map[gene])
        data_lines.append(newline)

    ##### BEGIN FIX SESSION CODE #####
    if fix_session and type == "GCT" :
        # expression tables in Session files have only one header line
        del data_lines[1:2]
    ##### END   FIX SESSION CODE #####
    
    return data_lines

def main(inputFileName, outputFileName, chipFileName, doCollapse, collapseMode, verbose, fix_session):
    "Main program function"
    try:
        if not chipFileName == "":
            idSymbolMap = read_chipfile(chipFileName)
        
        ##### BEGIN FIX SESSION CODE #####
        if fix_session :
            # collect genes of interest
            genes_of_interest = []
            
            try:
                expr_file = file(outputFileName, "r")
                for line in expr_file:
                    data = line.split("\t", 1)
                    if not data[0] == "NAME":
                        genes_of_interest.append(data[0])
            except IOError, text:
                raise IOError, text
            finally:
                expr_file.close()
            # make backup of the expression file
            os.rename(outputFileName, outputFileName + ".BAK")
        ##### END   FIX SESSION CODE #####
            
        (expr_file_lines, type) = read_inputFile(inputFileName)
        
        
        if not chipFileName == "":
            expr_file_lines = replace_IDs(expr_file_lines, idSymbolMap)
        
        if doCollapse == True:
            expr_file_lines = collapse_data(expr_file_lines, type, mode=collapseMode)
                
                
        # write expression data in output file
        try:
            outfile = file(outputFileName, "w")
            outfile.writelines(expr_file_lines)
        except IOError, text:
            raise IOError, text
        finally:
            outfile.close()
            
    except IOError, text:
        print parser.get_usage()
        print text
        print "exiting"
        sys.exit(1)


if __name__ == "__main__":
    from optparse import OptionParser, SUPPRESS_HELP
    import sys, os
    
    # Configure parser for command line options:
    __usage = "%prog [options] -i input.gct -o output.gct [-c platform.chip] [--collapse]"
    __description = "This tool can process a gene expression matrix (in GCT or TXT format) or ranked list (RNK format)\n" + \
                    "and either replace the Identifier based on a Chip Annotation file (e.g. AffyID -> Gene Symbol),\n" + \
                    "or collapse the expression values or rank-scores for Genes from more than one probe set.\n" + \
                    "Both can be done in one step by using both '-c platform.chip' and '--collapse' at the same time." + \
                    "For detailed descriptions of the file formats, please refer to: http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats \n\n" + \
                    "Call without any parameters to select the files and options with a GUI (Graphical User Interface)"
    parser = OptionParser(usage=__usage, description=__description, version="%prog " + __version__)
    parser.add_option("-i", "--input",
                        dest="infile",
    #                   default="",
                        help="input expression table or ranked list\n",
                        metavar="FILE")
    parser.add_option("-o", "--output",
                        dest="outfile",
    #                   default="output.gct",
                        help="output expression table or ranked list\n",
                        metavar="FILE")
    parser.add_option("-c", "--chip",
                        dest="chipfile",
                        default='',
                        help="Chip File\nThis implies that the Identifiers are to be replaced.",
                        metavar="FILE")
    
    parser.add_option("--collapse",
                        dest="collapse",
                        default=False,
                        help="Collapse multiple probe sets for the same gene symbol (max_probe)\n",
                        action="store_true",
                        )
    parser.add_option("--no-collapse",
                        dest="collapse",
                        help="Don't collapse multiple probesets\n[default]\n",
                        action="store_false",
                        )
    parser.add_option("-m", "--collapse-mode",
                        dest="mode",
                        default="max_probe",
                        type="choice",
                        choices=("max_probe", "median_of_probes"),
#                        help="Mode for collapsing data from multiple probe sets for the same gene symbol. Currently only 'max_probe' is supported.",
                        help=SUPPRESS_HELP
                        )
    
    parser.add_option("-g", "--gui",
                        dest="useGui",
                        action="store_true",
                        default=False,
                        help="Open a Window to choose the files and options.",
                        )
    parser.add_option("-q", "--quiet",
                        dest="verbose",
                        default=True,
                        help="be quiet\n",
                        action="store_false",
                        )
    parser.add_option("--cys",
                        dest="fix_session",
                        default=False,
                        help=SUPPRESS_HELP, #"write out shorter GCT format (only one header line)\n",
                        action="store_true",
                        )
    
    
    (options, args) = parser.parse_args()

    useGui = (not options.infile and
              not options.outfile and 
              not options.chipfile and 
              not options.collapse) or options.useGui
    
    if useGui:
        theGui = ReplaceCollapseGui()
#        theGui.master.title("Sample application")
        theGui.master.minsize(width=450, height=150)
        theGui.master.focus_set()
        theGui.center_window(w=450, h=150)
        theGui.mainloop()

    else:
        # Check Input
        if options.verbose :
            print parser.get_usage()
        if not options.infile :
            parser.error("input-file required")
        if not os.path.isfile(options.infile):
            parser.error("input-file does not exist")
        if not options.outfile :
            parser.error("output-file required")
    #    if not (options.chipfile or options.fix_session):
    #        parser.error("chip-file required")
        if not options.chipfile == "" and not os.path.isfile(options.chipfile):
            parser.error("chip-file does not exist")
    
        main(inputFileName=options.infile,
             outputFileName=options.outfile,
             chipFileName=options.chipfile,
             doCollapse=options.collapse,
             collapseMode=options.mode,
             verbose=options.verbose,
             fix_session=options.fix_session)

