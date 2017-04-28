/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.cytoscape.util.swing.LookAndFeelUtil.getErrorColor;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SliderBarPanel extends JPanel {

	private final int S_MIN;
	private final int S_MAX;
	private final int S_RANGE;
	private final int PRECISION;
	
	private JSlider slider;
	private JFormattedTextField textField;
	
	private final double min, max;
	private double value;
	
	private final String title;
	
	private final List<Object> listeners;
	private final DecimalFormat format;
	private boolean ignore;
	
	public SliderBarPanel(double min, double max, double value) {
		this(min, max, value, null);
	}
	
	public SliderBarPanel(double min, double max, double value, String title) {
		this.min = min;
		this.max = max;
		this.value = value;
		this.title = title;
		listeners = new ArrayList<>();
		
		String pattern = getFormatPattern(min, max, value);
		format = new DecimalFormat(pattern);
		
		PRECISION = precision(min, max, value);
		S_MIN = (int) (min * Math.pow(10, PRECISION + 1));
		S_MAX = (int) (max * Math.pow(10, PRECISION + 1));
		S_RANGE = S_MAX - S_MIN;
		
		init();
	}

	@SuppressWarnings("unchecked")
	protected void init() {
		JLabel titleLabel = new JLabel(title);
		titleLabel.setVisible(title != null && !title.trim().isEmpty());

		makeSmall(titleLabel, getSlider(), getTextField());
		
		final GroupLayout layout = new GroupLayout(this);
       	this.setLayout(layout);
   		layout.setAutoCreateContainerGaps(false);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
   				.addComponent(titleLabel)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(getSlider(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(titleLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
   						.addComponent(getSlider(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(getTextField(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		// Change the slider's label sizes -- only works if it's done after the slider has been added to
		// its parent container and had its UI assigned
		final Font tickFont = getSlider().getFont().deriveFont(getSmallFontSize());
		final Dictionary<Integer, JLabel> labelTable = getSlider().getLabelTable();
		
		for (Enumeration<Integer> enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
			int k = enumeration.nextElement();
			final JLabel label = labelTable.get(k);
			label.setFont(tickFont); // Updates the font size
			label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
		}
		
		revalidate();
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getValue(){
		return value;
	}

	public void setValue(double value) {
		ignore = true;
		this.value = value;
		setSliderValue();
		setFieldValue();
		ignore = false;
	}
	
	public JFormattedTextField getTextField() {
		if (textField == null) {
			textField = new JFormattedTextField(format) {
				@Override
				public Dimension getPreferredSize() {
					final Dimension d = super.getPreferredSize();
					
					if (this.getGraphics() != null) {
						// Set the preferred text field size after it gets a Graphics
						int sw = 16 + this.getGraphics().getFontMetrics().stringWidth(format.format(max));
						d.width = Math.max(sw, 48);
					}
					
					return d;
				}
			};
			
			textField.setHorizontalAlignment(JTextField.RIGHT);
			
			textField.addActionListener(evt -> {
				textFieldValueChanged();
			});
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					textFieldValueChanged();
				}
			});
		}
		
		return textField;
	}
	
	JSlider getSlider() {
		if (slider == null) {
			slider = new JSlider(S_MIN, S_MAX);
			final Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
			final double range = max - min;
			final int originalPrecision = PRECISION - 1;
			final int intRange = (int) (range * Math.pow(10, originalPrecision));
			
			Optional<Integer> result = Arrays.asList(new Integer[]{ 5, 4, 3, 2 }).stream()
					.filter(n -> intRange % n == 0)
					.findFirst();
			
			int n = result.isPresent() ? result.get() : 10;
			slider.setMajorTickSpacing(S_RANGE / n);
			
			if (n <= 5) {
				if (S_RANGE / (n * n) > 0)
					slider.setMinorTickSpacing(S_RANGE / (n * n));
				else
					slider.setMinorTickSpacing(S_RANGE / (n * 2));
			}
			
			labelTable.put(S_MIN, new JLabel(format.format(min)));
			
			if ((S_MIN + S_RANGE / 2) % 2 == 0)
				labelTable.put(S_MIN + S_RANGE / 2, new JLabel(format.format(min + range / 2)));
			
			labelTable.put(S_MAX, new JLabel(format.format(max)));
			
			slider.setLabelTable(labelTable);
			slider.setPaintTicks(true);
			slider.setPaintLabels(true);
			setSliderValue();
			setFieldValue();
			
			slider.addChangeListener((ChangeEvent e) -> {
				if (ignore)
					return;
				
				ignore = true;
				
				// update the value
				double val = getSlider().getValue();
				val = Math.min(val, S_MAX);
				val = Math.max(val, S_MIN);
				val = min + (val - S_MIN) * (max - min) / (double) S_RANGE;

				// Due to small inaccuracies in the slider position, it's possible
				// to get values less than the min or greater than the max.  If so,
				// just adjust the value and don't issue a warning.
				value = clamp(val);

				// set text field value
				setFieldValue();
				// fire event
				fireChangeEvent();
				ignore = false;
			});
		}
		
		return slider;
	}
	
	private void setSliderValue() {
		int val = S_MIN + (int) Math.round(((S_MAX - S_MIN) * (value - min)) / (max - min));
		getSlider().setValue(val);
	}
  
	private double getFieldValue(){
		Double val = null;
		Number n = format.parse(getTextField().getText(), new ParsePosition(0));
		final Color errColor = getErrorColor();
		
		if (n == null) {
			try {
				val = Double.valueOf(getTextField().getText());
			} catch (NumberFormatException nfe) {
				getTextField().setForeground(errColor);
				JOptionPane.showMessageDialog(
						null,
						"Please enter a valid number.",
						"Invalid Number",
						JOptionPane.ERROR_MESSAGE);
				setFieldValue();
				getTextField().setForeground(UIManager.getColor("TextField.foreground"));
				val = value;
			}
		} else {
			val = n.doubleValue();
		}
		
		if (val < min) {
			getTextField().setForeground(errColor);
			JOptionPane.showMessageDialog(
					null,
					"Value ("+val.doubleValue()+") is less than lower limit ("+format.format(min)+").",
					"Invalid Number",
					JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			getTextField().setForeground(UIManager.getColor("TextField.foreground"));
			
			return value;
		}
		
		if (val > max) {
			getTextField().setForeground(errColor);
			JOptionPane.showMessageDialog(
					null,
					"Value ("+val.doubleValue()+") is more than upper limit ("+format.format(max)+").",
					"Invalid Number",
					JOptionPane.ERROR_MESSAGE);
			setFieldValue();
			getTextField().setForeground(UIManager.getColor("TextField.foreground"));
			
			return value;
		}
		
		return val.doubleValue();
	}
	
	private void setFieldValue() {
		getTextField().setValue(value);
	}

	private double clamp(double value) {
		value = Math.min(value, max);
		value = Math.max(value, min);
		
		return value;
	}
	
	public void addChangeListener(ChangeListener cl) {
		if (!listeners.contains(cl))
			listeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		listeners.remove(cl);
	}
	
	protected void fireChangeEvent() {
		Iterator<Object> iter = listeners.iterator();
		ChangeEvent evt = new ChangeEvent(this);
		
		while (iter.hasNext()) {
			ChangeListener cl = (ChangeListener) iter.next();
			cl.stateChanged(evt);
		}
	}
	
	private void textFieldValueChanged() {
		if (ignore)
			return;
		
		ignore = true;
		double v = getFieldValue();
		
		if (v != value) {
			// update the value
			value = v;
			// set slider value
			setSliderValue();
			// fire event
			fireChangeEvent();
		}
		
		ignore = false;
	}
	
	private static String getFormatPattern(double... numbers) {
		StringBuilder sb = new StringBuilder("0.0"); // At least one decimal.
		int p = precision(numbers);
		
		if (p > 0)
			IntStream.range(0, p).forEach(v -> sb.append("#")); // Optional decimal digits
		
		return sb.toString();
	}
	
	private static int precision(double... numbers) {
		DecimalFormat df = new DecimalFormat("0.##############################");
		int p = 0;
		
		for (double n : numbers) {
			String text = df.format(n);
			
			if (text.indexOf('.') >= 0)
				p = Math.max(p, text.substring(text.indexOf('.')).length() - 1);
		}
		
		return p;
	}
}
