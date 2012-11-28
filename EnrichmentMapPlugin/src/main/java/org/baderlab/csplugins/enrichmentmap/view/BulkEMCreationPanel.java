package org.baderlab.csplugins.enrichmentmap.view;

import cytoscape.Cytoscape;
import cytoscape.data.readers.TextFileReader;
import cytoscape.generated.SessionState;
import cytoscape.util.FileUtil;
import cytoscape.util.OpenBrowser;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildBulkEnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class BulkEMCreationPanel extends JPanel{

    private static final long serialVersionUID = 7233557042420194604L;

    private EnrichmentMapParameters params;

    //Genesets file related components
    //user specified file names
    private JFormattedTextField GSEAResultsDirTextField;

    //directory for GMT and GCT if they are different than the RPT file
    private JFormattedTextField GMTDirectoryTextField;
    private JFormattedTextField GCTDirectoryTextField;

    private JFormattedTextField pvalueTextField;
    private JFormattedTextField qvalueTextField;
    private JFormattedTextField coeffecientTextField;

    private JFormattedTextField lowerLimitField;
    private JFormattedTextField upperLimitField;

    private JRadioButton gsea;
    private JRadioButton generic;
    private JRadioButton david;
    private JRadioButton overlap;
    private JRadioButton jaccard;
    private JRadioButton combined;

    private JRadioButton onesession;
    private JRadioButton multisession;
    private boolean sessions = true;

    DecimalFormat decFormat; // used in the formatted text fields
    NumberFormat numFormat;

    private int defaultColumns = 15;
    
    private boolean similarityCutOffChanged = false;
    
    public BulkEMCreationPanel() {
          params = new EnrichmentMapParameters();

        //create the three main panels: scope, advanced options, and bottom
        JPanel AnalysisTypePanel = createAnalysisTypePanel();

        //Put the options panel into a scroll pain

        CollapsiblePanel OptionsPanel = createOptionsPanel();
        OptionsPanel.setCollapsed(false);
        //JScrollPane scroll = new JScrollPane(OptionsPanel);

        //Since the advanced options panel is being added to the center of this border layout
        //it will stretch it's height to fit the main panel.  To prevent this we create an
        //additional border layout panel and add advanced options to it's north compartment
        JPanel advancedOptionsContainer = new JPanel(new BorderLayout());
        //advancedOptionsContainer.add(scroll, BorderLayout.CENTER);
        advancedOptionsContainer.add(OptionsPanel, BorderLayout.CENTER);
        JPanel bottomPanel = createBottomPanel();

        //Add all the vertically aligned components to the main panel
        //Add all the vertically aligned components to the main panel
        add(AnalysisTypePanel,BorderLayout.NORTH);
        add(advancedOptionsContainer,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);


    }

    /**
            * Creates a collapsible panel that holds main user inputs geneset files, datasets and parameters
            *
            * @return collapsablePanel - main analysis panel
            */
           private CollapsiblePanel createOptionsPanel() {
               CollapsiblePanel collapsiblePanel = new CollapsiblePanel("User Input");

               JPanel panel = new JPanel();
               panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

               //Gene set file panel
               CollapsiblePanel GMTcollapsiblePanel = createGMTPanel();
               GMTcollapsiblePanel.setCollapsed(false);

               //Parameters collapsible panel
               CollapsiblePanel ParametersPanel = createParametersPanel();
               ParametersPanel.setCollapsed(false);

                //GMT and GCT path changer
                CollapsiblePanel GMTDirPanel = createDiffGMTGCTDirectory();
                GMTDirPanel.setCollapsed(true);

               panel.add(GMTcollapsiblePanel);
               panel.add(ParametersPanel);
                panel.add(GMTDirPanel);

               collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
               return collapsiblePanel;
           }

    /**
     * Creates a collapsible panel that holds the different paths for the GMT and GCt file
     *
     * Only required when the user has moved the location of the these files and they are different
     * than the ones defined in the RPT file
     * @return CollapsiblePanel
     */

    private CollapsiblePanel createDiffGMTGCTDirectory(){
      CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Advanced");


            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 1));

            //add GMT file
            JLabel GMTLabel = new JLabel("Directory containing GMT file:"){
                 /**
                 *
                 */
                private static final long serialVersionUID = -122741876830022713L;

                public JToolTip createToolTip() {
                      return new JMultiLineToolTip();
                 }
            };
            JButton selectGMTDirButton = new JButton();
            GMTDirectoryTextField = new JFormattedTextField() ;
            GMTDirectoryTextField.setColumns(defaultColumns);


           //components needed for the directory load
           GMTDirectoryTextField.setFont(new java.awt.Font("Dialog",1,10));
           //GSEAResultsDirTextField.setText(gmt_instruction);
           GMTDirectoryTextField.addPropertyChangeListener("value",new BulkEMCreationPanel.FormattedTextFieldAction());


           selectGMTDirButton.setText("...");
           selectGMTDirButton.setMargin(new Insets(0,0,0,0));
           selectGMTDirButton
                           .addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectGMTDirButtonActionPerformed(evt);
                               }
           });

           JPanel newGMTDirPanel = new JPanel();
           newGMTDirPanel.setLayout(new BorderLayout());

           newGMTDirPanel.add(GMTLabel,BorderLayout.WEST);
           newGMTDirPanel.add(GMTDirectoryTextField, BorderLayout.CENTER);
           newGMTDirPanel.add( selectGMTDirButton, BorderLayout.EAST);

           panel.add(newGMTDirPanel);


            //add GMT file
            JLabel GCTLabel = new JLabel("Directory containing GCT file:"){
                 /**
                 *
                 */
                private static final long serialVersionUID = -122741876830022713L;

                public JToolTip createToolTip() {
                      return new JMultiLineToolTip();
                 }
            };
            JButton selectGCTDirButton = new JButton();
            GCTDirectoryTextField = new JFormattedTextField() ;
            GCTDirectoryTextField.setColumns(defaultColumns);


           //components needed for the directory load
           GCTDirectoryTextField.setFont(new java.awt.Font("Dialog",1,10));
           //GSEAResultsDirTextField.setText(gmt_instruction);
           GCTDirectoryTextField.addPropertyChangeListener("value",new BulkEMCreationPanel.FormattedTextFieldAction());


           selectGCTDirButton.setText("...");
           selectGCTDirButton.setMargin(new Insets(0,0,0,0));
           selectGCTDirButton
                           .addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectGCTDirButtonActionPerformed(evt);
                               }
           });

           JPanel newGCTDirPanel = new JPanel();
           newGCTDirPanel.setLayout(new BorderLayout());

           newGCTDirPanel.add(GCTLabel,BorderLayout.WEST);
           newGCTDirPanel.add(GCTDirectoryTextField, BorderLayout.CENTER);
           newGCTDirPanel.add( selectGCTDirButton, BorderLayout.EAST);

           panel.add(newGCTDirPanel);


          //add the ability to specify range of directories to use when building maps.
           //qvalue cutoff input
              JLabel lowerlimitLabel = new JLabel("Lower limit:");
              lowerLimitField = new JFormattedTextField(numFormat);
              lowerLimitField.setColumns(3);
              lowerLimitField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
              String lowerTip = "Sets the lower limit \n" +
                      "of the directory number you want to  \n"+
                       "use when creating networks.";
              lowerLimitField.setToolTipText(lowerTip);
              lowerLimitField.setText("1");
              lowerLimitField.setValue(1);

              JPanel lowerLimitPanel = new JPanel();
              lowerLimitPanel.setLayout(new BorderLayout());
              lowerLimitPanel.setToolTipText(lowerTip);

              lowerLimitPanel.add(lowerlimitLabel, BorderLayout.WEST);
              lowerLimitPanel.add(lowerLimitField, BorderLayout.EAST);

           //add the ability to specify range of directories to use when building maps.
           //qvalue cutoff input
              JLabel upperlimitLabel = new JLabel("Upper limit:");
              upperLimitField = new JFormattedTextField(numFormat);
              upperLimitField.setColumns(3);
              upperLimitField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
              String upperTip = "Sets the upper limit \n" +
                      "of the directory number you want to  \n"+
                       "use when creating networks.";
              upperLimitField.setToolTipText(upperTip);
              upperLimitField.setText("1");
              upperLimitField.setValue(1);

              JPanel upperLimitPanel = new JPanel();
              upperLimitPanel.setLayout(new BorderLayout());
              upperLimitPanel.setToolTipText(upperTip);

              upperLimitPanel.add(upperlimitLabel, BorderLayout.WEST);
              upperLimitPanel.add(upperLimitField, BorderLayout.EAST);

           panel.add(lowerLimitPanel);
           panel.add(upperLimitPanel);

            //add check box to indicate if you want to create one session or multiple sessions
           ButtonGroup sessionsgroup;

          onesession = new JRadioButton("One session File");
          onesession.setActionCommand("onesession");
          onesession.setSelected(true);
          multisession = new JRadioButton("Multiple session files");
          multisession.setActionCommand("multisession");

           multisession.setSelected(true);
           onesession.setSelected(false);

          sessionsgroup = new javax.swing.ButtonGroup();
          sessionsgroup.add(onesession);
          sessionsgroup.add(multisession);


          onesession.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                           selectOneMultiSessionActionPerformed(evt);
                    }
              });

          multisession.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                           selectOneMultiSessionActionPerformed(evt);
                    }
              });

        //create a panel for the two buttons
              JPanel session_buttons = new JPanel();
              session_buttons.setLayout(new BorderLayout());
              session_buttons.add(onesession, BorderLayout.NORTH);
              session_buttons.add(multisession, BorderLayout.SOUTH);

           panel.add(session_buttons);
           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           return collapsiblePanel;
    }


    /**
     * Creates a collapsible panel that holds gene set file specification
     *
     * @return collapsible panel - gmt gene set file specification interface
     */
        private CollapsiblePanel createGMTPanel() {
            CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene Sets");

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 1));

            //add GMT file
            JLabel GMTLabel = new JLabel("GSEA Results directory:"){
                 /**
                 *
                 */
                private static final long serialVersionUID = -122741876830022713L;

                public JToolTip createToolTip() {
                      return new JMultiLineToolTip();
                 }
            };
            JButton selectGMTFileButton = new JButton();
            GSEAResultsDirTextField = new JFormattedTextField() ;
            GSEAResultsDirTextField.setColumns(defaultColumns);


           //components needed for the directory load
           GSEAResultsDirTextField.setFont(new java.awt.Font("Dialog",1,10));
           //GSEAResultsDirTextField.setText(gmt_instruction);
            GSEAResultsDirTextField.addPropertyChangeListener("value",new BulkEMCreationPanel.FormattedTextFieldAction());


           selectGMTFileButton.setText("...");
           selectGMTFileButton.setMargin(new Insets(0,0,0,0));
           selectGMTFileButton
                           .addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectGMTFileButtonActionPerformed(evt);
                               }
           });

           JPanel newGMTPanel = new JPanel();
           newGMTPanel.setLayout(new BorderLayout());

           newGMTPanel.add(GMTLabel,BorderLayout.WEST);
           newGMTPanel.add(GSEAResultsDirTextField, BorderLayout.CENTER);
           newGMTPanel.add( selectGMTFileButton, BorderLayout.EAST);

           //add the components to the panel
           if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                panel.add(newGMTPanel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           return collapsiblePanel;

        }
    /**
           * Creates a collapsable panel that holds parameter inputs
           *
           * @return panel containing the parameter specification interface
           */
          private CollapsiblePanel createParametersPanel() {
              CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Parameters");

              JPanel panel = new JPanel();
              panel.setLayout(new GridLayout(0, 1));

              //pvalue cutoff input
              JLabel pvalueCutOffLabel = new JLabel("P-value Cutoff");
              pvalueTextField = new JFormattedTextField(decFormat);
              pvalueTextField.setColumns(3);
              pvalueTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
              String pvalueCutOffTip = "Sets the p-value cutoff \n" +
                      "only genesets with a p-value less than \n"+
                       "the cutoff will be included.";
              pvalueTextField.setToolTipText(pvalueCutOffTip);
              pvalueTextField.setText(Double.toString(params.getPvalue()));
              pvalueTextField.setValue(params.getPvalue());

              JPanel pvalueCutOffPanel = new JPanel();
              pvalueCutOffPanel.setLayout(new BorderLayout());
              pvalueCutOffPanel.setToolTipText(pvalueCutOffTip);

              pvalueCutOffPanel.add(pvalueCutOffLabel, BorderLayout.WEST);
              pvalueCutOffPanel.add(pvalueTextField, BorderLayout.EAST);


               //qvalue cutoff input
              JLabel qvalueCutOffLabel = new JLabel("FDR Q-value Cutoff");
              qvalueTextField = new JFormattedTextField(decFormat);
              qvalueTextField.setColumns(3);
              qvalueTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
              String qvalueCutOffTip = "Sets the FDR q-value cutoff \n" +
                      "only genesets with a FDR q-value less than \n"+
                       "the cutoff will be included.";
              qvalueTextField.setToolTipText(qvalueCutOffTip);
              qvalueTextField.setText(Double.toString(params.getQvalue()));
              qvalueTextField.setValue(params.getQvalue());

              JPanel qvalueCutOffPanel = new JPanel();
              qvalueCutOffPanel.setLayout(new BorderLayout());
              qvalueCutOffPanel.setToolTipText(qvalueCutOffTip);

              qvalueCutOffPanel.add(qvalueCutOffLabel, BorderLayout.WEST);
              qvalueCutOffPanel.add(qvalueTextField, BorderLayout.EAST);

              //coefficient cutoff input

              ButtonGroup jaccardOrOverlap;

              jaccard = new JRadioButton("Jaccard Coeffecient");
              jaccard.setActionCommand("jaccard");
              jaccard.setSelected(true);
              overlap = new JRadioButton("Overlap Coeffecient");
              overlap.setActionCommand("overlap");
              combined = new JRadioButton("Jaccard+Overlap Combined");
              combined.setActionCommand("combined");
               if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD) ) {
               jaccard.setSelected(true);
               overlap.setSelected(false);
               combined.setSelected(false);
           } else if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
               jaccard.setSelected(false);
               overlap.setSelected(true);
               combined.setSelected(false);
           }
           else if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)){
               jaccard.setSelected(false);
               overlap.setSelected(false);
               combined.setSelected(true);
           }
              jaccardOrOverlap = new javax.swing.ButtonGroup();
              jaccardOrOverlap.add(jaccard);
           jaccardOrOverlap.add(overlap);
           jaccardOrOverlap.add(combined);

           jaccard.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                               selectJaccardOrOverlapActionPerformed(evt);
                        }
                  });

           overlap.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                               selectJaccardOrOverlapActionPerformed(evt);
                        }
                  });
           combined.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                               selectJaccardOrOverlapActionPerformed(evt);
                        }
                  });

              //create a panel for the two buttons
              JPanel index_buttons = new JPanel();
              index_buttons.setLayout(new BorderLayout());
              index_buttons.add(jaccard, BorderLayout.NORTH);
              index_buttons.add(overlap, BorderLayout.SOUTH);

              JLabel coeffecientCutOffLabel = new JLabel("Cutoff");
              coeffecientTextField = new JFormattedTextField(decFormat);
              coeffecientTextField.setColumns(3);
              coeffecientTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
              String coeffecientCutOffTip = "Sets the Jaccard or Overlap coeffecient cutoff \n" +
                                "only edges with a Jaccard or Overlap coffecient less than \n"+
                                 "the cutoff will be added.";
             coeffecientTextField.setToolTipText(coeffecientCutOffTip);
