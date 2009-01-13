import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;

import javax.swing.*;
import java.io.File;
import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Jan 7, 2009
 * Time: 4:01:05 PM
 */

/**
 * Class that extends JPanel and takes care of the GUI-objects
 * Allows users to load in geneset GMT file, GSEA results for Up and DOWN genesets
 *
 * */

public class InputFilesPanel extends JDialog {


    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 200;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 350;

    private boolean status = false;

    /* components of the dialog */
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton importButton;

    //Genesets file related components
    private javax.swing.JTextField GMTFileNameTextField;
    private javax.swing.JButton selectGMTFileButton;

    private javax.swing.JTextField UPFileNameTextField;
    private javax.swing.JButton selectUPFileButton;

    private javax.swing.JTextField DOWNFileNameTextField;
    private javax.swing.JButton selectDOWNFileButton;

    private javax.swing.JTextField pvalueTextField;
    private javax.swing.JTextField qvalueTextField;
    private javax.swing.JTextField jaccardTextField; 

    private boolean UpFileSelected = false;
    private boolean DownFileSelected = false;
    private boolean GMTFileSelected = false;

    private EnrichmentMapParameters params;


    /** Creates new form AttributeMatrixImportDialog */
    public InputFilesPanel(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
	    status = false;
	    pack();
    }

    public void initComponents() {

        javax.swing.JLabel titleLabel;
        javax.swing.JLabel pvalueLabel;
        javax.swing.JLabel qvalueLabel;
        javax.swing.JLabel jaccardLabel;
        params = new EnrichmentMapParameters();

        titleLabel = new javax.swing.JLabel();
        pvalueLabel = new javax.swing.JLabel();
        qvalueLabel = new javax.swing.JLabel();
        jaccardLabel = new javax.swing.JLabel();

        //text boxes
        GMTFileNameTextField = new javax.swing.JTextField();
        UPFileNameTextField = new javax.swing.JTextField();
        DOWNFileNameTextField = new javax.swing.JTextField();
        pvalueTextField = new javax.swing.JTextField();
        qvalueTextField = new javax.swing.JTextField();
        jaccardTextField = new javax.swing.JTextField();

        //buttons
        selectGMTFileButton = new javax.swing.JButton();
        selectUPFileButton = new javax.swing.JButton();
        selectDOWNFileButton = new javax.swing.JButton();

        cancelButton = new javax.swing.JButton();
        importButton = new javax.swing.JButton();


        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText("Import GSEA files used to calculate enrichment maps");


        //components needed for the GMT file load
        GMTFileNameTextField.setText("Please select a geneset (.gmt) file...");


        selectGMTFileButton.setText("Select");
        selectGMTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGMTFileButtonActionPerformed(evt);
                    }
                });
        //components needed for the GSEA Up file
        UPFileNameTextField.setText("Please select a gsea UP regulated geneset result file...");

        selectUPFileButton.setText("Select");
        selectUPFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectUPFileButtonActionPerformed(evt);
                    }
                });


        //components needed for the GSEA Up file
        DOWNFileNameTextField.setText("Please select a gsea DOWN regulated geneset result file...");

        selectDOWNFileButton.setText("Select");
        selectDOWNFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDOWNFileButtonActionPerformed(evt);
                    }
                });



        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        importButton.setText("Build Enrichment Map");
        importButton.addActionListener(new BuildEnrichmentMapActionListener(this,params));
	    importButton.setEnabled(false);

        // Layout with GridBagLayout.
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        int current_row = 0;

        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(5,5,10,10);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(titleLabel, c);
        add(titleLabel);
        current_row++;

        //put in the fields for the genesets file
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(GMTFileNameTextField, c);
        add(GMTFileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectGMTFileButton, c);
        add(selectGMTFileButton);
        current_row++;

        //put in the fields for the GSEA UP results file
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(UPFileNameTextField, c);
        add(UPFileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectUPFileButton, c);
        add(selectUPFileButton);
        current_row++;

        //put in the fields for the GSEA DOWN results file.
        c.gridx = 0;
        c.gridy = current_row;
        gridbag.setConstraints(DOWNFileNameTextField, c);
        add(DOWNFileNameTextField);
        c.gridx = 1;
        gridbag.setConstraints(selectDOWNFileButton, c);
        add(selectDOWNFileButton);
        current_row++;

        //put the fields to set p-value
        c.gridx = 0;
        c.gridy = current_row;
        pvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        pvalueLabel.setText("P-value cut-off (Only genesets with p-value less than this value will be included)");
        pvalueTextField.setText("0.05");
        gridbag.setConstraints(pvalueLabel, c);
        add(pvalueLabel);
        c.gridx = 1;
        gridbag.setConstraints(pvalueTextField, c);
        add(pvalueTextField);
        current_row++;

        //put the fields to set q-value
        c.gridx = 0;
        c.gridy = current_row;
        qvalueLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        qvalueLabel.setText("FDR Q-value cut-off (Only genesets with fdr q-value less than this value will be included)");
        qvalueTextField.setText("0.25");
        gridbag.setConstraints(qvalueLabel, c);
        add(qvalueLabel);
        c.gridx = 1;
        gridbag.setConstraints(qvalueTextField, c);
        add(qvalueTextField);
        current_row++;


        //put the fields to set q-value
        c.gridx = 0;
        c.gridy = current_row;
        jaccardLabel.setFont(new java.awt.Font("Dialog", 1, 10));
        jaccardLabel.setText("Jaccard Coeffecient (only edges that exceed this cutoff will be included)");
        jaccardTextField.setText("0.50");
        gridbag.setConstraints(jaccardLabel, c);
        add(jaccardLabel);
        c.gridx = 1;
        gridbag.setConstraints(jaccardTextField, c);
        add(jaccardTextField);
        current_row++;

        //put in the cancel and import buttons.
        c.gridx = 0;
        c.gridy = current_row;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(cancelButton, c);
        add(cancelButton);
        c.gridx = 1;
        gridbag.setConstraints(importButton, c);
        add(importButton);


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
                if (UpFileSelected && DownFileSelected  ){
                    importButton.setEnabled(true);
               }
           }
       }

        private void selectUPFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.setDescription("All GSEA result files");

           // Get the file name
           File file = FileUtil.getFile("Import GSEA UP result File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               UPFileNameTextField.setText(file.getAbsolutePath());
               params.setGSEAUpFileName(file.getAbsolutePath());
               UPFileNameTextField.setToolTipText(file.getAbsolutePath());
               UpFileSelected = true;
               if ( DownFileSelected && GMTFileSelected ){
                importButton.setEnabled(true);
               }
           }
       }


        private void selectDOWNFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

            // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.setDescription("All GSEA results files");

           // Get the file name
           File file = FileUtil.getFile("Import GSEA DOWN result File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {
               DOWNFileNameTextField.setText(file.getAbsolutePath());
               params.setGSEADownFileName(file.getAbsolutePath());
               DOWNFileNameTextField.setToolTipText(file.getAbsolutePath());
               DownFileSelected = true;
               if (UpFileSelected &&  GMTFileSelected ){
                importButton.setEnabled(true);
               }
           }
       }
}
