package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class CombinedConstantSlider extends JPanel {

	private JSlider slider;
	private final int defaultValue;
	
	public CombinedConstantSlider(int defaultValue) {
		if(defaultValue < 0)
			defaultValue = 0;
		if(defaultValue > 100)
			defaultValue = 100;
		this.defaultValue = defaultValue;
		
		JLabel jaccardLabel = new JLabel(mkLabel("Jaccard", 100-defaultValue));
		JLabel overlapLabel = new JLabel(mkLabel("Overlap", defaultValue));
		JLabel plusLabel = new JLabel("+");
		SwingUtil.makeSmall(jaccardLabel, overlapLabel, plusLabel);
		
		plusLabel.setHorizontalAlignment(JLabel.CENTER);
		
		slider = new JSlider(0, 100, defaultValue);
		slider.addChangeListener(e -> {
			int percent = slider.getValue();
			jaccardLabel.setText(mkLabel("Jaccard", 100-percent));
			overlapLabel.setText(mkLabel("Overlap", percent));
			CombinedConstantSlider.this.revalidate();
		});
		
		setLayout(new BorderLayout());
		add(jaccardLabel, BorderLayout.WEST);
		add(plusLabel, BorderLayout.CENTER);
		add(overlapLabel, BorderLayout.EAST);
		add(slider, BorderLayout.SOUTH);
	}
	
	public int getValue() {
		return slider.getValue();
	}
	
	public void reset() {
		slider.setValue(defaultValue);
	}
	
	private static String mkLabel(String name, int percent) {
		return name + " (" + percent + "%)";
	}
}
