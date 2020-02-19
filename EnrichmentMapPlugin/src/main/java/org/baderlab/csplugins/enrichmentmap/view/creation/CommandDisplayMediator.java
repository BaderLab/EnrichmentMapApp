package org.baderlab.csplugins.enrichmentmap.view.creation;

import static org.baderlab.csplugins.enrichmentmap.EMBuildProps.HELP_URL_AUTO;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.FilterTunables;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.util.PathUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.OpenBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class CommandDisplayMediator {

	@Inject private OpenBrowser openBrowser;
	
	
	public void showCommand(
			JDialog parent,
			EMCreationParameters params, 
			List<DataSetParameters> dataSets, 
			String commonExprFile, 
			String commonGMTFile, 
			String commonClassFile
	) {
		Path root = getCommonRoot(dataSets);
		if(root == null) {
			showFailDialog(parent);
		} else {
			String command = generateCommand(root, params, dataSets, commonExprFile, commonGMTFile, commonClassFile);
			CommandDisplayDialog dialog = new CommandDisplayDialog(parent, command);
			dialog.showDialog();
		}
	}
	
	private static void showFailDialog(JDialog parent) {
		String message = "Data Set files do not have a common root folder.";
		String title = "Create EnrichmentMap Command";
		JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	
	private static Path getCommonRoot(List<DataSetParameters> dataSets) {
		List<Path> dataSetRoots = PathUtil.dataSetsRoots(dataSets);
		Path r = PathUtil.commonRoot(dataSetRoots);
		
		if(r == null || r.getNameCount() == 1) {
			return null;
		}
		for(Path p : dataSetRoots) {
			if(!(p.startsWith(r) && p.getNameCount() - r.getNameCount() < 2)) {
				return null;
			}
		}
		return r;
	}
	
	
	private static String generateCommand(
			Path root,
			EMCreationParameters params, 
			List<DataSetParameters> dataSets, 
			String commonExprFile, 
			String commonGMTFile, 
			String commonClassFile
	) {
		StringBuilder command = new StringBuilder("enrichmentmap mastermap ");
		command.append("rootFolder=\"").append(root).append("\" ");
		
		FilterTunables defaults = new FilterTunables();
		
		if(params.getSimilarityMetric() != defaults.getSimilarityMetric()) {
			command.append("coefficients=").append(params.getSimilarityMetric().name()).append(" ");
		}
		if(params.getPvalue() != defaults.pvalue) {
			command.append("pvalue=").append(params.getPvalue()).append(" ");
		}
		if(params.getQvalue() != defaults.qvalue) {
			command.append("qvalue=").append(params.getQvalue()).append(" ");
		}
		if(params.getSimilarityCutoff() != defaults.similaritycutoff) {
			command.append("similaritycutoff=").append(params.getSimilarityCutoff()).append(" ");
		}
		if(params.getSimilarityMetric() == SimilarityMetric.COMBINED) {
			command.append("combinedConstant=").append(params.getCombinedConstant()).append(" ");
		}
		if(params.getEdgeStrategy() != defaults.getEdgeStrategy()) {
			command.append("edgeStrategy=").append(params.getEdgeStrategy().name()).append(" ");
		}
		if(params.getMinExperiments().isPresent()) {
			command.append("minExperiments=").append(params.getMinExperiments().get()).append(" ");
		}
		if(params.getNESFilter() != defaults.getNesFilter()) {
			command.append("nesFilter=").append(params.getNESFilter().name()).append(" ");
		}
		if(params.isFilterByExpressions() != defaults.filterByExpressions) {
			command.append("filterByExpressions=").append(params.isFilterByExpressions()).append(" ");
		}
		if(params.isParseBaderlabGeneSets() != defaults.parseBaderlabNames) {
			command.append("parseBaderlabNames=").append(params.isParseBaderlabGeneSets()).append(" ");
		}
		if(params.getNetworkName() != null) {
			command.append("networkName=\"").append(params.getNetworkName()).append("\" ");
		}
		if(commonGMTFile != null) {
			command.append("commonGMTFile=\"").append(commonGMTFile).append("\" ");
		}
		if(commonExprFile != null) {
			command.append("commonExpressionFile=\"").append(commonExprFile).append("\" ");
		}
		if(commonClassFile != null) {
			command.append("commonClassFile=\"").append(commonClassFile).append("\" ");
		}
		
		return command.toString();
	}
	
	
	private class CommandDisplayDialog extends JDialog {
		
		private final String command;
		
		public CommandDisplayDialog(JDialog parent, String command) {
			super(parent);
			this.command = command;
			setResizable(true);
			setTitle("EnrichmentMap: Command");
			setMinimumSize(new Dimension(500, 300));
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			
			JPanel messagePanel = createMessagePanel();
			JPanel commandPanel = createCommandPanel();
			JPanel buttonPanel  = createButtonPanel();
			
			messagePanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
			commandPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
			buttonPanel .setBorder(BorderFactory.createMatteBorder(1,0,0,0, UIManager.getColor("Separator.foreground")));
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(messagePanel, BorderLayout.NORTH);
			panel.add(commandPanel, BorderLayout.CENTER);
			panel.add(buttonPanel, BorderLayout.SOUTH);
			setContentPane(panel);
		}
		
		public void showDialog() {
			setLocationRelativeTo(getParent());
			pack();
			setVisible(true);
		}
		
		private JPanel createMessagePanel() {
			String message = 
					"<html>The command below can be used in a script to automate the<br>"
					+ "creation of an EnrichmentMap network. Data files are expected to<br>"
					+ "be under a common root folder. The command arguments are based<br>"
					+ "on the values currently entered in the Create EnrichmentMap Dialog.</html>";
			JLabel label = new JLabel(message);
			JButton link = SwingUtil.createLinkButton(openBrowser, "View online help", HELP_URL_AUTO);
			
			JPanel panel = new JPanel(new GridBagLayout());
			panel.add(label, GBCFactory.grid(0,0).weightx(1.0).anchor(GridBagConstraints.WEST).get());
			panel.add(link,  GBCFactory.grid(0,1).fill(GridBagConstraints.NONE).anchor(GridBagConstraints.WEST).get());
			return panel;
		}
		
		
		private JPanel createCommandPanel() {
			JTextArea textArea = new JTextArea(command);
			textArea.setEditable(false);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			
			JPanel panel = new JPanel(new BorderLayout());
			panel.add(scrollPane, BorderLayout.CENTER);
			return panel;
		}
		
		
		private JPanel createButtonPanel() {
			JButton okButton = new JButton("Close");
			okButton.addActionListener(e -> dispose());
			
			JButton clipboardButton = new JButton("Copy to Clipboard");
			clipboardButton.addActionListener(e -> {
				StringSelection stringSelection = new StringSelection(command);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			});
			
			return LookAndFeelUtil.createOkCancelPanel(okButton, null, clipboardButton);
		}
	}
	
}