//          coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));
             coeffecientTextField.setValue(params.getSimilarityCutOff());
             similarityCutOffChanged = false; //reset for new Panel after .setValue(...) wrongly changed it to "true"

             JPanel coeffecientCutOffPanel = new JPanel();
             coeffecientCutOffPanel.setLayout(new BorderLayout());
             coeffecientCutOffPanel.setToolTipText(coeffecientCutOffTip);

             coeffecientCutOffPanel.add(index_buttons,BorderLayout.WEST);
             coeffecientCutOffPanel.add(coeffecientCutOffLabel, BorderLayout.CENTER);
             coeffecientCutOffPanel.add(coeffecientTextField, BorderLayout.EAST);

              //add the components to the panel
              panel.add(pvalueCutOffPanel);
              panel.add(qvalueCutOffPanel);
              //panel.add(coeffecientCutOffPanel);

              collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
              collapsiblePanel.getContentPane().add(coeffecientCutOffPanel, BorderLayout.SOUTH);
              return collapsiblePanel;
          }

     /**
     * Handles setting for the text field parameters that are numbers.
     * Makes sure that the numbers make sense.
     */
     private class FormattedTextFieldAction implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            JFormattedTextField source = (JFormattedTextField) e.getSource();

            String message = "The value you have entered is invalid.\n";
            boolean invalid = false;

            if (source == pvalueTextField) {
                Number value = (Number) pvalueTextField.getValue();
                if ((value != null) && (value.doubleValue() > 0.0) && (value.doubleValue() <= 1)) {
                    params.setPvalue(value.doubleValue());
                } else {
                    source.setValue(params.getPvalue());
                    message += "The pvalue cutoff must be greater than or equal 0 and less than or equal to 1.";
                    invalid = true;
                }
            } else if (source == qvalueTextField) {
                Number value = (Number) qvalueTextField.getValue();
                if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 100.0)) {
                    params.setQvalue(value.doubleValue());
                } else {
                    source.setValue(params.getQvalue());
                    message += "The FDR q-value cutoff must be between 0 and 100.";
                    invalid = true;
                }
            }else if (source == coeffecientTextField) {
                Number value = (Number) coeffecientTextField.getValue();
                if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
                    params.setSimilarityCutOff(value.doubleValue());
                    similarityCutOffChanged = true;
                } else {
                    source.setValue(params.getSimilarityCutOff());
                    message += "The Overlap/Jaccard Coeffecient cutoff must be between 0 and 1.";
                    invalid = true;
                }
            }else if(source == lowerLimitField){
                Number value = (Number) lowerLimitField.getValue();
                if ((value != null) && (value.intValue() >= 0)) {
                    params.setLowerlimit(value.intValue());
                } else {
                    source.setValue("1");
                    message += "The lower limit must be greater than 0.";
                    invalid = true;
                }
            }else if(source == upperLimitField){
                Number value = (Number) upperLimitField.getValue();
                if ((value != null) && (value.intValue() >= 0)) {
                    params.setUpperlimit(value.intValue());
                } else {
                    source.setValue("1");
                    message += "The upper limit must be greater than 0.";
                    invalid = true;
                }
            }else if (source == GSEAResultsDirTextField) {
                String value = GSEAResultsDirTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGSEAResultsDirName(value);
                else if(GSEAResultsDirTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GSEAResultsDirTextField.setForeground(checkFile(value));
                }
               else
            	   params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }else if (source == GMTDirectoryTextField) {
                String value = GMTDirectoryTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGMTDirName(value);
                else if(GMTDirectoryTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"Directory name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GMTDirectoryTextField.setForeground(checkFile(value));
                }
               else
            	   params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }else if (source == GCTDirectoryTextField) {
                String value = GCTDirectoryTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGCTDirName(value);
                else if(GCTDirectoryTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"Directory name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GCTDirectoryTextField.setForeground(checkFile(value));
                }
               else
            	   params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }

            if (invalid) {
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Check to see if the file is readable.  returns a color indicating whether the file is readable.  Color is red
     * if the file is not readable so we can set the font color to red to show the user the file name was invalid.
     *
     * @param filename - name of file to checked
     * @return Color, red if the file is not readable and black if it is.
     */
       public Color checkFile(String filename){
           //check to see if the files exist and are readable.
           //if the file is unreadable change the color of the font to red
           //otherwise the font should be black.
           if(filename != null){
               File tempfile = new File(filename);
               if(!tempfile.canRead())
                   return Color.RED;
           }
           return Color.BLACK;
       }

    /**
        * An rpt file can be entered instead of a GCT/expression file, or any of the enrichment results files
        * If an rpt file is specified all the fields in the dataset (expression file, enrichment results files, rank files,
        * phenotypes and class files) are populated.
        *
        * @param rptFile - rpt (GSEA analysis parameters file) file name
        *
        */
      public void populateFieldsFromRpt(File rptFile){

           TextFileReader reader = new TextFileReader(rptFile.getAbsolutePath());
           reader.read();
           String fullText = reader.getText();

           //Create a hashmap to contain all the values in the rpt file.
           HashMap<String, String> rpt = new HashMap<String, String>();

           String [] lines = fullText.split("\n");

           for (int i = 0; i < lines.length; i++) {
               String line = lines[i];
               String[] tokens = line.split("\t");
               //there should be two values on each line of the rpt file.
               if(tokens.length == 2 )
                   rpt.put(tokens[0] ,tokens[1]);
               else if (tokens.length == 3)
                   rpt.put(tokens[0] + " "+ tokens[1],tokens[2]);
           }

            //set all the variables based on the parameters in the rpt file
           //parameters needed
           String timestamp = (String)rpt.get("producer_timestamp");               // timestamp produced by GSEA
           String method = (String)rpt.get("producer_class");
           method = method.split("\\p{Punct}")[2];                                 // Gsea or GseaPreranked
           String out_dir = (String)rpt.get("param out");                          // output dir in which the GSEA-Jobdirs are supposed to be created
           String job_dir_name = null;                                             // name of the GSEA Job dir (excluding  out_dir + File.separator )
           String data = (String)rpt.get("param res");
           String label = (String)rpt.get("param rpt_label");
           String classes = (String)rpt.get("param cls");
           String gmt = (String)rpt.get("param gmx");
           String gmt_nopath =  gmt.substring(gmt.lastIndexOf(File.separator)+1, gmt.length()-1);
           String gseaHtmlReportFile = (String)rpt.get("file");

           String phenotype1 = "na";
           String phenotype2 = "na";
           //phenotypes are specified after # in the parameter cls and are separated by _versus_
           //but phenotypes are only specified for classic GSEA, not PreRanked.
           if(classes != null && method.equalsIgnoreCase("Gsea")){
               String[] classes_split = classes.split("#");
               String phenotypes = classes_split[1];
               String[] phenotypes_split = phenotypes.split("_versus_");
               phenotype1 = phenotypes_split[0];
               phenotype2 = phenotypes_split[1];

               params.getDatasetFiles().get(EnrichmentMap.DATASET1).setClassFile(classes_split[0]);
               params.setDataset1Phenotype1(phenotype1);
               params.setDataset1Phenotype2(phenotype2);


           }

           //check to see if the method is normal or pre-ranked GSEA.
           //If it is pre-ranked the data file is contained in a different field
           else if(method.equalsIgnoreCase("GseaPreranked")){
               data = (String)rpt.get("param rnk");
               phenotype1 = "na_pos";
               phenotype2 = "na_neg";
               params.setDataset1Phenotype1(phenotype1);
               params.setDataset1Phenotype2(phenotype2);

           }

           else{
               JOptionPane.showMessageDialog(this,"The class field in the rpt file has been modified or doesn't specify a class file\n but the analysis is a classic GSEA not PreRanked.  ");
           }

           //check to see if the rpt file path is the same as the one specified in the
           //rpt file.
           //if it isn't then assume that the rpt file has the right file names but if the files specified in the rpt
           //don't exist then use the path for the rpt to change the file paths.
           String results1 = "";
           String results2 = "";
           String ranks = "";

           //files built directly from the rpt specification
           //try these files first
           job_dir_name = label + "."+ method + "." + timestamp;
           results1 = "" + out_dir + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
           results2 = "" + out_dir + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
           ranks = "" + out_dir + File.separator + job_dir_name + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
           if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
               String out_dir_new = rptFile.getAbsolutePath();
               out_dir_new = out_dir_new.substring(0, out_dir_new.lastIndexOf(File.separator)); // drop rpt-filename
               out_dir_new = out_dir_new.substring(0, out_dir_new.lastIndexOf(File.separator)); // drop gsea report folder

               if( !(out_dir_new.equalsIgnoreCase(out_dir)) ){

//                    //trim the last File Separator
//                    String new_dir = rptFile.getAbsolutePath().substring(0,rptFile.getAbsolutePath().lastIndexOf(File.separator));
                       results1 = out_dir_new + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                       results2 = out_dir_new + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                       ranks = out_dir_new + File.separator + job_dir_name + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";

                       //If after trying the directory that the rpt file is in doesn't produce valid file names, revert to what
                       //is specified in the rpt.
                       if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
                           results1 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                           results2 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                           ranks = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
                       }
                       else{
                           out_dir = out_dir_new;
                           gseaHtmlReportFile = "" + out_dir + File.separator + job_dir_name + File.separator + "index.html";
                       }
               }

           }

        //check to see if the user supplied a directory for the gmt file
        if(params.getGMTDirName() != null){
            File temp = new File(gmt);
            //get the file name
            String filename = temp.getName();
            gmt = params.getGMTDirName() + File.separator + filename ;
        }
        if(params.getGCTDirName() != null){
            File temp = new File(data);
            //get the file name
            String filename = temp.getName();
            data = params.getGCTDirName() + File.separator + filename ;
        }

           //ranks, results file will be in the same directory as the rpt file
          //it is possible that the data and the gmt file are in different directories
          //than the one specified in the rpt file if the user has moved their results and files around
           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(gmt);
           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setExpressionFileName(data);
           params.setData(true);
           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setRankedFile(ranks);

           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(results1);
           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName2(results2);
           params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGseaHtmlReportFile(gseaHtmlReportFile);

       }

