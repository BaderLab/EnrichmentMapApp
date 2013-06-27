package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;


public class RawExpressionValueRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Component getTableCellRendererComponent(JTable table,
                                                            Object value, boolean isSelected, boolean hasFocus,
                                                            int row, int column) {
        Object result = null;
        JLabel label = null;
        
        Number numberValue;
        Color colorValue;
        

        	if (( value != null) && (value instanceof ExpressionTableValue)) {
        		  ExpressionTableValue avalue = (ExpressionTableValue)value;

        		  numberValue = (Number)avalue.getExpression_value();
        	      DecimalFormat formatter = new DecimalFormat("###.##");
        	      result = formatter.format(numberValue.doubleValue());
        	      
        	      label = new JLabel(result.toString());
        	      
        	      colorValue = avalue.getExpression_color();
        	      
        	      label.setBackground(colorValue);
        	      label.setOpaque(true);
        	      label.setFont(new Font((UIManager.getFont("TableHeader.font")).getName(), Font.PLAIN, (UIManager.getFont("TableHeader.font")).getSize()-2));
        	      label.setHorizontalAlignment(SwingConstants.RIGHT);
        	      
        	      label.setToolTipText("Exp value: " + numberValue);
        	    }
        	
        return label;
    }

}
