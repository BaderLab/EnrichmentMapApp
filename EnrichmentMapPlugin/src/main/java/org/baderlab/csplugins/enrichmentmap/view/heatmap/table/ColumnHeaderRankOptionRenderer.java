package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapContentPanel;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.RankingOption;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class ColumnHeaderRankOptionRenderer extends JPanel implements TableCellRenderer {

	private final int colIndex;
	private final HeatMapContentPanel heatMapPanel;
	
	private DefaultTableCellRenderer delegate;
	private Icon errorIcon;
	private JPanel datasetColorPanel;
	private boolean initialized = false;
	
	
	public interface Factory {
		ColumnHeaderRankOptionRenderer create(HeatMapContentPanel heatMapPanel, int colIndex);
	}
	
	@Inject
	public ColumnHeaderRankOptionRenderer(IconManager iconManager, @Assisted HeatMapContentPanel heatMapPanel, @Assisted int colIndex) {
		this.colIndex = colIndex;
		this.heatMapPanel = heatMapPanel;
		this.errorIcon = createErrorIcon(iconManager);
	}
	
	
	@Override
	public Component getTableCellRendererComponent(JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		if(!initialized) {
			// Have to initialize here instead of constructor because we need the JTable instance.
			JTableHeader header = table.getTableHeader();
			
			JButton button = new JButton(" Ranks... ");
			SwingUtil.makeSmall(button);
			if(LookAndFeelUtil.isAquaLAF())
				button.putClientProperty("JButton.buttonType", "gradient");
			
			button.addActionListener(e -> menuButtonClicked(table, button));
			JPanel buttonPanel = new JPanel(new BorderLayout());
			buttonPanel.add(button, BorderLayout.WEST);
			
			
			// Get the default cell renderer so that it looks the same. Wrap it in a JPanel so we can add a tooltip.
			delegate = new DefaultTableCellRenderer();
			Component component = delegate.getTableCellRendererComponent(table, "???", isSelected, hasFocus, row, col);
			
			datasetColorPanel = new JPanel();
			datasetColorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			
			JPanel bottomPanel = new JPanel(new BorderLayout());
			bottomPanel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			bottomPanel.setForeground(header.getForeground());   
			bottomPanel.setBackground(header.getBackground());
			bottomPanel.add(buttonPanel, BorderLayout.NORTH);
			bottomPanel.add(component, BorderLayout.CENTER);
			
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
			setForeground(header.getForeground());   
			setBackground(header.getBackground());
			add(datasetColorPanel, BorderLayout.NORTH);
			add(bottomPanel, BorderLayout.CENTER);
			
			// Replace the original mouse listener with our own. Otherwise it will always sort the column even when clicking on the button.
			MouseListener originalListener = removeListeners(header);
			
			header.addMouseListener(new MouseListener() {
	            @Override
	            public void mouseClicked(MouseEvent e) {
	            	int col = header.columnAtPoint(e.getPoint());
	            	if(col == colIndex && e.getY() <= button.getHeight()) {
            		    button.doClick();
	            	} else {
	            		fireMouseListener(originalListener, e);
	            	}
	            }
				@Override
				public void mousePressed(MouseEvent e) {
					fireMouseListener(originalListener, e);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					fireMouseListener(originalListener, e);
				}
				@Override
				public void mouseEntered(MouseEvent e) {
					fireMouseListener(originalListener, e);
				}
				@Override
				public void mouseExited(MouseEvent e) {
					fireMouseListener(originalListener, e);
				}
			});
			
			initialized = true;
		}
		
		delegate.setIcon(getIcon(value));
		delegate.setText(getText(value));
		
		datasetColorPanel.setPreferredSize(new Dimension(this.getWidth(), 5));
		datasetColorPanel.setBackground(getColor(value));
		
		return this;
	}
	
	
	
	private static MouseListener removeListeners(JTableHeader header) {
		MouseListener originalListeners = null;
		MouseListener[] listeners = header.getListeners(MouseListener.class);
		if(listeners != null) {
			for(int i = 0; i < listeners.length; i++) {
				var listener = listeners[i];
				header.removeMouseListener(listener);
				originalListeners = AWTEventMulticaster.add(originalListeners, listener);
			}
		}
		return originalListeners;
	}
	
	private static void fireMouseListener(MouseListener listener, MouseEvent e) {
		if(listener == null || e == null)
			return;

		int id = e.getID();
		switch(id) {
			case MouseEvent.MOUSE_PRESSED:
				listener.mousePressed(e);
				break;
			case MouseEvent.MOUSE_RELEASED:
				listener.mouseReleased(e);
				break;
			case MouseEvent.MOUSE_CLICKED:
				listener.mouseClicked(e);
				break;
			case MouseEvent.MOUSE_EXITED:
				listener.mouseExited(e);
				break;
			case MouseEvent.MOUSE_ENTERED:
				listener.mouseEntered(e);
				break;
		}
	}
	
	
	private static Icon createErrorIcon(IconManager iconManager) {
		int iconSize = 16;
		var iconFont = iconManager.getIconFont(13f);
		var icon = new TextIcon(IconManager.ICON_TIMES_CIRCLE, iconFont, Color.RED.darker(), iconSize, iconSize);
		return icon;
	}
	
	private Icon getIcon(Object value) {
		return value instanceof RankOptionErrorHeader ? errorIcon : null;
	}
	 
	private String getText(Object value) {
		// Convert RankingOption to display String
		if(value instanceof RankingOption) {
			var rankingOption = (RankingOption) value;
			return rankingOption.getTableHeaderText();
		} else if(value instanceof RankOptionErrorHeader) {
			var headerValue = (RankOptionErrorHeader) value;
			return headerValue.getRankingOption().getTableHeaderText();
		}
		return null;
	}
	
	private Color getColor(Object value) {
		if(value instanceof RankingOption) {
			var rankingOption = (RankingOption) value;
			return rankingOption.getColor();
		}
		return null;
	}
	
	
	private void menuButtonClicked(JTable table, JButton button) {
		JTableHeader header = table.getTableHeader();
		
		List<RankingOption> rankOptions = heatMapPanel.getAllRankingOptions();
		
		JPopupMenu menu = new JPopupMenu();
		for(RankingOption rankOption : rankOptions) {
			JMenuItem item = new JCheckBoxMenuItem(rankOption.getName());
			item.setSelected(rankOption == heatMapPanel.getSelectedRankingOption());
			SwingUtil.makeSmall(item);
			menu.add(item);
			item.addActionListener(e ->
				heatMapPanel.setSelectedRankingOption(rankOption)
			);
		}
		
		int y = button.getHeight();
		int x = 0;
		for(int i = 0; i < colIndex; i++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(i);
			x += tableColumn.getWidth();
		}
		menu.show(header, x, y);
	}
	
	public void dispose(JTableHeader header) {
		// TODO is this still needed?
//		if(mouseListener != null)
//			header.removeMouseListener(mouseListener);
	}
	
}
