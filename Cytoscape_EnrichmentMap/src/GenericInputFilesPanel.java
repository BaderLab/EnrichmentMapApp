
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.data.readers.TextFileReader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 1:12:45 PM
 */
public class GenericInputFilesPanel extends JDialog {
    /*--------------------------------------------------------------
       FIELDS.
       --------------------------------------------------------------*/

      private boolean status = false;

       /* components of the dialog */
       private javax.swing.JButton cancelButton;
       private javax.swing.JButton importButton;

       //Genesets file related components
       private javax.swing.JTextField GMTFileNameTextField;

       private javax.swing.JTextField GCTFileName1TextField;
       private javax.swing.JTextField GCTFileName2TextField;

       private javax.swing.JTextField Dataset1FileNameTextField;
       private javax.swing.JTextField Dataset1FileName2TextField;

       private javax.swing.JTextField Dataset2FileNameTextField;
       private javax.swing.JTextField Dataset2FileName2TextField;

       private javax.swing.JTextField pvalueTextField;
       private javax.swing.JTextField qvalueTextField;
       private javax.swing.JTextField jaccardTextField;

       private boolean Dataset1FileSelected = false;
       private boolean GMTFileSelected = false;
       private boolean GCTFileSelected = false;

       private EnrichmentMapParameters params;

    public static String title = "Import Generic Enrichment Result files to calculate enrichment maps";
    public static String dataset1_title = "Dataset 1";
    public static String dataset_instruction = "Please select the Generic result file for first dataset...";
    public static String dataset2_title = "Dataset 2 (OPTIONAL)";
    public static String gct_instruction = "Please select the dataset (.gct) or (.rpt) file used for GSEA analysis...";
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String pvalue_instruction = "P-value cut-off (Only genesets with p-value less than this value will be included)";
    public static String qvalue_instruction = "FDR Q-value cut-off (Only genesets with fdr q-value less than this value will be included)";
    public static String pvalue_default = "0.05";
    public static String qvalue_default = "0.25";
    public static String jaccard_default = "0.50";


    public GenericInputFilesPanel(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        params = new EnrichmentMapParameters();
        params.setGSEA(false);
        initComponents();
        status = false;
        pack();
    }

    public GenericInputFilesPanel(java.awt.Frame parent, boolean modal,boolean child) {
           super(parent, modal);
           status = false;
           pack();
           params = new EnrichmentMapParameters();
       }


    public void initComponents() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(5,5,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int current_row = 0;

        current_row = initTitleComponent(gridbag, c, current_row, title);
        current_row = initGMTComponent(gridbag, c,current_row);

        current_row = initEnrichmentFilesComponent(gridbag,c,current_row);
        current_row = initCutoffsComponent(gridbag, c,current_row);
        current_row = initActionButtonsComponent(gridbag,c,current_row);

    }

