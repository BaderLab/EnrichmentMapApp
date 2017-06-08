package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.BorderLayout;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class SimilaritySlider extends JPanel {

	private JSlider slider;
	private final int defaultValue = 3;
	
	public SimilaritySlider() {
		slider = new JSlider(1, 5, defaultValue);
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		
		JLabel sparseLabel = new JLabel("sparse");
		JLabel denseLabel = new JLabel("dense");
		SwingUtil.makeSmall(sparseLabel, denseLabel);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
		labelTable.put(1, sparseLabel);
		labelTable.put(5, denseLabel);
		
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		
		setLayout(new BorderLayout());
		add(slider, BorderLayout.SOUTH);
	}
	
	public SimilarityMetric getSimilarityMetric() {
		switch(slider.getValue()) {
			default:
			case 1: return SimilarityMetric.JACCARD;
			case 2: return SimilarityMetric.JACCARD;
			case 3: return SimilarityMetric.COMBINED;
			case 4: return SimilarityMetric.OVERLAP;
			case 5: return SimilarityMetric.OVERLAP;
		}
	}
	
	public double getCutoff() {
		switch(slider.getValue()) {
			default:
			case 1: return 0.35;
			case 2: return 0.25;
			case 3: return 0.375;
			case 4: return 0.5;
			case 5: return 0.25;
		}
	}
	
	public void reset() {
		slider.setValue(defaultValue);
	}
	
}
