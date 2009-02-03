
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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

       private javax.swing.JTextField GCTFileNameTextField;

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

        current_row = initTitleComponent(gridbag, c, current_row);
        current_row = initGMTComponent(gridbag, c,current_row);
        current_row = initGCTComponent(gridbag,c,current_row);
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


        //components needed for the GSEA Dataset1 Results
        Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileNameTextField.setText("Please select the Generic result file for first dataset...");

        selectDataset1FileButton.setText("Select");
        selectDataset1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1FileButtonActionPerformed(evt);
                    }
                });


        //components needed for the GSEA Dataset1 Results
        Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileNameTextField.setText("(OPTIONAL) Please select the Generic result file  for second dataset...");

        selectDataset2FileButton.setText("Select");
        selectDataset2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });



        //put in the fields for the GSEA UP results file
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset1FileNameTextField, c);
        add(Dataset1FileNameTextField);
        c.gridx = 3;
        gridbag.setConstraints(selectDataset1FileButton, c);
        add(selectDataset1FileButton);
        current_row++;


        //put in the fields for the GSEA DOWN results file.
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset2FileNameTextField, c);
        add(Dataset2FileNameTextField);
        c.gridx = 3;
        gridbag.setConstraints(selectDataset2FileButton, c);
        add(selectDataset2FileButton);
        current_row++;

       return current_row;

    }

    public int initTitleComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
        javax.swing.JLabel titleLabel;
        titleLabel = new javax.swing.JLabel();
        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText("Import Generic Enrichment files used to calculate enrichment maps");


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
        //importButton.addActionListener(new BuildEnrichmentMapActionListener( params));
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

       GCTFileNameTextField = new javax.swing.JTextField();
       selectGCTFileButton = new javax.swing.JButton();

       //components needed for the GMT file load
       GCTFileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
       GCTFileNameTextField.setText("Please select the dataset (.gct) file used for GSEA analysis...");

       selectGCTFileButton.setText("Select");
       selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButtonActionPerformed(evt);
                    }
                });

         //put in the fields for the gsea dataset file
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(GCTFileNameTextField, c);
        add(GCTFileNameTextField);
        c.gridx = 3;
        gridbag.setConstraints(selectGCTFileButton, c);
        add(selectGCTFileButton);
        current_row++;

        return current_row;

    }

    public int initGMTComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
        javax.swing.JButton selectGMTFileButton;

        GMTFileNameTextField = new javax.swing.JTextField();
        selectGMTFileButton = new javax.swing.JButton();


        //components needed for the GMT file load
        GMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        GMTFileNameTextField.setText("Please select a geneset (.gmt) file...");

        selectGMTFileButton.setText("Select");
        selectGMTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGMTFileButtonActionPerformed(evt);
                    }
                });


        //put in the fields for the genesets file
        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(GMTFileNameTextField, c);
        add(GMTFileNameTextField);
        c.gridx = 3;
        gridbag.setConstraints(selectGMTFileButton, c);
        add(selectGMTFileButton);
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
        pvalueLabel.setText("P-value cut-off (Only genesets with p-value less than this value will be included)");
        pvalueTextField.setText("0.05");
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
        qvalueLabel.setText("FDR Q-value cut-off (Only genesets with fdr q-value less than this value will be included)");
        qvalueTextField.setText("0.25");
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
        jaccardTextField.setText("0.50");
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
           filter.addExtension("txt");
           filter.setDescription("All GCT files");

           // Get the file name
           File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               GCTFileNameTextField.setText(file.getAbsolutePath());
               params.setGCTFileName(file.getAbsolutePath());
               GCTFileNameTextField.setToolTipText(file.getAbsolutePath());
               GCTFileSelected = true;
               params.setData(true);
                if (Dataset1FileSelected && GMTFileSelected   ){
                    importButton.setEnabled(true);
               }
           }
       }

        private void selectDataset1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("xls");
        filter.setDescription("All GSEA result files");

        // Get the file name
         File file = FileUtil.getFile("import GSEA dataset 1 result file 1", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {

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
        filter.setDescription("All GSEA result files");

        // Get the file name
         File file = FileUtil.getFile("import GSEA dataset 2 result file 1", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {

                 Dataset2FileNameTextField.setText(file.getName() );
                 params.setEnrichmentDataset2FileName1(file.getAbsolutePath());
                 Dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
                 params.setTwoDatasets(true);
                 if ( GMTFileSelected && Dataset1FileSelected){
                     importButton.setEnabled(true);
                 }

        }
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
