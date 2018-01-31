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

    $Id: collapse_ExpressionMatrix.py 507 2010-04-19 23:50:47Z revilo $
"""
#from __future__ import with_statement
__author__ = '$Author: revilo $'[9:-2]
__version__ = '0.' + '$Revision: 507 $'[11:-2]
__date__ = '$Date: 2010-04-19 19:50:47 -0400 (Mon, 19 Apr 2010) $'[7:17]

from Tkinter import *
import tkFileDialog, tkSimpleDialog, tkMessageBox
class ReplaceCollapseGui(Frame):
    
    def __init__(self, master=None, version="0.001"):
        Frame.__init__(self, master)
        self.master.title("collapse Expression Matrix" + " v" + version)
        self.grid(sticky=N + S + E + W)
        self.version = version
        self.createWidgets()
        
        
    def createWidgets(self):
        self.exprOrRankFileSelectorFrameText = StringVar(value="Expression Matrix or Ranked List:")
        self.inputFileName = StringVar()
        self.outputFileName = StringVar()
        self.chipFileName = StringVar()
        self.doCollapse = IntVar()
        self.doIdReplace = IntVar()
        self.suppress_null = IntVar()
        self.messages = StringVar()
        self.addExprInFileName = StringVar()
        self.addExprOutFileName = StringVar()
        self.collapseRankAndExpr = IntVar()
        
        self.messages.set("")
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

        # Basic Mode selectors (RadioButtons)
        currentRow = 0
        radioBoxFrame = LabelFrame(self, text="Mode:")
        radioBoxFrame.columnconfigure(0, weight=0)
        radioBoxFrame.columnconfigure(1, weight=6)
        radioBoxFrame.columnconfigure(2, weight=0)
        radioBoxFrame.grid(row=currentRow, columnspan=3, sticky=E + W, ipadx=2, ipady=1, padx=2, pady=2)
        
        radioRankOrEpr = Radiobutton(radioBoxFrame, text="ExpressionMatrix or Ranked List", variable=self.collapseRankAndExpr, value=0, command=self.modeRadioButtonPressed)
        radioRankOrEpr.grid(row=0, column=0, sticky=W)
        radioRankAndEpr = Radiobutton(radioBoxFrame, text="Ranked List with Expression Matrix", variable=self.collapseRankAndExpr, value=1, command=self.modeRadioButtonPressed)
        radioRankAndEpr.grid(row=1, column=0, sticky=W)

        # primary File Selectors Frame:
        currentRow += 1
        primaryFileSelectorsFrame = LabelFrame(self, relief=GROOVE, labelwidget=Label(self, textvariable=self.exprOrRankFileSelectorFrameText))
        primaryFileSelectorsFrame.columnconfigure(0, weight=0)
        primaryFileSelectorsFrame.columnconfigure(1, weight=6)
        primaryFileSelectorsFrame.columnconfigure(2, weight=0)
        primaryFileSelectorsFrame.grid(row=currentRow, columnspan=3, sticky=E + W, ipadx=2, ipady=1, padx=2, pady=2)
        
        # File selector: Input File
        infileLabel = Label(primaryFileSelectorsFrame, text='Input File:')
        infileLabel.grid(row=0, column=0, sticky=W)
        infileBox = Entry(primaryFileSelectorsFrame, textvariable=self.inputFileName, width=40)
        infileBox.grid(row=0, column=1, sticky=E + W)
        infileButton = Button(primaryFileSelectorsFrame, text="Browse", command=self.chooseInputFile)
        infileButton.grid(row=0, column=2, sticky=E)

        # File selector: Output File
        currentRow += 1
        outfileLabel = Label(primaryFileSelectorsFrame, text='Output File:')
        outfileLabel.grid(row=1, column=0, sticky=W)
        outfileBox = Entry(primaryFileSelectorsFrame, textvariable=self.outputFileName, width=40)
        outfileBox.grid(row=1, column=1, sticky=E + W)
        outfileButton = Button(primaryFileSelectorsFrame, text="Browse", command=self.chooseOutputFile)
        outfileButton.grid(row=1, column=2, sticky=E)


        #The Checkbuttons
        currentRow += 1

        checkbuttonFrame = LabelFrame(self, relief=GROOVE, labelanchor='nw', text="Modes for single file:")
        checkbuttonFrame.grid(row=currentRow, column=0, columnspan=2, sticky=E + W, padx=2, pady=2)
        self.doCollapseCheck = Checkbutton(checkbuttonFrame, text='Collapse Probesets', variable=self.doCollapse)
        self.doCollapseCheck.select()
        self.doCollapseCheck.grid(row=0, column=0, sticky=E)

        self.doReplaceCheck = Checkbutton(checkbuttonFrame, text='Translate IDs', variable=self.doIdReplace, command=self.doReplaceButtonPressed)
        self.doReplaceCheck.select()
        self.doReplaceCheck.grid(row=0, column=1, sticky=W)

        optionFrame = LabelFrame(self, relief=GROOVE, labelanchor='nw', text="Options:")
        optionFrame.grid(row=currentRow, column=2, sticky=E + W, padx=2, pady=2)
        self.suppressNullCheck = Checkbutton(optionFrame, text='Suppress Gene "NULL"', variable=self.suppress_null)
        self.suppressNullCheck.grid(row=0, column=0, sticky=W)

        # Additional file selectors:
        currentRow += 1
        secondaryFileSelectorsFrame = LabelFrame(self, relief=GROOVE, labelanchor='nw', text="Expression-file to be collapsed by probesets:")
        secondaryFileSelectorsFrame.columnconfigure(0, weight=0)
        secondaryFileSelectorsFrame.columnconfigure(1, weight=6)
        secondaryFileSelectorsFrame.columnconfigure(2, weight=0)
        secondaryFileSelectorsFrame.grid(row=currentRow, columnspan=3, sticky=E + W, ipadx=2, ipady=1, padx=2, pady=2)
         
        #File Selector: additional Expression Input File
        addExprInFileLabel = Label(secondaryFileSelectorsFrame, text='Input File:')
        addExprInFileLabel.grid(row=0, column=0, sticky=W)
        self.addExprInFileBox = Entry(secondaryFileSelectorsFrame, textvariable=self.addExprInFileName, width=40, disabledbackground='#CCCCCC', state=DISABLED) 
        self.addExprInFileBox.grid(row=0, column=1, sticky=E + W)
        self.addExprInFileButton = Button(secondaryFileSelectorsFrame, text="Browse", state=DISABLED, command=self.chooseAddExprInputFile)
        self.addExprInFileButton.grid(row=0, column=3, sticky=E)
        
        #File Selector: additional Expression Output File
        addExprOutFileLabel = Label(secondaryFileSelectorsFrame, text='Output File:')
        addExprOutFileLabel.grid(row=1, column=0, sticky=W)
        self.addExprOutFileBox = Entry(secondaryFileSelectorsFrame, textvariable=self.addExprOutFileName, width=40, disabledbackground='#CCCCCC', state=DISABLED) 
        self.addExprOutFileBox.grid(row=1, column=1, sticky=E + W)
        self.addExprOutFileButton = Button(secondaryFileSelectorsFrame, text="Browse", state=DISABLED, command=self.chooseAddExprOutputFile)
        self.addExprOutFileButton.grid(row=1, column=3, sticky=E)


        # File selector: Chip-Annotation File
        currentRow += 1
        chipfileLabel = Label(self, text='Chip File:')
        chipfileLabel.grid(row=currentRow, column=0, sticky=W)
        
        self.chipfileBox = Entry(self, textvariable=self.chipFileName, width=40, disabledbackground='#CCCCCC')
        self.chipfileBox.grid(row=currentRow, column=1, sticky=E + W)

        self.chipfileButton = Button(self, text="Browse", command=self.chooseChipFile, state=DISABLED)
        self.chipfileButton.grid(row=currentRow, column=2, sticky=E)
        if self.doIdReplace.get() == 0:
            self.chipfileBox.configure(state=DISABLED)
            self.chipfileButton.configure(state=DISABLED)
        else:
            self.chipfileBox.configure(state=NORMAL)
            self.chipfileButton.configure(state=NORMAL)
        

        # Control Buttons
        currentRow += 1
        controlButtonFrame = Frame(self)
        controlButtonFrame.grid(row=currentRow, columnspan=3, sticky=E + W)
        versionLabel = Label(controlButtonFrame, text="Version: " + self.version)
        versionLabel.grid(row=0, column=0, padx=10, sticky=W)

        self.quitButton = Button (controlButtonFrame, text='Quit', command=self.quit)
        self.quitButton.grid(row=0, column=1, padx=10, sticky=W)

        self.clearButton = Button (controlButtonFrame, text='Clear', command=self.clear)
        self.clearButton.grid(row=0, column=2, padx=10, sticky=E)

        self.runButton = Button(controlButtonFrame, text="Run", command=self.run)
        self.runButton.grid(row=0, column=3, padx=10, sticky=E)
        
        # Message Box
        currentRow += 1
        self.rowconfigure(currentRow, minsize=200)
        messageBoxBorder = LabelFrame(self, relief=RIDGE, height=150, width=425, text="Messages:")
        messageBoxBorder.columnconfigure(0, weight=0)
        messageBoxBorder.columnconfigure(1, weight=6)
        messageBoxBorder.columnconfigure(2, weight=0)
        messageBoxBorder.grid(row=currentRow, column=0, columnspan=3, sticky=N + S + E + W, padx=5, pady=5)
        self.messageBox = Message(messageBoxBorder, textvariable=self.messages, justify=LEFT, width=415, aspect=150)
        self.messageBox.rowconfigure(0, minsize=200)
        self.messageBox.columnconfigure(0, weight=0)
        self.messageBox.columnconfigure(1, weight=6)
        self.messageBox.columnconfigure(2, weight=0)
        self.messageBox.grid(row=0, columnspan=3, sticky=NW)
        
    def writeMessage(self, messageText):
        textLines = self.messages.get().splitlines()
        textLines.append(messageText)
        if len(textLines) > 10:
            textLines = textLines[-10:]
        
        newText = "\n".join(textLines) 
        self.messages.set(newText)
        self.messageBox.update()

    def center_window(self, w=450, h=250):
        root = self.winfo_toplevel()
        
        # get screen width and height
        ws = root.winfo_screenwidth()
        hs = root.winfo_screenheight()
        # calculate position x, y
        x = (ws / 2) - (w / 2)
        y = (hs / 2) - (h / 2)
        x = 50
        y = 50
        root.geometry('%dx%d+%d+%d' % (w, h, x, y))
        
    def clear(self):
        self.inputFileName.set("")
        self.outputFileName.set("")
        self.chipFileName.set("")
        self.addExprInFileName.set("")
        self.addExprOutFileName.set("")
        self.collapseRankAndExpr.set(0)
        self.modeRadioButtonPressed()
        
    def chooseInputFile(self):
        if self.collapseRankAndExpr.get() == 0:
            filetypes = [("Supported Files (GCT, TXT, RNK)", "*.gct"),
                         ("Supported Files (GCT, TXT, RNK)", "*.txt"),
                         ("Supported Files (GCT, TXT, RNK)", "*.rnk"),
                         ("Supported Files (GCT, TXT, RNK)", "*.GCT"),
                         ("Supported Files (GCT, TXT, RNK)", "*.TXT"),
                         ("Supported Files (GCT, TXT, RNK)", "*.RNK")]
        else:
            filetypes = [("Ranked List (RNK)", "*.rnk"),
                         ("Ranked List (RNK)", "*.RNK")]
        filename = tkFileDialog.askopenfilename(title="Choose Input Expression Matrix or Rank file",
                                                filetypes=filetypes)
        self.inputFileName.set(filename)
        
        if self.debug:
            self.writeMessage("selected Input File Name: %s " % filename)

    def chooseOutputFile(self):
        if self.collapseRankAndExpr.get() == 0:
            filetypes = [("Supported Files (GCT, TXT, RNK)", "*.gct"),
                         ("Supported Files (GCT, TXT, RNK)", "*.txt"),
                         ("Supported Files (GCT, TXT, RNK)", "*.rnk"),
                         ("Supported Files (GCT, TXT, RNK)", "*.GCT"),
                         ("Supported Files (GCT, TXT, RNK)", "*.TXT"),
                         ("Supported Files (GCT, TXT, RNK)", "*.RNK")]
        else:
            filetypes = [("Ranked List (RNK)", "*.rnk"),
                         ("Ranked List (RNK)", "*.RNK")]
        def_file = ""
        def_ext = ".TXT"
        if not self.inputFileName.get() == "":
            (inputFileDir, inputFileName) = os.path.split(self.inputFileName.get())
            tokens = inputFileName.rsplit(".", 1)
            def_ext = "." + tokens[-1]
            def_file = tokens[0] + "_collapsed" + "." + tokens[-1]
        
        filename = tkFileDialog.asksaveasfilename(title="Choose Output File",
                                                  filetypes=filetypes,
                                                  defaultextension=def_ext,
                                                  initialfile=def_file,
                                                  initialdir=inputFileDir)
        self.outputFileName.set(filename)
        if self.debug:
            self.writeMessage("selected Output File Name: %s " % filename)

    def chooseAddExprInputFile(self):
        filename = tkFileDialog.askopenfilename(title="Choose Additional Input Expression Matrix file",
                                                filetypes=[("Supported Files (GCT, TXT)", "*.gct"),
                                                           ("Supported Files (GCT, TXT)", "*.txt"),
                                                           ("Supported Files (GCT, TXT)", "*.GCT"),
                                                           ("Supported Files (GCT, TXT)", "*.TXT") ])
        self.addExprInFileName.set(filename)
        
        if self.debug:
            self.writeMessage("selected Input Expression File Name: %s " % filename)

    def chooseAddExprOutputFile(self):
        def_file = ""
        def_ext = ".TXT"
        if not self.addExprInFileName.get() == "":
            (inputFileDir, addExprInFileName) = os.path.split(self.addExprInFileName.get())
            tokens = addExprInFileName.rsplit(".", 1)
            def_ext = "." + tokens[-1]
            def_file = tokens[0] + "_collapsed" + "." + tokens[-1]
        
        filename = tkFileDialog.asksaveasfilename(title="Choose Output Expression Marix File",
                                                  filetypes=[("Supported Files (GCT, TXT)", "*.gct"),
                                                           ("Supported Files (GCT, TXT)", "*.txt"),
                                                           ("Supported Files (GCT, TXT)", "*.GCT"),
                                                           ("Supported Files (GCT, TXT)", "*.TXT")],
                                                  defaultextension=def_ext,
                                                  initialfile=def_file,
                                                  initialdir=inputFileDir)
        self.addExprOutFileName.set(filename)
        if self.debug:
            self.writeMessage("selected Output Expression File Name: %s " % filename)

    def chooseChipFile(self):
        filename = tkFileDialog.askopenfilename(title="Choose Chip Annotation file",
                                                filetypes=[("Chip Annotation file (CHIP)", "*.chip"),
                                                           ("Chip Annotation file (CHIP)", "*.CHIP")])
        self.chipFileName.set(filename)
        if self.debug:
            self.writeMessage("selected Output File Name: %s " % filename)

    def doReplaceButtonPressed(self):
        if self.doIdReplace.get() == 1:
            self.chipfileBox.configure(state=NORMAL)
            self.chipfileButton.configure(state=NORMAL)
        else:
            self.chipfileBox.configure(state=DISABLED)
            self.chipfileButton.configure(state=DISABLED)
            
    def modeRadioButtonPressed(self):
        if self.collapseRankAndExpr.get() == 1:
            #Change some text:
            self.exprOrRankFileSelectorFrameText.set('Ranked List:')
            #Enable some widgets
            self.addExprInFileBox.configure(state=NORMAL)
            self.addExprInFileButton.configure(state=NORMAL)
            self.addExprOutFileBox.configure(state=NORMAL)
            self.addExprOutFileButton.configure(state=NORMAL)
            self.chipfileBox.configure(state=NORMAL)
            self.chipfileButton.configure(state=NORMAL)
            #Disable some other widgets
            self.doCollapseCheck.configure(state=DISABLED)
            self.doReplaceCheck.configure(state=DISABLED)
            pass
        else:
            #Change some text:
            self.exprOrRankFileSelectorFrameText.set('Expression Matrix or Ranked List:')
            #Disable some widgets
            self.addExprInFileBox.configure(state=DISABLED)
            self.addExprInFileButton.configure(state=DISABLED)
            self.addExprOutFileBox.configure(state=DISABLED)
            self.addExprOutFileButton.configure(state=DISABLED)
            #Enable some other widgets
            self.doCollapseCheck.configure(state=NORMAL)
            self.doReplaceCheck.configure(state=NORMAL)
            #revert state of widgets to previous state
            if self.doIdReplace.get() == 1:
                self.chipfileBox.configure(state=NORMAL)
                self.chipfileButton.configure(state=NORMAL)
            else:
                self.chipfileBox.configure(state=DISABLED)
                self.chipfileButton.configure(state=DISABLED)

    def checkInput(self):
        self.inputOK = True
        
        if self.inputFileName.get() == "":
            self.inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Input-file required", icon=tkMessageBox.ERROR)
        elif not os.path.isfile(self.inputFileName.get()):
            self.inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Input-file does not exist", icon=tkMessageBox.ERROR)
        else:
            inputFileName = self.inputFileName.get() 

        if self.outputFileName.get() == "":
            self.inputOK = False
            tkMessageBox.showerror(title="Input Error", message="Output-file required", icon=tkMessageBox.ERROR)
        else:
            outputFileName = self.outputFileName.get()

        if self.doIdReplace.get() == 1 or self.collapseRankAndExpr.get() == 1:
            if self.chipFileName.get() == "":
                self.inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Chip-file required", icon=tkMessageBox.ERROR)
            elif not os.path.isfile(self.chipFileName.get()):
                self.inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Chip-file does not exist", icon=tkMessageBox.ERROR)
            else:
                chipFileName = self.chipFileName.get()

        if self.collapseRankAndExpr.get() == 1:
            if self.addExprInFileName.get() == "":
                self.inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Expression Matrix Input-file required", icon=tkMessageBox.ERROR)
            elif not os.path.isfile(self.addExprInFileName.get()):
                self.inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Expression Matrix Input-file does not exist", icon=tkMessageBox.ERROR)
            
            if self.addExprOutFileName.get() == "":
                self.inputOK = False
                tkMessageBox.showerror(title="Input Error", message="Expression Matrix Output-file required", icon=tkMessageBox.ERROR)
                

        return self.inputOK

    def run(self):
        self.writeMessage("Testing Input...")
        inputOK = self.checkInput()
        if inputOK:
            self.writeMessage("Starting....")
            if self.debug:
                print "Running..."
                print "Input File:  %s" % self.inputFileName.get()
                print "Output File: %s" % self.outputFileName.get()
                print "Chip File:   %s" % self.chipFileName.get()
                print "Do Collapse: %i" % self.doCollapse.get()
                print "Do Replace:  %i" % self.doIdReplace.get()
                
            if self.collapseRankAndExpr.get() == 0:
                collapser = CollapseExpressionMatrix(inputFileName=self.inputFileName.get(),
                                                     outputFileName=self.outputFileName.get(),
                                                     chipFileName=self.chipFileName.get(),
                                                     doCollapse=(self.doCollapse.get() == 1),
                                                     collapseMode='max_probe',
                                                     verbose=True,
                                                     fix_session=False,
                                                     suppress_null=(self.suppress_null.get() == 1),
                                                     gui=self)
            else:
                collapser = CollapseExpressionMatrix(inputFileName=self.inputFileName.get(),
                                                     outputFileName=self.outputFileName.get(),
                                                     chipFileName=self.chipFileName.get(),
                                                     extra_expr_in=self.addExprInFileName.get(),
                                                     extra_expr_out=self.addExprOutFileName.get(),
                                                     doCollapse=True,
                                                     collapseMode='max_probe',
                                                     verbose=True,
                                                     fix_session=False,
                                                     suppress_null=(self.suppress_null.get() == 1),
                                                     gui=self)
                
            collapser.main()
            tkMessageBox.showinfo(title="Done", message="Done. Check Message-Box for status.")
#            self.quit()

class CollapseExpressionMatrix:
    def __init__(self,
                 inputFileName, outputFileName, chipFileName, extra_expr_in="", extra_expr_out="",
                 doCollapse=False, collapseMode="max_probe", verbose=True, fix_session=False, suppress_null=False, gui=None):
        self.inputFileName = inputFileName
        self.outputFileName = outputFileName
        self.chipFileName = chipFileName
        self.extra_expr_in = extra_expr_in
        self.extra_expr_out = extra_expr_out
        self.doCollapse = doCollapse
        self.collapseMode = collapseMode
        self.verbose = verbose
        self.fix_session = fix_session
        self.suppress_null = suppress_null
        self.gui = gui

    def printMessage(self, text):
        if self.gui == None:
            sys.stdout.write(text)
        else:
            self.gui.writeMessage(text)
            
    def read_chipfile(self, chipfileName):
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
        if self.verbose :
            self.printMessage("reading Chip file...\n")
        
        try:
            chipfile = file(chipfileName, "rU")
            try:
                for line in chipfile:
                    if not passedHeader or re_chip_header.match(line):
                        passedHeader = 1
                        continue
                    probe = line.split("\t")
                    id_symbol_map[probe[0]] = probe[1]
            finally:
                chipfile.close()
        except IOError, (errorNo, text):
            raise IOError, (errorNo, text)
    
        return id_symbol_map
    
    
    def read_inputFile(self, inputFileName):
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
        # read expression data
        if self.verbose :
            self.printMessage("reading input file...\n")

        infile = file(inputFileName, "rU")
        try:
            inputFileLines = infile.readlines()
            
            ## Guess the type of file:
            if re.search("^#1.2\s*", inputFileLines[0]):
                type = "GCT"
                if self.verbose :
                    self.printMessage("...think it's GCT\n")
            elif re.search("^NAME\tDESCRIPTION\t", inputFileLines[0], re.IGNORECASE):
                type = "TXT"
                if self.verbose :
                    self.printMessage("...think it's TXT\n")
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
                    if self.verbose :
                        self.printMessage("...think it's RNK\n")
                else:
                    error_text = "Error in line %i\n" % i
                    error_text += "Invalid Input File: '%s' \n" % inputFileName
                    error_text += "\tIt seems it's neither an expression file (GCT or TXT) or Ranked Gene list\n"
                    error_text += "\tRefer to http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats for specifications\n"
                    raise IOError, (1, error_text)
                    
        finally:
            infile.close()
        
        return inputFileLines, type
    
    
    def replace_IDs(self, dataLines, id_symbol_map):
        """
        replaces the IDs in the first column by symbols (based on idSymbolMap)
        
        @return: dataLines
        """
        # replace Probeset IDs with Gene Symbols
        if self.verbose :
            self.printMessage("replacing Probeset IDs with Gene Symbols...\n")
    
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
    
    
    def collapse_data(self, data_lines, type, mode='max_probe'):
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
        
        if self.verbose :
            self.printMessage("collapsing Probe-Sets...\n")
        
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

            if self.suppress_null and tokens[0].strip().upper() == 'NULL':
                if self.verbose :
                    self.printMessage("dropping Gene 'NULL'...\n")
                continue

            ##### BEGIN FIX SESSION CODE #####
            if self.fix_session and (tokens[0] not in self.genes_of_interest) :
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
        if self.fix_session and type == "GCT" :
            # expression tables in Session files have only one header line
            del data_lines[1:2]
        ##### END   FIX SESSION CODE #####
        
        return data_lines
    
    def collapse_rank_and_expr(self, rank_data, expr_data, expr_type, idSymbolMap, mode="max_probe"):
        import re
        re_comment = re.compile("^#")
        # 1) First pass though ranks: create map: 
        ranks_map = {}
        """  
        ranks_map =  { "Symbol_A" :{ "probeID_A1": 'score_A1', 
                                     "probeID_A2": 'score_A2'  }, 
                       "Symbol_B" :{ "probeID_B1": 'score_B1'  }, 
                        ... 
                     }
        """
        rank_header_lines = []
        
        if self.verbose:
            self.printMessage('processing rank file...\n')
        for rank_line in rank_data:
            if re_comment.search(rank_line):
                rank_header_lines.append(rank_line)
            else:
                (probeID, score) = rank_line.split("\t", 1) # we already made sure that it's 2 column when in read_inputFile()
                if idSymbolMap.has_key(probeID):
                    symbol = idSymbolMap[probeID]
                    if not ranks_map.has_key(symbol):
                        ranks_map[symbol] = {probeID: score}
                    else:
                        if not ranks_map[symbol].has_key(probeID):
                            ranks_map[symbol][probeID] = score
                        else:
                            self.printMessage("WARNING: Duplicate Identifier '%s' in rank file '%s'\n Check your Input!!!!\n" % (probeID, self.inputFileName))
                else:
                    self.printMessage("WARNING: Identifier '%s' not found in annotation file '%s'\n" % (probeID, self.chipFileName))
                        
        
        # 2) make map from expressions: 
        #    exprs_map = { "probeID_A1": "rest of line A1", 
        #                  "probeID_A2": "rest of line A2", 
        #                    ...
        #                }
        exprs_map = {}
        exprs_header_lines = []

        if expr_type == "GCT":
            exprs_header_lines = expr_data[:3]
            expr_data = expr_data[3:]
        elif expr_type == "TXT":
            exprs_header_lines = expr_data[:1]
            expr_data = expr_data[1:]
            
        if self.verbose:
            self.printMessage('processing expressions file...\n')
        for expr_line in expr_data:
            (probeID, descr, data) = expr_line.split("\t", 2)
            if idSymbolMap.has_key(probeID):
                if not exprs_map.has_key(probeID):
                    exprs_map[probeID] = data
                else:
                    self.printMessage("WARNING: Duplicate Identifier '%s' in expressions file '%s'\n Check your Input!!!!\n" % (probeID, self.extra_expr_in))
            else:
                self.printMessage("WARNING: Identifier '%s' not found in annotations file '%s'\n" % (probeID, self.chipFileName))
            
            
        
        # 3) iterate over all symbols in ranks_map, 
        #        iterate over probeID's
        #            pick probeID with highest score
        #            collect this probeID for rank file (and replace ID to symbol)
        #            collect this probeID for expr file, replace ID to symbol, keep selected ID (and others) in descr. col.  
        rank_data_lines = []
        expr_data_lines = []
        
        for symbol in ranks_map.keys():
            best_probeID = ""
            best_score = 0.0

            if self.suppress_null and symbol.strip().upper() == 'NULL':
                if self.verbose :
                    self.printMessage("dropping Gene 'NULL'...\n")
                continue

            if len(ranks_map[symbol].keys()) > 1:
                for probeID in ranks_map[symbol].keys():
                    if abs(float(ranks_map[symbol][probeID])) > abs(best_score):
                        best_probeID = probeID
                        best_score = float(ranks_map[symbol][best_probeID])

                rank_data_line = "\t".join([ symbol, ranks_map[symbol][best_probeID] ])
                
                probeIDs = ranks_map[symbol].keys()
                probeIDs.remove(best_probeID)
                probeIDs = best_probeID + " " + "(" + ", ".join(probeIDs) + ")"
                expr_data_line = "\t".join([ symbol, probeIDs, exprs_map[best_probeID] ])
            else:
                probeID = ranks_map[symbol].keys()[0]
                rank_data_line = "\t".join([ symbol, ranks_map[symbol][probeID] ])
                expr_data_line = "\t".join([ symbol, probeID, exprs_map[probeID] ])
                
            rank_data_lines.append(rank_data_line)
            expr_data_lines.append(expr_data_line)
        
        # restore header
        rank_data_lines[:0] = rank_header_lines
        if expr_type == "GCT":
            # calculate new dimensions of collapsed expression table
            exprs_header_lines[1] = "\t".join([str(len(expr_data_lines)), exprs_header_lines[1].split("\t")[1] ]) + '\n'
        expr_data_lines[:0] = exprs_header_lines
        
        return (rank_data_lines, expr_data_lines)
    
    
    def main(self):
        "Main program function"
        try:
            if not self.chipFileName == "":
                idSymbolMap = self.read_chipfile(self.chipFileName)
            
            ##### BEGIN FIX SESSION CODE #####
            if self.fix_session :
                # collect genes of interest
                self.genes_of_interest = []
                
                expr_file = file(self.outputFileName, "rU")
                try:
                    for line in expr_file:
                        data = line.split("\t", 1)
                        if not data[0] == "NAME":
                            self.genes_of_interest.append(data[0])
                finally:
                    expr_file.close()
                # make backup of the expression file
                os.rename(self.outputFileName, self.outputFileName + ".BAK")
            ##### END   FIX SESSION CODE #####
                
           
            if self.extra_expr_in != '' and self.extra_expr_out != '' and self.chipFileName != '' and self.doCollapse :
                # if all data is available collapse a rank and expression file together
                (rank_data, rnk_type) = self.read_inputFile(self.inputFileName)
                (expr_data, expr_type) = self.read_inputFile(self.extra_expr_in)

                if not rnk_type == 'RNK':
                    raise IOError, (1, "ERROR: Wrong file type!\nInput file %s needs to be a ranked list (RNK) in this mode." % self.inputFileName)
                if not (expr_type == "GCT" or expr_type == "TXT"):
                    raise IOError, (1, "ERROR: Wrong file type!\n" + \
                                    "Additional input Expression-table %s needs to of type GCT or TXT but was identified as '%s'" % (self.extra_expr_in, expr_type))
                    

                (rank_file_lines, expr_file_lines) = self.collapse_rank_and_expr(rank_data=rank_data,
                                                                                  expr_data=expr_data,
                                                                                  expr_type=expr_type,
                                                                                  idSymbolMap=idSymbolMap,
                                                                                  mode=self.collapseMode)
                if self.verbose:
                    self.printMessage("writing RNK file...\n")
                try:
                    rank_outfile = file(self.outputFileName, "w")
                except IOError, (errorNo, text):
                    raise IOError, (errorNo, text + " : " + self.outputFileName)
                try:
                    rank_outfile.writelines(rank_file_lines)
                finally:
                    rank_outfile.close()
                
                try:
                    expr_outfile = file(self.extra_expr_out, "w")
                except IOError, (errorNo, text):
                    raise IOError, (errorNo, text + " : " + self.extra_expr_out)
                try:
                    if self.verbose:
                        self.printMessage("writing Expression file...\n")
                    expr_outfile.writelines(expr_file_lines)
                finally:
                    expr_outfile.close()
            
            else:
                # Do it the old fashioned way
                (expr_file_lines, type) = self.read_inputFile(self.inputFileName)
                
                if not self.chipFileName == "":
                    expr_file_lines = self.replace_IDs(expr_file_lines, idSymbolMap)
                
                if self.doCollapse == True:
                    expr_file_lines = self.collapse_data(expr_file_lines, type, mode=self.collapseMode)
                   
                # write expression data in output file
                outfile = file(self.outputFileName, "w")
                try:
                    outfile.writelines(expr_file_lines)
                finally:
                    outfile.close()

            self.printMessage("Done!\n")
                
        except IOError, (errorNo, text):
            print parser.get_usage()
            self.printMessage(text + '\n')
            self.printMessage("exiting\n")
            sys.exit(1)


if __name__ == "__main__":
    from optparse import OptionParser, SUPPRESS_HELP
    import sys, os
    
    
    # Configure parser for command line options:
    __usage = "%prog [options] -i input.gct -o output.gct [-c platform.chip] [--collapse]"
    __description = "This tool can process a gene expression matrix (in GCT or TXT format) or ranked list (RNK format)\n" + \
                    "and either replace the Identifier based on a Chip Annotation file (e.g. AffyID -> Gene Symbol),\n" + \
                    "or collapse the expression values or rank-scores for Genes from more than one probe set.\n" + \
                    "Both can be done in one step by using both '-c platform.chip' and '--collapse' at the same time.\n" + \
                    "If a ranked list is to be collapsed, an additional expression matrix can be supplied by the -e/-x parameters\n" + \
                    "and will be filtered to contain the same probe-sets as selected from the RNK file.\n" + \
                    "If however the file supplied by -i is not recognized as a RNK file, these options have no effect.\n" + \
                    "\n" + \
                    "For detailed descriptions of the file formats, please refer to: http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats \n\n" + \
                    "Call without any parameters to select the files and options with a GUI (Graphical User Interface)"
    parser = OptionParser(usage=__usage, description=__description, version="%prog " + __version__)
    parser.add_option("-i", "--input",
                      dest="infile",
    #                 default="",
                      help="input expression table or ranked list\n",
                      metavar="FILE")
    parser.add_option("-o", "--output",
                      dest="outfile",
    #                 default="output.gct",
                      help="output expression table or ranked list\n",
                      metavar="FILE")
    parser.add_option("-c", "--chip",
                      dest="chipfile",
                      default='',
                      help="Chip File\nThis implies that the Identifiers are to be replaced.",
                      metavar="FILE")
    
    parser.add_option("-e", "--ei",
                      dest="expr_in",
                      default='',
                      help="(optional) additional input Expression-table, to be restricted to the same probe-sets as the RNK file",
                      metavar="FILE"
                      )
    parser.add_option("-x", "--xo",
                      dest="expr_out",
                      default='',
                      help="(optional) corresponding output file for -i/--ei option",
                      metavar="FILE"
                      )
    
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
#                     help="Mode for collapsing data from multiple probe sets for the same gene symbol. Currently only 'max_probe' is supported.",
                      help=SUPPRESS_HELP
                      )
    parser.add_option("--null",
                      dest="suppress_null",
                      default=False,
                      help="suppress Gene with Symbol NULL\n",
                      action="store_true",
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

    # decide if we start the GUI or use the command line
    useGui = (not options.infile and
              not options.outfile and 
              not options.chipfile and 
              not options.collapse) or options.useGui
    
    if useGui:
        theGui = ReplaceCollapseGui(version=__version__)
#        theGui.master.title("Sample application")
        theGui.master.minsize(width=450, height=550)
        theGui.master.lift()
        theGui.center_window(w=450, h=550)
        
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
            parser.error("Chip-file does not exist.")
        if not options.expr_in == "" and not os.path.isfile(options.expr_in):
            parser.error("Expression-file (--ei) does not exist.")
        if not options.expr_in == "" and options.expr_out == "":
            parser.error("If additional expression input file (--ei) is given, a corresponding output file (--xo) is required, too.")
        if (options.expr_in != "" and options.expr_out != "") and not (options.chipfile != "" and options.collapse == True):
            parser.error("Filtering of additional expression table (--ei/--xo) requires specifying both --chip and --collapse")
        
        collapser = CollapseExpressionMatrix(inputFileName=options.infile,
                                             outputFileName=options.outfile,
                                             chipFileName=options.chipfile,
                                             extra_expr_in=options.expr_in,
                                             extra_expr_out=options.expr_out,
                                             doCollapse=options.collapse,
                                             collapseMode=options.mode,
                                             verbose=options.verbose,
                                             fix_session=options.fix_session,
                                             suppress_null=options.suppress_null)
        collapser.main()
        
        
