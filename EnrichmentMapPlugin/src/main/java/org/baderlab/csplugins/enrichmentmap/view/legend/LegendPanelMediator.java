package org.baderlab.csplugins.enrichmentmap.view.legend;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDT;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.invokeOnEDTAndWait;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class LegendPanelMediator {

	@Inject private EnrichmentMapManager emManager;
	@Inject private Provider<LegendPanel> legendPanelProvider;
	@Inject private CySwingApplication swingApplication;
	
	private JDialog dialog;
	private JButton creationParamsButton = new JButton("Creation Parameters...");
	
	public void showDialog(Collection<EMDataSet> filteredDataSets, CyNetworkView view) {
		invokeOnEDT(() -> {
			updateDialog(filteredDataSets, view, false);
			
			if (dialog != null) {
				dialog.pack();
				dialog.setVisible(true);
			}
		});
	}
	
	public void hideDialog() {
		invokeOnEDT(() -> {
			if (dialog != null)
				dialog.setVisible(false);
		});
	}
	
	public JDialog getDialog() {
		return dialog;
	}
	
	public void updateDialog(Collection<EMDataSet> filteredDataSets, CyNetworkView view) {
		updateDialog(filteredDataSets, view, true);
	}
	
	@AfterInjection
	@SuppressWarnings("serial")
	private void init() {
		invokeOnEDTAndWait(() -> {
			dialog = new JDialog(swingApplication.getJFrame(), "EnrichmentMap Legend", ModalityType.MODELESS);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setMinimumSize(new Dimension(440, 380));
			
			JButton closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.dispose();
				}
			});
			
			creationParamsButton.addActionListener(e -> showCreationParamsDialog());
			
			JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton, creationParamsButton);
			
			dialog.getContentPane().add(legendPanelProvider.get(), BorderLayout.CENTER);
			dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
			
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), null, closeButton.getAction());
			dialog.getRootPane().setDefaultButton(closeButton);
			dialog.setLocationRelativeTo(swingApplication.getJFrame());
		});
	}
	
	private void updateDialog(Collection<EMDataSet> filteredDataSets, CyNetworkView view, boolean onlyIfVisible) {
		if (onlyIfVisible && !dialog.isVisible())
			return;
		
		invokeOnEDT(() -> {
			creationParamsButton.setEnabled(view != null);
			legendPanelProvider.get().update(filteredDataSets, view);
		});
	}
	
	@SuppressWarnings("serial")
	private void showCreationParamsDialog() {
		JDialog d = new JDialog(dialog, "EnrichmentMap Creation Parameters", ModalityType.APPLICATION_MODAL);
		d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		d.setMinimumSize(new Dimension(640, 420));
		
		JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				d.dispose();
			}
		});
		JButton copyButton = new JButton("Copy to Clipboard");
		copyButton.setEnabled(false);
		
		JPanel bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton, copyButton);
		d.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		CyNetworkView netView = legendPanelProvider.get().getNetworkView();
		
		if (netView != null) {
			EnrichmentMap map = emManager.getEnrichmentMap(netView.getModel().getSUID());
			CreationParametersPanel paramsPanel = new CreationParametersPanel(map);
			d.getContentPane().add(paramsPanel, BorderLayout.CENTER);
			
			copyButton.setEnabled(true);
			copyButton.addActionListener(e -> {
				StyledDocument doc = paramsPanel.getInfoPane().getStyledDocument();
				RTFEditorKit rtfek = new RTFEditorKit();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				
				try {
					rtfek.write(baos, doc, 0, doc.getLength());
					baos.flush();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    DataHandler dh = new DataHandler(baos.toByteArray(), rtfek.getContentType());
			    clipboard.setContents(dh, null);
			});
		}
		
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(d.getRootPane(), null, closeButton.getAction());
		d.getRootPane().setDefaultButton(closeButton);
		
		d.setLocationRelativeTo(dialog);
		d.pack();
		d.setVisible(true);
	}
}
