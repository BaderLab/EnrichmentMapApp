package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;

import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientWidget;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class GradientLegendPanel extends JPanel {

	public static final int DEFAULT_HEIGHT = 25;
	private static final int BORDER_WIDTH = 1;
	
	
	public GradientLegendPanel(DataSetColorRange range) {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		ColorGradientWidget legend = ColorGradientWidget.getInstance(null, range.getTheme(),
				range.getRange(), true, ColorGradientWidget.LEGEND_POSITION.NA);

		int h = DEFAULT_HEIGHT - 2 * BORDER_WIDTH;
		
		hGroup.addComponent(legend, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(legend, h, h, h);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);

		revalidate();
		setOpaque(false);
	}
}
