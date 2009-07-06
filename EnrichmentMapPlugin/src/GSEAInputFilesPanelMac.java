import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

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

public class GSEAInputFilesPanelMac extends GenericInputFilesPanel {


    /*--------------------------------------------------------------
    FIELDS.
    --------------------------------------------------------------*/

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 350;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 350;

    private boolean status = false;

    private javax.swing.JTextField Dataset1FileNameTextField;
    private javax.swing.JButton selectDataset1FileButton;
    private javax.swing.JTextField Dataset1FileName2TextField;
    private javax.swing.JButton selectDataset1File2Button;

    private javax.swing.JTextField Dataset2FileNameTextField;
    private javax.swing.JButton selectDataset2FileButton;
    private javax.swing.JTextField Dataset2FileName2TextField;
    private javax.swing.JButton selectDataset2File2Button;




    /** Creates new form AttributeMatrixImportDialog */
    public GSEAInputFilesPanelMac(java.awt.Frame parent, boolean modal) {
        super(parent, modal,true);
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

        current_row = initTitleComponent(gridbag, c, current_row,"Import GSEA Result files used to calculate enrichment maps");
        current_row = initGMTComponent(gridbag, c, current_row);
        current_row = initEnrichmentFilesComponent(gridbag, c, current_row);
        current_row = initCutoffsComponent(gridbag, c, current_row);
        current_row = initActionButtonsComponent(gridbag, c, current_row);


    }


    public int initEnrichmentFilesComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
         Dataset1FileNameTextField = new javax.swing.JTextField();
        Dataset1FileName2TextField = new javax.swing.JTextField();
        Dataset2FileNameTextField = new javax.swing.JTextField();
        Dataset2FileName2TextField = new javax.swing.JTextField();

         selectDataset1FileButton = new javax.swing.JButton();
        selectDataset1File2Button = new javax.swing.JButton();
        selectDataset2FileButton = new javax.swing.JButton();
        selectDataset2File2Button = new javax.swing.JButton();

        //Add title for Dataset1
        current_row = initTitleComponent(gridbag, c, current_row,dataset1_title);

        //Put in the first GCT file load
        current_row = initGCTComponent(gridbag, c, current_row);

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

        current_row = addTextButtonRow(gridbag,c,current_row,Dataset1FileNameTextField,selectDataset1FileButton);

        //components needed for the GSEA Dataset1 Results
        Dataset1FileName2TextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileName2TextField.setText(dataset_instruction);

        selectDataset1File2Button.setText("Select");
        selectDataset1File2Button
                      .addActionListener(new java.awt.event.ActionListener() {
                          public void actionPerformed(java.awt.event.ActionEvent evt) {
                              selectDataset1File2ButtonActionPerformed(evt);
                          }
                      });

        current_row = addTextButtonRow(gridbag,c,current_row,Dataset1FileName2TextField,selectDataset1File2Button);

        current_row = addSeparator(gridbag,c,current_row);

        //Add title for Dataset2
        current_row = initTitleComponent(gridbag, c, current_row,dataset2_title);

        //Put in the first GCT file load
        current_row = initGCT2Component(gridbag, c, current_row);

        //components needed for the GSEA Dataset1 Results
        Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileNameTextField.setText("(OPTIONAL) "+ dataset_instruction);

