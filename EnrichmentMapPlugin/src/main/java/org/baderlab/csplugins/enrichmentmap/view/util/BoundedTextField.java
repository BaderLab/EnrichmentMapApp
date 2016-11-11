package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class BoundedTextField extends JTextField {
	
	private int value;
	private final int min, max;
	private final double precision;
	/** The number of decimals for given precision */
	private final int decPrecision;
	private final boolean smallNumber;
	
	private boolean validating;

	public BoundedTextField(final int value, final int min, final int max, double precision, int decPrecision,
			boolean smallNumber) {
		this.min = min;
		this.max = max;
		this.precision = precision;
		this.decPrecision = decPrecision;
		this.smallNumber = smallNumber;
		
		initUI();
		setValue(value);
	}

	public void setValue(int value) {
		if (this.value != value) {
			value = Math.max(value, min);
			value = Math.min(value, max);
			
			if (this.value != value) {
				int oldValue = this.value;
				this.value = value;
				updateText();
				
				firePropertyChange("value", oldValue, value);
			}
		}
	}
	
	protected double getPrecision() {
		if (smallNumber)
			return Math.pow(10, this.precision + this.decPrecision);
		else
			return precision;
	}
	
	private void initUI() {
		setHorizontalAlignment(JTextField.RIGHT);
		setColumns(8);
		
		addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				validateFieldText();
			}
		});
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validateFieldText();
			}
		});
	}

	private void validateFieldText() {
		if (validating)
			return;
		
		validating = true;
		int newValue;
		
		try {
			try {
				double dv = Double.parseDouble(getText());
				newValue = (int) (dv * getPrecision());
			} catch (NumberFormatException nfe) {
				setForeground(LookAndFeelUtil.getErrorColor());
				JOptionPane.showMessageDialog(null, "Please enter a valid number", "Alert", JOptionPane.ERROR_MESSAGE);
				updateText();
				setForeground(UIManager.getColor("TextField.foreground"));
	
				return;
			}
	
			if (newValue < min) {
				setForeground(LookAndFeelUtil.getErrorColor());
				JOptionPane.showMessageDialog(
						null,
						"Value is less than lower limit (" + format(min) + ").",
						"Alert",
						JOptionPane.ERROR_MESSAGE
				);
				updateText();
				setForeground(UIManager.getColor("TextField.foreground"));
			} else if (newValue > max) {
				setForeground(LookAndFeelUtil.getErrorColor());
				JOptionPane.showMessageDialog(
						null,
						"Value is larger than upper limit (" + format(max) + ").",
						"Alert",
						JOptionPane.ERROR_MESSAGE
				);
				updateText();
				setForeground(UIManager.getColor("TextField.foreground"));
			} else {
				setValue(newValue);
			}
		} finally {
			validating = false;
		}
	}
	
	private void updateText() {
		setText(format(value));
	}
	
	private String format(final int val) {
		final String txt;
    	
		if (smallNumber)
			txt = "" + (val / Math.pow(10, (decPrecision + precision)));
		else
			txt = String.format("%." + decPrecision + "f", (val / precision));
		
		return txt;
	}
}
