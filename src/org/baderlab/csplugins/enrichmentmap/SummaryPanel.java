package org.baderlab.csplugins.enrichmentmap;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import giny.model.Node;
import giny.model.Edge;

/**
 * Created by
 * User: risserlin
 * Date: Feb 6, 2009
 * Time: 1:38:22 PM
 */
public class SummaryPanel extends JPanel {

    private JTextPane textPane;


    public SummaryPanel() {
        this.setLayout(new java.awt.BorderLayout());


        //information about the current selected edges
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBorder(new EmptyBorder(5,5,5,5));
        textPane.setContentType("text/html");
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        JScrollPane jScrollPane = new javax.swing.JScrollPane(textPane);

        this.add(jScrollPane);

    }

    public void updateNodeInfo(Object[] nodes){

        String genesets = "<html> <h1>Genesets:\n</h1>";

        for(int i = 0; i<nodes.length;i++){
              genesets = genesets  + (i+1) +". " + ((Node)nodes[i]).getIdentifier() + "<br>";
        }

        genesets = genesets + "</html>";
        textPane.setText(genesets);

        this.revalidate();
    }

    public void updateEdgeInfo(Object[] edges){

        String genesets = "<html> <h1>Genesets Overlaps:\n</h1>";

        for(int i = 0; i<edges.length;i++){
            genesets = genesets +  (i+1) +". " + ((Edge)edges[i]).getIdentifier() + "\n<br>";

        }

        genesets = genesets + "</html>";
        textPane.setText(genesets);

        this.revalidate();
    }

    public void clearInfo(){
        textPane.setText("");
        this.revalidate();
    }
}
