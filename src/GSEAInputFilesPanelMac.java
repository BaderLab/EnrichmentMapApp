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

        current_row = initTitleComponent(gridbag, c, current_row);
        current_row = initGMTComponent(gridbag, c, current_row);
        current_row = initGCTComponent(gridbag, c, current_row);
        current_row = initEnrichmentFilesComponent(gridbag, c, current_row);
        current_row = initCutoffsComponent(gridbag, c, current_row);
        current_row = initActionButtonsComponent(gridbag, c, current_row);


    }



  public int initTitleComponent(GridBagLayout gridbag, GridBagConstraints c, int current_row){
        javax.swing.JLabel titleLabel;
        titleLabel = new javax.swing.JLabel();
        titleLabel.setFont(new java.awt.Font("Dialog", 1, 14));
        titleLabel.setText("Import GSEA Result files used to calculate enrichment maps");


        c.gridx = 0;
        c.gridy = current_row;
        c.gridwidth = 3;
        gridbag.setConstraints(titleLabel, c);
        add(titleLabel);
        current_row++;

        return current_row;
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

        //components needed for the GSEA Dataset1 Results
        Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileNameTextField.setText("Please select the gsea result file 1 for first dataset...");

        selectDataset1FileButton.setText("Select");
        selectDataset1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1FileButtonActionPerformed(evt);
                    }
                });

        //components needed for the GSEA Dataset1 Results
        Dataset1FileName2TextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset1FileName2TextField.setText("Please select the gsea result file 2 for first dataset...");

        selectDataset1File2Button.setText("Select");
        selectDataset1File2Button
                      .addActionListener(new java.awt.event.ActionListener() {
                          public void actionPerformed(java.awt.event.ActionEvent evt) {
                              selectDataset1File2ButtonActionPerformed(evt);
                          }
                      });


        //components needed for the GSEA Dataset1 Results
        Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileNameTextField.setText("(OPTIONAL) Please select the gsea result file 1 for second dataset...");

        selectDataset2FileButton.setText("Select");
        selectDataset2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });

        Dataset2FileName2TextField.setFont(new java.awt.Font("Dialog",1,12));
        Dataset2FileName2TextField.setText("(OPTIONAL) Please select the gsea result file 2 for second dataset...");

        selectDataset2File2Button.setText("Select");
        selectDataset2File2Button
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2File2ButtonActionPerformed(evt);
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

        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset1FileName2TextField, c);
        add(Dataset1FileName2TextField);
        c.gridx = 3;
        gridbag.setConstraints(selectDataset1File2Button, c);
        add(selectDataset1File2Button);
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

        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = current_row;
        gridbag.setConstraints(Dataset2FileName2TextField, c);
        add(Dataset2FileName2TextField);
        c.gridx = 3;
        gridbag.setConstraints(selectDataset2File2Button, c);
        add(selectDataset2File2Button);
        current_row++;

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

                Dataset1FileNameTextField.setText(file.getName() );
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


                Dataset1FileName2TextField.setText(file.getName() );

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

                 Dataset2FileNameTextField.setText(file.getName() );
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

                 Dataset2FileName2TextField.setText(file.getName() );
                 setGSEADataset2FileName2(file.getAbsolutePath());
                 Dataset2FileName2TextField.setToolTipText(file.getAbsolutePath() );
                 if ( isGMTFileSelected() && isGCTFileSelected() && isDataset1FileSelected()){
                     enableImport();
                 }
                 setTwoDatasets(true);

        }
    }
}
