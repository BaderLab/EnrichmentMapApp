package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class SimilaritySlider extends JPanel {

	private JSlider slider;
	private final int defaultTick;
	private final List<Pair<SimilarityMetric, Double>> cutoffs;
	
	public SimilaritySlider(List<Pair<SimilarityMetric, Double>> cutoffs, int defaultTick) {
		this.defaultTick = defaultTick;
		this.cutoffs = cutoffs;
		
		slider = new JSlider(1, cutoffs.size(), defaultTick);
		slider.setMajorTickSpacing(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		
		JLabel sparseLabel = new JLabel("sparse");
		JLabel denseLabel = new JLabel("dense");
		SwingUtil.makeSmall(sparseLabel, denseLabel);
		
		Hashtable<Integer,JLabel> labelTable = new Hashtable<>();
		labelTable.put(1, sparseLabel);
		labelTable.put(cutoffs.size(), denseLabel);
		
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);
		
		setLayout(new BorderLayout());
		add(slider, BorderLayout.SOUTH);
	}
	
	public void setTick(int tick) {
		slider.setValue(tick);
	}
	
	public Pair<SimilarityMetric,Double> getTickValue() {
		return cutoffs.get(slider.getValue()-1);
	}
	
	public SimilarityMetric getSimilarityMetric() {
		return getTickValue().getKey();
	}
	
	public double getCutoff() {
		return getTickValue().getValue();
	}
	
	public void reset() {
		slider.setValue(defaultTick);
	}
	
	public JSlider getSlider() {
		return slider;
	}
	
}