    public int initEnrichmentFilesComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){



       javax.swing.JButton selectDataset1FileButton;
       javax.swing.JButton selectDataset2FileButton;


        Dataset1FileNameTextField = new javax.swing.JTextField();
        Dataset2FileNameTextField = new javax.swing.JTextField();

        selectDataset1FileButton = new javax.swing.JButton();
        selectDataset2FileButton = new javax.swing.JButton();


        //Add title for Dataset1
        current_row = initTitleComponent(gridbag, c, current_row,dataset1_title);

        //Add the first GCT file load fields panel
        current_row = initGCTComponent(gridbag,c,current_row);

        //components needed for the GSEA Dataset1 Results
        Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileNameTextField.setText(dataset_instruction);

        selectDataset1FileButton.setText("Select");
        selectDataset1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1FileButtonActionPerformed(evt);
                    }
                });
         current_row = addTextButtonRow(gridbag,c,current_row, Dataset1FileNameTextField, selectDataset1FileButton);

        current_row = addSeparator(gridbag,c,current_row);

        //Add title for Dataset1
        current_row = initTitleComponent(gridbag, c, current_row,dataset2_title);


         //Add the first GCT file load fields panel
        current_row = initGCT2Component(gridbag,c,current_row);

        //components needed for the GSEA Dataset1 Results
        Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileNameTextField.setText(dataset_instruction);

        selectDataset2FileButton.setText("Select");
        selectDataset2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });


        current_row = addTextButtonRow(gridbag,c,current_row, Dataset2FileNameTextField, selectDataset2FileButton);

        current_row = addSeparator(gridbag, c,current_row);

       return current_row;

    }

    public int initTitleComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row,String title){
        javax.swing.JLabel titleLabel;
        titleLabel = new javax.swing.JLabel();
        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText(title);

        c.gridx = 0;
        c.gridy = current_row;
        c.gridwidth = 3;
        gridbag.setConstraints(titleLabel, c);
        add(titleLabel);
        current_row++;

        return current_row;
    }

    public int initActionButtonsComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){

        cancelButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        importButton.setText("Build Enrichment Map");
        importButton.addActionListener(new BuildEnrichmentMapActionListener(this, params));
        importButton.setEnabled(false);


        //put in the cancel and import buttons.
        c.gridx = 1;
        c.gridy = current_row;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(cancelButton, c);
        add(cancelButton);
        c.gridx = 3;
        gridbag.setConstraints(importButton, c);
        add(importButton);

        return current_row;

    }



    public int initGCTComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
       javax.swing.JButton selectGCTFileButton;

       GCTFileName1TextField = new javax.swing.JTextField();
       selectGCTFileButton = new javax.swing.JButton();

       //components needed for the GCT file load
       GCTFileName1TextField.setFont(new java.awt.Font("Dialog",1,12));
       GCTFileName1TextField.setText(gct_instruction);

       selectGCTFileButton.setText("Select");
       selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButtonActionPerformed(evt);
                    }
                });

        return addTextButtonRow(gridbag,c,current_row, GCTFileName1TextField, selectGCTFileButton);

    }


    public int initGCT2Component(GridBagLayout gridbag, GridBagConstraints c, int current_row){
       javax.swing.JButton selectGCTFileButton;

       GCTFileName2TextField = new javax.swing.JTextField();
       selectGCTFileButton = new javax.swing.JButton();

       //components needed for the GCT file load
       GCTFileName2TextField.setFont(new java.awt.Font("Dialog",1,12));
       GCTFileName2TextField.setText("OPTIONAL:" + gct_instruction);

       selectGCTFileButton.setText("Select");
       selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButton2ActionPerformed(evt);
                    }
                });

        return addTextButtonRow(gridbag,c,current_row, GCTFileName2TextField, selectGCTFileButton);

    }


    public int initGMTComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
        javax.swing.JButton selectGMTFileButton;

        GMTFileNameTextField = new javax.swing.JTextField();
        selectGMTFileButton = new javax.swing.JButton();


        //components needed for the GMT file load
        GMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        GMTFileNameTextField.setText(gmt_instruction);

        selectGMTFileButton.setText("Select");
        selectGMTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGMTFileButtonActionPerformed(evt);
                    }
                });


        return addTextButtonRow(gridbag,c,current_row, GMTFileNameTextField, selectGMTFileButton);

    }

    public int addTextButtonRow(GridBagLayout gridbag, GridBagConstraints c, int current_row, JTextField text, JButton button){

        //put in the fields for the genesets file
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(text, c);
        add(text);
        c.gridx = 3;
        gridbag.setConstraints(button, c);
        add(button);
        current_row++;
        return current_row;

    }

    public int addSeparator(GridBagLayout gridbag, GridBagConstraints c, int current_row){
        JSeparator sep = new JSeparator();

        c.gridx = 0;
        c.gridwidth = 4;
        c.gridy = current_row;

        gridbag.setConstraints(sep, c);
        add(sep);
        current_row++;
         return current_row;
    }

