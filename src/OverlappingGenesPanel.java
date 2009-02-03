import cytoscape.CyNetwork;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.view.CyNetworkView;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:32 AM
 */
public class OverlappingGenesPanel extends JPanel {

        javax.swing.JTable jTable1;
        private JScrollPane jScrollPane;

        private GeneExpressionMatrix expression;
        private HashMap currentExpressionSet;
        private GenesetSimilarity similarity;
        private JPanel jPanelDeBase;

        private Object[][] data;
        private Cursor hand;

        private Object[] columnNames;

        OverlappingGenesFrame parentFrame;

        private String tabName;

    /**
     * Creates a new instance of OverlappingGenesPanel
     */

   public OverlappingGenesPanel( GeneExpressionMatrix expression,
                                 HashMap currentExpressionSet,
                                 OverlappingGenesFrame parentFrame, GenesetSimilarity similarity) {

        this.hand = new Cursor(Cursor.HAND_CURSOR);
        this.parentFrame=parentFrame;
        this.expression = expression;
        this.currentExpressionSet = currentExpressionSet;
        this.similarity = similarity;
        initComponents();

    }


     void initComponents() {
                columnNames = makeHeadersForJTable();
                data = makeDataForJTable();

                jPanelDeBase = new javax.swing.JPanel();
                this.setLayout(new java.awt.BorderLayout());
                jPanelDeBase.setLayout(new BorderLayout());


                Toolkit kit = Toolkit.getDefaultToolkit();
                Dimension screenSize = kit.getScreenSize();

                JPanel jPanelButtons = new JPanel();

                JButton jCloseButton = new JButton("close");
                jCloseButton.addActionListener(new CloseOverlappingGenesPanelListener(this));
                jPanelButtons.add(jCloseButton);

                jPanelDeBase.add(jPanelButtons,java.awt.BorderLayout.SOUTH);

                //summary panel
                JPanel jPanelSummary = new JPanel();
                GridBagLayout gridbag = new GridBagLayout();
                GridBagConstraints c = new GridBagConstraints();
                jPanelSummary.setLayout(gridbag);
                JLabel geneset1Label = new JLabel("GeneSet 1: " + similarity.getGeneset1_Name());
                JLabel geneset2Label = new JLabel("GeneSet 2: " + similarity.getGeneset2_Name());
                JLabel overlapSize = new JLabel("Overlap Size : " + similarity.getSizeOfOverlap());
                c.gridy=0;
                c.gridx=0;
                gridbag.setConstraints(geneset1Label,c);
                jPanelSummary.add(geneset1Label);

                c.gridy=1;
                c.gridx=0;
                gridbag.setConstraints(geneset2Label,c);
                jPanelSummary.add(geneset2Label);

                c.gridy=2;
                c.gridx=0;
                gridbag.setConstraints(overlapSize,c);
                jPanelSummary.add(overlapSize);
                jPanelDeBase.add(jPanelSummary, java.awt.BorderLayout.NORTH);

                jTable1 = new JTable(new OverlappingGenesTableModel(columnNames,data));
                //jTable1.setPreferredScrollableViewportSize(new Dimension(500,100));

                 //Set up renderer and editor for the Color column.
                jTable1.setDefaultRenderer(Color.class,new ColorRenderer());

                TableColumnModel tcModel = jTable1.getColumnModel();


                jTable1.setDragEnabled(false);
                jTable1.setCellSelectionEnabled(true);
                for (int i=0;i<columnNames.length;i++){
                        if (i==0 || columnNames[i].equals("Name"))
                            tcModel.getColumn(i).setPreferredWidth(50);
                         else if (i==1 || columnNames[i].equals("Description"))
                            tcModel.getColumn(i).setPreferredWidth(50);
                         else
                           tcModel.getColumn(i).setMaxWidth(10);
                }

                jTable1.setColumnModel(tcModel);

                jScrollPane = new javax.swing.JScrollPane(jTable1);
                jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

                jPanelDeBase.add(jScrollPane);

                this.add(jPanelDeBase,java.awt.BorderLayout.CENTER);

               //parentFrame.synchroColor(this);
               //parentFrame.synchroSelections(this);


      }

    private Object[][] makeDataForJTable(){

        Object[][] data = new Object[currentExpressionSet.size()][expression.getNumConditions()];
        //Got through the hashmap and put all the values is

        double maxExpression = expression.getMaxExpression();
        double minExpression = expression.getMinExpression();

        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
		continuousMapping.setInterpolator(new LinearNumberToColorInterpolator());

        final Color minColor = new Color(0,0,255);
        final Color medColor = Color.WHITE;
        final Color maxColor = new Color(255,0,0);

		final BoundaryRangeValues bv0 = new BoundaryRangeValues(minColor, minColor, minColor);
		final BoundaryRangeValues bv1a = new BoundaryRangeValues(medColor, medColor, medColor);
		final BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);

        // add points to continuous mapper
		continuousMapping.addPoint(minExpression, bv1a);
        continuousMapping.addPoint(maxExpression, bv2);

        int k = 0;
        for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
            //Current expression row
            GeneExpression row = (GeneExpression)currentExpressionSet.get(i.next());
            Double[] expression_values = row.getExpression();
            String gene = row.getName();
            data[k][0] = row.getName();
            data[k][1] = row.getDescription();

            for(int j = 0; j < row.getExpression().length;j++){
                HashMap<String,Double> value =  new java.util.HashMap<String, Double>();
                value.put(row.getName(),expression_values[j]);

                cytoscape.visual.mappings.continuous.ContinuousRangeCalculator calculator =
                        new cytoscape.visual.mappings.continuous.ContinuousRangeCalculator(continuousMapping.getAllPoints(),
                                                                                       continuousMapping.getInterpolator(),
                                                                                        value);
                data[k][j+2] = (Color)calculator.calculateRangeValue(gene);
            }
            k++;
        }
        return data;
        }

    private Object[] makeHeadersForJTable(){
        Object[] columnNames = expression.getColumnNames();
        return columnNames;
    }

    public String getTabName() {
        return tabName;
    }

    public void setTabName(String tabName) {
        this.tabName = tabName;
    }
}


