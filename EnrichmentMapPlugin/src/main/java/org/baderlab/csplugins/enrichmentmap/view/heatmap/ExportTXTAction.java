package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.HeatMapTableModel;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ExportTXTAction extends AbstractAction {
	
	@Inject private FileUtil fileUtil;
	@Inject private Provider<JFrame> jframeProvider;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final JTable table;
	
	public interface Factory {
		ExportTXTAction create(JTable table);
	}
	
	@Inject
	public ExportTXTAction(@Assisted JTable table) {
		super("Export to TXT");
		this.table = table;
	}
	
	
	private boolean promptForLeadingEdgeOnly(HeatMapTableModel tableModel) {
		if(!tableModel.hasSignificantRanks()) {
			return false;
		}
		
		JLabel label = new JLabel("Genes to export:");
		JRadioButton allButton = new JRadioButton("All genes");
		JRadioButton leadingEdgeButton = new JRadioButton("Leading edge only");
		SwingUtil.makeSmall(label, allButton, leadingEdgeButton);
		allButton.setSelected(true);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(allButton);
		buttonGroup.add(leadingEdgeButton);
		
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(label, GBCFactory.grid(0,0).weightx(1.0).get());
		panel.add(allButton, GBCFactory.grid(0,1).get());
		panel.add(leadingEdgeButton, GBCFactory.grid(0,2).get());
		
		JOptionPane.showMessageDialog(jframeProvider.get(), panel, "Export to TXT", JOptionPane.QUESTION_MESSAGE);
		return leadingEdgeButton.isSelected();
	}
	
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		boolean leadingEdgeOnly = promptForLeadingEdgeOnly((HeatMapTableModel)table.getModel());
		
		Optional<File> file = FileBrowser.promptForTXTExport(fileUtil, jframeProvider.get());
		if(file.isPresent()) {
			ExportTXTTask task = new ExportTXTTask(file.get(), table, leadingEdgeOnly);
			dialogTaskManager.execute(new TaskIterator(task));
		}
	}

}