/*Section of the input panel that specifies the p-value, fdr q-value, jaccard cutoffs
*/
    public int initCutoffsComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){

       javax.swing.JRadioButton jaccard;
       javax.swing.JRadioButton overlap;
       javax.swing.ButtonGroup jaccardOrOverlap;


        javax.swing.JLabel pvalueLabel;
        javax.swing.JLabel qvalueLabel;
        javax.swing.JLabel jaccardLabel;

        pvalueTextField = new javax.swing.JTextField();
        qvalueTextField = new javax.swing.JTextField();
        jaccardTextField = new javax.swing.JTextField();

        pvalueLabel = new javax.swing.JLabel();
        qvalueLabel = new javax.swing.JLabel();
        jaccardLabel = new javax.swing.JLabel();

        jaccard = new javax.swing.JRadioButton("Jaccard Coeffecient");
        jaccard.setActionCommand("jaccard");
        jaccard.setSelected(true);
        overlap = new javax.swing.JRadioButton("Overlap Coeffecient");
        overlap.setActionCommand("overlap");
        jaccardOrOverlap = new javax.swing.ButtonGroup();
        jaccardOrOverlap.add(jaccard);
        jaccardOrOverlap.add(overlap);

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


        //put the fields to set p-value
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        pvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        pvalueLabel.setText(pvalue_instruction);
        pvalueTextField.setText(pvalue_default);
        gridbag.setConstraints(pvalueLabel, c);
        add(pvalueLabel);
        c.gridx = 3;
        gridbag.setConstraints(pvalueTextField, c);
        add(pvalueTextField);
        current_row++;

        //put the fields to set q-value
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        qvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        qvalueLabel.setText(qvalue_instruction);
        qvalueTextField.setText(qvalue_default);
        gridbag.setConstraints(qvalueLabel, c);
        add(qvalueLabel);
        c.gridx = 3;
        gridbag.setConstraints(qvalueTextField, c);
        add(qvalueTextField);
        current_row++;


        //put the fields to set jaccard coeffecient
        c.gridx = 0;
        c.gridwidth = 1;
        c.gridy = current_row;
        jaccardLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        jaccardLabel.setText("Cut-off");
        jaccardTextField.setText(jaccard_default);
        gridbag.setConstraints(jaccard,c);
        add(jaccard);
        c.gridx = 1;
        gridbag.setConstraints(overlap,c);
        add(overlap);
        c.gridx = 2;
        gridbag.setConstraints(jaccardLabel, c);
        add(jaccardLabel);
        c.gridx = 3;
        gridbag.setConstraints(jaccardTextField, c);
        add(jaccardTextField);
        current_row++;

        return current_row;

    }


    public double getPvalue() {
        try{
            return  Double.parseDouble(pvalueTextField.getText());
        } catch (NumberFormatException nfe) {
            return -1;
        }

    }

    public double getQvalue() {
        try{
            return  Double.parseDouble(qvalueTextField.getText());
        }catch (NumberFormatException nfe) {
            return -1;
        }
    }

    public double getJaccard() {
        try{
            return  Double.parseDouble(jaccardTextField.getText());
        } catch (NumberFormatException nfe) {
            return -1;
        }

    }



    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
           // TODO add your handling code here:
           status = false;
            this.dispose();
       }

    public void close() {
            // TODO add your handling code here:
            status = false;
             this.dispose();
        }


 private void selectJaccardOrOverlapActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("jaccard")){
            params.setJaccard(true);
        }
     else if(evt.getActionCommand().equalsIgnoreCase("overlap")){
            params.setJaccard(false);
        }
     else{
            JOptionPane.showMessageDialog(this,"Invalid Jaccard Radio Button action command");
        }
 }



       private void selectGMTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("gmt");
           filter.setDescription("All GMT files");

           // Get the file name
           File file = FileUtil.getFile("Import GMT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               GMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
               GMTFileNameTextField.setText(file.getAbsolutePath());
               params.setGMTFileName(file.getAbsolutePath());
               GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
               GMTFileSelected = true;
                if (Dataset1FileSelected && GCTFileSelected  ){
                    importButton.setEnabled(true);
               }
           }
       }

         private void selectGCTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("gct");
           filter.addExtension("rpt");
           filter.addExtension("txt");
           filter.setDescription("All GCT files");

           // Get the file name
           File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {

               if(file.getPath().contains(".rpt")){
                   //The file loaded is an rpt file --> populate the fields based on the
                   populateFieldsFromRpt(file,true);
                   Dataset1FileSelected = true;
                   GMTFileSelected = true;
               }
               else{
                    GCTFileName1TextField.setForeground(checkFile(file.getAbsolutePath()));
                    GCTFileName1TextField.setText(file.getAbsolutePath());
                    params.setGCTFileName1(file.getAbsolutePath());
                    GCTFileName1TextField.setToolTipText(file.getAbsolutePath());
               }
               GCTFileSelected = true;
               params.setData(true);
                if (Dataset1FileSelected && GMTFileSelected   ){
                    importButton.setEnabled(true);
               }
           }
       }

    private void selectGCTFileButton2ActionPerformed(
          java.awt.event.ActionEvent evt) {

//         Create FileFilter
      CyFileFilter filter = new CyFileFilter();

      // Add accepted File Extensions
      filter.addExtension("gct");
      filter.addExtension("txt");
      filter.addExtension("rpt");
      filter.setDescription("All GCT files");

      // Get the file name
      File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                   new CyFileFilter[] { filter });
      if(file != null) {
          if(file.getPath().contains(".rpt")){
                   //The file loaded is an rpt file --> populate the fields based on the
                   populateFieldsFromRpt(file,false);
                   params.setTwoDatasets(true);
               }
          else{
            GCTFileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
            GCTFileName2TextField.setText(file.getAbsolutePath());
            params.setGCTFileName2(file.getAbsolutePath());
            GCTFileName2TextField.setToolTipText(file.getAbsolutePath());
          }
          params.setData2(true);
      }
  }


        private void selectDataset1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("xls");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import dataset 1 result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                Dataset1FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileNameTextField.setText(file.getName() );
                params.setEnrichmentDataset1FileName1(file.getAbsolutePath());
                Dataset1FileNameTextField.setToolTipText(file.getAbsolutePath() );

                if ( GMTFileSelected ){
                     importButton.setEnabled(true);
                }
                Dataset1FileSelected = true;

        }
    }



   private void selectDataset2FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("xls");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import dataset 2 result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                 Dataset2FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                 Dataset2FileNameTextField.setText(file.getName() );
                 params.setEnrichmentDataset2FileName1(file.getAbsolutePath());
                 Dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
                 params.setTwoDatasets(true);
                 if ( GMTFileSelected && Dataset1FileSelected){
                     importButton.setEnabled(true);
                 }

        }
  }

    private void populateFieldsFromRpt(File file, boolean dataset1){

        TextFileReader reader = new TextFileReader(file.getAbsolutePath());
        reader.read();
        String fullText = reader.getText();

        //Create a hashmap to contain all the values in the rpt file.
        HashMap rpt = new HashMap();

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
        String timestamp = (String)rpt.get("producer_timestamp");
        String out_dir = (String)rpt.get("param out");
        String data = (String)rpt.get("param res");
        String label = (String)rpt.get("param rpt_label");
        String classes = (String)rpt.get("param cls");
        String gmt = (String)rpt.get("param gmx");
        String gmt_nopath =  gmt.substring(gmt.lastIndexOf(File.separator)+1, gmt.length()-1);

        //phenotypes are specified after # in the parameter cls and are separated by _versus_
        String[] classes_split = classes.split("#");
        String phenotypes = classes_split[1];
        String[] phenotypes_split = phenotypes.split("_versus_");
        String phenotype1 = phenotypes_split[0];
        String phenotype2 = phenotypes_split[1];



       if(dataset1){
            params.setClassFile1(classes_split[0]);
            params.setDataset1Phenotype1(phenotype1);
            params.setDataset1Phenotype2(phenotype2);
       }
       else{
            params.setClassFile2(classes_split[0]);
            params.setDataset2Phenotype1(phenotype1);
            params.setDataset2Phenotype2(phenotype2);
       }

        String results1 = "" + out_dir + File.separator + label + ".Gsea." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
        String results2 = "" + out_dir + File.separator + label + ".Gsea." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";

        if(dataset1){

            //check to see if the gmt file has already been set.  if it has then
            //make sure that this file is the same as the one from the other dataset.
            if((!GMTFileNameTextField.getText().equalsIgnoreCase(gmt) && !GMTFileNameTextField.getText().contains(gmt_nopath)) &&
                    !GMTFileNameTextField.getText().equalsIgnoreCase(gmt_instruction))
                JOptionPane.showMessageDialog(this,"The gmt files between the two analyses do not match.\n  To compare two analyses the geneset files for the two analyses must be the same.");
            //only change the text if it hasn't been set yet.
            else if(GMTFileNameTextField.getText().equalsIgnoreCase(gmt_instruction)){
                //check to see the file exists and can be read
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                params.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
            }
                GCTFileName1TextField.setForeground(checkFile(data));
                GCTFileName1TextField.setText(data);
                params.setGCTFileName1(data);
                GCTFileName1TextField.setToolTipText(data);

                params.setEnrichmentDataset1FileName1(results1);
                params.setEnrichmentDataset1FileName2(results2);
                this.setDatasetnames(results1,results2,dataset1);

        }
        else{
            //check to see if the gmt file has already been set.  if it has then
            //make sure that this file is the same as the one from the other dataset.
             if((!GMTFileNameTextField.getText().equalsIgnoreCase(gmt) && !GMTFileNameTextField.getText().contains(gmt_nopath)) &&
                    !GMTFileNameTextField.getText().equalsIgnoreCase(gmt_instruction))
                JOptionPane.showMessageDialog(this,"The gmt files between the two analyses do not match.\n  To compare two analyses the geneset files for the two analyses must be the same.");
             //only change the text if it hasn't been set yet.
             else if(GMTFileNameTextField.getText().equalsIgnoreCase(gmt_instruction)){
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                params.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
             }
                GCTFileName2TextField.setForeground(checkFile(data));
                GCTFileName2TextField.setText(data);
                params.setGCTFileName2(data);
                GCTFileName2TextField.setToolTipText(data);

                params.setEnrichmentDataset2FileName1(results1);
                params.setEnrichmentDataset2FileName2(results2);
                this.setDatasetnames(results1,results2,dataset1);

        }
    }


    protected void setDatasetnames(String file1, String file2, boolean dataset1){

        if(dataset1){
            Dataset1FileNameTextField.setForeground(checkFile(file1));
            Dataset1FileNameTextField.setText(file1 );
            Dataset1FileNameTextField.setToolTipText(file1 );

            Dataset1FileName2TextField.setForeground(checkFile(file2));
            Dataset1FileName2TextField.setText(file2 );
            Dataset1FileName2TextField.setToolTipText(file2 );
        }
        else{
            Dataset2FileNameTextField.setForeground(checkFile(file1));
            Dataset2FileNameTextField.setText(file1 );
            Dataset2FileNameTextField.setToolTipText(file1 );

            Dataset2FileName2TextField.setForeground(checkFile(file2));
            Dataset2FileName2TextField.setText(file2 );
            Dataset2FileName2TextField.setToolTipText(file2 );
        }
    }

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

    public void enableImport(){
        importButton.setEnabled(true);
    }

    public void setGSEADataset1FileName1(String name){
        params.setEnrichmentDataset1FileName1(name);
    }

    public void setGSEADataset1FileName2(String name){
        params.setEnrichmentDataset1FileName2(name);
    }


    public void setGSEADataset2FileName1(String name){
        params.setEnrichmentDataset2FileName1(name);
    }

    public void setGSEADataset2FileName2(String name){
        params.setEnrichmentDataset2FileName2(name);
    }

    public void setTwoDatasets(boolean datasets){
        params.setTwoDatasets(datasets);
    }

    public boolean isGCTFileSelected() {
        return GCTFileSelected;
    }

    public void setGCTFileSelected(boolean GCTFileSelected) {
        this.GCTFileSelected = GCTFileSelected;
    }

    public boolean isGMTFileSelected() {
        return GMTFileSelected;
    }

    public void setGMTFileSelected(boolean GMTFileSelected) {
        this.GMTFileSelected = GMTFileSelected;
    }

    public boolean isDataset1FileSelected() {
        return Dataset1FileSelected;
    }

    public void setDataset1FileSelected(boolean dataset1FileSelected) {
        Dataset1FileSelected = dataset1FileSelected;
    }
}