        selectDataset2FileButton.setText("Select");
        selectDataset2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });

        current_row = addTextButtonRow(gridbag,c,current_row,Dataset2FileNameTextField,selectDataset2FileButton);

        Dataset2FileName2TextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileName2TextField.setText("(OPTIONAL)"+ dataset_instruction);

        selectDataset2File2Button.setText("Select");
        selectDataset2File2Button
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2File2ButtonActionPerformed(evt);
                    }
                });


        current_row = addTextButtonRow(gridbag,c,current_row,Dataset2FileName2TextField,selectDataset2File2Button);

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
         File file = FileUtil.getFile("import GSEA dataset 1 result file 1", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                Dataset1FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileNameTextField.setText(file.getAbsolutePath() );
                setGSEADataset1FileName1(file.getAbsolutePath());
                Dataset1FileNameTextField.setToolTipText(file.getAbsolutePath() );

                if ( isGMTFileSelected() && isGCTFileSelected() && isDataset1FileSelected()){
                     enableImport();
                }
                setDataset1FileSelected(true);

        }
    }


    private void selectDataset1File2ButtonActionPerformed(
            java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("xls");
        filter.setDescription("All GSEA result files");

        // Get the file name
         File file = FileUtil.getFile("import GSEA dataset 1 result file 2", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {

                Dataset1FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileName2TextField.setText(file.getAbsolutePath() );

                setGSEADataset1FileName2(file.getAbsolutePath());
                Dataset1FileName2TextField.setToolTipText(file.getAbsolutePath() );

                if ( isGMTFileSelected() && isGCTFileSelected() && isDataset1FileSelected()){
                   enableImport();
                }
                setDataset1FileSelected(true);

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
                 Dataset2FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                 Dataset2FileNameTextField.setText(file.getAbsolutePath() );
                 setGSEADataset2FileName1(file.getAbsolutePath());
                 Dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
                 if ( isGMTFileSelected() && isGCTFileSelected() && isDataset1FileSelected()){
                     enableImport();
                 }

        }
  }
    private void selectDataset2File2ButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("xls");
        filter.setDescription("All GSEA result files");

        // Get the file name
         File file = FileUtil.getFile("import GSEA dataset 2 result file 2", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                 Dataset2FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
                 Dataset2FileName2TextField.setText(file.getAbsolutePath() );
                 setGSEADataset2FileName2(file.getAbsolutePath());
                 Dataset2FileName2TextField.setToolTipText(file.getAbsolutePath() );
                 if ( isGMTFileSelected() && isGCTFileSelected() && isDataset1FileSelected()){
                     enableImport();
                 }
                 setTwoDatasets(true);

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

    public boolean checkResultsFiles(){

          if(!getGSEADataset1FileName1().equalsIgnoreCase( Dataset1FileNameTextField.getText())){
            int answer = JOptionPane.showConfirmDialog(this,"The Dataset 1 Filename has been modified from the original one loaded.  Would you like to use the original file specified??","File changed",JOptionPane.YES_NO_OPTION) ;
            if(!(answer == JOptionPane.YES_OPTION))
                if(checkFile( Dataset1FileNameTextField.getText()) == Color.RED){
                    JOptionPane.showMessageDialog(this, "The file specified in the Dataset 1 file 1 does not exist.");
                    return false;
                }
                else
                    setGSEADataset1FileName1(Dataset1FileNameTextField.getText());
         }

        if(!getGSEADataset1FileName2().equalsIgnoreCase( Dataset1FileName2TextField.getText())){
            int answer = JOptionPane.showConfirmDialog(this,"The Dataset 1 Filename 2 has been modified from the original one loaded.  Would you like to use the original file specified??","File changed",JOptionPane.YES_NO_OPTION);
            if(!(answer == JOptionPane.YES_OPTION))
                if(checkFile( Dataset1FileName2TextField.getText()) == Color.RED){
                    JOptionPane.showMessageDialog(this, "The file specified in the Dataset 1 file 2 does not exist.");
                return false;
                }
                else
                    setGSEADataset1FileName2(Dataset1FileName2TextField.getText());
         }

        if(isTwoDatasets()){
            if(!getGSEADataset2FileName1().equalsIgnoreCase( Dataset2FileNameTextField.getText())){
                int answer = JOptionPane.showConfirmDialog(this,"The Dataset 2 Filename has been modified from the original one loaded.  Would you like to use the original file specified??","File changed",JOptionPane.YES_NO_OPTION);
                if(!(answer == JOptionPane.YES_OPTION))
                    if(checkFile( Dataset2FileNameTextField.getText()) == Color.RED){
                        JOptionPane.showMessageDialog(this, "The file specified in the Dataset 2 file 1 does not exist.");
                        return false;
                    }
                    else
                        setGSEADataset2FileName1(Dataset2FileNameTextField.getText());
            }

            if(!getGSEADataset2FileName2().equalsIgnoreCase( Dataset2FileName2TextField.getText())){
                int answer = JOptionPane.showConfirmDialog(this,"The Dataset 2 Filename 2 has been modified from the original one loaded.  Would you like to use the original file specified??","File changed",JOptionPane.YES_NO_OPTION);
                if(!(answer == JOptionPane.YES_OPTION))
                    if(checkFile( Dataset2FileName2TextField.getText()) == Color.RED){
                        JOptionPane.showMessageDialog(this, "The file specified in the Dataset 2 file 2 does not exist.");
                        return false;
                    }
                    else
                        setGSEADataset2FileName2(Dataset2FileName2TextField.getText());
           }
        }
        return true;

    }

}
