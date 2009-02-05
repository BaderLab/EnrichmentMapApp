import cytoscape.view.CyNetworkView;
import cytoscape.data.annotation.Annotation;
import cytoscape.data.annotation.Ontology;
import cytoscape.CyNetwork;
import cytoscape.Cytoscape;

import javax.swing.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import giny.view.GraphViewChangeListener;
import giny.view.GraphViewChangeEvent;
import giny.model.Edge;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:15 AM
 */
public class OverlappingGenesFrame  extends javax.swing.JFrame  {

    private final JTabbedPane jTabbedPane;
     private boolean bingoLaunched=false;
     private int resultPanelCount =0;

     private HashMap network_Options=new HashMap();

     private Color layoutColor = new java.awt.Color(51,255,51);
     private Color noLayoutColor = Color.WHITE;

    private EnrichmentMapParameters params;
     //private CyNetworkView networkView ;

 public OverlappingGenesFrame( EnrichmentMapParameters params){

     this.setTitle("Overlapping Genes Viewer");
     //this.networkView = networkView;
     this.jTabbedPane=new javax.swing.JTabbedPane();
     this.params = params;
     initComponents();

 }



 private void initComponents() {

     //JPanel northPanel = new JPanel();
     Toolkit kit = Toolkit.getDefaultToolkit();
     Dimension screenSize = kit.getScreenSize();

     getJTabbedPane().setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
     setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     getContentPane().add(getJTabbedPane(), java.awt.BorderLayout.CENTER);

     pack();

     this.addWindowListener(new WindowAdapter(){

     public void windowClosing(WindowEvent e){

         }
     });

     this.setLocation(25,screenSize.height-450);
     this.setSize(screenSize.width-50,400);

     setVisible(true);
     setResizable(true);
     this.validate();

     this.repaint();

   }

 public void createResultTab(String edgeName,
                             GeneExpressionMatrix matrix,
                             HashMap currentOverlappingSet, GenesetSimilarity similarity){

     OverlappingGenesPanel result=new OverlappingGenesPanel(matrix, currentOverlappingSet,
                                       this, similarity);


     if (getResultPanelCount()!=0)
         result.setTabName(edgeName);

     getJTabbedPane().addTab(edgeName,result);
     getJTabbedPane().setSelectedIndex(getJTabbedPane().getTabCount()-1);

     result.validate();
     this.validate();

     resultPanelCount++;

 }

    public void createResultTab(String edgeName,
                                GeneExpressionMatrix matrix,
                                HashMap currentOverlappingSet, String label){

        OverlappingGenesPanel result=new OverlappingGenesPanel(matrix, currentOverlappingSet,
                                          this,label);


        if (getResultPanelCount()!=0)
            result.setTabName(edgeName);

        getJTabbedPane().addTab(edgeName,result);
        getJTabbedPane().setSelectedIndex(getJTabbedPane().getTabCount()-1);

        result.validate();
        this.validate();

        resultPanelCount++;

    }


 void removeTab(OverlappingGenesPanel result){
     for (int i =1;i<getResultPanelCount()+1;i++){
         if ((OverlappingGenesPanel)getResultPanelAt(i)==result){
             getJTabbedPane().removeTabAt(i-1);
             break;
         }

     }
     resultPanelCount--;
 }

 JTable getResultTableAt (int i){
     return ((OverlappingGenesPanel) getJTabbedPane().getComponentAt(i-1)).jTable1;
 }

OverlappingGenesPanel getResultPanelAt (int i){
     return (OverlappingGenesPanel)getJTabbedPane().getComponentAt(i-1);
 }

OverlappingGenesPanel getResultTabAt (int i){
     return (OverlappingGenesPanel)getJTabbedPane().getComponentAt(i);
 }

 public HashMap getNetwork_Options() {
     return network_Options;
 }

 public void setNetwork_Options(HashMap network_Options) {
     this.network_Options = network_Options;
 }

 public JTabbedPane getJTabbedPane() {
     return jTabbedPane;
 }

 public int getResultPanelCount() {
     return resultPanelCount;
 }

 public Color getLayoutColor() {
     return layoutColor;
 }

 public Color getNoLayoutColor() {
     return noLayoutColor;
 }


}
