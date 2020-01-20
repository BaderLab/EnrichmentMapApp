package org.baderlab.csplugins.enrichmentmap.view.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.cytoscape.util.swing.ColorButton;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class DataSetColorSelectorDialog extends JDialog {
	
	private final List<EMDataSet> dataSets;
	private final Map<EMDataSet,Color> newColors = new IdentityHashMap<>();
	
	private boolean colorsChanged = false;
	
	public interface Factory {
		DataSetColorSelectorDialog create(List<EMDataSet> dataSets);
	}
	
	@Inject
	public DataSetColorSelectorDialog(@Assisted List<EMDataSet> dataSets) {
		setTitle("EnrichmentMap: Data Set Colors");
		this.dataSets = dataSets;
	}
	
	@AfterInjection
	private void createContents() {
		JPanel dataSetPanel = createDataSetColorsPanel();
		JPanel buttonPanel  = createButtonPanel();
		
		JScrollPane scrollPane = new JScrollPane(dataSetPanel);
		scrollPane.setAlignmentX(TOP_ALIGNMENT);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(panel);
	}
	
	private JPanel createDataSetColorsPanel() {
		JPanel dataSetPanel = new JPanel(new GridBagLayout());
		
		int y = 0;
		for(EMDataSet dataSet : dataSets) {
			JLabel label = new JLabel(dataSet.getName());
			ColorButton colorButton = new ColorButton(dataSet.getColor());
			colorButton.addPropertyChangeListener("color", pce -> {
				Color newColor = (Color) pce.getNewValue();
				newColors.put(dataSet, newColor);
			});
			
			dataSetPanel.add(label,       GBCFactory.grid(0,y).get());
			dataSetPanel.add(colorButton, GBCFactory.grid(1,y).get());
			y += 1;
		}
		
		dataSetPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return dataSetPanel;
	}
	
	private JPanel createButtonPanel() {
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(e -> {
			applyColors();
			dispose();
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dispose());
		
		return LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton);
	}
	
	private void applyColors() {
		if(newColors.isEmpty())
			return;
		
		colorsChanged = true;
		newColors.forEach(EMDataSet::setColor);
	}
	
	public boolean colorsChanged() {
		return colorsChanged;
	}
}
