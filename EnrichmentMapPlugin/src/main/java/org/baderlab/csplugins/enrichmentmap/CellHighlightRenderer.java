package org.baderlab.csplugins.enrichmentmap;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;

/**
 * Created by
 * User: risserlin
 * Date: Mar 23, 2010
 * Time: 2:57:09 PM
 */
public class CellHighlightRenderer extends DefaultTableCellRenderer{


        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                                Object value, boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            JLabel label;

            if(value != null){
                label = new JLabel(value.toString());

                if(value instanceof SignificantGene){
                    label.setBackground(Color.YELLOW);
                    label.setOpaque(true);
                    label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.BOLD, (UIManager.getFont("TableHeader.font")).getSize()+2));
                }
                else{
                    label.setBackground(this.getBackground());
                    label.setForeground(UIManager.getColor("TableHeader.foreground"));
                    label.setFont(UIManager.getFont("TableHeader.font"));
                }
            }
            else{
                label = new JLabel();
            }

            return label;
        }


}
