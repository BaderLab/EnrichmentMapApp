import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.task.util.TaskManager;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.data.readers.TextFileReader;

import javax.swing.*;
import java.io.File;
import java.awt.*;
import java.util.HashMap;

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

public class GSEAInputFilesPanel extends GenericInputFilesPanel {


    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 250;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 350;

    private boolean status = false;


    private javax.swing.JTextArea Dataset1FileNameTextField;
    private javax.swing.JButton selectDataset1FileButton;

    private javax.swing.JTextArea Dataset2FileNameTextField;
    private javax.swing.JButton selectDataset2FileButton;




    public GSEAInputFilesPanel(java.awt.Frame parent, boolean modal) {
        super(parent, modal,false);
        initComponents();
	    status = false;
	    pack();
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

        current_row = initTitleComponent(gridbag,c, current_row,"Import GSEA Result files used to calculate enrichment maps");
        current_row = initGMTComponent(gridbag, c, current_row);

        current_row = initEnrichmentFilesComponent(gridbag, c, current_row);
        current_row = initCutoffsComponent(gridbag, c, current_row);
        current_row = initActionButtonsComponent(gridbag, c, current_row);


    }

   public int initEnrichmentFilesComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){

       Dataset1FileNameTextField = new javax.swing.JTextArea();
       Dataset2FileNameTextField = new javax.swing.JTextArea();

       selectDataset1FileButton = new javax.swing.JButton();
       selectDataset2FileButton = new javax.swing.JButton();

    //Add title for Dataset1
       current_row = initTitleComponent(gridbag, c, current_row,dataset1_title);


       //add the first data file fields
       current_row = initGCTComponent(gridbag, c, current_row);

       //components needed for the GSEA Dataset1 Results
       Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
       Dataset1FileNameTextField.setText(dataset_instruction);
       Dataset1FileNameTextField.setLineWrap(true);
       Dataset1FileNameTextField.setRows(4);
       Dataset1FileNameTextField.setText("\n\n\n\n");

       selectDataset1FileButton.setText("Select");
       selectDataset1FileButton
               .addActionListener(new java.awt.event.ActionListener() {
                   public void actionPerformed(java.awt.event.ActionEvent evt) {
                       selectDataset1FileButtonActionPerformed(evt);
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

       current_row = addSeparator(gridbag,c,current_row);

        //Add title for Dataset1
       current_row = initTitleComponent(gridbag, c, current_row,dataset2_title);

       //add the second data file fields
       current_row = initGCT2Component(gridbag, c, current_row);


       //components needed for the GSEA Dataset1 Results
       Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
       Dataset2FileNameTextField.setText(dataset_instruction);
       Dataset2FileNameTextField.setLineWrap(true);
       Dataset2FileNameTextField.setRows(4);
       Dataset2FileNameTextField.setText("\n\n\n\n");

       selectDataset2FileButton.setText("Select");
       selectDataset2FileButton
               .addActionListener(new java.awt.event.ActionListener() {
                   public void actionPerformed(java.awt.event.ActionEvent evt) {
                       selectDataset2FileButtonActionPerformed(evt);
                   }
               });

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

       current_row = addSeparator(gridbag,c,current_row);

       return current_row;
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
            File files[] = FileUtil.getFiles("import GSEA dataset 1 result files", FileUtil.LOAD, new CyFileFilter[]{ filter });

           if(files != null) {
               //There should be two files inputted.  If it more or less then report error
               if(files.length == 2){
                    Dataset1FileNameTextField.setText(files[0].getName() + "\n" + files[1].getName());
                    setGSEADataset1FileName1(files[0].getAbsolutePath());
                    setGSEADataset1FileName2(files[1].getAbsolutePath());
                    Dataset1FileNameTextField.setToolTipText(files[0].getAbsolutePath() + "," + files[1].getAbsolutePath());
                    setDataset1FileSelected(true);
                    if ( isGMTFileSelected() && isGCTFileSelected()){
                        enableImport();
                    }
               }
               else{
                   JOptionPane.showMessageDialog(this,"Expecting two files to be selected.");
               }
           }
       }


        private void selectDataset2FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

            // Add accepted File Extensions
           filter.addExtension("txt");
           filter.addExtension("xls");
           filter.setDescription("All GSEA results files");

           // Get the file name
           File files[] = FileUtil.getFiles("Import GSEA Dataset 2 result Files", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(files != null) {
               if(files.length == 2){
                    Dataset2FileNameTextField.setText(files[0].getName() + "\n" + files[1].getName());
                    setGSEADataset2FileName1(files[0].getAbsolutePath());
                    setGSEADataset2FileName2(files[1].getAbsolutePath());
                    setTwoDatasets(true);
                    Dataset2FileNameTextField.setToolTipText(files[0].getAbsolutePath() + "," + files[1].getAbsolutePath());
                    if (isDataset1FileSelected() &&  isGMTFileSelected() && isGCTFileSelected()){
                        enableImport();
                    }
               }
                else{
                   JOptionPane.showMessageDialog(this,"Expecting two files to be selected.");
               }
           }
       }


    protected void setDatasetnames(String file1, String file2, boolean dataset1){

        if(dataset1){

            Dataset1FileNameTextField.setText(file1 + "\n" + file2);
            Dataset1FileNameTextField.setToolTipText(file1 + "\n" + file2 );
        }
        else{
            Dataset2FileNameTextField.setText(file1 + "\n" + file2 );
            Dataset2FileNameTextField.setToolTipText(file1 + "\n" + file2 );
        }
    }
}
