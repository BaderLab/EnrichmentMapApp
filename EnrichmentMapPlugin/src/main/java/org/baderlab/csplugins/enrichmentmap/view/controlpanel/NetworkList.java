package org.baderlab.csplugins.enrichmentmap.view.controlpanel;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class NetworkList extends JList<CyNetworkView> {
	
	@Inject private IconManager iconManager;
	@Inject private EnrichmentMapManager emManager;

	public static interface Factory {
		NetworkList create(ListModel<CyNetworkView> model);
	}
	
	@Inject
	public NetworkList(@Assisted ListModel<CyNetworkView> model) {
		setModel(model);
		setCellRenderer(new CellRenderer());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private class CellRenderer implements ListCellRenderer<CyNetworkView> {

		@Override
		public Component getListCellRendererComponent(JList<? extends CyNetworkView> list, CyNetworkView networkView, 
				int index, boolean isSelected, boolean cellHasFocus) {
			
			JLabel iconLabel = new JLabel(IconManager.ICON_SHARE_ALT_SQUARE);
			iconLabel.setFont(iconManager.getIconFont(16.0f));
			
			CyNetwork network = networkView.getModel();
			String text = getNetworkLabelText(network);
			JLabel nameLabel = new JLabel(text);
			nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
			
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.add(iconLabel, BorderLayout.WEST);
			panel.add(nameLabel, BorderLayout.CENTER);
			
			return panel;
		}
		
	
		private String getNetworkLabelText(CyNetwork network) {
			String name = network.getRow(network).get(CyNetwork.NAME, String.class);
			EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
			
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append(name);
			if(map!= null) {
				sb.append("<font color='grey'>");
				int dscount = map.getDataSetCount();
				if(dscount == 1) {
					sb.append("(1 data set)");
				} else {
					sb.append("(").append(dscount).append(" data sets");
				}
				sb.append("</font>");
			}
			sb.append("</html>");
			return sb.toString();
		}
		
	}
}
