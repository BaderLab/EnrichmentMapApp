package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyDisposable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

import com.google.inject.Inject;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel implements CytoPanelComponent2, CyDisposable, 
				NetworkViewAboutToBeDestroyedListener, NetworkViewAddedListener, SetCurrentNetworkViewListener {
	
	public static final String ID = "enrichmentmap.view.ControlPanel";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private EnrichmentMapManager emManager;
	@Inject private NetworkList.Factory networkListFactory;
	
	private SortedListModel<CyNetworkView> networkListModel;
	private NetworkList networkList;
	private ListSelectionListener listSelectionListener;
	

	@AfterInjection
	private void createContents() {
		setLayout(new BorderLayout());
		
		JMenuBar menuBar = createMenuBar();
		JPanel networkListPanel = createNetworkListPanel();
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, networkListPanel, new JPanel());
		
		add(menuBar, BorderLayout.PAGE_START);
		add(splitPane, BorderLayout.CENTER);
	}
	
	
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu newMenu = new JMenu("New");
		JMenu optionsMenu = new JMenu("Options");
		JMenu helpMenu = new JMenu("Help");
		
		menuBar.add(newMenu);
		menuBar.add(optionsMenu);
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	private JPanel createNetworkListPanel() {
		JPanel panel = new JPanel();
		
		networkListModel = new SortedListModel<>(
			(nv1, nv2) -> {
				String name1 = nv1.getModel().getRow(nv1.getModel()).get(CyNetwork.NAME, String.class);
				String name2 = nv2.getModel().getRow(nv2.getModel()).get(CyNetwork.NAME, String.class);
				return name1.compareToIgnoreCase(name2);
			}
		);
		
		networkList = networkListFactory.create(networkListModel);
		networkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		networkViewManager.getNetworkViewSet().stream()
			.filter(emManager::isEnrichmentMap)
			.forEach(networkListModel::add);
		
		listSelectionListener = e -> {
			int index = e.getFirstIndex();
			CyNetworkView selectedView = networkListModel.getElementAt(index);
			applicationManager.setCurrentNetworkView(selectedView);
		};
		networkList.getSelectionModel().addListSelectionListener(listSelectionListener);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(networkList);
		
		panel.setLayout(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(300, 200));
		
		return panel;
	}
	
	
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		if(emManager.isEnrichmentMap(networkView.getModel().getSUID())) {
			networkListModel.add(networkView);
		}
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		CyNetworkView networkView = e.getNetworkView();
		networkListModel.remove(networkView);
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		ListSelectionModel selectionModel = networkList.getSelectionModel();
		int index = networkListModel.indexOf(e.getNetworkView());
		if(index > 0) {
			selectionModel.setSelectionInterval(index, index);
		} else {
			selectionModel.clearSelection();
			// MKTODO disable the panel controls
		}
	}
	
	@Override
	public void dispose() {
		
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "EnrichmentMap";
	}

	@Override
	public Icon getIcon() {
		String path = "org/baderlab/csplugins/enrichmentmap/view/enrichmentmap_logo_notext_small.png";
		URL url = getClass().getClassLoader().getResource(path);
		return url == null ? null : new ImageIcon(url);
	}

	@Override
	public String getIdentifier() {
		return ID;
	}

	@Override
	public Component getComponent() {
		return this;
	}


	


	

}
