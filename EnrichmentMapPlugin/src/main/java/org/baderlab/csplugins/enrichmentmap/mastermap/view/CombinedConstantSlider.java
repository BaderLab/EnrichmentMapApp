package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

@SuppressWarnings("serial")
public class CombinedConstantSlider extends JPanel {

	private JSlider slider;
	
	public CombinedConstantSlider(int defaultValue) {
		if(defaultValue < 0)
			defaultValue = 0;
		if(defaultValue > 100)
			defaultValue = 100;
		
		JLabel jaccardLabel = new JLabel(mkLabel("Jaccard", 100-defaultValue));
		JLabel overlapLabel = new JLabel(mkLabel("Overlap", defaultValue));
		JLabel plusLabel = new JLabel("+");
		
		jaccardLabel.setFont(jaccardLabel.getFont().deriveFont(10f));
		overlapLabel.setFont(overlapLabel.getFont().deriveFont(10f));
		plusLabel.setFont(plusLabel.getFont().deriveFont(10f));
		
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
	
	
	private static String mkLabel(String name, int percent) {
		return name + " (" + percent + "%)";
	}
}