//Action listeners for buttons in input panel

    private void selectOneMultiSessionActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("onesession")){
            sessions = false;

        }
     else if(evt.getActionCommand().equalsIgnoreCase("multisession")){
            sessions = true;
        }
    }


    /**
     * jaccard or overlap radio button action listener
     *
     * @param evt
     */
    private void selectJaccardOrOverlapActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("jaccard")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff( params.getDefaultJaccardCutOff() );
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
            }
        }
     else if(evt.getActionCommand().equalsIgnoreCase("overlap")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
            if ( ! similarityCutOffChanged  ) {
                params.setSimilarityCutOff(params.getDefaultOverlapCutOff());
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
          }
        }
        else if(evt.getActionCommand().equalsIgnoreCase("combined")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff((params.getDefaultOverlapCutOff() * params.getCombinedConstant()) + ((1-params.getCombinedConstant()) * params.getDefaultJaccardCutOff()) );
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
          }
        }
     else{
            JOptionPane.showMessageDialog(this,"Invalid Jaccard Radio Button action command");
        }
    }

    /**
     * gene set (gmt) file selector action listener
     *
     * @param evt
     */
     private void selectGMTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

           // Get the file name
           File file = FileUtil.getFile("Import GSEA results directory", FileUtil.LOAD);
           if(file != null) {
               GSEAResultsDirTextField.setText(file.getParent());
               params.setGSEAResultsDirName(file.getParent());
           }
       }

     /**
     * gene set (gmt) file selector action listener
     *
     * @param evt
     */
     private void selectGMTDirButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

           // Get the file name
           File file = FileUtil.getFile("GMT directory", FileUtil.LOAD);
           if(file != null) {
               GMTDirectoryTextField.setText(file.getParent());
               params.setGMTDirName(file.getParent());
           }
       }


     /**
     * gene set (gmt) file selector action listener
     *
     * @param evt
     */
     private void selectGCTDirButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

           // Get the file name
           File file = FileUtil.getFile("GCT directory", FileUtil.LOAD);
           if(file != null) {
               GCTDirectoryTextField.setText(file.getParent());
               params.setGCTDirName(file.getParent());
           }
       }

    /**
         * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
         *
         * @return a flow layout panel containing the build map and cancel buttons
         */
        private JPanel createBottomPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            JButton closeButton = new JButton();
            JButton importButton = new JButton();

            JButton resetButton = new JButton ("Reset");
               resetButton.addActionListener(new java.awt.event.ActionListener() {
                                          public void actionPerformed(java.awt.event.ActionEvent evt) {
                                              resetPanel();
                                          }
                      });

            closeButton.setText("Close");
            closeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    cancelButtonActionPerformed(evt);
                }
            });

            importButton.setText("Build");
            importButton.addActionListener(new BuildBulkEnrichmentMapActionListener(this));
            importButton.setEnabled(true);

            panel.add(resetButton);
            panel.add(closeButton);
            panel.add(importButton);

            return panel;
        }

        private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
            // set the input window to null in the instance
            EnrichmentMapManager.getInstance().setInputWindow(null);
            cytoPanel.remove(this);
        }

        public void close() {
            CytoscapeDesktop desktop = Cytoscape.getDesktop();
            CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
            // set the input window to null in the instance
            EnrichmentMapManager.getInstance().setInputWindow(null);
            cytoPanel.remove(this);
        }
    /**
         *  Clear the current panel and clear the params associated with this panel
         */
        private void resetPanel(){

            this.params = new EnrichmentMapParameters();

            GSEAResultsDirTextField.setText("");
            GSEAResultsDirTextField.setToolTipText(null);

            pvalueTextField.setText(Double.toString(params.getPvalue()));
            qvalueTextField.setText(Double.toString(params.getQvalue()));
            coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));

            pvalueTextField.setValue(params.getPvalue());
            qvalueTextField.setValue(params.getQvalue());
            coeffecientTextField.setValue(params.getSimilarityCutOff());
            //reset for cleared Panel after .setValue(...) wrongly changed it to "true"
            similarityCutOffChanged = false;

             if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){
            jaccard.setSelected(true);
            overlap.setSelected(false);
            combined.setSelected(false);
        }
        else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
            jaccard.setSelected(false);
            overlap.setSelected(true);
            combined.setSelected(false);
        }  else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)){
            jaccard.setSelected(false);
            overlap.setSelected(false);
            combined.setSelected(true);
        }
        }

    public EnrichmentMapParameters getParams() {
        return params;
    }

    public void setParams(EnrichmentMapParameters params) {
        this.params = params;
    }

    private JPanel createAnalysisTypePanel() {


        JPanel panel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.setBorder(BorderFactory.createTitledBorder("Analysis Type"));

        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
             gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, true);
             generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
             //david = new JRadioButton(EnrichmentMapParameters.method_DAVID, false);
        }else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
             gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
             generic = new JRadioButton(EnrichmentMapParameters.method_generic, true);
             //david = new JRadioButton(EnrichmentMapParameters.method_DAVID, false);
        }else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID)){
             gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
             generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
             //david = new JRadioButton(EnrichmentMapParameters.method_DAVID, true);
        }

        gsea.setActionCommand(EnrichmentMapParameters.method_GSEA);
        generic.setActionCommand(EnrichmentMapParameters.method_generic);
        //david.setActionCommand(EnrichmentMapParameters.method_DAVID);

        gsea.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                selectAnalysisTypeActionPerformed(evt);
                            }
        });
        generic.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                selectAnalysisTypeActionPerformed(evt);
                            }
        });
        /*david.addActionListener(new java.awt.event.ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent evt) {
                                selectAnalysisTypeActionPerformed(evt);
                            }
        });*/
        ButtonGroup analysisOptions = new ButtonGroup();
        analysisOptions.add(gsea);
        analysisOptions.add(generic);
        //analysisOptions.add(david);


        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 0;
        gridbag.setConstraints(gsea, c);
        panel.add(gsea);
        c.gridy = 1;
        gridbag.setConstraints(generic, c);
        panel.add(generic);
        /*c.gridy = 2;
        gridbag.setConstraints(david, c);
        panel.add(david);
        */
       JPanel topPanel = new JPanel();
       topPanel.setLayout(new BorderLayout());
       topPanel.add(panel, BorderLayout.CENTER);

        return topPanel;
    }

     /**
     * Change the analysis type (either GSEA or Generic)
     * When the analysis type is changed the interface needs to be cleared and updated.
     *
     * @param evt
     */
  private void selectAnalysisTypeActionPerformed(ActionEvent evt){
      String analysisType = evt.getActionCommand();

      if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
          params.setMethod(EnrichmentMapParameters.method_GSEA);
      else if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_generic))
          params.setMethod(EnrichmentMapParameters.method_generic);
      /*else if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
          params.setMethod(EnrichmentMapParameters.method_DAVID);
      */
  }

    public boolean isSessions() {
        return sessions;
    }
}
