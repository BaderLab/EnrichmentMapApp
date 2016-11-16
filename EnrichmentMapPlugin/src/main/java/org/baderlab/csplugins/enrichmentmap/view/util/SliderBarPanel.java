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
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;

import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class SliderBarPanel extends JPanel {

	private JLabel label = new JLabel();
	private JSlider slider;
	private BoundedTextField textField;
	
	// required services
	private CyApplicationManager applicationManager;
	private EnrichmentMapManager emManager;

	// min and max values for the slider
	private final int min, max, initialValue;

	// flag to indicate very small number
	private boolean smallNumber;

	/** Precision that the slider can be adjusted to */
	private double precision = 1000.0;
	/** The number of decimals for given precision */
	private int decPrecision = (int) Math.log10(precision);

	private String labelText;

	private boolean edgesOnly;

    /**
     * @param min - slider mininmum value
     * @param max - slider maximum value
     * @param labelText
     * @param attrib1 - attribute for dataset 1 that the slider bar is specific to (i.e. p-value or q-value)
     * @param attrib2 - attribute for dataset 2 that the slider bar is specific to (i.e. p-value or q-value)
     */
	public SliderBarPanel(
			double min,
			double max,
			String labelText,
			String attrib1,
			String attrib2,
			boolean edgesOnly,
			double initialValue,
			CyApplicationManager applicationManager,
			EnrichmentMapManager emManager
	) {
		this.applicationManager = applicationManager;
		this.emManager = emManager;

		if ((min <= 1) && (max <= 1)) {
			// if the max is a very small number then use the precision to filter the results
			if (max <= 0.0001) {
				DecimalFormat df = new DecimalFormat("#.##############################");
				String text = df.format(max);
				int integerPlaces = text.indexOf('.');
				int decimalPlaces = text.length() - integerPlaces - 1;
				this.precision = decimalPlaces;
				this.min = (int) (min * Math.pow(10, (this.precision + this.decPrecision)));
				this.max = (int) (max * Math.pow(10, (this.precision + this.decPrecision)));

				this.initialValue = (int) (initialValue * Math.pow(10, (this.precision + this.decPrecision)));
				this.smallNumber = true;
			} else {
				this.min = (int) (min * precision);
				this.max = (int) (max * precision);
				this.initialValue = (int) (initialValue * precision);
			}
		} else {
			this.min = (int) min;
			this.max = (int) max;
			this.initialValue = (int) initialValue;
		}

		this.labelText = labelText;
		this.edgesOnly = edgesOnly;

		initPanel(attrib1, attrib2);
    }

    /**
     * Initialize panel based on enrichment map parameters and desired attributes
     *
     * @param params - enrichment map parameters for current map
     * @param attrib1 - attribute for dataset 1 that the slider bar is specific to (i.e. p-value or q-value)
     * @param attrib2 - attribute for dataset 2 that the slider bar is specific to (i.e. p-value or q-value)
     * @param desiredWidth
     */
	public void initPanel(String attrib1, String attrib2) {
		label = new JLabel(labelText);
		
		slider = new JSlider(JSlider.HORIZONTAL, min, max, initialValue);
		slider.addChangeListener(
				new SliderBarActionListener(this, attrib1, attrib2, edgesOnly, applicationManager, emManager));
		slider.setMajorTickSpacing((max - min) / 5);
		slider.setPaintTicks(true);

        //Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        
		if (smallNumber) {
			labelTable.put(
					new Integer(min),
					new JLabel("" + (int) this.min / Math.pow(10, decPrecision) + "E-" + (int) precision));
			labelTable.put(
					new Integer(max),
					new JLabel("" + (int) this.max / Math.pow(10, decPrecision) + "E-" + (int) precision));
		} else {
			labelTable.put(new Integer(min), new JLabel("" + min / precision));
			labelTable.put(new Integer(max), new JLabel("" + max / precision));
		}
        
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		
		textField = new BoundedTextField(initialValue, min, max, precision, decPrecision, smallNumber);
		textField.addPropertyChangeListener("value", evt -> {
			slider.setValue((int) evt.getNewValue());
		});

		makeSmall(label, slider, textField);
		
        final GroupLayout layout = new GroupLayout(this);
       	this.setLayout(layout);
   		layout.setAutoCreateContainerGaps(LookAndFeelUtil.isWinLAF());
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
   				.addComponent(label)
   				.addGroup(layout.createSequentialGroup()
   						.addComponent(slider, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   						.addComponent(textField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(label, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
   						.addComponent(slider, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   						.addComponent(textField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
   		
		// Change the slider's label sizes -- only works if it's done after the
		// slider has been added to its parent container and had its UI assigned
		final Font tickFont = slider.getFont().deriveFont(getSmallFontSize());

		for (Enumeration<Integer> enumeration = labelTable.keys(); enumeration.hasMoreElements();) {
			int k = enumeration.nextElement();
			final JLabel label = labelTable.get(k);
			label.setFont(tickFont); // Updates the font size
			label.setSize(label.getPreferredSize()); // Updates the label size and slider layout
		}

        this.revalidate();
    }

    // Getters and Setters
	
	public BoundedTextField getTextField() {
		return textField;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		label.setEnabled(enabled);
		slider.setEnabled(enabled);
		textField.setEnabled(enabled);
	}
	
    protected void setValue(final int newValue) {
    	textField.setValue(newValue);
	}

	protected double getPrecision() {
		return textField.getPrecision();
	}

    
    //Methods are currently not used.  If they become useful in the future need to add case for smallNumbers case
    
    /*public double getMin() {
        return min/precision;
    }

    public void setMin(double min) {
        this.min = (int)(min * precision);
    }

    public double getMax() {
        return max/precision;
    }

    public void setMax(double max) {
        this.max = (int) (max*precision);
    }

    public NumberRangeModel getRangeModel() {
        return rangeModel;
    }

    public void setRangeModel(NumberRangeModel rangeModel) {
        this.rangeModel = rangeModel;
    }*/
}
